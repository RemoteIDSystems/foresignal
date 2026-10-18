/// An in-process HTTP server for the SDK tests: records every request and lets each test script the answers,
/// including Server-Sent Event streams that stay open, end, or go silent.
library;

import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:foresignal_sdk/foresignal_sdk.dart';

/// A request the server received.
class Recorded {
  /// Creates the record.
  Recorded(this.method, this.path, this.query, this.headers, this.body);

  /// HTTP method.
  final String method;

  /// Path without the query.
  final String path;

  /// Query parameters (all values per name).
  final Map<String, List<String>> query;

  /// Request headers (case-insensitive).
  final HttpHeaders headers;

  /// The body as text.
  final String body;

  /// The first value of a query parameter.
  String? q(String name) => query[name]?.first;

  /// A header value.
  String? header(String name) => headers.value(name);
}

/// Answers one request.
typedef Handler = FutureOr<void> Function(Recorded request, HttpResponse response);

/// A running mock server.
class MockServer {
  MockServer._(this._server, this.requests);

  final HttpServer _server;

  /// Every request received.
  final List<Recorded> requests;

  /// The base URL.
  String get url => 'http://127.0.0.1:${_server.port}';

  /// Starts a server on a free loopback port.
  static Future<MockServer> start(Handler handler) async {
    final server = await HttpServer.bind(InternetAddress.loopbackIPv4, 0);
    final requests = <Recorded>[];
    server.listen((request) async {
      final body = await utf8.decodeStream(request);
      final recorded = Recorded(request.method, request.uri.path, request.uri.queryParametersAll, request.headers, body);
      requests.add(recorded);
      try {
        await handler(recorded, request.response);
      } catch (error) {
        try {
          request.response.statusCode = 500;
          request.response.write('$error');
          await request.response.close();
        } catch (_) {
          // The response was already started.
        }
      }
    });
    return MockServer._(server, requests);
  }

  /// Requests to one path.
  List<Recorded> to(String path, [String? method]) =>
      requests.where((r) => r.path == path && (method == null || r.method == method)).toList();

  /// Stops the server and drops open connections.
  Future<void> close() => _server.close(force: true);
}

/// Formats SSE frames.
String frames(List<Map<String, Object?>> frames) {
  final out = StringBuffer();
  for (final f in frames) {
    if (f.containsKey('comment')) {
      out.write(':${f['comment']}\n');
    }
    if (f.containsKey('retry')) {
      out.write('retry: ${f['retry']}\n');
    }
    if (f.containsKey('id')) {
      out.write('id: ${f['id']}\n');
    }
    if (f.containsKey('event')) {
      out.write('event: ${f['event']}\n');
    }
    if (f.containsKey('data')) {
      final data = f['data'];
      out.write('data: ${data is String ? data : jsonEncode(data)}\n');
    }
    out.write('\n');
  }
  return out.toString();
}

/// Answers with an SSE stream of [list], then ends it (or keeps it open with [keepOpen]).
Future<void> sse(HttpResponse response, List<Map<String, Object?>> list, {bool keepOpen = false}) async {
  response.statusCode = 200;
  response.headers.contentType = ContentType('text', 'event-stream', charset: 'utf-8');
  response.headers.set('Cache-Control', 'no-cache');
  response.bufferOutput = false;
  response.write(frames(list));
  await response.flush();
  if (!keepOpen) {
    await response.close();
  }
}

/// Answers with JSON.
Future<void> json(HttpResponse response, int status, Object? body, [Map<String, String> headers = const {}]) async {
  response.statusCode = status;
  response.headers.contentType =
      status >= 400 ? ContentType('application', 'problem+json') : ContentType('application', 'json');
  headers.forEach(response.headers.set);
  response.write(jsonEncode(body));
  await response.close();
}

/// Collects signals until [until] is true (or [limit] signals), then closes the stream.
Future<List<StreamSignal>> collect(ChangeStream stream, bool Function(StreamSignal s, List<StreamSignal> all) until,
    {int limit = 200}) async {
  final out = <StreamSignal>[];
  await for (final s in stream.signals()) {
    out.add(s);
    if (until(s, out) || out.length >= limit) {
      stream.close();
      break;
    }
  }
  return out;
}

/// An instant sleep that records the requested delays.
class RecordingSleep {
  /// The delays requested so far.
  final List<Duration> delays = [];

  /// The sleep function to pass to the stream.
  Future<void> call(Duration delay, Future<void> stop) async {
    delays.add(delay);
  }
}

/// Waits until [predicate] is true.
Future<void> waitFor(bool Function() predicate, {Duration timeout = const Duration(seconds: 5)}) async {
  final deadline = DateTime.now().add(timeout);
  while (!predicate()) {
    if (DateTime.now().isAfter(deadline)) {
      throw TimeoutException('condition not met', timeout);
    }
    await Future<void>.delayed(const Duration(milliseconds: 10));
  }
}
