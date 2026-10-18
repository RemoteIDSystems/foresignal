// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_history_tiers_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoHistoryTiersInnerCWProxy {
  SystemInfoHistoryTiersInner tier(HistoryTier tier);

  SystemInfoHistoryTiersInner partitions(int partitions);

  SystemInfoHistoryTiersInner rows(int rows);

  SystemInfoHistoryTiersInner bytes(int bytes);

  SystemInfoHistoryTiersInner headroomSeq(int? headroomSeq);

  SystemInfoHistoryTiersInner windowDays(num? windowDays);

  SystemInfoHistoryTiersInner downsampledLeaves(int? downsampledLeaves);

  SystemInfoHistoryTiersInner downsampledRows(int? downsampledRows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoHistoryTiersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoHistoryTiersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoHistoryTiersInner call({
    HistoryTier tier,
    int partitions,
    int rows,
    int bytes,
    int? headroomSeq,
    num? windowDays,
    int? downsampledLeaves,
    int? downsampledRows,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoHistoryTiersInner.copyWith(...)` or call `instanceOfSystemInfoHistoryTiersInner.copyWith.fieldName(value)` for a single field.
class _$SystemInfoHistoryTiersInnerCWProxyImpl
    implements _$SystemInfoHistoryTiersInnerCWProxy {
  const _$SystemInfoHistoryTiersInnerCWProxyImpl(this._value);

  final SystemInfoHistoryTiersInner _value;

  @override
  SystemInfoHistoryTiersInner tier(HistoryTier tier) => call(tier: tier);

  @override
  SystemInfoHistoryTiersInner partitions(int partitions) =>
      call(partitions: partitions);

  @override
  SystemInfoHistoryTiersInner rows(int rows) => call(rows: rows);

  @override
  SystemInfoHistoryTiersInner bytes(int bytes) => call(bytes: bytes);

  @override
  SystemInfoHistoryTiersInner headroomSeq(int? headroomSeq) =>
      call(headroomSeq: headroomSeq);

  @override
  SystemInfoHistoryTiersInner windowDays(num? windowDays) =>
      call(windowDays: windowDays);

  @override
  SystemInfoHistoryTiersInner downsampledLeaves(int? downsampledLeaves) =>
      call(downsampledLeaves: downsampledLeaves);

  @override
  SystemInfoHistoryTiersInner downsampledRows(int? downsampledRows) =>
      call(downsampledRows: downsampledRows);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoHistoryTiersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoHistoryTiersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoHistoryTiersInner call({
    Object? tier = const $CopyWithPlaceholder(),
    Object? partitions = const $CopyWithPlaceholder(),
    Object? rows = const $CopyWithPlaceholder(),
    Object? bytes = const $CopyWithPlaceholder(),
    Object? headroomSeq = const $CopyWithPlaceholder(),
    Object? windowDays = const $CopyWithPlaceholder(),
    Object? downsampledLeaves = const $CopyWithPlaceholder(),
    Object? downsampledRows = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoHistoryTiersInner(
      tier: tier == const $CopyWithPlaceholder() || tier == null
          ? _value.tier
          // ignore: cast_nullable_to_non_nullable
          : tier as HistoryTier,
      partitions:
          partitions == const $CopyWithPlaceholder() || partitions == null
          ? _value.partitions
          // ignore: cast_nullable_to_non_nullable
          : partitions as int,
      rows: rows == const $CopyWithPlaceholder() || rows == null
          ? _value.rows
          // ignore: cast_nullable_to_non_nullable
          : rows as int,
      bytes: bytes == const $CopyWithPlaceholder() || bytes == null
          ? _value.bytes
          // ignore: cast_nullable_to_non_nullable
          : bytes as int,
      headroomSeq: headroomSeq == const $CopyWithPlaceholder()
          ? _value.headroomSeq
          // ignore: cast_nullable_to_non_nullable
          : headroomSeq as int?,
      windowDays: windowDays == const $CopyWithPlaceholder()
          ? _value.windowDays
          // ignore: cast_nullable_to_non_nullable
          : windowDays as num?,
      downsampledLeaves: downsampledLeaves == const $CopyWithPlaceholder()
          ? _value.downsampledLeaves
          // ignore: cast_nullable_to_non_nullable
          : downsampledLeaves as int?,
      downsampledRows: downsampledRows == const $CopyWithPlaceholder()
          ? _value.downsampledRows
          // ignore: cast_nullable_to_non_nullable
          : downsampledRows as int?,
    );
  }
}

extension $SystemInfoHistoryTiersInnerCopyWith on SystemInfoHistoryTiersInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoHistoryTiersInner.copyWith(...)` or `instanceOfSystemInfoHistoryTiersInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoHistoryTiersInnerCWProxy get copyWith =>
      _$SystemInfoHistoryTiersInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoHistoryTiersInner _$SystemInfoHistoryTiersInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SystemInfoHistoryTiersInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['tier', 'partitions', 'rows', 'bytes']);
  final val = SystemInfoHistoryTiersInner(
    tier: $checkedConvert('tier', (v) => $enumDecode(_$HistoryTierEnumMap, v)),
    partitions: $checkedConvert('partitions', (v) => (v as num).toInt()),
    rows: $checkedConvert('rows', (v) => (v as num).toInt()),
    bytes: $checkedConvert('bytes', (v) => (v as num).toInt()),
    headroomSeq: $checkedConvert('headroomSeq', (v) => (v as num?)?.toInt()),
    windowDays: $checkedConvert('windowDays', (v) => v as num?),
    downsampledLeaves: $checkedConvert(
      'downsampledLeaves',
      (v) => (v as num?)?.toInt(),
    ),
    downsampledRows: $checkedConvert(
      'downsampledRows',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SystemInfoHistoryTiersInnerToJson(
  SystemInfoHistoryTiersInner instance,
) => <String, dynamic>{
  'tier': _$HistoryTierEnumMap[instance.tier]!,
  'partitions': instance.partitions,
  'rows': instance.rows,
  'bytes': instance.bytes,
  'headroomSeq': ?instance.headroomSeq,
  'windowDays': ?instance.windowDays,
  'downsampledLeaves': ?instance.downsampledLeaves,
  'downsampledRows': ?instance.downsampledRows,
};

const _$HistoryTierEnumMap = {
  HistoryTier.STANDARD: 'STANDARD',
  HistoryTier.TRACK: 'TRACK',
};
