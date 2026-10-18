/// `WorkspaceSync` keeps a local copy of a workspace context in step with Foresignal (docs/Client-Contract.md §5),
/// with the same semantics as the TypeScript and Python SDKs:
///
/// - it reads the workspace, then follows the `workspace` topic of `/v1/events/stream` with this client's id and
///   kind;
/// - it applies remote `workspace.context` patches in version order, and re-reads the workspace on a version gap
///   or a reconnect (workspace events are id-less and not replayed);
/// - it ignores the content of events carrying its own `originClientId` (the PATCH response already gave it the
///   new state), using them only to advance the version;
/// - it sends local changes as JSON merge patches with `If-Match: <contextVersion>` and `Foresignal-Client-Id`,
///   and on `412 WORKSPACE_CONTEXT_STALE` re-reads the workspace, re-applies the intent and retries;
/// - it exposes presence from `workspace.presence` events.
library;

import 'dart:async';
import 'dart:convert';
import 'dart:math' as math;

import '../errors.dart';
import '../http.dart';
import '../realtime/backoff.dart';
import '../realtime/change_stream.dart';
import 'context.dart';

/// Client kinds accepted by the `workspace` topic.
const clientKinds = ['globe', 'map', 'analyst', 'satellite', 'rf', 'mobile', 'agent', 'other'];

/// Computes a merge patch from the current context (null for no change).
typedef ContextIntent = Map<String, Object?>? Function(Map<String, Object?> context);

/// Where a context change came from.
enum ContextOrigin {
  /// A remote `workspace.context` event.
  remote,

  /// This client's own PATCH response.
  local,

  /// A fresh read of the workspace.
  refresh,
}

/// A change of the local context copy.
class ContextChange {
  /// Creates the change.
  const ContextChange(this.context, this.version, this.origin, [this.event]);

  /// The context after the change (a copy).
  final Map<String, Object?> context;

  /// The context version after the change.
  final int version;

  /// Where it came from.
  final ContextOrigin origin;

  /// The remote event, for [ContextOrigin.remote].
  final Map<String, Object?>? event;
}

/// Generates a client id (`c-` and 16 random hex characters).
String generateClientId() {
  final random = math.Random.secure();
  return 'c-${List.generate(8, (_) => random.nextInt(256).toRadixString(16).padLeft(2, '0')).join()}';
}

/// Keeps one workspace context synchronized.
class WorkspaceSync {
  /// Creates the helper; call [start] to follow the workspace.
  WorkspaceSync({
    required this.transport,
    required this.workspaceId,
    String? clientId,
    this.clientKind = 'other',
    this.maxAttempts = 5,
    this.heartbeatInterval = const Duration(seconds: 15),
    this.backoff,
    this.useTicket = false,
    this.sleep,
  }) : clientId = clientId ?? generateClientId() {
    if (!clientKinds.contains(clientKind)) {
      throw ArgumentError.value(clientKind, 'clientKind', 'must be one of ${clientKinds.join(', ')}');
    }
  }

  /// The transport (base URL, token, dio).
  final HttpTransport transport;

  /// The workspace followed.
  final String workspaceId;

  /// This client's id: presence and `originClientId`.
  final String clientId;

  /// This client's kind.
  final String clientKind;

  /// Attempts per local change when the version is stale.
  final int maxAttempts;

  /// The server's heartbeat interval.
  final Duration heartbeatInterval;

  /// Reconnect backoff of the event stream.
  final Backoff? backoff;

  /// Whether the event stream uses a stream ticket.
  final bool useTicket;

  /// Sleep between stream reconnects (tests replace it).
  final StreamSleep? sleep;

  final StreamController<ContextChange> _changes = StreamController<ContextChange>.broadcast();
  final StreamController<Map<String, Object?>> _presence = StreamController<Map<String, Object?>>.broadcast();
  final StreamController<Object> _errors = StreamController<Object>.broadcast();
  final Map<int, Map<String, Object?>> _pending = {};
  Map<String, Object?>? _context;
  int? _version;
  List<Map<String, Object?>> _clients = const [];
  ChangeStream? _stream;
  StreamSubscription<StreamSignal>? _subscription;
  Future<void>? _refreshing;
  int _opened = 0;
  Future<void> _queue = Future.value();

  /// The current context (a copy), or null before the first read.
  Map<String, Object?>? get context => _context == null ? null : cloneJson(_context);

  /// The current context version, or null before the first read.
  int? get version => _version;

  /// The clients connected to the workspace, from the latest presence event.
  List<Map<String, Object?>> get presence => List.unmodifiable(_clients);

  /// Context changes.
  Stream<ContextChange> get changes => _changes.stream;

  /// Presence events.
  Stream<Map<String, Object?>> get presenceEvents => _presence.stream;

  /// Background errors (stream failures, failed re-reads).
  Stream<Object> get errors => _errors.stream;

  String get _path => '/v1/workspaces/${Uri.encodeComponent(workspaceId)}';

  /// Reads the workspace and starts following its events.
  Future<void> start() async {
    if (_stream != null) {
      return;
    }
    await refresh();
    final stream = ChangeStream(
      transport: transport,
      endpoint: StreamEndpoint.events,
      filters: {
        'topics': 'workspace',
        'workspace.workspaceId': workspaceId,
        'workspace.clientId': clientId,
        'workspace.clientKind': clientKind,
      },
      heartbeatInterval: heartbeatInterval,
      backoff: backoff,
      useTicket: useTicket,
      snapshot: false,
      sleep: sleep,
    );
    _stream = stream;
    _subscription = stream.signals().listen(
      (signal) => _queue = _queue.then((_) => _onSignal(signal)),
      onError: (Object error) {
        if (!stream.isClosed) {
          _errors.add(error);
        }
      },
    );
  }

  /// Stops following the workspace.
  Future<void> stop() async {
    _stream?.close();
    await _subscription?.cancel();
    await _queue;
    _stream = null;
    _subscription = null;
  }

  /// Re-reads the workspace (`GET /v1/workspaces/{id}`) and replaces the local copy.
  Future<void> refresh() {
    return _refreshing ??= () async {
      try {
        final response = await transport.getJson(_path);
        final body = response.json;
        _accept(Map<String, Object?>.from((body['context'] as Map?) ?? const {}),
            (body['contextVersion'] as num).toInt(), ContextOrigin.refresh);
      } finally {
        _refreshing = null;
      }
    }();
  }

  /// Sends a merge [patch] (a fixed change; see [updateWith] for one recomputed after a 412).
  Future<Map<String, Object?>> update(Map<String, Object?> patch) => updateWith((_) => patch);

  /// Sends a local change computed by [intent] from the current context, re-evaluated against the fresh
  /// context after a `412`. Returns the new `WorkspaceContextState` (or the current one when [intent] returns
  /// null); throws [ApiException] when the server refuses, or still answers 412 after [maxAttempts].
  Future<Map<String, Object?>> updateWith(ContextIntent intent) async {
    if (_version == null) {
      await refresh();
    }
    ApiException? last;
    for (var attempt = 0; attempt < maxAttempts; attempt++) {
      final current = cloneJson(_context!);
      final currentVersion = _version!;
      final patch = intent(current);
      if (patch == null) {
        return {'workspaceId': workspaceId, 'contextVersion': currentVersion, 'context': current};
      }
      final response = await transport.send('PATCH', '$_path/context',
          body: patch,
          contentType: 'application/merge-patch+json',
          headers: {'If-Match': '$currentVersion', 'Foresignal-Client-Id': clientId});
      final status = response.statusCode ?? 0;
      if (status >= 200 && status < 300) {
        final state = transportJson(response.data);
        _accept(Map<String, Object?>.from((state['context'] as Map?) ?? const {}),
            (state['contextVersion'] as num).toInt(), ContextOrigin.local);
        return state;
      }
      final error = apiExceptionFrom(status, response.data, response.headers.value('retry-after'));
      if (error.status != 412) {
        throw error;
      }
      last = error;
      await refresh();
    }
    throw last!;
  }

  /// Handles a `workspace.context` event (called for stream events; public for custom transports).
  Future<void> handleContextEvent(Map<String, Object?> event) async {
    if (event['workspaceId'] != workspaceId || _version == null) {
      return;
    }
    final version = event['version'];
    if (version is! int || version <= _version!) {
      return;
    }
    _pending[version] = event;
    _drain();
    if (_pending.isNotEmpty) {
      await refresh();
    }
  }

  /// Handles a `workspace.presence` event.
  void handlePresenceEvent(Map<String, Object?> event) {
    if (event['workspaceId'] != workspaceId) {
      return;
    }
    _clients = ((event['clients'] as List?) ?? const []).map((c) => Map<String, Object?>.from(c as Map)).toList();
    _presence.add(event);
  }

  Future<void> _onSignal(StreamSignal signal) async {
    try {
      switch (signal.type) {
        case SignalType.open:
          _opened++;
          if (_opened > 1) {
            await refresh();
          }
        case SignalType.event:
          if (signal.event == 'workspace.context') {
            await handleContextEvent(signal.json);
          } else if (signal.event == 'workspace.presence') {
            handlePresenceEvent(signal.json);
          }
        default:
          break;
      }
    } catch (error) {
      _errors.add(error);
    }
  }

  void _drain() {
    if (_version == null) {
      return;
    }
    _pending.removeWhere((v, _) => v <= _version!);
    while (_pending.containsKey(_version! + 1)) {
      final event = _pending.remove(_version! + 1)!;
      _version = event['version'] as int;
      if (event['originClientId'] == clientId) {
        continue; // our own change: the PATCH response carries the state
      }
      _context = applyMergePatch(_context, event['patch']) as Map<String, Object?>;
      _changes.add(ContextChange(cloneJson(_context!), _version!, ContextOrigin.remote, event));
    }
  }

  void _accept(Map<String, Object?> context, int version, ContextOrigin origin) {
    if (_version != null && version < _version!) {
      return;
    }
    _context = cloneJson(context);
    _version = version;
    _drain();
    _changes.add(ContextChange(cloneJson(_context!), _version!, origin));
  }
}

/// Decodes a JSON object body (empty when it is not one or does not parse).
Map<String, Object?> transportJson(String? body) {
  if (body == null || body.isEmpty) {
    return {};
  }
  try {
    final decoded = jsonDecode(body);
    return decoded is Map ? Map<String, Object?>.from(decoded) : {};
  } on FormatException {
    return {};
  }
}
