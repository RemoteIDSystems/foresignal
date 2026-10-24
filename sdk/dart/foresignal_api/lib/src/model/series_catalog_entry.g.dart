// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series_catalog_entry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesCatalogEntryCWProxy {
  SeriesCatalogEntry sourceId(String sourceId);

  SeriesCatalogEntry series(String series);

  SeriesCatalogEntry seriesName(String seriesName);

  SeriesCatalogEntry dataset(String? dataset);

  SeriesCatalogEntry unit(String unit);

  SeriesCatalogEntry frequency(SeriesCatalogEntryFrequencyEnum frequency);

  SeriesCatalogEntry area(String? area);

  SeriesCatalogEntry latestPeriod(String? latestPeriod);

  SeriesCatalogEntry latestValue(num? latestValue);

  SeriesCatalogEntry status(SeriesCatalogEntryStatusEnum? status);

  SeriesCatalogEntry family(SeriesCatalogEntryFamilyEnum family);

  SeriesCatalogEntry transform(SeriesCatalogEntryTransformEnum transform);

  SeriesCatalogEntry releaseLagDays(num releaseLagDays);

  SeriesCatalogEntry egress(SeriesCatalogEntryEgressEnum egress);

  SeriesCatalogEntry regionCode(String? regionCode);

  SeriesCatalogEntry topicId(String? topicId);

  SeriesCatalogEntry attribution(String? attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesCatalogEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesCatalogEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  SeriesCatalogEntry call({
    String sourceId,
    String series,
    String seriesName,
    String? dataset,
    String unit,
    SeriesCatalogEntryFrequencyEnum frequency,
    String? area,
    String? latestPeriod,
    num? latestValue,
    SeriesCatalogEntryStatusEnum? status,
    SeriesCatalogEntryFamilyEnum family,
    SeriesCatalogEntryTransformEnum transform,
    num releaseLagDays,
    SeriesCatalogEntryEgressEnum egress,
    String? regionCode,
    String? topicId,
    String? attribution,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeriesCatalogEntry.copyWith(...)` or call `instanceOfSeriesCatalogEntry.copyWith.fieldName(value)` for a single field.
class _$SeriesCatalogEntryCWProxyImpl implements _$SeriesCatalogEntryCWProxy {
  const _$SeriesCatalogEntryCWProxyImpl(this._value);

  final SeriesCatalogEntry _value;

  @override
  SeriesCatalogEntry sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  SeriesCatalogEntry series(String series) => call(series: series);

  @override
  SeriesCatalogEntry seriesName(String seriesName) =>
      call(seriesName: seriesName);

  @override
  SeriesCatalogEntry dataset(String? dataset) => call(dataset: dataset);

  @override
  SeriesCatalogEntry unit(String unit) => call(unit: unit);

  @override
  SeriesCatalogEntry frequency(SeriesCatalogEntryFrequencyEnum frequency) =>
      call(frequency: frequency);

  @override
  SeriesCatalogEntry area(String? area) => call(area: area);

  @override
  SeriesCatalogEntry latestPeriod(String? latestPeriod) =>
      call(latestPeriod: latestPeriod);

  @override
  SeriesCatalogEntry latestValue(num? latestValue) =>
      call(latestValue: latestValue);

  @override
  SeriesCatalogEntry status(SeriesCatalogEntryStatusEnum? status) =>
      call(status: status);

  @override
  SeriesCatalogEntry family(SeriesCatalogEntryFamilyEnum family) =>
      call(family: family);

  @override
  SeriesCatalogEntry transform(SeriesCatalogEntryTransformEnum transform) =>
      call(transform: transform);

  @override
  SeriesCatalogEntry releaseLagDays(num releaseLagDays) =>
      call(releaseLagDays: releaseLagDays);

  @override
  SeriesCatalogEntry egress(SeriesCatalogEntryEgressEnum egress) =>
      call(egress: egress);

  @override
  SeriesCatalogEntry regionCode(String? regionCode) =>
      call(regionCode: regionCode);

  @override
  SeriesCatalogEntry topicId(String? topicId) => call(topicId: topicId);

  @override
  SeriesCatalogEntry attribution(String? attribution) =>
      call(attribution: attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesCatalogEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesCatalogEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SeriesCatalogEntry call({
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? series = const $CopyWithPlaceholder(),
    Object? seriesName = const $CopyWithPlaceholder(),
    Object? dataset = const $CopyWithPlaceholder(),
    Object? unit = const $CopyWithPlaceholder(),
    Object? frequency = const $CopyWithPlaceholder(),
    Object? area = const $CopyWithPlaceholder(),
    Object? latestPeriod = const $CopyWithPlaceholder(),
    Object? latestValue = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? transform = const $CopyWithPlaceholder(),
    Object? releaseLagDays = const $CopyWithPlaceholder(),
    Object? egress = const $CopyWithPlaceholder(),
    Object? regionCode = const $CopyWithPlaceholder(),
    Object? topicId = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
  }) {
    return SeriesCatalogEntry(
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      series: series == const $CopyWithPlaceholder() || series == null
          ? _value.series
          // ignore: cast_nullable_to_non_nullable
          : series as String,
      seriesName:
          seriesName == const $CopyWithPlaceholder() || seriesName == null
          ? _value.seriesName
          // ignore: cast_nullable_to_non_nullable
          : seriesName as String,
      dataset: dataset == const $CopyWithPlaceholder()
          ? _value.dataset
          // ignore: cast_nullable_to_non_nullable
          : dataset as String?,
      unit: unit == const $CopyWithPlaceholder() || unit == null
          ? _value.unit
          // ignore: cast_nullable_to_non_nullable
          : unit as String,
      frequency: frequency == const $CopyWithPlaceholder() || frequency == null
          ? _value.frequency
          // ignore: cast_nullable_to_non_nullable
          : frequency as SeriesCatalogEntryFrequencyEnum,
      area: area == const $CopyWithPlaceholder()
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as String?,
      latestPeriod: latestPeriod == const $CopyWithPlaceholder()
          ? _value.latestPeriod
          // ignore: cast_nullable_to_non_nullable
          : latestPeriod as String?,
      latestValue: latestValue == const $CopyWithPlaceholder()
          ? _value.latestValue
          // ignore: cast_nullable_to_non_nullable
          : latestValue as num?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SeriesCatalogEntryStatusEnum?,
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as SeriesCatalogEntryFamilyEnum,
      transform: transform == const $CopyWithPlaceholder() || transform == null
          ? _value.transform
          // ignore: cast_nullable_to_non_nullable
          : transform as SeriesCatalogEntryTransformEnum,
      releaseLagDays:
          releaseLagDays == const $CopyWithPlaceholder() ||
              releaseLagDays == null
          ? _value.releaseLagDays
          // ignore: cast_nullable_to_non_nullable
          : releaseLagDays as num,
      egress: egress == const $CopyWithPlaceholder() || egress == null
          ? _value.egress
          // ignore: cast_nullable_to_non_nullable
          : egress as SeriesCatalogEntryEgressEnum,
      regionCode: regionCode == const $CopyWithPlaceholder()
          ? _value.regionCode
          // ignore: cast_nullable_to_non_nullable
          : regionCode as String?,
      topicId: topicId == const $CopyWithPlaceholder()
          ? _value.topicId
          // ignore: cast_nullable_to_non_nullable
          : topicId as String?,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
    );
  }
}

extension $SeriesCatalogEntryCopyWith on SeriesCatalogEntry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeriesCatalogEntry.copyWith(...)` or `instanceOfSeriesCatalogEntry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesCatalogEntryCWProxy get copyWith =>
      _$SeriesCatalogEntryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SeriesCatalogEntry _$SeriesCatalogEntryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SeriesCatalogEntry', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'sourceId',
          'series',
          'seriesName',
          'unit',
          'frequency',
          'family',
          'transform',
          'releaseLagDays',
          'egress',
        ],
      );
      final val = SeriesCatalogEntry(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        series: $checkedConvert('series', (v) => v as String),
        seriesName: $checkedConvert('seriesName', (v) => v as String),
        dataset: $checkedConvert('dataset', (v) => v as String?),
        unit: $checkedConvert('unit', (v) => v as String),
        frequency: $checkedConvert(
          'frequency',
          (v) => $enumDecode(_$SeriesCatalogEntryFrequencyEnumEnumMap, v),
        ),
        area: $checkedConvert('area', (v) => v as String?),
        latestPeriod: $checkedConvert('latestPeriod', (v) => v as String?),
        latestValue: $checkedConvert('latestValue', (v) => v as num?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$SeriesCatalogEntryStatusEnumEnumMap, v),
        ),
        family: $checkedConvert(
          'family',
          (v) => $enumDecode(_$SeriesCatalogEntryFamilyEnumEnumMap, v),
        ),
        transform: $checkedConvert(
          'transform',
          (v) => $enumDecode(_$SeriesCatalogEntryTransformEnumEnumMap, v),
        ),
        releaseLagDays: $checkedConvert('releaseLagDays', (v) => v as num),
        egress: $checkedConvert(
          'egress',
          (v) => $enumDecode(_$SeriesCatalogEntryEgressEnumEnumMap, v),
        ),
        regionCode: $checkedConvert('regionCode', (v) => v as String?),
        topicId: $checkedConvert('topicId', (v) => v as String?),
        attribution: $checkedConvert('attribution', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SeriesCatalogEntryToJson(
  SeriesCatalogEntry instance,
) => <String, dynamic>{
  'sourceId': instance.sourceId,
  'series': instance.series,
  'seriesName': instance.seriesName,
  'dataset': ?instance.dataset,
  'unit': instance.unit,
  'frequency': _$SeriesCatalogEntryFrequencyEnumEnumMap[instance.frequency]!,
  'area': ?instance.area,
  'latestPeriod': ?instance.latestPeriod,
  'latestValue': ?instance.latestValue,
  'status': ?_$SeriesCatalogEntryStatusEnumEnumMap[instance.status],
  'family': _$SeriesCatalogEntryFamilyEnumEnumMap[instance.family]!,
  'transform': _$SeriesCatalogEntryTransformEnumEnumMap[instance.transform]!,
  'releaseLagDays': instance.releaseLagDays,
  'egress': _$SeriesCatalogEntryEgressEnumEnumMap[instance.egress]!,
  'regionCode': ?instance.regionCode,
  'topicId': ?instance.topicId,
  'attribution': ?instance.attribution,
};

const _$SeriesCatalogEntryFrequencyEnumEnumMap = {
  SeriesCatalogEntryFrequencyEnum.PT15S: 'PT15S',
  SeriesCatalogEntryFrequencyEnum.PT15M: 'PT15M',
  SeriesCatalogEntryFrequencyEnum.PT30M: 'PT30M',
  SeriesCatalogEntryFrequencyEnum.PT1H: 'PT1H',
  SeriesCatalogEntryFrequencyEnum.P1D: 'P1D',
  SeriesCatalogEntryFrequencyEnum.P1W: 'P1W',
  SeriesCatalogEntryFrequencyEnum.P1M: 'P1M',
  SeriesCatalogEntryFrequencyEnum.P3M: 'P3M',
  SeriesCatalogEntryFrequencyEnum.P1Y: 'P1Y',
};

const _$SeriesCatalogEntryStatusEnumEnumMap = {
  SeriesCatalogEntryStatusEnum.final_: 'final',
  SeriesCatalogEntryStatusEnum.preliminary: 'preliminary',
  SeriesCatalogEntryStatusEnum.estimated: 'estimated',
  SeriesCatalogEntryStatusEnum.forecast: 'forecast',
};

const _$SeriesCatalogEntryFamilyEnumEnumMap = {
  SeriesCatalogEntryFamilyEnum.price: 'price',
  SeriesCatalogEntryFamilyEnum.volume: 'volume',
  SeriesCatalogEntryFamilyEnum.openInterest: 'open-interest',
  SeriesCatalogEntryFamilyEnum.position: 'position',
  SeriesCatalogEntryFamilyEnum.rate: 'rate',
  SeriesCatalogEntryFamilyEnum.attention: 'attention',
  SeriesCatalogEntryFamilyEnum.tone: 'tone',
  SeriesCatalogEntryFamilyEnum.count: 'count',
  SeriesCatalogEntryFamilyEnum.indexValue: 'index',
  SeriesCatalogEntryFamilyEnum.structural: 'structural',
};

const _$SeriesCatalogEntryTransformEnumEnumMap = {
  SeriesCatalogEntryTransformEnum.logReturn: 'log-return',
  SeriesCatalogEntryTransformEnum.logLevel: 'log-level',
  SeriesCatalogEntryTransformEnum.difference: 'difference',
  SeriesCatalogEntryTransformEnum.level: 'level',
};

const _$SeriesCatalogEntryEgressEnumEnumMap = {
  SeriesCatalogEntryEgressEnum.OPEN: 'OPEN',
  SeriesCatalogEntryEgressEnum.LOCAL_ONLY: 'LOCAL_ONLY',
};
