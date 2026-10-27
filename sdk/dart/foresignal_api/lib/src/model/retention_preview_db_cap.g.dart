// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'retention_preview_db_cap.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RetentionPreviewDbCapCWProxy {
  RetentionPreviewDbCap capBytes(int? capBytes);

  RetentionPreviewDbCap sizeBytes(int? sizeBytes);

  RetentionPreviewDbCap exceeded(bool? exceeded);

  RetentionPreviewDbCap windowDays(int? windowDays);

  RetentionPreviewDbCap overwriteOldest(bool? overwriteOldest);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionPreviewDbCap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionPreviewDbCap(...).copyWith(id: 12, name: "My name")
  /// ```
  RetentionPreviewDbCap call({
    int? capBytes,
    int? sizeBytes,
    bool? exceeded,
    int? windowDays,
    bool? overwriteOldest,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRetentionPreviewDbCap.copyWith(...)` or call `instanceOfRetentionPreviewDbCap.copyWith.fieldName(value)` for a single field.
class _$RetentionPreviewDbCapCWProxyImpl
    implements _$RetentionPreviewDbCapCWProxy {
  const _$RetentionPreviewDbCapCWProxyImpl(this._value);

  final RetentionPreviewDbCap _value;

  @override
  RetentionPreviewDbCap capBytes(int? capBytes) => call(capBytes: capBytes);

  @override
  RetentionPreviewDbCap sizeBytes(int? sizeBytes) => call(sizeBytes: sizeBytes);

  @override
  RetentionPreviewDbCap exceeded(bool? exceeded) => call(exceeded: exceeded);

  @override
  RetentionPreviewDbCap windowDays(int? windowDays) =>
      call(windowDays: windowDays);

  @override
  RetentionPreviewDbCap overwriteOldest(bool? overwriteOldest) =>
      call(overwriteOldest: overwriteOldest);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionPreviewDbCap(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionPreviewDbCap(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RetentionPreviewDbCap call({
    Object? capBytes = const $CopyWithPlaceholder(),
    Object? sizeBytes = const $CopyWithPlaceholder(),
    Object? exceeded = const $CopyWithPlaceholder(),
    Object? windowDays = const $CopyWithPlaceholder(),
    Object? overwriteOldest = const $CopyWithPlaceholder(),
  }) {
    return RetentionPreviewDbCap(
      capBytes: capBytes == const $CopyWithPlaceholder()
          ? _value.capBytes
          // ignore: cast_nullable_to_non_nullable
          : capBytes as int?,
      sizeBytes: sizeBytes == const $CopyWithPlaceholder()
          ? _value.sizeBytes
          // ignore: cast_nullable_to_non_nullable
          : sizeBytes as int?,
      exceeded: exceeded == const $CopyWithPlaceholder()
          ? _value.exceeded
          // ignore: cast_nullable_to_non_nullable
          : exceeded as bool?,
      windowDays: windowDays == const $CopyWithPlaceholder()
          ? _value.windowDays
          // ignore: cast_nullable_to_non_nullable
          : windowDays as int?,
      overwriteOldest: overwriteOldest == const $CopyWithPlaceholder()
          ? _value.overwriteOldest
          // ignore: cast_nullable_to_non_nullable
          : overwriteOldest as bool?,
    );
  }
}

extension $RetentionPreviewDbCapCopyWith on RetentionPreviewDbCap {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRetentionPreviewDbCap.copyWith(...)` or `instanceOfRetentionPreviewDbCap.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RetentionPreviewDbCapCWProxy get copyWith =>
      _$RetentionPreviewDbCapCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RetentionPreviewDbCap _$RetentionPreviewDbCapFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RetentionPreviewDbCap', json, ($checkedConvert) {
  final val = RetentionPreviewDbCap(
    capBytes: $checkedConvert('capBytes', (v) => (v as num?)?.toInt()),
    sizeBytes: $checkedConvert('sizeBytes', (v) => (v as num?)?.toInt()),
    exceeded: $checkedConvert('exceeded', (v) => v as bool?),
    windowDays: $checkedConvert('windowDays', (v) => (v as num?)?.toInt()),
    overwriteOldest: $checkedConvert('overwriteOldest', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$RetentionPreviewDbCapToJson(
  RetentionPreviewDbCap instance,
) => <String, dynamic>{
  'capBytes': ?instance.capBytes,
  'sizeBytes': ?instance.sizeBytes,
  'exceeded': ?instance.exceeded,
  'windowDays': ?instance.windowDays,
  'overwriteOldest': ?instance.overwriteOldest,
};
