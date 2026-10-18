/// `ChangeStream`: a resuming reader of `GET /v1/changes/stream` and `GET /v1/events/stream` implementing the
/// client contract of docs/Core-Data-Model.md §10.2 (prompts 015 and 023), with the same semantics as the
/// TypeScript and Python SDKs:
///
/// - resumes with `Last-Event-ID` (or `after=` / `lastEventId=` when a stream ticket replaces the header) and
///   tracks the last `change` id;
/// - on `reset`: emits the reset; when the reason is `resync_required` it pages the snapshot
///   (`/v1/records?order=id`, keeping `watermarkSeq`) and reopens at the watermark, otherwise it reopens at
///   `resumeAfter` (or the last id);
/// - on `resync`: pages the current state again and keeps the stream open;
/// - applies `live` and `touch` events only when their `observedAt` is not older than the stored one;
/// - reconnects when no event or heartbeat arrives within 2.5 × the heartbeat interval, with jittered exponential
///   backoff (1 s → 60 s) that honors `retry:` and `Retry-After`.
library;

import 'dart:async';
import 'dart:convert';

import 'package:dio/dio.dart';

import '../errors.dart';
import '../http.dart';
import 'backoff.dart';
import 'sse.dart';

/// Which endpoint to read.
enum StreamEndpoint {
  /// `/v1/changes/stream`.
  changes,

  /// `/v1/events/stream`.
  events,
}

/// The kind of a [StreamSignal].
enum SignalType {
  /// A connection opened.
  open,

  /// The `hello` event of `/v1/events/stream`.
  hello,

  /// A lossless `change` event.
  change,

  /// A conflated `live` update that passed the observedAt guard.
  live,

  /// A `touch` (re-observed unchanged) that passed the observedAt guard.
  touch,

  /// A `resync` event (a snapshot follows when enabled).
  resync,

  /// A `reset` event (the server closes the stream).
  reset,

  /// One page of a snapshot.
  snapshot,

  /// A `heartbeat` event.
  heartbeat,

  /// An event of another topic.
  event,

  /// The stream is about to reconnect.
  reconnecting,
}

/// What the stream emits; the members besides [type] are set as the type needs.
class StreamSignal {
  /// Creates a signal.
  const StreamSignal(this.type,
      {this.data,
      this.id,
      this.event,
      this.url,
      this.cause,
      this.items,
      this.watermarkSeq,
      this.done,
      this.attempt,
      this.delay,
      this.reason});

  /// The kind of signal.
  final SignalType type;

  /// The event payload (decoded JSON, or the raw text).
  final Object? data;

  /// The event id, when the event carried one.
  final String? id;

  /// The event name (for [SignalType.event]).
  final String? event;

  /// The stream URL without the ticket (for [SignalType.open]).
  final String? url;

  /// `reset` or `resync` (for [SignalType.snapshot]).
  final String? cause;

  /// Snapshot records (for [SignalType.snapshot]).
  final List<Object?>? items;

  /// The snapshot's watermark (for [SignalType.snapshot]).
  final int? watermarkSeq;

  /// True on the last snapshot page.
  final bool? done;

  /// Reconnect attempt (for [SignalType.reconnecting]).
  final int? attempt;

  /// Reconnect delay (for [SignalType.reconnecting]).
  final Duration? delay;

  /// Why the stream reconnects (for [SignalType.reconnecting]).
  final String? reason;

  /// The payload as a JSON object (empty when it is not one).
  Map<String, Object?> get json => data is Map<String, Object?> ? data as Map<String, Object?> : const {};

  @override
  String toString() => 'StreamSignal(${type.name}${id == null ? '' : ' id=$id'}${event == null ? '' : ' $event'})';
}

/// Filter names a records snapshot understands.
const snapshotKeys = {
  'source', 'kind', 'bbox', 'externalId', 'near', 'radiusKm', 'place', 'view', 'inWatchlist', 'licencePolicy',
};

const _coreEvents = {'hello', 'change', 'live', 'touch', 'resync', 'reset', 'heartbeat'};

/// The value of [key] in a composite cursor (`c=1234;a=56`), or null.
int? compositeCursorValue(String? cursor, String key) {
  if (cursor == null || cursor.isEmpty) {
    return null;
  }
  for (final part in cursor.split(';')) {
    final eq = part.indexOf('=');
    if (eq > 0 && part.substring(0, eq) == key) {
      return int.tryParse(part.substring(eq + 1));
    }
  }
  return null;
}

/// [cursor] with [key] set to [value] (added first when absent).
String withCompositeCursorValue(String? cursor, String key, int value) {
  final parts = (cursor == null || cursor.isEmpty ? <String>[] : cursor.split(';'))
      .where((p) => p.isNotEmpty && !p.startsWith('$key='))
      .toList();
  return ['$key=$value', ...parts].join(';');
}

/// Remembers the newest `observedAt` per entity and refuses older updates.
class ObservedAtGuard {
  final Map<String, DateTime> _latest = {};

  /// The key of an entity.
  static String key(String sourceId, String externalId) => '$sourceId\u0000$externalId';

  /// True (and remembered) when [observedAt] is not older than the stored value; unparseable times pass.
  bool accept(String key, Object? observedAt) {
    final t = observedAt is String ? DateTime.tryParse(observedAt) : null;
    if (t == null) {
      return true;
    }
    final stored = _latest[key];
    if (stored != null && t.isBefore(stored)) {
      return false;
    }
    _latest[key] = t;
    return true;
  }

  /// Remembers [observedAt] when it is newer than the stored value (changes and snapshot rows).
  void observe(String key, Object? observedAt) {
    accept(key, observedAt);
  }

  /// The stored `observedAt`, or null.
  DateTime? operator [](String key) => _latest[key];

  /// Number of entities tracked.
  int get length => _latest.length;
}

/// The sleep between reconnects: completes after [delay], or early when [stop] completes.
typedef StreamSleep = Future<void> Function(Duration delay, Future<void> stop);

Future<void> _defaultSleep(Duration delay, Future<void> stop) {
  if (delay <= Duration.zero) {
    return Future.value();
  }
  return Future.any([Future<void>.delayed(delay), stop]);
}

Object? _parseData(String raw) {
  try {
    return jsonDecode(raw);
  } on FormatException {
    return raw;
  }
}

/// A resuming change stream. Listen to [signals] once; call [close] to stop.
class ChangeStream {
  /// Creates a stream; nothing is sent until [signals] is listened to.
  ///
  /// [filters] are the stream query parameters (`source`, `kind`, `bbox`, … or `topics` and the prefixed topic
  /// filters for [StreamEndpoint.events]); [after] / [lastEventId] set the starting point; [heartbeatInterval]
  /// is the server's `stream.limits.heartbeatSeconds` (default 15 s); [useTicket] authenticates with a stream
  /// ticket instead of the header and sends the cursor in the query; [snapshot] pages `/v1/records?order=id` on
  /// `reset {resync_required}` and on `resync`.
  ChangeStream({
    required this.transport,
    this.endpoint = StreamEndpoint.changes,
    Map<String, Object?>? filters,
    int? after,
    String? lastEventId,
    Duration heartbeatInterval = const Duration(seconds: 15),
    Backoff? backoff,
    this.useTicket = false,
    this.snapshot = true,
    int snapshotPageSize = 1000,
    Map<String, Object?>? snapshotQuery,
    StreamSleep? sleep,
  })  : filters = Map.unmodifiable(filters ?? const {}),
        watchdogTimeout = heartbeatInterval * 2.5,
        backoff = backoff ?? Backoff(),
        snapshotPageSize = snapshotPageSize.clamp(1, 5000),
        _snapshotQuery = snapshotQuery,
        _sleep = sleep ?? _defaultSleep,
        _seq = after ?? compositeCursorValue(lastEventId, 'c'),
        _eventId = lastEventId ?? (after != null && endpoint == StreamEndpoint.events ? 'c=$after' : null);

  /// The transport (base URL, token, dio).
  final HttpTransport transport;

  /// The endpoint read.
  final StreamEndpoint endpoint;

  /// The stream query parameters.
  final Map<String, Object?> filters;

  /// 2.5 × the heartbeat interval.
  final Duration watchdogTimeout;

  /// The reconnect backoff.
  final Backoff backoff;

  /// Whether a stream ticket replaces the `Authorization` header.
  final bool useTicket;

  /// Whether snapshots are paged on reset and resync.
  final bool snapshot;

  /// Snapshot page size.
  final int snapshotPageSize;

  /// The observedAt guard.
  final ObservedAtGuard guard = ObservedAtGuard();

  /// Live and touch events dropped by the guard.
  int staleDropped = 0;

  /// The `streamId` from the latest `hello`.
  String? streamId;

  final Map<String, Object?>? _snapshotQuery;
  final StreamSleep _sleep;
  final Completer<void> _stop = Completer<void>();
  CancelToken? _connection;
  String? _ticket;
  int? _seq;
  String? _eventId;
  Duration? _serverRetry;
  bool _started = false;

  /// The last change seq received (or the watermark the stream reopened at).
  int? get lastSeq => _seq;

  /// The last event id (`events` composite cursor; the seq on `changes`).
  String? get lastEventId => endpoint == StreamEndpoint.events ? _eventId : _seq?.toString();

  /// Whether [close] was called.
  bool get isClosed => _stop.isCompleted;

  /// Stops the stream: the current read is canceled and [signals] completes.
  void close() {
    if (!_stop.isCompleted) {
      _stop.complete();
    }
    _connection?.cancel('closed');
  }

  /// The signals (a single-subscription stream; listen once).
  Stream<StreamSignal> signals() {
    if (_started) {
      throw StateError('A ChangeStream can be listened to only once');
    }
    _started = true;
    return _run();
  }

  Stream<StreamSignal> _run() async* {
    while (!isClosed) {
      Response<ResponseBody> response;
      String url;
      try {
        (response, url) = await _connect();
      } catch (error) {
        if (isClosed) {
          return;
        }
        if (!_retryable(error)) {
          rethrow;
        }
        var floor = _serverRetry ?? Duration.zero;
        if (error is ApiException && error.retryAfter != null && error.retryAfter! > floor) {
          floor = error.retryAfter!;
        }
        yield* _pause(backoff.next(floor), _describe(error));
        continue;
      }
      backoff.reset();
      yield StreamSignal(SignalType.open, url: url);

      Map<String, Object?>? reset;
      Object? failure;
      final parser = SseParser();
      try {
        final chunks = response.data!.stream.cast<List<int>>().transform(utf8.decoder).timeout(watchdogTimeout);
        await for (final chunk in chunks) {
          final messages = parser.feed(chunk);
          if (parser.retryMs != null) {
            _serverRetry = Duration(milliseconds: parser.retryMs!);
          }
          for (final message in messages) {
            if (message.event == 'reset') {
              final data = _parseData(message.data);
              reset = data is Map<String, Object?> ? data : {'reason': '$data'};
              yield StreamSignal(SignalType.reset, data: reset);
              break;
            }
            yield* _handle(message.event, message.data, message.id);
          }
          if (reset != null) {
            break;
          }
        }
      } on TimeoutException {
        failure = HeartbeatTimeoutException(watchdogTimeout);
      } catch (error) {
        failure = error;
      } finally {
        _connection?.cancel('done');
      }
      if (isClosed) {
        return;
      }

      if (reset != null) {
        try {
          yield* _afterReset(reset);
        } catch (error) {
          if (isClosed) {
            return;
          }
          if (!_retryable(error)) {
            rethrow;
          }
          yield* _pause(backoff.next(_serverRetry ?? Duration.zero), _describe(error));
          continue;
        }
        final reason = 'reset: ${reset['reason']}';
        if (_serverRetry != null && _serverRetry! > Duration.zero) {
          yield* _pause(_serverRetry!, reason);
        } else {
          yield StreamSignal(SignalType.reconnecting, attempt: 0, delay: Duration.zero, reason: reason);
        }
      } else {
        final reason = failure == null ? 'stream closed by the server' : _describe(failure);
        yield* _pause(backoff.next(_serverRetry ?? Duration.zero), reason);
      }
    }
  }

  Stream<StreamSignal> _handle(String event, String raw, String? id) async* {
    if (id != null) {
      if (endpoint == StreamEndpoint.events) {
        _eventId = id;
        final c = compositeCursorValue(id, 'c');
        if (c != null) {
          _seq = c;
        }
      } else if (event == 'change') {
        final n = int.tryParse(id);
        if (n != null) {
          _seq = n;
        }
      }
    }
    final data = _parseData(raw);
    final json = data is Map<String, Object?> ? data : const <String, Object?>{};
    switch (event) {
      case 'hello':
        streamId = json['streamId'] as String?;
        yield StreamSignal(SignalType.hello, data: data);
      case 'change':
        guard.observe(ObservedAtGuard.key('${json['source']}', '${json['externalId']}'), json['observedAt']);
        yield StreamSignal(SignalType.change, data: data, id: id);
      case 'live':
      case 'touch':
        if (!guard.accept(ObservedAtGuard.key('${json['sourceId']}', '${json['externalId']}'), json['observedAt'])) {
          staleDropped++;
          return;
        }
        yield StreamSignal(event == 'live' ? SignalType.live : SignalType.touch, data: data);
      case 'resync':
        yield StreamSignal(SignalType.resync, data: data);
        if (snapshot) {
          await for (final page in _snapshot('resync')) {
            yield page;
          }
        }
      case 'heartbeat':
        yield const StreamSignal(SignalType.heartbeat);
      default:
        if (!_coreEvents.contains(event)) {
          yield StreamSignal(SignalType.event, event: event, data: data, id: id);
        }
    }
  }

  Stream<StreamSignal> _afterReset(Map<String, Object?> reset) async* {
    if (reset['reason'] == 'resync_required') {
      int? watermark = reset['watermarkSeq'] is int ? reset['watermarkSeq'] as int : null;
      if (snapshot) {
        await for (final page in _snapshot('reset')) {
          watermark = page.watermarkSeq;
          yield page;
        }
      }
      _moveCursor(watermark);
      return;
    }
    final resume = reset['resumeAfter'];
    if (resume is int) {
      _moveCursor(resume);
    }
  }

  void _moveCursor(int? seq) {
    _seq = seq;
    if (endpoint == StreamEndpoint.events) {
      _eventId = seq == null ? null : withCompositeCursorValue(_eventId, 'c', seq);
    }
  }

  /// Pages `/v1/records?order=id` with the stream's record filters (each page carries the watermark).
  Stream<StreamSignal> _snapshot(String cause) async* {
    final query = <String, Object?>{..._recordQuery(), 'order': 'id', 'limit': snapshotPageSize};
    String? cursor;
    int? watermark;
    do {
      final page = await transport.request('GET', '/v1/records', query: cursor == null ? query : {...query, 'cursor': cursor});
      final body = page.json;
      if (watermark == null) {
        final value = body['watermarkSeq'] ?? body['asOfSeq'];
        watermark = value is int ? value : int.tryParse(page.headers.value('foresignal-watermark-seq') ?? '');
      }
      final items = (body['items'] as List?)?.cast<Object?>() ?? const <Object?>[];
      for (final item in items) {
        if (item is Map) {
          guard.observe(ObservedAtGuard.key('${item['source']}', '${item['externalId']}'), item['observedAt']);
        }
      }
      final next = body['nextCursor'];
      cursor = next is String && next.isNotEmpty ? next : null;
      yield StreamSignal(SignalType.snapshot,
          cause: cause, items: items, watermarkSeq: watermark, done: cursor == null);
    } while (cursor != null && !isClosed);
  }

  Map<String, Object?> _recordQuery() {
    if (_snapshotQuery != null) {
      return {..._snapshotQuery};
    }
    final out = <String, Object?>{};
    filters.forEach((key, value) {
      final name = endpoint == StreamEndpoint.events ? (key.startsWith('changes.') ? key.substring(8) : null) : key;
      if (name != null && snapshotKeys.contains(name)) {
        out[name] = value;
      }
    });
    return out;
  }

  Future<(Response<ResponseBody>, String)> _connect() async {
    final path = endpoint == StreamEndpoint.events ? '/v1/events/stream' : '/v1/changes/stream';
    for (var ticketAttempt = 0;; ticketAttempt++) {
      final query = <String, Object?>{...filters};
      final headers = <String, String>{'Accept': 'text/event-stream', 'Cache-Control': 'no-cache'};
      if (endpoint == StreamEndpoint.events) {
        if (_eventId != null) {
          if (useTicket) {
            query['lastEventId'] = _eventId;
          } else {
            headers['Last-Event-ID'] = _eventId!;
          }
        }
      } else if (_seq != null) {
        if (useTicket) {
          query['after'] = _seq;
        } else {
          headers['Last-Event-ID'] = '$_seq';
        }
      }
      if (useTicket) {
        query['ticket'] = await _ensureTicket();
      }
      _connection = CancelToken();
      final response = await transport.openStream(path,
          query: query, headers: headers, auth: !useTicket, cancelToken: _connection);
      final status = response.statusCode ?? 0;
      if (status == 200) {
        final shown = {...query}..remove('ticket');
        return (response, transport.uri(path, shown).toString());
      }
      final body = await utf8.decodeStream(response.data!.stream.cast<List<int>>());
      if (status == 401 && useTicket && ticketAttempt == 0) {
        // The ticket expired (5 min after the last disconnect, 24 h in total): mint a new one once.
        _ticket = null;
        continue;
      }
      throw apiExceptionFrom(status, body, response.headers.value('retry-after'));
    }
  }

  Future<String> _ensureTicket() async {
    if (_ticket == null) {
      final minted = await transport.postJson('/v1/stream-tickets', {'purpose': 'stream'});
      _ticket = minted.json['ticket'] as String;
    }
    return _ticket!;
  }

  /// Network failures, timeouts and 408/425/429/5xx are retried; other API errors and programming errors are not.
  bool _retryable(Object error) {
    if (error is ApiException) {
      return error.status == 408 || error.status == 425 || error.status == 429 || error.status >= 500;
    }
    if (error is DioException) {
      return error.type != DioExceptionType.cancel && error.type != DioExceptionType.badCertificate;
    }
    return error is Exception;
  }

  Stream<StreamSignal> _pause(Duration delay, String reason) async* {
    yield StreamSignal(SignalType.reconnecting, attempt: backoff.attempt, delay: delay, reason: reason);
    await _sleep(delay, _stop.future);
  }
}

String _describe(Object error) => error.toString();
