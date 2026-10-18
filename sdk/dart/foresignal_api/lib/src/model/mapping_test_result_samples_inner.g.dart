// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_test_result_samples_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingTestResultSamplesInnerCWProxy {
  MappingTestResultSamplesInner records(List<NormalizedRecordPreview> records);

  MappingTestResultSamplesInner rejections(List<MappingRejection> rejections);

  MappingTestResultSamplesInner skipped(int? skipped);

  MappingTestResultSamplesInner warnings(List<MappingWarning> warnings);

  MappingTestResultSamplesInner receivedAt(DateTime receivedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingTestResultSamplesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingTestResultSamplesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingTestResultSamplesInner call({
    List<NormalizedRecordPreview> records,
    List<MappingRejection> rejections,
    int? skipped,
    List<MappingWarning> warnings,
    DateTime receivedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingTestResultSamplesInner.copyWith(...)` or call `instanceOfMappingTestResultSamplesInner.copyWith.fieldName(value)` for a single field.
class _$MappingTestResultSamplesInnerCWProxyImpl
    implements _$MappingTestResultSamplesInnerCWProxy {
  const _$MappingTestResultSamplesInnerCWProxyImpl(this._value);

  final MappingTestResultSamplesInner _value;

  @override
  MappingTestResultSamplesInner records(
    List<NormalizedRecordPreview> records,
  ) => call(records: records);

  @override
  MappingTestResultSamplesInner rejections(List<MappingRejection> rejections) =>
      call(rejections: rejections);

  @override
  MappingTestResultSamplesInner skipped(int? skipped) => call(skipped: skipped);

  @override
  MappingTestResultSamplesInner warnings(List<MappingWarning> warnings) =>
      call(warnings: warnings);

  @override
  MappingTestResultSamplesInner receivedAt(DateTime receivedAt) =>
      call(receivedAt: receivedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingTestResultSamplesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingTestResultSamplesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingTestResultSamplesInner call({
    Object? records = const $CopyWithPlaceholder(),
    Object? rejections = const $CopyWithPlaceholder(),
    Object? skipped = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? receivedAt = const $CopyWithPlaceholder(),
  }) {
    return MappingTestResultSamplesInner(
      records: records == const $CopyWithPlaceholder() || records == null
          ? _value.records
          // ignore: cast_nullable_to_non_nullable
          : records as List<NormalizedRecordPreview>,
      rejections:
          rejections == const $CopyWithPlaceholder() || rejections == null
          ? _value.rejections
          // ignore: cast_nullable_to_non_nullable
          : rejections as List<MappingRejection>,
      skipped: skipped == const $CopyWithPlaceholder()
          ? _value.skipped
          // ignore: cast_nullable_to_non_nullable
          : skipped as int?,
      warnings: warnings == const $CopyWithPlaceholder() || warnings == null
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<MappingWarning>,
      receivedAt:
          receivedAt == const $CopyWithPlaceholder() || receivedAt == null
          ? _value.receivedAt
          // ignore: cast_nullable_to_non_nullable
          : receivedAt as DateTime,
    );
  }
}

extension $MappingTestResultSamplesInnerCopyWith
    on MappingTestResultSamplesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingTestResultSamplesInner.copyWith(...)` or `instanceOfMappingTestResultSamplesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingTestResultSamplesInnerCWProxy get copyWith =>
      _$MappingTestResultSamplesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingTestResultSamplesInner _$MappingTestResultSamplesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MappingTestResultSamplesInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['records', 'rejections', 'warnings', 'receivedAt'],
  );
  final val = MappingTestResultSamplesInner(
    records: $checkedConvert(
      'records',
      (v) => (v as List<dynamic>)
          .map(
            (e) => NormalizedRecordPreview.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    rejections: $checkedConvert(
      'rejections',
      (v) => (v as List<dynamic>)
          .map((e) => MappingRejection.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    skipped: $checkedConvert('skipped', (v) => (v as num?)?.toInt()),
    warnings: $checkedConvert(
      'warnings',
      (v) => (v as List<dynamic>)
          .map((e) => MappingWarning.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    receivedAt: $checkedConvert(
      'receivedAt',
      (v) => DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$MappingTestResultSamplesInnerToJson(
  MappingTestResultSamplesInner instance,
) => <String, dynamic>{
  'records': instance.records.map((e) => e.toJson()).toList(),
  'rejections': instance.rejections.map((e) => e.toJson()).toList(),
  'skipped': ?instance.skipped,
  'warnings': instance.warnings.map((e) => e.toJson()).toList(),
  'receivedAt': instance.receivedAt.toIso8601String(),
};
