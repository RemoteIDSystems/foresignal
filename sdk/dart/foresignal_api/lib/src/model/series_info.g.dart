// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series_info.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesInfoCWProxy {
  SeriesInfo sourceId(String sourceId);

  SeriesInfo series(String series);

  SeriesInfo seriesName(String seriesName);

  SeriesInfo dataset(String? dataset);

  SeriesInfo unit(String unit);

  SeriesInfo frequency(SeriesInfoFrequencyEnum frequency);

  SeriesInfo area(String? area);

  SeriesInfo latestPeriod(String? latestPeriod);

  SeriesInfo latestValue(num? latestValue);

  SeriesInfo status(SeriesInfoStatusEnum? status);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  SeriesInfo call({
    String sourceId,
    String series,
    String seriesName,
    String? dataset,
    String unit,
    SeriesInfoFrequencyEnum frequency,
    String? area,
    String? latestPeriod,
    num? latestValue,
    SeriesInfoStatusEnum? status,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeriesInfo.copyWith(...)` or call `instanceOfSeriesInfo.copyWith.fieldName(value)` for a single field.
class _$SeriesInfoCWProxyImpl implements _$SeriesInfoCWProxy {
  const _$SeriesInfoCWProxyImpl(this._value);

  final SeriesInfo _value;

  @override
  SeriesInfo sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  SeriesInfo series(String series) => call(series: series);

  @override
  SeriesInfo seriesName(String seriesName) => call(seriesName: seriesName);

  @override
  SeriesInfo dataset(String? dataset) => call(dataset: dataset);

  @override
  SeriesInfo unit(String unit) => call(unit: unit);

  @override
  SeriesInfo frequency(SeriesInfoFrequencyEnum frequency) =>
      call(frequency: frequency);

  @override
  SeriesInfo area(String? area) => call(area: area);

  @override
  SeriesInfo latestPeriod(String? latestPeriod) =>
      call(latestPeriod: latestPeriod);

  @override
  SeriesInfo latestValue(num? latestValue) => call(latestValue: latestValue);

  @override
  SeriesInfo status(SeriesInfoStatusEnum? status) => call(status: status);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SeriesInfo call({
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
  }) {
    return SeriesInfo(
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
          : frequency as SeriesInfoFrequencyEnum,
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
          : status as SeriesInfoStatusEnum?,
    );
  }
}

extension $SeriesInfoCopyWith on SeriesInfo {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeriesInfo.copyWith(...)` or `instanceOfSeriesInfo.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesInfoCWProxy get copyWith => _$SeriesInfoCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SeriesInfo _$SeriesInfoFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SeriesInfo', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'sourceId',
          'series',
          'seriesName',
          'unit',
          'frequency',
        ],
      );
      final val = SeriesInfo(
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        series: $checkedConvert('series', (v) => v as String),
        seriesName: $checkedConvert('seriesName', (v) => v as String),
        dataset: $checkedConvert('dataset', (v) => v as String?),
        unit: $checkedConvert('unit', (v) => v as String),
        frequency: $checkedConvert(
          'frequency',
          (v) => $enumDecode(_$SeriesInfoFrequencyEnumEnumMap, v),
        ),
        area: $checkedConvert('area', (v) => v as String?),
        latestPeriod: $checkedConvert('latestPeriod', (v) => v as String?),
        latestValue: $checkedConvert('latestValue', (v) => v as num?),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(_$SeriesInfoStatusEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SeriesInfoToJson(SeriesInfo instance) =>
    <String, dynamic>{
      'sourceId': instance.sourceId,
      'series': instance.series,
      'seriesName': instance.seriesName,
      'dataset': ?instance.dataset,
      'unit': instance.unit,
      'frequency': _$SeriesInfoFrequencyEnumEnumMap[instance.frequency]!,
      'area': ?instance.area,
      'latestPeriod': ?instance.latestPeriod,
      'latestValue': ?instance.latestValue,
      'status': ?_$SeriesInfoStatusEnumEnumMap[instance.status],
    };

const _$SeriesInfoFrequencyEnumEnumMap = {
  SeriesInfoFrequencyEnum.PT15S: 'PT15S',
  SeriesInfoFrequencyEnum.PT15M: 'PT15M',
  SeriesInfoFrequencyEnum.PT30M: 'PT30M',
  SeriesInfoFrequencyEnum.PT1H: 'PT1H',
  SeriesInfoFrequencyEnum.P1D: 'P1D',
  SeriesInfoFrequencyEnum.P1W: 'P1W',
  SeriesInfoFrequencyEnum.P1M: 'P1M',
  SeriesInfoFrequencyEnum.P3M: 'P3M',
  SeriesInfoFrequencyEnum.P1Y: 'P1Y',
};

const _$SeriesInfoStatusEnumEnumMap = {
  SeriesInfoStatusEnum.final_: 'final',
  SeriesInfoStatusEnum.preliminary: 'preliminary',
  SeriesInfoStatusEnum.estimated: 'estimated',
  SeriesInfoStatusEnum.forecast: 'forecast',
};
