// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'archive.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ArchiveCWProxy {
  Archive id(String id);

  Archive status(String status);

  Archive createdAt(DateTime createdAt);

  Archive cutoff(DateTime? cutoff);

  Archive target(String target);

  Archive location(String? location);

  Archive sizeBytes(int? sizeBytes);

  Archive itemCount(int? itemCount);

  Archive checksum(String? checksum);

  Archive purgedFromHot(bool? purgedFromHot);

  Archive restoredAt(DateTime? restoredAt);

  Archive trigger(String? trigger);

  Archive domain(String? domain);

  Archive sources(List<String>? sources);

  Archive fromSeq(int? fromSeq);

  Archive toSeq(int? toSeq);

  Archive fromTime(DateTime? fromTime);

  Archive toTime(DateTime? toTime);

  Archive files(List<ArchiveFilesInner>? files);

  Archive error(String? error);

  Archive updatedAt(DateTime? updatedAt);

  Archive completedAt(DateTime? completedAt);

  Archive purgedAt(DateTime? purgedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Archive(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Archive(...).copyWith(id: 12, name: "My name")
  /// ```
  Archive call({
    String id,
    String status,
    DateTime createdAt,
    DateTime? cutoff,
    String target,
    String? location,
    int? sizeBytes,
    int? itemCount,
    String? checksum,
    bool? purgedFromHot,
    DateTime? restoredAt,
    String? trigger,
    String? domain,
    List<String>? sources,
    int? fromSeq,
    int? toSeq,
    DateTime? fromTime,
    DateTime? toTime,
    List<ArchiveFilesInner>? files,
    String? error,
    DateTime? updatedAt,
    DateTime? completedAt,
    DateTime? purgedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfArchive.copyWith(...)` or call `instanceOfArchive.copyWith.fieldName(value)` for a single field.
class _$ArchiveCWProxyImpl implements _$ArchiveCWProxy {
  const _$ArchiveCWProxyImpl(this._value);

  final Archive _value;

  @override
  Archive id(String id) => call(id: id);

  @override
  Archive status(String status) => call(status: status);

  @override
  Archive createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Archive cutoff(DateTime? cutoff) => call(cutoff: cutoff);

  @override
  Archive target(String target) => call(target: target);

  @override
  Archive location(String? location) => call(location: location);

  @override
  Archive sizeBytes(int? sizeBytes) => call(sizeBytes: sizeBytes);

  @override
  Archive itemCount(int? itemCount) => call(itemCount: itemCount);

  @override
  Archive checksum(String? checksum) => call(checksum: checksum);

  @override
  Archive purgedFromHot(bool? purgedFromHot) =>
      call(purgedFromHot: purgedFromHot);

  @override
  Archive restoredAt(DateTime? restoredAt) => call(restoredAt: restoredAt);

  @override
  Archive trigger(String? trigger) => call(trigger: trigger);

  @override
  Archive domain(String? domain) => call(domain: domain);

  @override
  Archive sources(List<String>? sources) => call(sources: sources);

  @override
  Archive fromSeq(int? fromSeq) => call(fromSeq: fromSeq);

  @override
  Archive toSeq(int? toSeq) => call(toSeq: toSeq);

  @override
  Archive fromTime(DateTime? fromTime) => call(fromTime: fromTime);

  @override
  Archive toTime(DateTime? toTime) => call(toTime: toTime);

  @override
  Archive files(List<ArchiveFilesInner>? files) => call(files: files);

  @override
  Archive error(String? error) => call(error: error);

  @override
  Archive updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  @override
  Archive completedAt(DateTime? completedAt) => call(completedAt: completedAt);

  @override
  Archive purgedAt(DateTime? purgedAt) => call(purgedAt: purgedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Archive(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Archive(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Archive call({
    Object? id = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? cutoff = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? location = const $CopyWithPlaceholder(),
    Object? sizeBytes = const $CopyWithPlaceholder(),
    Object? itemCount = const $CopyWithPlaceholder(),
    Object? checksum = const $CopyWithPlaceholder(),
    Object? purgedFromHot = const $CopyWithPlaceholder(),
    Object? restoredAt = const $CopyWithPlaceholder(),
    Object? trigger = const $CopyWithPlaceholder(),
    Object? domain = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? fromTime = const $CopyWithPlaceholder(),
    Object? toTime = const $CopyWithPlaceholder(),
    Object? files = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? completedAt = const $CopyWithPlaceholder(),
    Object? purgedAt = const $CopyWithPlaceholder(),
  }) {
    return Archive(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      cutoff: cutoff == const $CopyWithPlaceholder()
          ? _value.cutoff
          // ignore: cast_nullable_to_non_nullable
          : cutoff as DateTime?,
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as String,
      location: location == const $CopyWithPlaceholder()
          ? _value.location
          // ignore: cast_nullable_to_non_nullable
          : location as String?,
      sizeBytes: sizeBytes == const $CopyWithPlaceholder()
          ? _value.sizeBytes
          // ignore: cast_nullable_to_non_nullable
          : sizeBytes as int?,
      itemCount: itemCount == const $CopyWithPlaceholder()
          ? _value.itemCount
          // ignore: cast_nullable_to_non_nullable
          : itemCount as int?,
      checksum: checksum == const $CopyWithPlaceholder()
          ? _value.checksum
          // ignore: cast_nullable_to_non_nullable
          : checksum as String?,
      purgedFromHot: purgedFromHot == const $CopyWithPlaceholder()
          ? _value.purgedFromHot
          // ignore: cast_nullable_to_non_nullable
          : purgedFromHot as bool?,
      restoredAt: restoredAt == const $CopyWithPlaceholder()
          ? _value.restoredAt
          // ignore: cast_nullable_to_non_nullable
          : restoredAt as DateTime?,
      trigger: trigger == const $CopyWithPlaceholder()
          ? _value.trigger
          // ignore: cast_nullable_to_non_nullable
          : trigger as String?,
      domain: domain == const $CopyWithPlaceholder()
          ? _value.domain
          // ignore: cast_nullable_to_non_nullable
          : domain as String?,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      fromSeq: fromSeq == const $CopyWithPlaceholder()
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int?,
      toSeq: toSeq == const $CopyWithPlaceholder()
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int?,
      fromTime: fromTime == const $CopyWithPlaceholder()
          ? _value.fromTime
          // ignore: cast_nullable_to_non_nullable
          : fromTime as DateTime?,
      toTime: toTime == const $CopyWithPlaceholder()
          ? _value.toTime
          // ignore: cast_nullable_to_non_nullable
          : toTime as DateTime?,
      files: files == const $CopyWithPlaceholder()
          ? _value.files
          // ignore: cast_nullable_to_non_nullable
          : files as List<ArchiveFilesInner>?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      completedAt: completedAt == const $CopyWithPlaceholder()
          ? _value.completedAt
          // ignore: cast_nullable_to_non_nullable
          : completedAt as DateTime?,
      purgedAt: purgedAt == const $CopyWithPlaceholder()
          ? _value.purgedAt
          // ignore: cast_nullable_to_non_nullable
          : purgedAt as DateTime?,
    );
  }
}

extension $ArchiveCopyWith on Archive {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfArchive.copyWith(...)` or `instanceOfArchive.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ArchiveCWProxy get copyWith => _$ArchiveCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Archive _$ArchiveFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Archive', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'status', 'createdAt', 'target']);
  final val = Archive(
    id: $checkedConvert('id', (v) => v as String),
    status: $checkedConvert('status', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    cutoff: $checkedConvert(
      'cutoff',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    target: $checkedConvert('target', (v) => v as String),
    location: $checkedConvert('location', (v) => v as String?),
    sizeBytes: $checkedConvert('sizeBytes', (v) => (v as num?)?.toInt()),
    itemCount: $checkedConvert('itemCount', (v) => (v as num?)?.toInt()),
    checksum: $checkedConvert('checksum', (v) => v as String?),
    purgedFromHot: $checkedConvert('purgedFromHot', (v) => v as bool? ?? false),
    restoredAt: $checkedConvert(
      'restoredAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    trigger: $checkedConvert('trigger', (v) => v as String?),
    domain: $checkedConvert('domain', (v) => v as String?),
    sources: $checkedConvert(
      'sources',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    fromSeq: $checkedConvert('fromSeq', (v) => (v as num?)?.toInt()),
    toSeq: $checkedConvert('toSeq', (v) => (v as num?)?.toInt()),
    fromTime: $checkedConvert(
      'fromTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    toTime: $checkedConvert(
      'toTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    files: $checkedConvert(
      'files',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ArchiveFilesInner.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    error: $checkedConvert('error', (v) => v as String?),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    completedAt: $checkedConvert(
      'completedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    purgedAt: $checkedConvert(
      'purgedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ArchiveToJson(Archive instance) => <String, dynamic>{
  'id': instance.id,
  'status': instance.status,
  'createdAt': instance.createdAt.toIso8601String(),
  'cutoff': ?instance.cutoff?.toIso8601String(),
  'target': instance.target,
  'location': ?instance.location,
  'sizeBytes': ?instance.sizeBytes,
  'itemCount': ?instance.itemCount,
  'checksum': ?instance.checksum,
  'purgedFromHot': ?instance.purgedFromHot,
  'restoredAt': ?instance.restoredAt?.toIso8601String(),
  'trigger': ?instance.trigger,
  'domain': ?instance.domain,
  'sources': ?instance.sources,
  'fromSeq': ?instance.fromSeq,
  'toSeq': ?instance.toSeq,
  'fromTime': ?instance.fromTime?.toIso8601String(),
  'toTime': ?instance.toTime?.toIso8601String(),
  'files': ?instance.files?.map((e) => e.toJson()).toList(),
  'error': ?instance.error,
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'completedAt': ?instance.completedAt?.toIso8601String(),
  'purgedAt': ?instance.purgedAt?.toIso8601String(),
};
