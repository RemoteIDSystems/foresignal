// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'export_file.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExportFileCWProxy {
  ExportFile name(String name);

  ExportFile url(String url);

  ExportFile bytes(int bytes);

  ExportFile sha256(String sha256);

  ExportFile mediaType(String? mediaType);

  ExportFile expiresAt(DateTime expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportFile(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportFile(...).copyWith(id: 12, name: "My name")
  /// ```
  ExportFile call({
    String name,
    String url,
    int bytes,
    String sha256,
    String? mediaType,
    DateTime expiresAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExportFile.copyWith(...)` or call `instanceOfExportFile.copyWith.fieldName(value)` for a single field.
class _$ExportFileCWProxyImpl implements _$ExportFileCWProxy {
  const _$ExportFileCWProxyImpl(this._value);

  final ExportFile _value;

  @override
  ExportFile name(String name) => call(name: name);

  @override
  ExportFile url(String url) => call(url: url);

  @override
  ExportFile bytes(int bytes) => call(bytes: bytes);

  @override
  ExportFile sha256(String sha256) => call(sha256: sha256);

  @override
  ExportFile mediaType(String? mediaType) => call(mediaType: mediaType);

  @override
  ExportFile expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExportFile(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExportFile(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExportFile call({
    Object? name = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? mediaType = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return ExportFile(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      url: url == const $CopyWithPlaceholder() || url == null
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String,
      bytes: bytes == const $CopyWithPlaceholder() || bytes == null
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int,
      sha256: sha256 == const $CopyWithPlaceholder() || sha256 == null
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String,
      mediaType: mediaType == const $CopyWithPlaceholder()
          ? _value.mediaType
          // ignore: cast_nullable_to_non_nullable
          : mediaType as String?,
      expiresAt: expiresAt == const $CopyWithPlaceholder() || expiresAt == null
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime,
    );
  }
}

extension $ExportFileCopyWith on ExportFile {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExportFile.copyWith(...)` or `instanceOfExportFile.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExportFileCWProxy get copyWith => _$ExportFileCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExportFile _$ExportFileFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExportFile', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['name', 'url', 'bytes', 'sha256', 'expiresAt'],
      );
      final val = ExportFile(
        name: $checkedConvert('name', (v) => v as String),
        url: $checkedConvert('url', (v) => v as String),
        bytes: $checkedConvert('bytes', (v) => (v as num).toInt()),
        sha256: $checkedConvert('sha256', (v) => v as String),
        mediaType: $checkedConvert('mediaType', (v) => v as String?),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ExportFileToJson(ExportFile instance) =>
    <String, dynamic>{
      'name': instance.name,
      'url': instance.url,
      'bytes': instance.bytes,
      'sha256': instance.sha256,
      'mediaType': ?instance.mediaType,
      'expiresAt': instance.expiresAt.toIso8601String(),
    };
