// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_smoke.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherSmokeCWProxy {
  PlaceWeatherSmoke status(WeatherSectionStatus status);

  PlaceWeatherSmoke asOf(DateTime? asOf);

  PlaceWeatherSmoke statusText(String? statusText);

  PlaceWeatherSmoke hrrrPm25Max24h(num? hrrrPm25Max24h);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherSmoke(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherSmoke(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherSmoke call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    num? hrrrPm25Max24h,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherSmoke.copyWith(...)` or call `instanceOfPlaceWeatherSmoke.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherSmokeCWProxyImpl implements _$PlaceWeatherSmokeCWProxy {
  const _$PlaceWeatherSmokeCWProxyImpl(this._value);

  final PlaceWeatherSmoke _value;

  @override
  PlaceWeatherSmoke status(WeatherSectionStatus status) => call(status: status);

  @override
  PlaceWeatherSmoke asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherSmoke statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherSmoke hrrrPm25Max24h(num? hrrrPm25Max24h) =>
      call(hrrrPm25Max24h: hrrrPm25Max24h);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherSmoke(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherSmoke(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherSmoke call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? hrrrPm25Max24h = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherSmoke(
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
      hrrrPm25Max24h: hrrrPm25Max24h == const $CopyWithPlaceholder()
          ? _value.hrrrPm25Max24h
          // ignore: cast_nullable_to_non_nullable
          : hrrrPm25Max24h as num?,
    );
  }
}

extension $PlaceWeatherSmokeCopyWith on PlaceWeatherSmoke {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherSmoke.copyWith(...)` or `instanceOfPlaceWeatherSmoke.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherSmokeCWProxy get copyWith =>
      _$PlaceWeatherSmokeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherSmoke _$PlaceWeatherSmokeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlaceWeatherSmoke', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = PlaceWeatherSmoke(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        statusText: $checkedConvert('statusText', (v) => v as String?),
        hrrrPm25Max24h: $checkedConvert('hrrrPm25Max24h', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$PlaceWeatherSmokeToJson(PlaceWeatherSmoke instance) =>
    <String, dynamic>{
      'status': _$WeatherSectionStatusEnumMap[instance.status]!,
      'asOf': ?instance.asOf?.toIso8601String(),
      'statusText': ?instance.statusText,
      'hrrrPm25Max24h': ?instance.hrrrPm25Max24h,
    };

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
