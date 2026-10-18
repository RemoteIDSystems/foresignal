// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_test_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingTestResultCWProxy {
  MappingTestResult mappingSource(
    MappingTestResultMappingSourceEnum mappingSource,
  );

  MappingTestResult samples(List<MappingTestResultSamplesInner> samples);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingTestResult call({
    MappingTestResultMappingSourceEnum mappingSource,
    List<MappingTestResultSamplesInner> samples,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingTestResult.copyWith(...)` or call `instanceOfMappingTestResult.copyWith.fieldName(value)` for a single field.
class _$MappingTestResultCWProxyImpl implements _$MappingTestResultCWProxy {
  const _$MappingTestResultCWProxyImpl(this._value);

  final MappingTestResult _value;

  @override
  MappingTestResult mappingSource(
    MappingTestResultMappingSourceEnum mappingSource,
  ) => call(mappingSource: mappingSource);

  @override
  MappingTestResult samples(List<MappingTestResultSamplesInner> samples) =>
      call(samples: samples);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingTestResult call({
    Object? mappingSource = const $CopyWithPlaceholder(),
    Object? samples = const $CopyWithPlaceholder(),
  }) {
    return MappingTestResult(
      mappingSource:
          mappingSource == const $CopyWithPlaceholder() || mappingSource == null
          ? _value.mappingSource
          // ignore: cast_nullable_to_non_nullable
          : mappingSource as MappingTestResultMappingSourceEnum,
      samples: samples == const $CopyWithPlaceholder() || samples == null
          ? _value.samples
          // ignore: cast_nullable_to_non_nullable
          : samples as List<MappingTestResultSamplesInner>,
    );
  }
}

extension $MappingTestResultCopyWith on MappingTestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingTestResult.copyWith(...)` or `instanceOfMappingTestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingTestResultCWProxy get copyWith =>
      _$MappingTestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingTestResult _$MappingTestResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MappingTestResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['mappingSource', 'samples']);
      final val = MappingTestResult(
        mappingSource: $checkedConvert(
          'mappingSource',
          (v) => $enumDecode(_$MappingTestResultMappingSourceEnumEnumMap, v),
        ),
        samples: $checkedConvert(
          'samples',
          (v) => (v as List<dynamic>)
              .map(
                (e) => MappingTestResultSamplesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MappingTestResultToJson(MappingTestResult instance) =>
    <String, dynamic>{
      'mappingSource':
          _$MappingTestResultMappingSourceEnumEnumMap[instance.mappingSource]!,
      'samples': instance.samples.map((e) => e.toJson()).toList(),
    };

const _$MappingTestResultMappingSourceEnumEnumMap = {
  MappingTestResultMappingSourceEnum.stored: 'stored',
  MappingTestResultMappingSourceEnum.proposed: 'proposed',
};
