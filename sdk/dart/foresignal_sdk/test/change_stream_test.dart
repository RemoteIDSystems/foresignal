// ChangeStream against an in-process SSE server: resume with the last id, reset with a snapshot, heartbeat
// watchdog, observedAt guard, stream tickets, retry hints and the multiplexed events endpoint.
import 'package:foresignal_sdk/foresignal_sdk.dart';
import 'package:test/test.dart';

import 'mock_server.dart';

Map<String, Object?> change(int seq, [String externalId = 'ae1234', String observedAt = '2026-10-18T00:00:00Z']) => {
      'seq': seq,
      'source': 'adsb',
      'externalId': externalId,
      'kind': 'aircraft',
      'version': 1,
      'observedAt': observedAt,
    };

void main() {
  MockServer? server;

  tearDown(() async {
    await server?.close();
    server = null;
  });

  group('ChangeStream on /v1/changes/stream', () {
    test('resumes after a disconnect with the last change id', () async {
      var connection = 0;
      server = await MockServer.start((req, res) {
        connection++;
        if (connection == 1) {
          return sse(res, [
            {'id': '5', 'event': 'change', 'data': change(5)},
            {'id': '6', 'event': 'change', 'data': change(6)},
          ]);
        }
        return sse(res, [{'id': '7', 'event': 'change', 'data': change(7)}], keepOpen: true);
      });
      final stream = ChangeStream(
          transport: HttpTransport(baseUrl: server!.url, token: 'fs_test'), after: 4, sleep: RecordingSleep().call);
      final signals = await collect(stream, (s, _) => s.type == SignalType.change && s.json['seq'] == 7);

      final streams = server!.to('/v1/changes/stream');
      expect(streams, hasLength(2));
      expect(streams[0].header('last-event-id'), '4');
      expect(streams[0].header('authorization'), 'Bearer fs_test');
      expect(streams[1].header('last-event-id'), '6');
      expect(signals.where((s) => s.type == SignalType.change).map((s) => s.id), ['5', '6', '7']);
      expect(signals.any((s) => s.type == SignalType.reconnecting), isTrue);
      expect(stream.lastSeq, 7);
      expect(stream.lastEventId, '7');
    });

    test('on reset resync_required pages the snapshot and reopens at the watermark', () async {
      var connection = 0;
      server = await MockServer.start((req, res) {
        if (req.path == '/v1/records') {
          if (req.q('cursor') == null) {
            return json(res, 200, {
              'items': [
                {'source': 'adsb', 'externalId': 'a', 'observedAt': '2026-10-18T00:00:10Z'},
              ],
              'nextCursor': 'p2',
              'watermarkSeq': 500,
            });
          }
          return json(res, 200, {
            'items': [
              {'source': 'adsb', 'externalId': 'b', 'observedAt': '2026-10-18T00:00:11Z'},
            ],
            'nextCursor': null,
            'watermarkSeq': 500,
          });
        }
        connection++;
        if (connection == 1) {
          return sse(res, [
            {'id': '10', 'event': 'change', 'data': change(10)},
            {
              'event': 'reset',
              'data': {'reason': 'resync_required', 'resumeAfter': null, 'watermarkSeq': null},
            },
          ]);
        }
        return sse(res, [{'id': '501', 'event': 'change', 'data': change(501)}], keepOpen: true);
      });
      final stream = ChangeStream(
        transport: HttpTransport(baseUrl: server!.url),
        after: 9,
        sleep: RecordingSleep().call,
        filters: {
          'source': ['adsb'],
          'kind': 'aircraft',
          'live': true,
          'touches': true,
        },
      );
      final signals = await collect(stream, (s, _) => s.type == SignalType.change && s.json['seq'] == 501);

      expect(signals.map((s) => s.type), [
        SignalType.open,
        SignalType.change,
        SignalType.reset,
        SignalType.snapshot,
        SignalType.snapshot,
        SignalType.reconnecting,
        SignalType.open,
        SignalType.change,
      ]);
      final snapshots = signals.where((s) => s.type == SignalType.snapshot).toList();
      expect(snapshots.map((s) => s.done), [false, true]);
      expect(snapshots.every((s) => s.cause == 'reset' && s.watermarkSeq == 500), isTrue);
      expect(snapshots.expand((s) => s.items!).map((i) => (i as Map)['externalId']), ['a', 'b']);

      final records = server!.to('/v1/records');
      expect(records, hasLength(2));
      expect(records[0].q('order'), 'id');
      expect(records[0].query['source'], ['adsb']);
      expect(records[0].q('kind'), 'aircraft');
      expect(records[0].query.containsKey('live'), isFalse);
      expect(records[0].query.containsKey('touches'), isFalse);
      expect(records[1].q('cursor'), 'p2');
      expect(server!.to('/v1/changes/stream')[1].header('last-event-id'), '500');
      expect(stream.guard[ObservedAtGuard.key('adsb', 'b')], DateTime.parse('2026-10-18T00:00:11Z'));
    });

    test('on reset buffer_overflow reopens at resumeAfter without a snapshot', () async {
      var connection = 0;
      server = await MockServer.start((req, res) {
        connection++;
        if (connection == 1) {
          return sse(res, [
            {'id': '40', 'event': 'change', 'data': change(40)},
            {
              'event': 'reset',
              'data': {'reason': 'buffer_overflow', 'resumeAfter': 42, 'watermarkSeq': 90},
            },
          ]);
        }
        return sse(res, [{'id': '43', 'event': 'change', 'data': change(43)}], keepOpen: true);
      });
      await collect(ChangeStream(transport: HttpTransport(baseUrl: server!.url), sleep: RecordingSleep().call),
          (s, _) => s.type == SignalType.change && s.json['seq'] == 43);
      expect(server!.to('/v1/records'), isEmpty);
      expect(server!.to('/v1/changes/stream')[1].header('last-event-id'), '42');
    });

    test('re-pages the live state on resync and keeps the stream open', () async {
      server = await MockServer.start((req, res) {
        if (req.path == '/v1/records') {
          return json(res, 200, {
            'items': [
              {'source': 'adsb', 'externalId': 'a'},
            ],
            'nextCursor': null,
            'watermarkSeq': 77,
          });
        }
        return sse(res, [
          {
            'event': 'resync',
            'data': {'reason': 'live_dropped', 'watermarkSeq': 77},
          },
          {'id': '78', 'event': 'change', 'data': change(78)},
        ], keepOpen: true);
      });
      final signals = await collect(
          ChangeStream(transport: HttpTransport(baseUrl: server!.url), sleep: RecordingSleep().call),
          (s, _) => s.type == SignalType.change);
      expect(signals.map((s) => s.type),
          [SignalType.open, SignalType.resync, SignalType.snapshot, SignalType.change]);
      expect(server!.to('/v1/changes/stream'), hasLength(1));
      expect(signals[2].cause, 'resync');
      expect(signals[2].watermarkSeq, 77);
    });

    test('reconnects when no event or heartbeat arrives within 2.5 x the heartbeat interval', () async {
      var connection = 0;
      server = await MockServer.start((req, res) {
        connection++;
        if (connection == 1) {
          return sse(res, [{'event': 'heartbeat', 'data': <String, Object?>{}}], keepOpen: true);
        }
        return sse(res, [{'id': '1', 'event': 'change', 'data': change(1)}], keepOpen: true);
      });
      final sleep = RecordingSleep();
      final stream = ChangeStream(
          transport: HttpTransport(baseUrl: server!.url),
          heartbeatInterval: const Duration(milliseconds: 40),
          sleep: sleep.call);
      expect(stream.watchdogTimeout, const Duration(milliseconds: 100));
      final watch = Stopwatch()..start();
      final signals = await collect(stream, (s, _) => s.type == SignalType.change);
      final reconnect = signals.firstWhere((s) => s.type == SignalType.reconnecting);
      expect(reconnect.reason, contains('HeartbeatTimeoutException'));
      expect(signals.where((s) => s.type == SignalType.heartbeat), hasLength(1));
      expect(watch.elapsedMilliseconds, greaterThanOrEqualTo(90));
      expect(sleep.delays.first, greaterThanOrEqualTo(const Duration(seconds: 1)));
      expect(server!.to('/v1/changes/stream'), hasLength(2));
    });

    test('applies live and touch events only when observedAt is not older than the stored one', () async {
      Map<String, Object?> live(String ext, int sec) => {
            'event': 'live',
            'data': {
              'sourceId': 'adsb',
              'externalId': ext,
              'kind': 'aircraft',
              'version': 1,
              'observedAt': '2026-10-18T00:00:0${sec}Z',
            },
          };
      Map<String, Object?> touch(String ext, int sec) => {
            'event': 'touch',
            'data': {'sourceId': 'adsb', 'externalId': ext, 'observedAt': '2026-10-18T00:00:0${sec}Z'},
          };
      server = await MockServer.start((req, res) => sse(res, [
            live('A', 2),
            live('A', 1),
            touch('A', 3),
            live('B', 1),
            {'id': '9', 'event': 'change', 'data': change(9, 'A', '2026-10-18T00:00:05Z')},
            live('A', 4),
            touch('A', 5),
            {'event': 'heartbeat', 'data': <String, Object?>{}},
          ], keepOpen: true));
      final stream = ChangeStream(transport: HttpTransport(baseUrl: server!.url), sleep: RecordingSleep().call);
      final signals = await collect(stream, (s, _) => s.type == SignalType.heartbeat);
      final applied = signals
          .where((s) => s.type == SignalType.live || s.type == SignalType.touch)
          .map((s) => '${s.type.name}:${s.json['externalId']}@${(s.json['observedAt'] as String).substring(17, 19)}')
          .toList();
      expect(applied, ['live:A@02', 'touch:A@03', 'live:B@01', 'touch:A@05']);
      expect(stream.staleDropped, 2);
    });

    test('uses a stream ticket instead of the Authorization header and mints a new one on 401', () async {
      var tickets = 0;
      var connection = 0;
      server = await MockServer.start((req, res) {
        if (req.path == '/v1/stream-tickets') {
          tickets++;
          return json(res, 201, {'ticket': 'fst_t$tickets', 'purpose': 'stream', 'expiresAt': '2026-10-18T00:01:00Z'});
        }
        connection++;
        if (connection == 1) {
          return sse(res, [{'id': '8', 'event': 'change', 'data': change(8)}]);
        }
        if (connection == 2) {
          return json(res, 401, {'status': 401, 'code': 'UNAUTHORIZED'});
        }
        return sse(res, [{'id': '9', 'event': 'change', 'data': change(9)}], keepOpen: true);
      });
      final stream = ChangeStream(
          transport: HttpTransport(baseUrl: server!.url, token: 'fs_secret'),
          useTicket: true,
          after: 7,
          sleep: RecordingSleep().call);
      await collect(stream, (s, _) => s.type == SignalType.change && s.json['seq'] == 9);

      final mints = server!.to('/v1/stream-tickets', 'POST');
      expect(mints, hasLength(2));
      expect(mints[0].header('authorization'), 'Bearer fs_secret');
      expect(mints[0].body, '{"purpose":"stream"}');
      final streams = server!.to('/v1/changes/stream');
      expect(streams, hasLength(3));
      for (final s in streams) {
        expect(s.header('authorization'), isNull);
        expect(s.header('last-event-id'), isNull);
      }
      expect(streams.map((s) => '${s.q('ticket')}/${s.q('after')}'), ['fst_t1/7', 'fst_t1/8', 'fst_t2/8']);
    });

    test('honors Retry-After and retry: in the reconnect delay', () async {
      var connection = 0;
      server = await MockServer.start((req, res) {
        connection++;
        if (connection == 1) {
          return json(res, 429, {'status': 429, 'code': 'STREAM_LIMIT'}, {'Retry-After': '3'});
        }
        if (connection == 2) {
          return sse(res, [
            {'retry': 5000, 'comment': 'hint'},
            {'id': '1', 'event': 'change', 'data': change(1)},
          ]);
        }
        return sse(res, [{'id': '2', 'event': 'change', 'data': change(2)}], keepOpen: true);
      });
      final sleep = RecordingSleep();
      await collect(
          ChangeStream(
              transport: HttpTransport(baseUrl: server!.url), sleep: sleep.call, backoff: Backoff(random: () => 0.5)),
          (s, _) => s.type == SignalType.change && s.json['seq'] == 2);
      expect(sleep.delays[0], greaterThanOrEqualTo(const Duration(seconds: 3)));
      expect(sleep.delays[1], greaterThanOrEqualTo(const Duration(seconds: 5)));
    });

    test('fails on a non-retryable status', () async {
      server = await MockServer.start((req, res) => json(res, 403, {'status': 403, 'code': 'INSUFFICIENT_SCOPE'}));
      final stream = ChangeStream(transport: HttpTransport(baseUrl: server!.url), sleep: RecordingSleep().call);
      await expectLater(collect(stream, (_, _) => false), throwsA(isA<ApiException>()));
    });

    test('can be listened to only once and stops on close', () async {
      server = await MockServer.start(
          (req, res) => sse(res, [{'event': 'heartbeat', 'data': <String, Object?>{}}], keepOpen: true));
      final stream = ChangeStream(transport: HttpTransport(baseUrl: server!.url), sleep: RecordingSleep().call);
      final signals = await collect(stream, (s, _) => s.type == SignalType.heartbeat);
      expect(signals.last.type, SignalType.heartbeat);
      expect(stream.isClosed, isTrue);
      expect(stream.signals, throwsStateError);
    });
  });

  group('ChangeStream on /v1/events/stream', () {
    test('tracks the composite cursor, yields other topics and resumes with Last-Event-ID', () async {
      var connection = 0;
      server = await MockServer.start((req, res) {
        connection++;
        if (connection == 1) {
          return sse(res, [
            {
              'event': 'hello',
              'data': {
                'streamId': 'AAAAAAAAAAAAAAAAAAAAAA',
                'topics': ['changes', 'alerts'],
                'watermarkSeq': 100,
                'filterVersion': 1,
              },
            },
            {'id': 'c=101;a=5', 'event': 'change', 'data': change(101)},
            {
              'id': 'c=101;a=6',
              'event': 'alert',
              'data': {'id': 6, 'state': 'firing'},
            },
          ]);
        }
        return sse(res, [{'id': 'c=102;a=6', 'event': 'change', 'data': change(102)}], keepOpen: true);
      });
      final stream = ChangeStream(
        transport: HttpTransport(baseUrl: server!.url),
        endpoint: StreamEndpoint.events,
        sleep: RecordingSleep().call,
        filters: {'topics': 'changes,alerts', 'changes.source': 'adsb'},
      );
      final signals = await collect(stream, (s, _) => s.type == SignalType.change && s.json['seq'] == 102);
      expect(stream.streamId, 'AAAAAAAAAAAAAAAAAAAAAA');
      final alert = signals.firstWhere((s) => s.type == SignalType.event);
      expect(alert.event, 'alert');
      expect(alert.id, 'c=101;a=6');
      final streams = server!.to('/v1/events/stream');
      expect(streams[0].q('topics'), 'changes,alerts');
      expect(streams[0].q('changes.source'), 'adsb');
      expect(streams[1].header('last-event-id'), 'c=101;a=6');
      expect(stream.lastSeq, 102);
      expect(stream.lastEventId, 'c=102;a=6');
    });

    test('reopens at the watermark keeping the other topic cursors after a resync_required reset', () async {
      var connection = 0;
      server = await MockServer.start((req, res) {
        if (req.path == '/v1/records') {
          return json(res, 200, {'items': <Object?>[], 'nextCursor': null, 'watermarkSeq': 900});
        }
        connection++;
        if (connection == 1) {
          return sse(res, [
            {'id': 'c=10;a=3', 'event': 'change', 'data': change(10)},
            {
              'event': 'reset',
              'data': {'reason': 'resync_required'},
            },
          ]);
        }
        return sse(res, [{'id': 'c=901;a=3', 'event': 'change', 'data': change(901)}], keepOpen: true);
      });
      await collect(
          ChangeStream(
              transport: HttpTransport(baseUrl: server!.url),
              endpoint: StreamEndpoint.events,
              sleep: RecordingSleep().call,
              filters: {'changes.source': 'adsb', 'changes.live': true}),
          (s, _) => s.type == SignalType.change && s.json['seq'] == 901);
      expect(server!.to('/v1/records')[0].q('source'), 'adsb');
      expect(server!.to('/v1/events/stream')[1].header('last-event-id'), 'c=900;a=3');
    });

    test('sends the cursor as lastEventId when a ticket is used', () async {
      server = await MockServer.start((req, res) {
        if (req.path == '/v1/stream-tickets') {
          return json(res, 201, {'ticket': 'fst_x', 'purpose': 'stream', 'expiresAt': '2026-10-18T00:01:00Z'});
        }
        return sse(res, [{'event': 'heartbeat', 'data': <String, Object?>{}}], keepOpen: true);
      });
      await collect(
          ChangeStream(
              transport: HttpTransport(baseUrl: server!.url),
              endpoint: StreamEndpoint.events,
              lastEventId: 'c=5;a=1',
              useTicket: true,
              sleep: RecordingSleep().call),
          (s, _) => s.type == SignalType.heartbeat);
      final req = server!.to('/v1/events/stream')[0];
      expect(req.q('lastEventId'), 'c=5;a=1');
      expect(req.q('ticket'), 'fst_x');
      expect(req.header('last-event-id'), isNull);
    });
  });

  group('helpers', () {
    test('composite cursors', () {
      expect(compositeCursorValue('c=1234;a=56;n=7', 'a'), 56);
      expect(compositeCursorValue('a=56', 'c'), isNull);
      expect(compositeCursorValue(null, 'c'), isNull);
      expect(withCompositeCursorValue('c=1;a=2', 'c', 9), 'c=9;a=2');
      expect(withCompositeCursorValue(null, 'c', 9), 'c=9');
      expect(withCompositeCursorValue('a=2', 'c', 9), 'c=9;a=2');
    });

    test('backoff doubles from 1 s to 60 s with jitter and server floors', () {
      final backoff = Backoff(random: () => 0.5);
      expect(List.generate(9, (_) => backoff.next().inMilliseconds),
          [1000, 2000, 4000, 8000, 16000, 32000, 60000, 60000, 60000]);
      backoff.reset();
      expect(backoff.next(const Duration(seconds: 90)), const Duration(seconds: 90));
      final low = Backoff(random: () => 0);
      expect(low.next(), const Duration(seconds: 1));
      expect(low.next(), const Duration(milliseconds: 1600));
      final high = Backoff(random: () => 0.999);
      high.next();
      expect(high.next().inMilliseconds, lessThanOrEqualTo(2400));
    });

    test('observedAt guard', () {
      final guard = ObservedAtGuard();
      final key = ObservedAtGuard.key('s', 'e');
      expect(guard.accept(key, '2026-10-18T00:00:02Z'), isTrue);
      expect(guard.accept(key, '2026-10-18T00:00:01Z'), isFalse);
      expect(guard.accept(key, '2026-10-18T00:00:02Z'), isTrue);
      expect(guard.accept(key, null), isTrue);
      expect(guard.length, 1);
    });

    test('SSE parser handles split lines, CRLF, comments and ids', () {
      final parser = SseParser();
      final out = [
        ...parser.feed('﻿id: 1\r'),
        ...parser.feed('\nevent: change\r\ndata: {"a":\ndata: 1}\r\n\r\n: comment\n\nretry: 250\n'),
        ...parser.feed('data: x\n\nid\ndata: y\n\nevent: e\n\n'),
      ];
      expect(out.map((m) => '${m.event}|${m.data}|${m.id}'), ['change|{"a":\n1}|1', 'message|x|null', 'message|y|null']);
      expect(parser.retryMs, 250);
    });
  });
}
