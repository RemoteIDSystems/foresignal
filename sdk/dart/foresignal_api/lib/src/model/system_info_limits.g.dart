// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_limits.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoLimitsCWProxy {
  SystemInfoLimits pageMax(int? pageMax);

  SystemInfoLimits historyPageMax(int? historyPageMax);

  SystemInfoLimits sseClientBuffer(int? sseClientBuffer);

  SystemInfoLimits liveMaxEntities(int? liveMaxEntities);

  SystemInfoLimits ingestMaxBytes(int? ingestMaxBytes);

  SystemInfoLimits ingestMaxRecords(int? ingestMaxRecords);

  SystemInfoLimits tileMaxFeatures(int? tileMaxFeatures);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoLimits(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoLimits(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoLimits call({
    int? pageMax,
    int? historyPageMax,
    int? sseClientBuffer,
    int? liveMaxEntities,
    int? ingestMaxBytes,
    int? ingestMaxRecords,
    int? tileMaxFeatures,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoLimits.copyWith(...)` or call `instanceOfSystemInfoLimits.copyWith.fieldName(value)` for a single field.
class _$SystemInfoLimitsCWProxyImpl implements _$SystemInfoLimitsCWProxy {
  const _$SystemInfoLimitsCWProxyImpl(this._value);

  final SystemInfoLimits _value;

  @override
  SystemInfoLimits pageMax(int? pageMax) => call(pageMax: pageMax);

  @override
  SystemInfoLimits historyPageMax(int? historyPageMax) =>
      call(historyPageMax: historyPageMax);

  @override
  SystemInfoLimits sseClientBuffer(int? sseClientBuffer) =>
      call(sseClientBuffer: sseClientBuffer);

  @override
  SystemInfoLimits liveMaxEntities(int? liveMaxEntities) =>
      call(liveMaxEntities: liveMaxEntities);

  @override
  SystemInfoLimits ingestMaxBytes(int? ingestMaxBytes) =>
      call(ingestMaxBytes: ingestMaxBytes);

  @override
  SystemInfoLimits ingestMaxRecords(int? ingestMaxRecords) =>
      call(ingestMaxRecords: ingestMaxRecords);

  @override
  SystemInfoLimits tileMaxFeatures(int? tileMaxFeatures) =>
      call(tileMaxFeatures: tileMaxFeatures);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoLimits(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoLimits(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoLimits call({
    Object? pageMax = const $CopyWithPlaceholder(),
    Object? historyPageMax = const $CopyWithPlaceholder(),
    Object? sseClientBuffer = const $CopyWithPlaceholder(),
    Object? liveMaxEntities = const $CopyWithPlaceholder(),
    Object? ingestMaxBytes = const $CopyWithPlaceholder(),
    Object? ingestMaxRecords = const $CopyWithPlaceholder(),
    Object? tileMaxFeatures = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoLimits(
      pageMax: pageMax == const $CopyWithPlaceholder()
          ? _value.pageMax
          // ignore: cast_nullable_to_non_nullable
          : pageMax as int?,
      historyPageMax: historyPageMax == const $CopyWithPlaceholder()
          ? _value.historyPageMax
          // ignore: cast_nullable_to_non_nullable
          : historyPageMax as int?,
      sseClientBuffer: sseClientBuffer == const $CopyWithPlaceholder()
          ? _value.sseClientBuffer
          // ignore: cast_nullable_to_non_nullable
          : sseClientBuffer as int?,
      liveMaxEntities: liveMaxEntities == const $CopyWithPlaceholder()
          ? _value.liveMaxEntities
          // ignore: cast_nullable_to_non_nullable
          : liveMaxEntities as int?,
      ingestMaxBytes: ingestMaxBytes == const $CopyWithPlaceholder()
          ? _value.ingestMaxBytes
          // ignore: cast_nullable_to_non_nullable
          : ingestMaxBytes as int?,
      ingestMaxRecords: ingestMaxRecords == const $CopyWithPlaceholder()
          ? _value.ingestMaxRecords
          // ignore: cast_nullable_to_non_nullable
          : ingestMaxRecords as int?,
      tileMaxFeatures: tileMaxFeatures == const $CopyWithPlaceholder()
          ? _value.tileMaxFeatures
          // ignore: cast_nullable_to_non_nullable
          : tileMaxFeatures as int?,
    );
  }
}

extension $SystemInfoLimitsCopyWith on SystemInfoLimits {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoLimits.copyWith(...)` or `instanceOfSystemInfoLimits.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoLimitsCWProxy get copyWith => _$SystemInfoLimitsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoLimits _$SystemInfoLimitsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoLimits', json, ($checkedConvert) {
      final val = SystemInfoLimits(
        pageMax: $checkedConvert('pageMax', (v) => (v as num?)?.toInt()),
        historyPageMax: $checkedConvert(
          'historyPageMax',
          (v) => (v as num?)?.toInt(),
        ),
        sseClientBuffer: $checkedConvert(
          'sseClientBuffer',
          (v) => (v as num?)?.toInt(),
        ),
        liveMaxEntities: $checkedConvert(
          'liveMaxEntities',
          (v) => (v as num?)?.toInt(),
        ),
        ingestMaxBytes: $checkedConvert(
          'ingestMaxBytes',
          (v) => (v as num?)?.toInt(),
        ),
        ingestMaxRecords: $checkedConvert(
          'ingestMaxRecords',
          (v) => (v as num?)?.toInt(),
        ),
        tileMaxFeatures: $checkedConvert(
          'tileMaxFeatures',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoLimitsToJson(SystemInfoLimits instance) =>
    <String, dynamic>{
      'pageMax': ?instance.pageMax,
      'historyPageMax': ?instance.historyPageMax,
      'sseClientBuffer': ?instance.sseClientBuffer,
      'liveMaxEntities': ?instance.liveMaxEntities,
      'ingestMaxBytes': ?instance.ingestMaxBytes,
      'ingestMaxRecords': ?instance.ingestMaxRecords,
      'tileMaxFeatures': ?instance.tileMaxFeatures,
    };
