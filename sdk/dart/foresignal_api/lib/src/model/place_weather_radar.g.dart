// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_radar.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherRadarCWProxy {
  PlaceWeatherRadar status(WeatherSectionStatus status);

  PlaceWeatherRadar asOf(DateTime? asOf);

  PlaceWeatherRadar statusText(String? statusText);

  PlaceWeatherRadar maxDbzInRadius(num? maxDbzInRadius);

  PlaceWeatherRadar nearest40Km(num? nearest40Km);

  PlaceWeatherRadar nearest50Km(num? nearest50Km);

  PlaceWeatherRadar nearest50Km10m(num? nearest50Km10m);

  PlaceWeatherRadar nearest60Km(num? nearest60Km);

  PlaceWeatherRadar verifiedNearest50Km10m(num? verifiedNearest50Km10m);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherRadar(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherRadar(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherRadar call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    num? maxDbzInRadius,
    num? nearest40Km,
    num? nearest50Km,
    num? nearest50Km10m,
    num? nearest60Km,
    num? verifiedNearest50Km10m,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherRadar.copyWith(...)` or call `instanceOfPlaceWeatherRadar.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherRadarCWProxyImpl implements _$PlaceWeatherRadarCWProxy {
  const _$PlaceWeatherRadarCWProxyImpl(this._value);

  final PlaceWeatherRadar _value;

  @override
  PlaceWeatherRadar status(WeatherSectionStatus status) => call(status: status);

  @override
  PlaceWeatherRadar asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherRadar statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherRadar maxDbzInRadius(num? maxDbzInRadius) =>
      call(maxDbzInRadius: maxDbzInRadius);

  @override
  PlaceWeatherRadar nearest40Km(num? nearest40Km) =>
      call(nearest40Km: nearest40Km);

  @override
  PlaceWeatherRadar nearest50Km(num? nearest50Km) =>
      call(nearest50Km: nearest50Km);

  @override
  PlaceWeatherRadar nearest50Km10m(num? nearest50Km10m) =>
      call(nearest50Km10m: nearest50Km10m);

  @override
  PlaceWeatherRadar nearest60Km(num? nearest60Km) =>
      call(nearest60Km: nearest60Km);

  @override
  PlaceWeatherRadar verifiedNearest50Km10m(num? verifiedNearest50Km10m) =>
      call(verifiedNearest50Km10m: verifiedNearest50Km10m);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherRadar(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherRadar(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherRadar call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? maxDbzInRadius = const $CopyWithPlaceholder(),
    Object? nearest40Km = const $CopyWithPlaceholder(),
    Object? nearest50Km = const $CopyWithPlaceholder(),
    Object? nearest50Km10m = const $CopyWithPlaceholder(),
    Object? nearest60Km = const $CopyWithPlaceholder(),
    Object? verifiedNearest50Km10m = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherRadar(
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
      nearest40Km: nearest40Km == const $CopyWithPlaceholder()
          ? _value.nearest40Km
          // ignore: cast_nullable_to_non_nullable
          : nearest40Km as num?,
      nearest50Km: nearest50Km == const $CopyWithPlaceholder()
          ? _value.nearest50Km
          // ignore: cast_nullable_to_non_nullable
          : nearest50Km as num?,
      nearest50Km10m: nearest50Km10m == const $CopyWithPlaceholder()
          ? _value.nearest50Km10m
          // ignore: cast_nullable_to_non_nullable
          : nearest50Km10m as num?,
      nearest60Km: nearest60Km == const $CopyWithPlaceholder()
          ? _value.nearest60Km
          // ignore: cast_nullable_to_non_nullable
          : nearest60Km as num?,
      verifiedNearest50Km10m:
          verifiedNearest50Km10m == const $CopyWithPlaceholder()
          ? _value.verifiedNearest50Km10m
          // ignore: cast_nullable_to_non_nullable
          : verifiedNearest50Km10m as num?,
    );
  }
}

extension $PlaceWeatherRadarCopyWith on PlaceWeatherRadar {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherRadar.copyWith(...)` or `instanceOfPlaceWeatherRadar.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherRadarCWProxy get copyWith =>
      _$PlaceWeatherRadarCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherRadar _$PlaceWeatherRadarFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlaceWeatherRadar', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = PlaceWeatherRadar(
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
        nearest40Km: $checkedConvert('nearest40Km', (v) => v as num?),
        nearest50Km: $checkedConvert('nearest50Km', (v) => v as num?),
        nearest50Km10m: $checkedConvert('nearest50Km10m', (v) => v as num?),
        nearest60Km: $checkedConvert('nearest60Km', (v) => v as num?),
        verifiedNearest50Km10m: $checkedConvert(
          'verifiedNearest50Km10m',
          (v) => v as num?,
        ),
      );
      return val;
    });

Map<String, dynamic> _$PlaceWeatherRadarToJson(PlaceWeatherRadar instance) =>
    <String, dynamic>{
      'status': _$WeatherSectionStatusEnumMap[instance.status]!,
      'asOf': ?instance.asOf?.toIso8601String(),
      'statusText': ?instance.statusText,
      'maxDbzInRadius': ?instance.maxDbzInRadius,
      'nearest40Km': ?instance.nearest40Km,
      'nearest50Km': ?instance.nearest50Km,
      'nearest50Km10m': ?instance.nearest50Km10m,
      'nearest60Km': ?instance.nearest60Km,
      'verifiedNearest50Km10m': ?instance.verifiedNearest50Km10m,
    };

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
