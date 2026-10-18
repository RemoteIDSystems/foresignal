// Cursor streams across pages, the changes feed, and the typed 410 RESYNC_REQUIRED error.
import 'package:foresignal_sdk/foresignal_sdk.dart';
import 'package:test/test.dart';

import 'mock_server.dart';

void main() {
  MockServer? server;

  tearDown(() async {
    await server?.close();
    server = null;
  });

  test('paginate follows nextCursor across three pages', () async {
    server = await MockServer.start((req, res) {
      final cursor = req.q('cursor');
      if (cursor == null) {
        return json(res, 200, {
          'items': [
            {'id': 1},
            {'id': 2},
          ],
          'nextCursor': 'c2',
        });
      }
      if (cursor == 'c2') {
        return json(res, 200, {
          'items': [
            {'id': 3},
          ],
          'nextCursor': 'c3',
        });
      }
      return json(res, 200, {
        'items': [
          {'id': 4},
        ],
        'nextCursor': null,
      });
    });
    final transport = HttpTransport(baseUrl: server!.url, token: 'fs_t');
    final items = await paginate(transport, '/v1/alerts', query: {'status': 'firing'}, pageSize: 2).toList();
    expect(items.map((i) => (i as Map)['id']), [1, 2, 3, 4]);
    final requests = server!.to('/v1/alerts');
    expect(requests.map((r) => r.q('cursor')), [null, 'c2', 'c3']);
    expect(requests.every((r) => r.q('status') == 'firing' && r.q('limit') == '2'), isTrue);
    expect(requests[0].header('authorization'), 'Bearer fs_t');
  });

  test('paginatePages stops at maxPages', () async {
    server = await MockServer.start((req, res) => json(res, 200, {
          'items': [req.q('cursor') ?? 'first'],
          'nextCursor': 'again',
        }));
    final pages = await paginatePages(HttpTransport(baseUrl: server!.url), '/v1/places', maxPages: 3).toList();
    expect(pages.map((p) => (p['items'] as List).first), ['first', 'again', 'again']);
  });

  test('paginateWith works with any fetcher and the older next spelling', () async {
    final pages = <String?, Map<String, Object?>>{
      null: {
        'items': [1],
        'next': 'b',
      },
      'b': {
        'items': [2],
        'next': 'c',
      },
      'c': {
        'items': [3],
        'next': null,
      },
    };
    final seen = <String?>[];
    final items = await paginateWith((cursor) async {
      seen.add(cursor);
      return pages[cursor]!;
    }).toList();
    expect(items, [1, 2, 3]);
    expect(seen, [null, 'b', 'c']);
    expect(nextCursorOf({'nextCursor': '', 'next': null}), isNull);
    expect(nextCursorOf({'next': 'x'}), 'x');
  });

  test('paginate surfaces API errors', () async {
    server = await MockServer.start(
        (req, res) => json(res, 400, {'status': 400, 'code': 'INVALID_CURSOR', 'detail': 'bad cursor'}));
    await expectLater(
        paginate(HttpTransport(baseUrl: server!.url), '/v1/records').toList(),
        throwsA(isA<ApiException>()
            .having((e) => e.code, 'code', 'INVALID_CURSOR')
            .having((e) => e.message, 'message', 'bad cursor')));
  });

  test('iterateChanges pages by seq until hasMore is false', () async {
    server = await MockServer.start((req, res) {
      final after = int.parse(req.q('after')!);
      if (after == 0) {
        return json(res, 200, {
          'items': [
            {'seq': 1},
            {'seq': 2},
          ],
          'lastSeq': 2,
          'hasMore': true,
          'watermarkSeq': 5,
        });
      }
      if (after == 2) {
        return json(res, 200, {
          'items': [
            {'seq': 4},
          ],
          'lastSeq': 4,
          'hasMore': true,
          'watermarkSeq': 5,
        });
      }
      return json(res, 200, {'items': <Object?>[], 'lastSeq': 5, 'hasMore': false, 'watermarkSeq': 5});
    });
    final lastSeqs = <Object?>[];
    final changes = await iterateChanges(HttpTransport(baseUrl: server!.url),
        filters: {
          'source': ['adsb', 'ais'],
        },
        limit: 2,
        onPage: (p) => lastSeqs.add(p['lastSeq'])).toList();
    expect(changes.map((c) => c['seq']), [1, 2, 4]);
    expect(lastSeqs, [2, 4, 5]);
    final requests = server!.to('/v1/changes');
    expect(requests.map((r) => r.q('after')), ['0', '2', '4']);
    expect(requests[0].query['source'], ['adsb', 'ais']);
    expect(requests[0].q('limit'), '2');
  });

  test('iterateChanges fails with ResyncRequiredException on 410 RESYNC_REQUIRED', () async {
    server = await MockServer.start((req, res) => json(res, 410, {
          'type': 'https://foresignal.ai/problems/resync-required',
          'title': 'Resync required',
          'status': 410,
          'code': 'RESYNC_REQUIRED',
          'purgeHorizon': 1200,
          'watermarkSeq': 5000,
        }));
    await expectLater(
        iterateChanges(HttpTransport(baseUrl: server!.url), after: 10).toList(),
        throwsA(isA<ResyncRequiredException>()
            .having((e) => e.status, 'status', 410)
            .having((e) => e.code, 'code', 'RESYNC_REQUIRED')
            .having((e) => e.purgeHorizon, 'purgeHorizon', 1200)
            .having((e) => e.watermarkSeq, 'watermarkSeq', 5000)));
  });

  test('another 410 stays a plain ApiException', () async {
    server = await MockServer.start((req, res) => json(res, 410, {'status': 410, 'code': 'GONE'}));
    try {
      await iterateChanges(HttpTransport(baseUrl: server!.url)).toList();
      fail('expected an ApiException');
    } on ApiException catch (e) {
      expect(e, isNot(isA<ResyncRequiredException>()));
      expect(e.code, 'GONE');
    }
  });

  test('parses Retry-After in seconds and as an HTTP date', () {
    expect(parseRetryAfter('3'), const Duration(seconds: 3));
    expect(parseRetryAfter('Sun, 18 Oct 2026 00:00:10 GMT', now: DateTime.utc(2026, 10, 18)),
        const Duration(seconds: 10));
    expect(parseRetryAfter('soon'), isNull);
    expect(parseRetryAfter(null), isNull);
  });
}
