// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'archive_files_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ArchiveFilesInnerCWProxy {
  ArchiveFilesInner name(String? name);

  ArchiveFilesInner sourceId(String? sourceId);

  ArchiveFilesInner tier(ArchiveFilesInnerTierEnum? tier);

  ArchiveFilesInner fromSeq(int? fromSeq);

  ArchiveFilesInner toSeq(int? toSeq);

  ArchiveFilesInner fromTime(DateTime? fromTime);

  ArchiveFilesInner toTime(DateTime? toTime);

  ArchiveFilesInner rows(int? rows);

  ArchiveFilesInner bytes(int? bytes);

  ArchiveFilesInner sha256(String? sha256);

  ArchiveFilesInner location(String? location);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchiveFilesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchiveFilesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ArchiveFilesInner call({
    String? name,
    String? sourceId,
    ArchiveFilesInnerTierEnum? tier,
    int? fromSeq,
    int? toSeq,
    DateTime? fromTime,
    DateTime? toTime,
    int? rows,
    int? bytes,
    String? sha256,
    String? location,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfArchiveFilesInner.copyWith(...)` or call `instanceOfArchiveFilesInner.copyWith.fieldName(value)` for a single field.
class _$ArchiveFilesInnerCWProxyImpl implements _$ArchiveFilesInnerCWProxy {
  const _$ArchiveFilesInnerCWProxyImpl(this._value);

  final ArchiveFilesInner _value;

  @override
  ArchiveFilesInner name(String? name) => call(name: name);

  @override
  ArchiveFilesInner sourceId(String? sourceId) => call(sourceId: sourceId);

  @override
  ArchiveFilesInner tier(ArchiveFilesInnerTierEnum? tier) => call(tier: tier);

  @override
  ArchiveFilesInner fromSeq(int? fromSeq) => call(fromSeq: fromSeq);

  @override
  ArchiveFilesInner toSeq(int? toSeq) => call(toSeq: toSeq);

  @override
  ArchiveFilesInner fromTime(DateTime? fromTime) => call(fromTime: fromTime);

  @override
  ArchiveFilesInner toTime(DateTime? toTime) => call(toTime: toTime);

  @override
  ArchiveFilesInner rows(int? rows) => call(rows: rows);

  @override
  ArchiveFilesInner bytes(int? bytes) => call(bytes: bytes);

  @override
  ArchiveFilesInner sha256(String? sha256) => call(sha256: sha256);

  @override
  ArchiveFilesInner location(String? location) => call(location: location);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchiveFilesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchiveFilesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ArchiveFilesInner call({
    Object? name = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? tier = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? fromTime = const $CopyWithPlaceholder(),
    Object? toTime = const $CopyWithPlaceholder(),
    Object? rows = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? location = const $CopyWithPlaceholder(),
  }) {
    return ArchiveFilesInner(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      sourceId: sourceId == const $CopyWithPlaceholder()
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String?,
      tier: tier == const $CopyWithPlaceholder()
          ? _value.tier
          // ignore: cast_nullable_to_non_nullable
          : tier as ArchiveFilesInnerTierEnum?,
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
      rows: rows == const $CopyWithPlaceholder()
          ? _value.rows
          // ignore: cast_nullable_to_non_nullable
          : rows as int?,
      bytes: bytes == const $CopyWithPlaceholder()
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int?,
      sha256: sha256 == const $CopyWithPlaceholder()
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String?,
      location: location == const $CopyWithPlaceholder()
          ? _value.location
          // ignore: cast_nullable_to_non_nullable
          : location as String?,
    );
  }
}

extension $ArchiveFilesInnerCopyWith on ArchiveFilesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfArchiveFilesInner.copyWith(...)` or `instanceOfArchiveFilesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ArchiveFilesInnerCWProxy get copyWith =>
      _$ArchiveFilesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ArchiveFilesInner _$ArchiveFilesInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ArchiveFilesInner', json, ($checkedConvert) {
      final val = ArchiveFilesInner(
        name: $checkedConvert('name', (v) => v as String?),
        sourceId: $checkedConvert('sourceId', (v) => v as String?),
        tier: $checkedConvert(
          'tier',
          (v) => $enumDecodeNullable(_$ArchiveFilesInnerTierEnumEnumMap, v),
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
        rows: $checkedConvert('rows', (v) => (v as num?)?.toInt()),
        bytes: $checkedConvert('bytes', (v) => (v as num?)?.toInt()),
        sha256: $checkedConvert('sha256', (v) => v as String?),
        location: $checkedConvert('location', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ArchiveFilesInnerToJson(ArchiveFilesInner instance) =>
    <String, dynamic>{
      'name': ?instance.name,
      'sourceId': ?instance.sourceId,
      'tier': ?_$ArchiveFilesInnerTierEnumEnumMap[instance.tier],
      'fromSeq': ?instance.fromSeq,
      'toSeq': ?instance.toSeq,
      'fromTime': ?instance.fromTime?.toIso8601String(),
      'toTime': ?instance.toTime?.toIso8601String(),
      'rows': ?instance.rows,
      'bytes': ?instance.bytes,
      'sha256': ?instance.sha256,
      'location': ?instance.location,
    };

const _$ArchiveFilesInnerTierEnumEnumMap = {
  ArchiveFilesInnerTierEnum.standard: 'standard',
  ArchiveFilesInnerTierEnum.track: 'track',
};
