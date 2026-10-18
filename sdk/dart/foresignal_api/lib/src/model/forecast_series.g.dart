// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_series.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastSeriesCWProxy {
  ForecastSeries provider(ForecastSeriesProviderEnum provider);

  ForecastSeries run(String? run);

  ForecastSeries updatedAt(DateTime updatedAt);

  ForecastSeries validFrom(DateTime? validFrom);

  ForecastSeries status(WeatherSectionStatus? status);

  ForecastSeries items(List<ForecastSeriesPoint> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastSeries call({
    ForecastSeriesProviderEnum provider,
    String? run,
    DateTime updatedAt,
    DateTime? validFrom,
    WeatherSectionStatus? status,
    List<ForecastSeriesPoint> items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastSeries.copyWith(...)` or call `instanceOfForecastSeries.copyWith.fieldName(value)` for a single field.
class _$ForecastSeriesCWProxyImpl implements _$ForecastSeriesCWProxy {
  const _$ForecastSeriesCWProxyImpl(this._value);

  final ForecastSeries _value;

  @override
  ForecastSeries provider(ForecastSeriesProviderEnum provider) =>
      call(provider: provider);

  @override
  ForecastSeries run(String? run) => call(run: run);

  @override
  ForecastSeries updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  @override
  ForecastSeries validFrom(DateTime? validFrom) => call(validFrom: validFrom);

  @override
  ForecastSeries status(WeatherSectionStatus? status) => call(status: status);

  @override
  ForecastSeries items(List<ForecastSeriesPoint> items) => call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastSeries(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastSeries(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastSeries call({
    Object? provider = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return ForecastSeries(
      provider: provider == const $CopyWithPlaceholder() || provider == null
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as ForecastSeriesProviderEnum,
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
      validFrom: validFrom == const $CopyWithPlaceholder()
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as WeatherSectionStatus?,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ForecastSeriesPoint>,
    );
  }
}

extension $ForecastSeriesCopyWith on ForecastSeries {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastSeries.copyWith(...)` or `instanceOfForecastSeries.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastSeriesCWProxy get copyWith => _$ForecastSeriesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastSeries _$ForecastSeriesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastSeries', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['provider', 'updatedAt', 'items']);
  final val = ForecastSeries(
    provider: $checkedConvert(
      'provider',
      (v) => $enumDecode(_$ForecastSeriesProviderEnumEnumMap, v),
    ),
    run: $checkedConvert('run', (v) => v as String?),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
    validFrom: $checkedConvert(
      'validFrom',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecodeNullable(_$WeatherSectionStatusEnumMap, v),
    ),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => ForecastSeriesPoint.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ForecastSeriesToJson(ForecastSeries instance) =>
    <String, dynamic>{
      'provider': _$ForecastSeriesProviderEnumEnumMap[instance.provider]!,
      'run': ?instance.run,
      'updatedAt': instance.updatedAt.toIso8601String(),
      'validFrom': ?instance.validFrom?.toIso8601String(),
      'status': ?_$WeatherSectionStatusEnumMap[instance.status],
      'items': instance.items.map((e) => e.toJson()).toList(),
    };

const _$ForecastSeriesProviderEnumEnumMap = {
  ForecastSeriesProviderEnum.gfs: 'gfs',
  ForecastSeriesProviderEnum.hrrr: 'hrrr',
  ForecastSeriesProviderEnum.ifs: 'ifs',
  ForecastSeriesProviderEnum.aifs: 'aifs',
  ForecastSeriesProviderEnum.nws: 'nws',
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
