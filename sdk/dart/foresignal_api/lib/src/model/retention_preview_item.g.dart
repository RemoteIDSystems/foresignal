// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'retention_preview_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RetentionPreviewItemCWProxy {
  RetentionPreviewItem subsystem(String subsystem);

  RetentionPreviewItem kind(String? kind);

  RetentionPreviewItem cutoff(DateTime? cutoff);

  RetentionPreviewItem affectedCount(int affectedCount);

  RetentionPreviewItem bytesEstimate(int? bytesEstimate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionPreviewItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionPreviewItem(...).copyWith(id: 12, name: "My name")
  /// ```
  RetentionPreviewItem call({
    String subsystem,
    String? kind,
    DateTime? cutoff,
    int affectedCount,
    int? bytesEstimate,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRetentionPreviewItem.copyWith(...)` or call `instanceOfRetentionPreviewItem.copyWith.fieldName(value)` for a single field.
class _$RetentionPreviewItemCWProxyImpl
    implements _$RetentionPreviewItemCWProxy {
  const _$RetentionPreviewItemCWProxyImpl(this._value);

  final RetentionPreviewItem _value;

  @override
  RetentionPreviewItem subsystem(String subsystem) =>
      call(subsystem: subsystem);

  @override
  RetentionPreviewItem kind(String? kind) => call(kind: kind);

  @override
  RetentionPreviewItem cutoff(DateTime? cutoff) => call(cutoff: cutoff);

  @override
  RetentionPreviewItem affectedCount(int affectedCount) =>
      call(affectedCount: affectedCount);

  @override
  RetentionPreviewItem bytesEstimate(int? bytesEstimate) =>
      call(bytesEstimate: bytesEstimate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionPreviewItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionPreviewItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RetentionPreviewItem call({
    Object? subsystem = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? cutoff = const $CopyWithPlaceholder(),
    Object? affectedCount = const $CopyWithPlaceholder(),
    Object? bytesEstimate = const $CopyWithPlaceholder(),
  }) {
    return RetentionPreviewItem(
      subsystem: subsystem == const $CopyWithPlaceholder() || subsystem == null
          ? _value.subsystem
          // ignore: cast_nullable_to_non_nullable
          : subsystem as String,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      cutoff: cutoff == const $CopyWithPlaceholder()
          ? _value.cutoff
          // ignore: cast_nullable_to_non_nullable
          : cutoff as DateTime?,
      affectedCount:
          affectedCount == const $CopyWithPlaceholder() || affectedCount == null
          ? _value.affectedCount
          // ignore: cast_nullable_to_non_nullable
          : affectedCount as int,
      bytesEstimate: bytesEstimate == const $CopyWithPlaceholder()
          ? _value.bytesEstimate
          // ignore: cast_nullable_to_non_nullable
          : bytesEstimate as int?,
    );
  }
}

extension $RetentionPreviewItemCopyWith on RetentionPreviewItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRetentionPreviewItem.copyWith(...)` or `instanceOfRetentionPreviewItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RetentionPreviewItemCWProxy get copyWith =>
      _$RetentionPreviewItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RetentionPreviewItem _$RetentionPreviewItemFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RetentionPreviewItem', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['subsystem', 'affectedCount']);
  final val = RetentionPreviewItem(
    subsystem: $checkedConvert('subsystem', (v) => v as String),
    kind: $checkedConvert('kind', (v) => v as String?),
    cutoff: $checkedConvert(
      'cutoff',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    affectedCount: $checkedConvert('affectedCount', (v) => (v as num).toInt()),
    bytesEstimate: $checkedConvert(
      'bytesEstimate',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$RetentionPreviewItemToJson(
  RetentionPreviewItem instance,
) => <String, dynamic>{
  'subsystem': instance.subsystem,
  'kind': ?instance.kind,
  'cutoff': ?instance.cutoff?.toIso8601String(),
  'affectedCount': instance.affectedCount,
  'bytesEstimate': ?instance.bytesEstimate,
};
