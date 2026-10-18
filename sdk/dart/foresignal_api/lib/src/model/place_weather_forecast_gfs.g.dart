// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_forecast_gfs.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherForecastGfsCWProxy {
  PlaceWeatherForecastGfs status(WeatherSectionStatus? status);

  PlaceWeatherForecastGfs asOf(DateTime? asOf);

  PlaceWeatherForecastGfs statusText(String? statusText);

  PlaceWeatherForecastGfs gustMax24h(num? gustMax24h);

  PlaceWeatherForecastGfs precip24hMm(num? precip24hMm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherForecastGfs(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherForecastGfs(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherForecastGfs call({
    WeatherSectionStatus? status,
    DateTime? asOf,
    String? statusText,
    num? gustMax24h,
    num? precip24hMm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherForecastGfs.copyWith(...)` or call `instanceOfPlaceWeatherForecastGfs.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherForecastGfsCWProxyImpl
    implements _$PlaceWeatherForecastGfsCWProxy {
  const _$PlaceWeatherForecastGfsCWProxyImpl(this._value);

  final PlaceWeatherForecastGfs _value;

  @override
  PlaceWeatherForecastGfs status(WeatherSectionStatus? status) =>
      call(status: status);

  @override
  PlaceWeatherForecastGfs asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherForecastGfs statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherForecastGfs gustMax24h(num? gustMax24h) =>
      call(gustMax24h: gustMax24h);

  @override
  PlaceWeatherForecastGfs precip24hMm(num? precip24hMm) =>
      call(precip24hMm: precip24hMm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherForecastGfs(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherForecastGfs(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherForecastGfs call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? gustMax24h = const $CopyWithPlaceholder(),
    Object? precip24hMm = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherForecastGfs(
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as WeatherSectionStatus?,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      statusText: statusText == const $CopyWithPlaceholder()
          ? _value.statusText
          // ignore: cast_nullable_to_non_nullable
          : statusText as String?,
      gustMax24h: gustMax24h == const $CopyWithPlaceholder()
          ? _value.gustMax24h
          // ignore: cast_nullable_to_non_nullable
          : gustMax24h as num?,
      precip24hMm: precip24hMm == const $CopyWithPlaceholder()
          ? _value.precip24hMm
          // ignore: cast_nullable_to_non_nullable
          : precip24hMm as num?,
    );
  }
}

extension $PlaceWeatherForecastGfsCopyWith on PlaceWeatherForecastGfs {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherForecastGfs.copyWith(...)` or `instanceOfPlaceWeatherForecastGfs.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherForecastGfsCWProxy get copyWith =>
      _$PlaceWeatherForecastGfsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherForecastGfs _$PlaceWeatherForecastGfsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherForecastGfs', json, ($checkedConvert) {
  final val = PlaceWeatherForecastGfs(
    status: $checkedConvert(
      'status',
      (v) => $enumDecodeNullable(_$WeatherSectionStatusEnumMap, v),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    statusText: $checkedConvert('statusText', (v) => v as String?),
    gustMax24h: $checkedConvert('gustMax24h', (v) => v as num?),
    precip24hMm: $checkedConvert('precip24hMm', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherForecastGfsToJson(
  PlaceWeatherForecastGfs instance,
) => <String, dynamic>{
  'status': ?_$WeatherSectionStatusEnumMap[instance.status],
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'gustMax24h': ?instance.gustMax24h,
  'precip24hMm': ?instance.precip24hMm,
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
