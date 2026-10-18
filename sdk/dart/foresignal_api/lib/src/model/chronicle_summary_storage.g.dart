// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_summary_storage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleSummaryStorageCWProxy {
  ChronicleSummaryStorage bytesUsed(int bytesUsed);

  ChronicleSummaryStorage sizeCapBytes(int sizeCapBytes);

  ChronicleSummaryStorage rows(int? rows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSummaryStorage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSummaryStorage(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleSummaryStorage call({int bytesUsed, int sizeCapBytes, int? rows});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleSummaryStorage.copyWith(...)` or call `instanceOfChronicleSummaryStorage.copyWith.fieldName(value)` for a single field.
class _$ChronicleSummaryStorageCWProxyImpl
    implements _$ChronicleSummaryStorageCWProxy {
  const _$ChronicleSummaryStorageCWProxyImpl(this._value);

  final ChronicleSummaryStorage _value;

  @override
  ChronicleSummaryStorage bytesUsed(int bytesUsed) =>
      call(bytesUsed: bytesUsed);

  @override
  ChronicleSummaryStorage sizeCapBytes(int sizeCapBytes) =>
      call(sizeCapBytes: sizeCapBytes);

  @override
  ChronicleSummaryStorage rows(int? rows) => call(rows: rows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleSummaryStorage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleSummaryStorage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleSummaryStorage call({
    Object? bytesUsed = const $CopyWithPlaceholder(),
    Object? sizeCapBytes = const $CopyWithPlaceholder(),
    Object? rows = const $CopyWithPlaceholder(),
  }) {
    return ChronicleSummaryStorage(
      bytesUsed: bytesUsed == const $CopyWithPlaceholder() || bytesUsed == null
          ? _value.bytesUsed
          // ignore: cast_nullable_to_non_nullable
          : bytesUsed as int,
      sizeCapBytes:
          sizeCapBytes == const $CopyWithPlaceholder() || sizeCapBytes == null
          ? _value.sizeCapBytes
          // ignore: cast_nullable_to_non_nullable
          : sizeCapBytes as int,
      rows: rows == const $CopyWithPlaceholder()
          ? _value.rows
          // ignore: cast_nullable_to_non_nullable
          : rows as int?,
    );
  }
}

extension $ChronicleSummaryStorageCopyWith on ChronicleSummaryStorage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleSummaryStorage.copyWith(...)` or `instanceOfChronicleSummaryStorage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleSummaryStorageCWProxy get copyWith =>
      _$ChronicleSummaryStorageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleSummaryStorage _$ChronicleSummaryStorageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleSummaryStorage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['bytesUsed', 'sizeCapBytes']);
  final val = ChronicleSummaryStorage(
    bytesUsed: $checkedConvert('bytesUsed', (v) => (v as num).toInt()),
    sizeCapBytes: $checkedConvert('sizeCapBytes', (v) => (v as num).toInt()),
    rows: $checkedConvert('rows', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$ChronicleSummaryStorageToJson(
  ChronicleSummaryStorage instance,
) => <String, dynamic>{
  'bytesUsed': instance.bytesUsed,
  'sizeCapBytes': instance.sizeCapBytes,
  'rows': ?instance.rows,
};
