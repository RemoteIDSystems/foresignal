// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backup_files_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BackupFilesInnerCWProxy {
  BackupFilesInner name(String name);

  BackupFilesInner location(String location);

  BackupFilesInner bytes(int bytes);

  BackupFilesInner sha256(String sha256);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackupFilesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackupFilesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  BackupFilesInner call({
    String name,
    String location,
    int bytes,
    String sha256,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBackupFilesInner.copyWith(...)` or call `instanceOfBackupFilesInner.copyWith.fieldName(value)` for a single field.
class _$BackupFilesInnerCWProxyImpl implements _$BackupFilesInnerCWProxy {
  const _$BackupFilesInnerCWProxyImpl(this._value);

  final BackupFilesInner _value;

  @override
  BackupFilesInner name(String name) => call(name: name);

  @override
  BackupFilesInner location(String location) => call(location: location);

  @override
  BackupFilesInner bytes(int bytes) => call(bytes: bytes);

  @override
  BackupFilesInner sha256(String sha256) => call(sha256: sha256);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BackupFilesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BackupFilesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BackupFilesInner call({
    Object? name = const $CopyWithPlaceholder(),
    Object? location = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
  }) {
    return BackupFilesInner(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      location: location == const $CopyWithPlaceholder() || location == null
          ? _value.location
          // ignore: cast_nullable_to_non_nullable
          : location as String,
      bytes: bytes == const $CopyWithPlaceholder() || bytes == null
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int,
      sha256: sha256 == const $CopyWithPlaceholder() || sha256 == null
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String,
    );
  }
}

extension $BackupFilesInnerCopyWith on BackupFilesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBackupFilesInner.copyWith(...)` or `instanceOfBackupFilesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BackupFilesInnerCWProxy get copyWith => _$BackupFilesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BackupFilesInner _$BackupFilesInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BackupFilesInner', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['name', 'location', 'bytes', 'sha256'],
      );
      final val = BackupFilesInner(
        name: $checkedConvert('name', (v) => v as String),
        location: $checkedConvert('location', (v) => v as String),
        bytes: $checkedConvert('bytes', (v) => (v as num).toInt()),
        sha256: $checkedConvert('sha256', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$BackupFilesInnerToJson(BackupFilesInner instance) =>
    <String, dynamic>{
      'name': instance.name,
      'location': instance.location,
      'bytes': instance.bytes,
      'sha256': instance.sha256,
    };
