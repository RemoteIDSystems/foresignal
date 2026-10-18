// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'archive_partition_files_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ArchivePartitionFilesInnerCWProxy {
  ArchivePartitionFilesInner name(String name);

  ArchivePartitionFilesInner fromSeq(int fromSeq);

  ArchivePartitionFilesInner toSeq(int toSeq);

  ArchivePartitionFilesInner rows(int rows);

  ArchivePartitionFilesInner bytes(int bytes);

  ArchivePartitionFilesInner sha256(String sha256);

  ArchivePartitionFilesInner local(bool? local);

  ArchivePartitionFilesInner uploadedAt(DateTime? uploadedAt);

  ArchivePartitionFilesInner s3Key(String? s3Key);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchivePartitionFilesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchivePartitionFilesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ArchivePartitionFilesInner call({
    String name,
    int fromSeq,
    int toSeq,
    int rows,
    int bytes,
    String sha256,
    bool? local,
    DateTime? uploadedAt,
    String? s3Key,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfArchivePartitionFilesInner.copyWith(...)` or call `instanceOfArchivePartitionFilesInner.copyWith.fieldName(value)` for a single field.
class _$ArchivePartitionFilesInnerCWProxyImpl
    implements _$ArchivePartitionFilesInnerCWProxy {
  const _$ArchivePartitionFilesInnerCWProxyImpl(this._value);

  final ArchivePartitionFilesInner _value;

  @override
  ArchivePartitionFilesInner name(String name) => call(name: name);

  @override
  ArchivePartitionFilesInner fromSeq(int fromSeq) => call(fromSeq: fromSeq);

  @override
  ArchivePartitionFilesInner toSeq(int toSeq) => call(toSeq: toSeq);

  @override
  ArchivePartitionFilesInner rows(int rows) => call(rows: rows);

  @override
  ArchivePartitionFilesInner bytes(int bytes) => call(bytes: bytes);

  @override
  ArchivePartitionFilesInner sha256(String sha256) => call(sha256: sha256);

  @override
  ArchivePartitionFilesInner local(bool? local) => call(local: local);

  @override
  ArchivePartitionFilesInner uploadedAt(DateTime? uploadedAt) =>
      call(uploadedAt: uploadedAt);

  @override
  ArchivePartitionFilesInner s3Key(String? s3Key) => call(s3Key: s3Key);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchivePartitionFilesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchivePartitionFilesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ArchivePartitionFilesInner call({
    Object? name = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? rows = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? local = const $CopyWithPlaceholder(),
    Object? uploadedAt = const $CopyWithPlaceholder(),
    Object? s3Key = const $CopyWithPlaceholder(),
  }) {
    return ArchivePartitionFilesInner(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      fromSeq: fromSeq == const $CopyWithPlaceholder() || fromSeq == null
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int,
      toSeq: toSeq == const $CopyWithPlaceholder() || toSeq == null
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int,
      rows: rows == const $CopyWithPlaceholder() || rows == null
          ? _value.rows
          // ignore: cast_nullable_to_non_nullable
          : rows as int,
      bytes: bytes == const $CopyWithPlaceholder() || bytes == null
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int,
      sha256: sha256 == const $CopyWithPlaceholder() || sha256 == null
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String,
      local: local == const $CopyWithPlaceholder()
          ? _value.local
          // ignore: cast_nullable_to_non_nullable
          : local as bool?,
      uploadedAt: uploadedAt == const $CopyWithPlaceholder()
          ? _value.uploadedAt
          // ignore: cast_nullable_to_non_nullable
          : uploadedAt as DateTime?,
      s3Key: s3Key == const $CopyWithPlaceholder()
          ? _value.s3Key
          // ignore: cast_nullable_to_non_nullable
          : s3Key as String?,
    );
  }
}

extension $ArchivePartitionFilesInnerCopyWith on ArchivePartitionFilesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfArchivePartitionFilesInner.copyWith(...)` or `instanceOfArchivePartitionFilesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ArchivePartitionFilesInnerCWProxy get copyWith =>
      _$ArchivePartitionFilesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ArchivePartitionFilesInner _$ArchivePartitionFilesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ArchivePartitionFilesInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['name', 'fromSeq', 'toSeq', 'rows', 'bytes', 'sha256'],
  );
  final val = ArchivePartitionFilesInner(
    name: $checkedConvert('name', (v) => v as String),
    fromSeq: $checkedConvert('fromSeq', (v) => (v as num).toInt()),
    toSeq: $checkedConvert('toSeq', (v) => (v as num).toInt()),
    rows: $checkedConvert('rows', (v) => (v as num).toInt()),
    bytes: $checkedConvert('bytes', (v) => (v as num).toInt()),
    sha256: $checkedConvert('sha256', (v) => v as String),
    local: $checkedConvert('local', (v) => v as bool?),
    uploadedAt: $checkedConvert(
      'uploadedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    s3Key: $checkedConvert('s3Key', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ArchivePartitionFilesInnerToJson(
  ArchivePartitionFilesInner instance,
) => <String, dynamic>{
  'name': instance.name,
  'fromSeq': instance.fromSeq,
  'toSeq': instance.toSeq,
  'rows': instance.rows,
  'bytes': instance.bytes,
  'sha256': instance.sha256,
  'local': ?instance.local,
  'uploadedAt': ?instance.uploadedAt?.toIso8601String(),
  's3Key': ?instance.s3Key,
};
