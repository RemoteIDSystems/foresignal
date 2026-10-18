// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'archive_partition.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ArchivePartitionCWProxy {
  ArchivePartition tier(ArchivePartitionTierEnum tier);

  ArchivePartition sourceId(String sourceId);

  ArchivePartition date(DateTime date);

  ArchivePartition path(String path);

  ArchivePartition files(List<ArchivePartitionFilesInner> files);

  ArchivePartition licence(ExportLicenceSummary? licence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchivePartition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchivePartition(...).copyWith(id: 12, name: "My name")
  /// ```
  ArchivePartition call({
    ArchivePartitionTierEnum tier,
    String sourceId,
    DateTime date,
    String path,
    List<ArchivePartitionFilesInner> files,
    ExportLicenceSummary? licence,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfArchivePartition.copyWith(...)` or call `instanceOfArchivePartition.copyWith.fieldName(value)` for a single field.
class _$ArchivePartitionCWProxyImpl implements _$ArchivePartitionCWProxy {
  const _$ArchivePartitionCWProxyImpl(this._value);

  final ArchivePartition _value;

  @override
  ArchivePartition tier(ArchivePartitionTierEnum tier) => call(tier: tier);

  @override
  ArchivePartition sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  ArchivePartition date(DateTime date) => call(date: date);

  @override
  ArchivePartition path(String path) => call(path: path);

  @override
  ArchivePartition files(List<ArchivePartitionFilesInner> files) =>
      call(files: files);

  @override
  ArchivePartition licence(ExportLicenceSummary? licence) =>
      call(licence: licence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchivePartition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchivePartition(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ArchivePartition call({
    Object? tier = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? date = const $CopyWithPlaceholder(),
    Object? path = const $CopyWithPlaceholder(),
    Object? files = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
  }) {
    return ArchivePartition(
      tier: tier == const $CopyWithPlaceholder() || tier == null
          ? _value.tier
          // ignore: cast_nullable_to_non_nullable
          : tier as ArchivePartitionTierEnum,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      date: date == const $CopyWithPlaceholder() || date == null
          ? _value.date
          // ignore: cast_nullable_to_non_nullable
          : date as DateTime,
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
      files: files == const $CopyWithPlaceholder() || files == null
          ? _value.files
          // ignore: cast_nullable_to_non_nullable
          : files as List<ArchivePartitionFilesInner>,
      licence: licence == const $CopyWithPlaceholder()
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as ExportLicenceSummary?,
    );
  }
}

extension $ArchivePartitionCopyWith on ArchivePartition {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfArchivePartition.copyWith(...)` or `instanceOfArchivePartition.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ArchivePartitionCWProxy get copyWith => _$ArchivePartitionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ArchivePartition _$ArchivePartitionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ArchivePartition', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['tier', 'sourceId', 'date', 'path', 'files'],
      );
      final val = ArchivePartition(
        tier: $checkedConvert(
          'tier',
          (v) => $enumDecode(_$ArchivePartitionTierEnumEnumMap, v),
        ),
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        date: $checkedConvert('date', (v) => DateTime.parse(v as String)),
        path: $checkedConvert('path', (v) => v as String),
        files: $checkedConvert(
          'files',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ArchivePartitionFilesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        licence: $checkedConvert(
          'licence',
          (v) => v == null
              ? null
              : ExportLicenceSummary.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ArchivePartitionToJson(ArchivePartition instance) =>
    <String, dynamic>{
      'tier': _$ArchivePartitionTierEnumEnumMap[instance.tier]!,
      'sourceId': instance.sourceId,
      'date': instance.date.toIso8601String(),
      'path': instance.path,
      'files': instance.files.map((e) => e.toJson()).toList(),
      'licence': ?instance.licence?.toJson(),
    };

const _$ArchivePartitionTierEnumEnumMap = {
  ArchivePartitionTierEnum.std: 'std',
  ArchivePartitionTierEnum.trk: 'trk',
};
