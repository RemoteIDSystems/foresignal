// WorkspaceSync: remote patches in version order, own-origin events ignored, gap and reconnect re-reads,
// presence, and the 412 retry.
import 'dart:convert';

import 'package:foresignal_sdk/foresignal_sdk.dart';
import 'package:test/test.dart';

import 'mock_server.dart';

const w = '01JAB0000000000000000000WS';

Map<String, Object?> ctxEvent(int version, Map<String, Object?> patch, String? origin) => {
      'event': 'workspace.context',
      'data': {
        'workspaceId': w,
        'version': version,
        'patch': patch,
        'originClientId': origin,
        'principal': 'p1',
        'at': '2026-10-18T00:00:00Z',
      },
    };

void main() {
  MockServer? server;
  WorkspaceSync? sync;

  tearDown(() async {
    await sync?.stop();
    sync = null;
    await server?.close();
    server = null;
  });

  test('applies remote patches in version order, ignores its own origin, re-reads on a gap, exposes presence',
      () async {
    var gets = 0;
    server = await MockServer.start((req, res) {
      if (req.path == '/v1/workspaces/$w' && req.method == 'GET') {
        gets++;
        if (gets == 1) {
          return json(res, 200, {
            'id': w,
            'context': {
              'layers': ['a'],
            },
            'contextVersion': 3,
          });
        }
        return json(res, 200, {
          'id': w,
          'context': {
            'layers': ['refreshed'],
          },
          'contextVersion': 9,
        });
      }
      if (req.path == '/v1/events/stream') {
        return sse(res, [
          {
            'event': 'hello',
            'data': {
              'streamId': 'AAAAAAAAAAAAAAAAAAAAAA',
              'topics': ['workspace'],
              'watermarkSeq': 0,
              'filterVersion': 1,
            },
          },
          {
            'event': 'workspace.presence',
            'data': {
              'workspaceId': w,
              'at': '2026-10-18T00:00:00Z',
              'clients': [
                {'clientId': 'me', 'clientKind': 'map', 'connectedAt': '2026-10-18T00:00:00Z'},
                {'clientId': 'other', 'clientKind': 'globe', 'principal': 'p1', 'connectedAt': '2026-10-18T00:00:00Z'},
              ],
              'change': {'type': 'joined', 'clientId': 'other'},
            },
          },
          ctxEvent(4, {
            'layers': ['a', 'b'],
          }, 'other'),
          ctxEvent(5, {'focusRef': 'situation:own'}, 'me'),
          ctxEvent(6, {
            'timeRange': {'start': '2026-10-01T00:00:00Z'},
          }, 'mcp:session-1'),
          ctxEvent(6, {
            'layers': ['duplicate'],
          }, 'other'),
          ctxEvent(9, {
            'layers': ['after-gap'],
          }, 'other'),
        ], keepOpen: true);
      }
      return json(res, 404, <String, Object?>{});
    });
    final changes = <ContextChange>[];
    final presence = <Map<String, Object?>>[];
    sync = WorkspaceSync(
        transport: HttpTransport(baseUrl: server!.url, token: 'fs_t'),
        workspaceId: w,
        clientId: 'me',
        clientKind: 'map',
        sleep: RecordingSleep().call);
    sync!.changes.listen(changes.add);
    sync!.presenceEvents.listen(presence.add);
    await sync!.start();
    await waitFor(() => changes.any((c) => c.version == 9));

    expect(changes.map((c) => '${c.origin.name}:${c.version}'), ['refresh:3', 'remote:4', 'remote:6', 'refresh:9']);
    final afterSix = changes[2].context;
    expect(afterSix['layers'], ['a', 'b']);
    expect(afterSix['timeRange'], {'start': '2026-10-01T00:00:00Z'});
    expect(afterSix.containsKey('focusRef'), isFalse);
    expect(sync!.version, 9);
    expect(sync!.context, {
      'layers': ['refreshed'],
    });
    expect(presence, hasLength(1));
    expect(sync!.presence.map((c) => c['clientId']), ['me', 'other']);

    final stream = server!.to('/v1/events/stream').first;
    expect(stream.q('topics'), 'workspace');
    expect(stream.q('workspace.workspaceId'), w);
    expect(stream.q('workspace.clientId'), 'me');
    expect(stream.q('workspace.clientKind'), 'map');
    expect(stream.header('authorization'), 'Bearer fs_t');
  });

  test('re-reads the workspace after a reconnect', () async {
    var gets = 0;
    var streams = 0;
    server = await MockServer.start((req, res) {
      if (req.path == '/v1/workspaces/$w') {
        gets++;
        return json(res, 200, {'id': w, 'context': <String, Object?>{}, 'contextVersion': gets});
      }
      streams++;
      return sse(res, [{'event': 'heartbeat', 'data': <String, Object?>{}}], keepOpen: streams > 1);
    });
    sync = WorkspaceSync(transport: HttpTransport(baseUrl: server!.url), workspaceId: w, sleep: RecordingSleep().call);
    await sync!.start();
    await waitFor(() => gets >= 2);
    await waitFor(() => sync!.version == 2);
    expect(sync!.clientId, matches(RegExp(r'^c-[0-9a-f]{16}$')));
  });

  test('retries a stale PATCH with If-Match after re-reading and re-applying the intent', () async {
    var gets = 0;
    var patches = 0;
    server = await MockServer.start((req, res) {
      if (req.method == 'GET') {
        gets++;
        return json(res, 200, {
          'id': w,
          'context': {'layers': gets == 1 ? ['a'] : ['a', 'x']},
          'contextVersion': gets == 1 ? 3 : 4,
        });
      }
      patches++;
      if (patches == 1) {
        return json(res, 412, {'status': 412, 'code': 'WORKSPACE_CONTEXT_STALE', 'contextVersion': 4});
      }
      final patch = jsonDecode(req.body) as Map<String, Object?>;
      return json(res, 200, {
        'workspaceId': w,
        'contextVersion': 5,
        'context': {'layers': patch['layers']},
      });
    });
    sync = WorkspaceSync(transport: HttpTransport(baseUrl: server!.url, token: 'fs_t'), workspaceId: w, clientId: 'me');
    final state = await sync!.updateWith((ctx) => {
          'layers': [...((ctx['layers'] as List?) ?? const []), 'b'],
        });

    expect(state['contextVersion'], 5);
    expect(sync!.version, 5);
    expect(sync!.context, {
      'layers': ['a', 'x', 'b'],
    });
    final sent = server!.to('/v1/workspaces/$w/context', 'PATCH');
    expect(sent.map((r) => r.header('if-match')), ['3', '4']);
    expect(sent.map((r) => jsonDecode(r.body)), [
      {
        'layers': ['a', 'b'],
      },
      {
        'layers': ['a', 'x', 'b'],
      },
    ]);
    expect(sent.every((r) => r.header('content-type')!.startsWith('application/merge-patch+json')), isTrue);
    expect(sent.every((r) => r.header('foresignal-client-id') == 'me'), isTrue);
    expect(gets, 2);
  });

  test('gives up after maxAttempts and passes other errors through', () async {
    server = await MockServer.start((req, res) {
      if (req.method == 'GET') {
        return json(res, 200, {'id': w, 'context': <String, Object?>{}, 'contextVersion': 1});
      }
      if (req.body.contains('forbidden')) {
        return json(res, 403, {'status': 403, 'code': 'WORKSPACE_FORBIDDEN'});
      }
      return json(res, 412, {'status': 412, 'code': 'WORKSPACE_CONTEXT_STALE'});
    });
    sync = WorkspaceSync(transport: HttpTransport(baseUrl: server!.url), workspaceId: w, maxAttempts: 2);
    await expectLater(
        sync!.update({
          'layers': ['x'],
        }),
        throwsA(isA<ApiException>().having((e) => e.status, 'status', 412)));
    expect(server!.to('/v1/workspaces/$w/context', 'PATCH'), hasLength(2));
    await expectLater(
        sync!.update({
          'layers': ['forbidden'],
        }),
        throwsA(isA<ApiException>().having((e) => e.code, 'code', 'WORKSPACE_FORBIDDEN')));
    final none = await sync!.updateWith((_) => null);
    expect(none['contextVersion'], 1);
  });

  test('rejects an unknown client kind', () {
    expect(() => WorkspaceSync(transport: HttpTransport(baseUrl: 'http://127.0.0.1:9'), workspaceId: w, clientKind: 'tablet'),
        throwsArgumentError);
  });
}
