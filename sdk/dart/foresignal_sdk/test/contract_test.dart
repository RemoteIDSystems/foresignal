// Client-contract round trips on the fixtures shared by the three SDKs (sdk/contract-fixtures), plus resolve
// batching against an in-process server.
import 'dart:convert';
import 'dart:io';

import 'package:foresignal_sdk/foresignal_sdk.dart';
import 'package:test/test.dart';

import 'mock_server.dart';

Map<String, Object?> _fixture(String name) =>
    jsonDecode(File('../../contract-fixtures/$name').readAsStringSync()) as Map<String, Object?>;

void main() {
  final refs = _fixture('refs.json');
  final contexts = _fixture('contexts.json');
  MockServer? server;

  tearDown(() async {
    await server?.close();
    server = null;
  });

  group('ObjectRef fixtures', () {
    for (final raw in refs['valid'] as List) {
      final c = raw as Map<String, Object?>;
      test('parses and formats ${c['ref']}', () {
        final ref = c['ref'] as String;
        final parsed = parseObjectRef(ref);
        expect(parsed.ref, ref);
        expect(parsed.type, c['type']);
        expect(parsed.id, c['id']);
        expect(formatObjectRef(parsed.type, parsed.id), ref);
        expect(objectRefPathSegment(ref), c['pathSegment']);
        expect(isValidObjectRef(ref), isTrue);
        expect(ObjectRef.parse(ref), parsed);
        final record = c['record'] as Map<String, Object?>?;
        if (record != null) {
          expect(parsed.record!.sourceId, record['sourceId']);
          expect(parsed.record!.externalId, record['externalId']);
          expect(parsed.record!.seq, record['seq']);
          expect(recordRef(record['sourceId'] as String, record['externalId'] as String, record['seq'] as int?), ref);
          expect(decodeExternalId(encodeExternalId(record['externalId'] as String)), record['externalId']);
        } else {
          expect(parsed.record, isNull);
        }
        final sensor = c['sensor'] as Map<String, Object?>?;
        expect(parsed.sensorKind, sensor?['kind']);
        expect(parsed.sensorId, sensor?['id']);
        final remote = c['remote'] as Map<String, Object?>?;
        expect(parsed.remoteInstanceId, remote?['instanceId']);
        expect(parsed.remoteRef, remote?['ref']);
        if (remote != null) {
          expect(remoteRef(remote['instanceId'] as String, remote['ref'] as String), ref);
        }
      });
    }

    for (final raw in refs['invalid'] as List) {
      final c = raw as Map<String, Object?>;
      test('rejects ${c['reason']}', () {
        expect(() => parseObjectRef(c['ref'] as String), throwsA(isA<ObjectRefException>()));
        expect(isValidObjectRef(c['ref'] as String), isFalse);
      });
    }

    test('rejects types outside the contract list when one is given', () {
      expect(() => parseObjectRef('future-type:x', knownTypes: ['situation', 'record']),
          throwsA(isA<ObjectRefException>()));
      expect(parseObjectRef('situation:x', knownTypes: ['situation']).type, 'situation');
    });
  });

  group('deep link fixtures', () {
    for (final raw in contexts['contexts'] as List) {
      final c = raw as Map<String, Object?>;
      test('encodes ${c['name']} exactly', () {
        final link = encodeDeepLink(c['workspaceId'] as String?, Map<String, Object?>.from(c['context'] as Map));
        expect(link.payload, c['payload']);
        expect(link.fragment, c['fragment']);
        expect(link.bytes, c['bytes']);
        expect(link.version, contexts['deepLinkVersion']);
      });

      test('decodes ${c['name']} from the payload, the fragment and a URL', () {
        for (final input in [c['payload'] as String, c['fragment'] as String, 'https://globe.example.org/app${c['fragment']}']) {
          final decoded = decodeDeepLink(input);
          expect(decoded.version, 1);
          expect(decoded.workspaceId, c['workspaceId']);
          expect(decoded.context, c['decodedContext']);
        }
        expect(stripNullMembers(Map<String, Object?>.from(c['context'] as Map)), c['decodedContext']);
      });
    }

    const kinds = {
      'version': DeepLinkErrorKind.version,
      'malformed': DeepLinkErrorKind.malformed,
      'context': DeepLinkErrorKind.context,
      'too-large': DeepLinkErrorKind.tooLarge,
    };

    for (final raw in contexts['invalidPayloads'] as List) {
      final c = raw as Map<String, Object?>;
      test('refuses ${c['name']}', () {
        expect(() => decodeDeepLink(c['payload'] as String),
            throwsA(isA<DeepLinkException>().having((e) => e.kind, 'kind', kinds[c['error']])));
      });
    }

    test('refuses to encode a context over 2 KB', () {
      final c = contexts['tooLarge'] as Map<String, Object?>;
      expect(() => encodeDeepLink(c['workspaceId'] as String?, Map<String, Object?>.from(c['context'] as Map)),
          throwsA(isA<DeepLinkException>().having((e) => e.kind, 'kind', kinds[c['error']])));
    });
  });

  group('context helpers', () {
    for (final raw in contexts['mergePatches'] as List) {
      final c = raw as Map<String, Object?>;
      test('merge patch: ${c['name']}', () {
        final target = cloneJson(c['target']);
        expect(applyMergePatch(target, c['patch']), c['result']);
        expect(target, c['target']);
      });
    }

    test('validates contexts like the server', () {
      final first = ((contexts['contexts'] as List).first as Map)['context'];
      expect(contextProblems(first), isEmpty);
      final errors = contextProblems({
        'camera': <String, Object?>{},
        'timeRange': {'end': '2026-10-01T00:00:00Z'},
        'playback': {'state': 'rewinding'},
        'viewport': {
          'bbox': [0, 0, 1, 1],
          'center': [0, 0],
        },
        'layers': [''],
        'activeSituationId': 'nope',
        'selectedRefs': ['situation:ok', 'bad ref'],
      });
      expect(
          errors.map((e) => e.field).toList()..sort(),
          [
            'context.activeSituationId',
            'context.camera',
            'context.layers',
            'context.playback.state',
            'context.selectedRefs[1]',
            'context.timeRange.start',
            'context.viewport',
          ]..sort());
      expect(
          () => validateContext({
                'timeRange': {'start': '2026-10-02T00:00:00Z', 'end': '2026-10-01T00:00:00Z'},
              }),
          throwsA(isA<ContextValidationException>()));
      expect(
          contextProblems({
            'viewport': {
              'center': [10, 10],
              'radiusKm': 25000,
            },
          }).first.field,
          'context.viewport.radiusKm');
    });

    test('checks contract versions', () {
      final ok = <String, Object?>{
        'contractVersion': '1.5',
        'objectTypes': <String>[],
        'relationTypes': <String>[],
        'contextSchemaVersion': 1,
        'deepLinkVersion': 1,
      };
      expect(contractProblems(ok), isEmpty);
      expect(contractProblems({...ok, 'contractVersion': '2.0', 'deepLinkVersion': 2}), hasLength(2));
    });
  });

  group('resolveObjects', () {
    test('resolves in batches of at most 500, keeping order and dropping duplicates', () async {
      server = await MockServer.start((req, res) {
        final body = jsonDecode(req.body) as Map<String, Object?>;
        final batch = (body['refs'] as List).cast<String>();
        return json(res, 200, {
          'items': [
            for (final r in batch)
              if (!r.endsWith('7')) {'ref': r, 'type': 'situation', 'title': r},
          ],
          'missing': [for (final r in batch) if (r.endsWith('7')) r],
          'asOf': body['asOf'],
        });
      });
      final input = [for (var i = 0; i < 1200; i++) 'situation:S$i', 'situation:S0'];
      final result = await resolveObjects(HttpTransport(baseUrl: server!.url, token: 'fs_t'), input,
          asOf: '2026-10-01T00:00:00Z');
      final calls = server!.to('/v1/objects/resolve', 'POST');
      expect(calls.map((c) => ((jsonDecode(c.body) as Map)['refs'] as List).length), [500, 500, 200]);
      expect((jsonDecode(calls[0].body) as Map)['asOf'], '2026-10-01T00:00:00Z');
      expect(calls[0].header('authorization'), 'Bearer fs_t');
      expect(result.items.length + result.missing.length, 1200);
      expect(result.missing, contains('situation:S7'));
      expect(result.items.first.ref, 'situation:S0');
      expect(result.items.first.title, 'situation:S0');
      expect(result.asOf, '2026-10-01T00:00:00Z');
    });

    test('validates references before sending and splits batches', () async {
      await expectLater(resolveObjects(HttpTransport(baseUrl: 'http://127.0.0.1:9'), ['situation:ok', 'nope']),
          throwsA(isA<ObjectRefException>()));
      expect(batchRefs(['a:1', 'b:2', 'c:3'], 2), [
        ['a:1', 'b:2'],
        ['c:3'],
      ]);
    });
  });
}
