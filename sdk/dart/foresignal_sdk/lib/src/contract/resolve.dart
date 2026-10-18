/// Batched envelope resolution (`POST /v1/objects/resolve`, at most 500 refs per call). Missing and invisible
/// references come back in `missing`, not as errors (docs/Client-Contract.md §8).
library;

import '../http.dart';
import 'envelope.dart';
import 'object_ref.dart';

/// Most references per resolve call.
const maxResolveBatch = 500;

/// Splits references into batches of at most [size].
List<List<String>> batchRefs(List<String> refs, [int size = maxResolveBatch]) => [
      for (var i = 0; i < refs.length; i += size) refs.sublist(i, i + size > refs.length ? refs.length : i + size),
    ];

/// Resolves any number of references in batches of at most 500, keeping the first occurrence of duplicates and
/// the input order. Throws [ObjectRefException] for a malformed reference (when [validate] is on) and
/// [ApiException] when a call fails.
Future<ResolveResult> resolveObjects(HttpTransport transport, Iterable<String> refs,
    {String? asOf, int batchSize = maxResolveBatch, bool validate = true}) async {
  final size = batchSize.clamp(1, maxResolveBatch);
  final unique = <String>[];
  final seen = <String>{};
  for (final ref in refs) {
    if (validate) {
      parseObjectRef(ref);
    }
    if (seen.add(ref)) {
      unique.add(ref);
    }
  }
  final items = <ObjectEnvelope>[];
  final missing = <String>[];
  var resultAsOf = asOf;
  for (final batch in batchRefs(unique, size)) {
    final response =
        await transport.request('POST', '/v1/objects/resolve', body: {'refs': batch, 'asOf': ?asOf});
    final body = response.json;
    for (final item in (body['items'] as List?) ?? const []) {
      items.add(ObjectEnvelope(Map<String, Object?>.from(item as Map)));
    }
    missing.addAll(((body['missing'] as List?) ?? const []).cast<String>());
    resultAsOf = (body['asOf'] as String?) ?? resultAsOf;
  }
  return ResolveResult(items, missing, resultAsOf);
}
