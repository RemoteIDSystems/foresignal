// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_manifest_inputs.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisManifestInputsCWProxy {
  AnalysisManifestInputs refs(List<String>? refs);

  AnalysisManifestInputs query(Map<String, Object>? query);

  AnalysisManifestInputs count(int count);

  AnalysisManifestInputs hash(String hash);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisManifestInputs(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisManifestInputs(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisManifestInputs call({
    List<String>? refs,
    Map<String, Object>? query,
    int count,
    String hash,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisManifestInputs.copyWith(...)` or call `instanceOfAnalysisManifestInputs.copyWith.fieldName(value)` for a single field.
class _$AnalysisManifestInputsCWProxyImpl
    implements _$AnalysisManifestInputsCWProxy {
  const _$AnalysisManifestInputsCWProxyImpl(this._value);

  final AnalysisManifestInputs _value;

  @override
  AnalysisManifestInputs refs(List<String>? refs) => call(refs: refs);

  @override
  AnalysisManifestInputs query(Map<String, Object>? query) =>
      call(query: query);

  @override
  AnalysisManifestInputs count(int count) => call(count: count);

  @override
  AnalysisManifestInputs hash(String hash) => call(hash: hash);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisManifestInputs(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisManifestInputs(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisManifestInputs call({
    Object? refs = const $CopyWithPlaceholder(),
    Object? query = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
    Object? hash = const $CopyWithPlaceholder(),
  }) {
    return AnalysisManifestInputs(
      refs: refs == const $CopyWithPlaceholder()
          ? _value.refs
          // ignore: cast_nullable_to_non_nullable
          : refs as List<String>?,
      query: query == const $CopyWithPlaceholder()
          ? _value.query
          // ignore: cast_nullable_to_non_nullable
          : query as Map<String, Object>?,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
      hash: hash == const $CopyWithPlaceholder() || hash == null
          ? _value.hash
          // ignore: cast_nullable_to_non_nullable
          : hash as String,
    );
  }
}

extension $AnalysisManifestInputsCopyWith on AnalysisManifestInputs {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisManifestInputs.copyWith(...)` or `instanceOfAnalysisManifestInputs.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisManifestInputsCWProxy get copyWith =>
      _$AnalysisManifestInputsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisManifestInputs _$AnalysisManifestInputsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalysisManifestInputs', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['count', 'hash']);
  final val = AnalysisManifestInputs(
    refs: $checkedConvert(
      'refs',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    query: $checkedConvert(
      'query',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    count: $checkedConvert('count', (v) => (v as num).toInt()),
    hash: $checkedConvert('hash', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$AnalysisManifestInputsToJson(
  AnalysisManifestInputs instance,
) => <String, dynamic>{
  'refs': ?instance.refs,
  'query': ?instance.query,
  'count': instance.count,
  'hash': instance.hash,
};
