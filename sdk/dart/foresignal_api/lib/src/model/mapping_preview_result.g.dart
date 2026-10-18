// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_preview_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingPreviewResultCWProxy {
  MappingPreviewResult records(List<NormalizedRecordPreview> records);

  MappingPreviewResult rejections(List<MappingRejection> rejections);

  MappingPreviewResult skipped(int? skipped);

  MappingPreviewResult warnings(List<MappingWarning> warnings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingPreviewResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingPreviewResult(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingPreviewResult call({
    List<NormalizedRecordPreview> records,
    List<MappingRejection> rejections,
    int? skipped,
    List<MappingWarning> warnings,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingPreviewResult.copyWith(...)` or call `instanceOfMappingPreviewResult.copyWith.fieldName(value)` for a single field.
class _$MappingPreviewResultCWProxyImpl
    implements _$MappingPreviewResultCWProxy {
  const _$MappingPreviewResultCWProxyImpl(this._value);

  final MappingPreviewResult _value;

  @override
  MappingPreviewResult records(List<NormalizedRecordPreview> records) =>
      call(records: records);

  @override
  MappingPreviewResult rejections(List<MappingRejection> rejections) =>
      call(rejections: rejections);

  @override
  MappingPreviewResult skipped(int? skipped) => call(skipped: skipped);

  @override
  MappingPreviewResult warnings(List<MappingWarning> warnings) =>
      call(warnings: warnings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingPreviewResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingPreviewResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingPreviewResult call({
    Object? records = const $CopyWithPlaceholder(),
    Object? rejections = const $CopyWithPlaceholder(),
    Object? skipped = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
  }) {
    return MappingPreviewResult(
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
    );
  }
}

extension $MappingPreviewResultCopyWith on MappingPreviewResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingPreviewResult.copyWith(...)` or `instanceOfMappingPreviewResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingPreviewResultCWProxy get copyWith =>
      _$MappingPreviewResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingPreviewResult _$MappingPreviewResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MappingPreviewResult', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['records', 'rejections', 'warnings']);
  final val = MappingPreviewResult(
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
  );
  return val;
});

Map<String, dynamic> _$MappingPreviewResultToJson(
  MappingPreviewResult instance,
) => <String, dynamic>{
  'records': instance.records.map((e) => e.toJson()).toList(),
  'rejections': instance.rejections.map((e) => e.toJson()).toList(),
  'skipped': ?instance.skipped,
  'warnings': instance.warnings.map((e) => e.toJson()).toList(),
};
