// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'export_manifest_files_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExportManifestFilesInnerCWProxy {
  ExportManifestFilesInner name(String name);

  ExportManifestFilesInner mediaType(String? mediaType);

  ExportManifestFilesInner bytes(int bytes);

  ExportManifestFilesInner sha256(String sha256);

  ExportManifestFilesInner rows(int? rows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportManifestFilesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportManifestFilesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ExportManifestFilesInner call({
    String name,
    String? mediaType,
    int bytes,
    String sha256,
    int? rows,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExportManifestFilesInner.copyWith(...)` or call `instanceOfExportManifestFilesInner.copyWith.fieldName(value)` for a single field.
class _$ExportManifestFilesInnerCWProxyImpl
    implements _$ExportManifestFilesInnerCWProxy {
  const _$ExportManifestFilesInnerCWProxyImpl(this._value);

  final ExportManifestFilesInner _value;

  @override
  ExportManifestFilesInner name(String name) => call(name: name);

  @override
  ExportManifestFilesInner mediaType(String? mediaType) =>
      call(mediaType: mediaType);

  @override
  ExportManifestFilesInner bytes(int bytes) => call(bytes: bytes);

  @override
  ExportManifestFilesInner sha256(String sha256) => call(sha256: sha256);

  @override
  ExportManifestFilesInner rows(int? rows) => call(rows: rows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportManifestFilesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportManifestFilesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExportManifestFilesInner call({
    Object? name = const $CopyWithPlaceholder(),
    Object? mediaType = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? rows = const $CopyWithPlaceholder(),
  }) {
    return ExportManifestFilesInner(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      mediaType: mediaType == const $CopyWithPlaceholder()
          ? _value.mediaType
          // ignore: cast_nullable_to_non_nullable
          : mediaType as String?,
      bytes: bytes == const $CopyWithPlaceholder() || bytes == null
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int,
      sha256: sha256 == const $CopyWithPlaceholder() || sha256 == null
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String,
      rows: rows == const $CopyWithPlaceholder()
          ? _value.rows
          // ignore: cast_nullable_to_non_nullable
          : rows as int?,
    );
  }
}

extension $ExportManifestFilesInnerCopyWith on ExportManifestFilesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExportManifestFilesInner.copyWith(...)` or `instanceOfExportManifestFilesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExportManifestFilesInnerCWProxy get copyWith =>
      _$ExportManifestFilesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExportManifestFilesInner _$ExportManifestFilesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExportManifestFilesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name', 'bytes', 'sha256']);
  final val = ExportManifestFilesInner(
    name: $checkedConvert('name', (v) => v as String),
    mediaType: $checkedConvert('mediaType', (v) => v as String?),
    bytes: $checkedConvert('bytes', (v) => (v as num).toInt()),
    sha256: $checkedConvert('sha256', (v) => v as String),
    rows: $checkedConvert('rows', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$ExportManifestFilesInnerToJson(
  ExportManifestFilesInner instance,
) => <String, dynamic>{
  'name': instance.name,
  'mediaType': ?instance.mediaType,
  'bytes': instance.bytes,
  'sha256': instance.sha256,
  'rows': ?instance.rows,
};
