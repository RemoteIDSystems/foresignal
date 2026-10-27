// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backup.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BackupCWProxy {
  Backup id(String id);

  Backup type(String type);

  Backup status(String status);

  Backup createdAt(DateTime createdAt);

  Backup sizeBytes(int? sizeBytes);

  Backup target(String? target);

  Backup location(String? location);

  Backup checksum(String? checksum);

  Backup label(String? label);

  Backup trigger(String? trigger);

  Backup files(List<BackupFilesInner>? files);

  Backup error(String? error);

  Backup startedAt(DateTime? startedAt);

  Backup finishedAt(DateTime? finishedAt);

  Backup restoredAt(DateTime? restoredAt);

  Backup importReport(ImportReport? importReport);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Backup(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Backup(...).copyWith(id: 12, name: "My name")
  /// ```
  Backup call({
    String id,
    String type,
    String status,
    DateTime createdAt,
    int? sizeBytes,
    String? target,
    String? location,
    String? checksum,
    String? label,
    String? trigger,
    List<BackupFilesInner>? files,
    String? error,
    DateTime? startedAt,
    DateTime? finishedAt,
    DateTime? restoredAt,
    ImportReport? importReport,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBackup.copyWith(...)` or call `instanceOfBackup.copyWith.fieldName(value)` for a single field.
class _$BackupCWProxyImpl implements _$BackupCWProxy {
  const _$BackupCWProxyImpl(this._value);

  final Backup _value;

  @override
  Backup id(String id) => call(id: id);

  @override
  Backup type(String type) => call(type: type);

  @override
  Backup status(String status) => call(status: status);

  @override
  Backup createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Backup sizeBytes(int? sizeBytes) => call(sizeBytes: sizeBytes);

  @override
  Backup target(String? target) => call(target: target);

  @override
  Backup location(String? location) => call(location: location);

  @override
  Backup checksum(String? checksum) => call(checksum: checksum);

  @override
  Backup label(String? label) => call(label: label);

  @override
  Backup trigger(String? trigger) => call(trigger: trigger);

  @override
  Backup files(List<BackupFilesInner>? files) => call(files: files);

  @override
  Backup error(String? error) => call(error: error);

  @override
  Backup startedAt(DateTime? startedAt) => call(startedAt: startedAt);

  @override
  Backup finishedAt(DateTime? finishedAt) => call(finishedAt: finishedAt);

  @override
  Backup restoredAt(DateTime? restoredAt) => call(restoredAt: restoredAt);

  @override
  Backup importReport(ImportReport? importReport) =>
      call(importReport: importReport);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Backup(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Backup(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Backup call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? sizeBytes = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? location = const $CopyWithPlaceholder(),
    Object? checksum = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? trigger = const $CopyWithPlaceholder(),
    Object? files = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? restoredAt = const $CopyWithPlaceholder(),
    Object? importReport = const $CopyWithPlaceholder(),
  }) {
    return Backup(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      sizeBytes: sizeBytes == const $CopyWithPlaceholder()
          ? _value.sizeBytes
          // ignore: cast_nullable_to_non_nullable
          : sizeBytes as int?,
      target: target == const $CopyWithPlaceholder()
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as String?,
      location: location == const $CopyWithPlaceholder()
          ? _value.location
          // ignore: cast_nullable_to_non_nullable
          : location as String?,
      checksum: checksum == const $CopyWithPlaceholder()
          ? _value.checksum
          // ignore: cast_nullable_to_non_nullable
          : checksum as String?,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      trigger: trigger == const $CopyWithPlaceholder()
          ? _value.trigger
          // ignore: cast_nullable_to_non_nullable
          : trigger as String?,
      files: files == const $CopyWithPlaceholder()
          ? _value.files
          // ignore: cast_nullable_to_non_nullable
          : files as List<BackupFilesInner>?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      restoredAt: restoredAt == const $CopyWithPlaceholder()
          ? _value.restoredAt
          // ignore: cast_nullable_to_non_nullable
          : restoredAt as DateTime?,
      importReport: importReport == const $CopyWithPlaceholder()
          ? _value.importReport
          // ignore: cast_nullable_to_non_nullable
          : importReport as ImportReport?,
    );
  }
}

extension $BackupCopyWith on Backup {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBackup.copyWith(...)` or `instanceOfBackup.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BackupCWProxy get copyWith => _$BackupCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Backup _$BackupFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Backup', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'type', 'status', 'createdAt']);
  final val = Backup(
    id: $checkedConvert('id', (v) => v as String),
    type: $checkedConvert('type', (v) => v as String),
    status: $checkedConvert('status', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    sizeBytes: $checkedConvert('sizeBytes', (v) => (v as num?)?.toInt()),
    target: $checkedConvert('target', (v) => v as String?),
    location: $checkedConvert('location', (v) => v as String?),
    checksum: $checkedConvert('checksum', (v) => v as String?),
    label: $checkedConvert('label', (v) => v as String?),
    trigger: $checkedConvert('trigger', (v) => v as String?),
    files: $checkedConvert(
      'files',
      (v) => (v as List<dynamic>?)
          ?.map((e) => BackupFilesInner.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    error: $checkedConvert('error', (v) => v as String?),
    startedAt: $checkedConvert(
      'startedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    restoredAt: $checkedConvert(
      'restoredAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    importReport: $checkedConvert(
      'importReport',
      (v) =>
          v == null ? null : ImportReport.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$BackupToJson(Backup instance) => <String, dynamic>{
  'id': instance.id,
  'type': instance.type,
  'status': instance.status,
  'createdAt': instance.createdAt.toIso8601String(),
  'sizeBytes': ?instance.sizeBytes,
  'target': ?instance.target,
  'location': ?instance.location,
  'checksum': ?instance.checksum,
  'label': ?instance.label,
  'trigger': ?instance.trigger,
  'files': ?instance.files?.map((e) => e.toJson()).toList(),
  'error': ?instance.error,
  'startedAt': ?instance.startedAt?.toIso8601String(),
  'finishedAt': ?instance.finishedAt?.toIso8601String(),
  'restoredAt': ?instance.restoredAt?.toIso8601String(),
  'importReport': ?instance.importReport?.toJson(),
};
