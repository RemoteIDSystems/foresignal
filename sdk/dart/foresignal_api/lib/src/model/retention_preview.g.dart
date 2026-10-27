// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'retention_preview.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RetentionPreviewCWProxy {
  RetentionPreview items(List<RetentionPreviewItem> items);

  RetentionPreview totalAffected(int totalAffected);

  RetentionPreview totalBytesEstimate(int? totalBytesEstimate);

  RetentionPreview generatedAt(DateTime generatedAt);

  RetentionPreview dbCap(RetentionPreviewDbCap? dbCap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionPreview(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionPreview(...).copyWith(id: 12, name: "My name")
  /// ```
  RetentionPreview call({
    List<RetentionPreviewItem> items,
    int totalAffected,
    int? totalBytesEstimate,
    DateTime generatedAt,
    RetentionPreviewDbCap? dbCap,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRetentionPreview.copyWith(...)` or call `instanceOfRetentionPreview.copyWith.fieldName(value)` for a single field.
class _$RetentionPreviewCWProxyImpl implements _$RetentionPreviewCWProxy {
  const _$RetentionPreviewCWProxyImpl(this._value);

  final RetentionPreview _value;

  @override
  RetentionPreview items(List<RetentionPreviewItem> items) =>
      call(items: items);

  @override
  RetentionPreview totalAffected(int totalAffected) =>
      call(totalAffected: totalAffected);

  @override
  RetentionPreview totalBytesEstimate(int? totalBytesEstimate) =>
      call(totalBytesEstimate: totalBytesEstimate);

  @override
  RetentionPreview generatedAt(DateTime generatedAt) =>
      call(generatedAt: generatedAt);

  @override
  RetentionPreview dbCap(RetentionPreviewDbCap? dbCap) => call(dbCap: dbCap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionPreview(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionPreview(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RetentionPreview call({
    Object? items = const $CopyWithPlaceholder(),
    Object? totalAffected = const $CopyWithPlaceholder(),
    Object? totalBytesEstimate = const $CopyWithPlaceholder(),
    Object? generatedAt = const $CopyWithPlaceholder(),
    Object? dbCap = const $CopyWithPlaceholder(),
  }) {
    return RetentionPreview(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<RetentionPreviewItem>,
      totalAffected:
          totalAffected == const $CopyWithPlaceholder() || totalAffected == null
          ? _value.totalAffected
          // ignore: cast_nullable_to_non_nullable
          : totalAffected as int,
      totalBytesEstimate: totalBytesEstimate == const $CopyWithPlaceholder()
          ? _value.totalBytesEstimate
          // ignore: cast_nullable_to_non_nullable
          : totalBytesEstimate as int?,
      generatedAt:
          generatedAt == const $CopyWithPlaceholder() || generatedAt == null
          ? _value.generatedAt
          // ignore: cast_nullable_to_non_nullable
          : generatedAt as DateTime,
      dbCap: dbCap == const $CopyWithPlaceholder()
          ? _value.dbCap
          // ignore: cast_nullable_to_non_nullable
          : dbCap as RetentionPreviewDbCap?,
    );
  }
}

extension $RetentionPreviewCopyWith on RetentionPreview {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRetentionPreview.copyWith(...)` or `instanceOfRetentionPreview.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RetentionPreviewCWProxy get copyWith => _$RetentionPreviewCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RetentionPreview _$RetentionPreviewFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RetentionPreview', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['items', 'totalAffected', 'generatedAt'],
  );
  final val = RetentionPreview(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => RetentionPreviewItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    totalAffected: $checkedConvert('totalAffected', (v) => (v as num).toInt()),
    totalBytesEstimate: $checkedConvert(
      'totalBytesEstimate',
      (v) => (v as num?)?.toInt(),
    ),
    generatedAt: $checkedConvert(
      'generatedAt',
      (v) => DateTime.parse(v as String),
    ),
    dbCap: $checkedConvert(
      'dbCap',
      (v) => v == null
          ? null
          : RetentionPreviewDbCap.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$RetentionPreviewToJson(RetentionPreview instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'totalAffected': instance.totalAffected,
      'totalBytesEstimate': ?instance.totalBytesEstimate,
      'generatedAt': instance.generatedAt.toIso8601String(),
      'dbCap': ?instance.dbCap?.toJson(),
    };
