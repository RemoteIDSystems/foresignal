// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_forecast_nws.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherForecastNwsCWProxy {
  PlaceWeatherForecastNws status(WeatherSectionStatus status);

  PlaceWeatherForecastNws asOf(DateTime? asOf);

  PlaceWeatherForecastNws statusText(String? statusText);

  PlaceWeatherForecastNws popMax24h(int? popMax24h);

  PlaceWeatherForecastNws qpf24hMm(num? qpf24hMm);

  PlaceWeatherForecastNws gustMaxMs24h(num? gustMaxMs24h);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherForecastNws(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherForecastNws(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherForecastNws call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    int? popMax24h,
    num? qpf24hMm,
    num? gustMaxMs24h,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherForecastNws.copyWith(...)` or call `instanceOfPlaceWeatherForecastNws.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherForecastNwsCWProxyImpl
    implements _$PlaceWeatherForecastNwsCWProxy {
  const _$PlaceWeatherForecastNwsCWProxyImpl(this._value);

  final PlaceWeatherForecastNws _value;

  @override
  PlaceWeatherForecastNws status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  PlaceWeatherForecastNws asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherForecastNws statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherForecastNws popMax24h(int? popMax24h) =>
      call(popMax24h: popMax24h);

  @override
  PlaceWeatherForecastNws qpf24hMm(num? qpf24hMm) => call(qpf24hMm: qpf24hMm);

  @override
  PlaceWeatherForecastNws gustMaxMs24h(num? gustMaxMs24h) =>
      call(gustMaxMs24h: gustMaxMs24h);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherForecastNws(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherForecastNws(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherForecastNws call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? popMax24h = const $CopyWithPlaceholder(),
    Object? qpf24hMm = const $CopyWithPlaceholder(),
    Object? gustMaxMs24h = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherForecastNws(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as WeatherSectionStatus,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      statusText: statusText == const $CopyWithPlaceholder()
          ? _value.statusText
          // ignore: cast_nullable_to_non_nullable
          : statusText as String?,
      popMax24h: popMax24h == const $CopyWithPlaceholder()
          ? _value.popMax24h
          // ignore: cast_nullable_to_non_nullable
          : popMax24h as int?,
      qpf24hMm: qpf24hMm == const $CopyWithPlaceholder()
          ? _value.qpf24hMm
          // ignore: cast_nullable_to_non_nullable
          : qpf24hMm as num?,
      gustMaxMs24h: gustMaxMs24h == const $CopyWithPlaceholder()
          ? _value.gustMaxMs24h
          // ignore: cast_nullable_to_non_nullable
          : gustMaxMs24h as num?,
    );
  }
}

extension $PlaceWeatherForecastNwsCopyWith on PlaceWeatherForecastNws {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherForecastNws.copyWith(...)` or `instanceOfPlaceWeatherForecastNws.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherForecastNwsCWProxy get copyWith =>
      _$PlaceWeatherForecastNwsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherForecastNws _$PlaceWeatherForecastNwsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherForecastNws', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = PlaceWeatherForecastNws(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    statusText: $checkedConvert('statusText', (v) => v as String?),
    popMax24h: $checkedConvert('popMax24h', (v) => (v as num?)?.toInt()),
    qpf24hMm: $checkedConvert('qpf24hMm', (v) => v as num?),
    gustMaxMs24h: $checkedConvert('gustMaxMs24h', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherForecastNwsToJson(
  PlaceWeatherForecastNws instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'popMax24h': ?instance.popMax24h,
  'qpf24hMm': ?instance.qpf24hMm,
  'gustMaxMs24h': ?instance.gustMaxMs24h,
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
