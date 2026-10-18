// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_digest_now_radar.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherDigestNowRadarCWProxy {
  PlaceWeatherDigestNowRadar status(WeatherSectionStatus status);

  PlaceWeatherDigestNowRadar asOf(DateTime? asOf);

  PlaceWeatherDigestNowRadar statusText(String? statusText);

  PlaceWeatherDigestNowRadar maxDbzInRadius(num? maxDbzInRadius);

  PlaceWeatherDigestNowRadar nearest50Km(num? nearest50Km);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNowRadar(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNowRadar(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherDigestNowRadar call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    num? maxDbzInRadius,
    num? nearest50Km,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherDigestNowRadar.copyWith(...)` or call `instanceOfPlaceWeatherDigestNowRadar.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherDigestNowRadarCWProxyImpl
    implements _$PlaceWeatherDigestNowRadarCWProxy {
  const _$PlaceWeatherDigestNowRadarCWProxyImpl(this._value);

  final PlaceWeatherDigestNowRadar _value;

  @override
  PlaceWeatherDigestNowRadar status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  PlaceWeatherDigestNowRadar asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherDigestNowRadar statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherDigestNowRadar maxDbzInRadius(num? maxDbzInRadius) =>
      call(maxDbzInRadius: maxDbzInRadius);

  @override
  PlaceWeatherDigestNowRadar nearest50Km(num? nearest50Km) =>
      call(nearest50Km: nearest50Km);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNowRadar(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNowRadar(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherDigestNowRadar call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? maxDbzInRadius = const $CopyWithPlaceholder(),
    Object? nearest50Km = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherDigestNowRadar(
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
      maxDbzInRadius: maxDbzInRadius == const $CopyWithPlaceholder()
          ? _value.maxDbzInRadius
          // ignore: cast_nullable_to_non_nullable
          : maxDbzInRadius as num?,
      nearest50Km: nearest50Km == const $CopyWithPlaceholder()
          ? _value.nearest50Km
          // ignore: cast_nullable_to_non_nullable
          : nearest50Km as num?,
    );
  }
}

extension $PlaceWeatherDigestNowRadarCopyWith on PlaceWeatherDigestNowRadar {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherDigestNowRadar.copyWith(...)` or `instanceOfPlaceWeatherDigestNowRadar.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherDigestNowRadarCWProxy get copyWith =>
      _$PlaceWeatherDigestNowRadarCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherDigestNowRadar _$PlaceWeatherDigestNowRadarFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherDigestNowRadar', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = PlaceWeatherDigestNowRadar(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    statusText: $checkedConvert('statusText', (v) => v as String?),
    maxDbzInRadius: $checkedConvert('maxDbzInRadius', (v) => v as num?),
    nearest50Km: $checkedConvert('nearest50Km', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherDigestNowRadarToJson(
  PlaceWeatherDigestNowRadar instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'maxDbzInRadius': ?instance.maxDbzInRadius,
  'nearest50Km': ?instance.nearest50Km,
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
