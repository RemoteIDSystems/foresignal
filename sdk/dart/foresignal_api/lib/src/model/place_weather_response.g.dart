// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherResponseCWProxy {
  PlaceWeatherResponse placeId(String placeId);

  PlaceWeatherResponse place(String? place);

  PlaceWeatherResponse timezone(String? timezone);

  PlaceWeatherResponse record(Record? record);

  PlaceWeatherResponse weather(PlaceWeather? weather);

  PlaceWeatherResponse forecastSeries(List<ForecastSeries> forecastSeries);

  PlaceWeatherResponse serverTime(DateTime serverTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherResponse call({
    String placeId,
    String? place,
    String? timezone,
    Record? record,
    PlaceWeather? weather,
    List<ForecastSeries> forecastSeries,
    DateTime serverTime,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherResponse.copyWith(...)` or call `instanceOfPlaceWeatherResponse.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherResponseCWProxyImpl
    implements _$PlaceWeatherResponseCWProxy {
  const _$PlaceWeatherResponseCWProxyImpl(this._value);

  final PlaceWeatherResponse _value;

  @override
  PlaceWeatherResponse placeId(String placeId) => call(placeId: placeId);

  @override
  PlaceWeatherResponse place(String? place) => call(place: place);

  @override
  PlaceWeatherResponse timezone(String? timezone) => call(timezone: timezone);

  @override
  PlaceWeatherResponse record(Record? record) => call(record: record);

  @override
  PlaceWeatherResponse weather(PlaceWeather? weather) => call(weather: weather);

  @override
  PlaceWeatherResponse forecastSeries(List<ForecastSeries> forecastSeries) =>
      call(forecastSeries: forecastSeries);

  @override
  PlaceWeatherResponse serverTime(DateTime serverTime) =>
      call(serverTime: serverTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherResponse call({
    Object? placeId = const $CopyWithPlaceholder(),
    Object? place = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? record = const $CopyWithPlaceholder(),
    Object? weather = const $CopyWithPlaceholder(),
    Object? forecastSeries = const $CopyWithPlaceholder(),
    Object? serverTime = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherResponse(
      placeId: placeId == const $CopyWithPlaceholder() || placeId == null
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String,
      place: place == const $CopyWithPlaceholder()
          ? _value.place
          // ignore: cast_nullable_to_non_nullable
          : place as String?,
      timezone: timezone == const $CopyWithPlaceholder()
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String?,
      record: record == const $CopyWithPlaceholder()
          ? _value.record
          // ignore: cast_nullable_to_non_nullable
          : record as Record?,
      weather: weather == const $CopyWithPlaceholder()
          ? _value.weather
          // ignore: cast_nullable_to_non_nullable
          : weather as PlaceWeather?,
      forecastSeries:
          forecastSeries == const $CopyWithPlaceholder() ||
              forecastSeries == null
          ? _value.forecastSeries
          // ignore: cast_nullable_to_non_nullable
          : forecastSeries as List<ForecastSeries>,
      serverTime:
          serverTime == const $CopyWithPlaceholder() || serverTime == null
          ? _value.serverTime
          // ignore: cast_nullable_to_non_nullable
          : serverTime as DateTime,
    );
  }
}

extension $PlaceWeatherResponseCopyWith on PlaceWeatherResponse {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherResponse.copyWith(...)` or `instanceOfPlaceWeatherResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherResponseCWProxy get copyWith =>
      _$PlaceWeatherResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherResponse _$PlaceWeatherResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherResponse', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['placeId', 'forecastSeries', 'serverTime'],
  );
  final val = PlaceWeatherResponse(
    placeId: $checkedConvert('placeId', (v) => v as String),
    place: $checkedConvert('place', (v) => v as String?),
    timezone: $checkedConvert('timezone', (v) => v as String?),
    record: $checkedConvert(
      'record',
      (v) => v == null ? null : Record.fromJson(v as Map<String, dynamic>),
    ),
    weather: $checkedConvert(
      'weather',
      (v) =>
          v == null ? null : PlaceWeather.fromJson(v as Map<String, dynamic>),
    ),
    forecastSeries: $checkedConvert(
      'forecastSeries',
      (v) => (v as List<dynamic>)
          .map((e) => ForecastSeries.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    serverTime: $checkedConvert(
      'serverTime',
      (v) => DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherResponseToJson(
  PlaceWeatherResponse instance,
) => <String, dynamic>{
  'placeId': instance.placeId,
  'place': ?instance.place,
  'timezone': ?instance.timezone,
  'record': ?instance.record?.toJson(),
  'weather': ?instance.weather?.toJson(),
  'forecastSeries': instance.forecastSeries.map((e) => e.toJson()).toList(),
  'serverTime': instance.serverTime.toIso8601String(),
};
