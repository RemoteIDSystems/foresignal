// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_lightning.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherLightningCWProxy {
  PlaceWeatherLightning status(WeatherSectionStatus status);

  PlaceWeatherLightning asOf(DateTime? asOf);

  PlaceWeatherLightning statusText(String? statusText);

  PlaceWeatherLightning nearestKm15m(num? nearestKm15m);

  PlaceWeatherLightning nearestKm30m(num? nearestKm30m);

  PlaceWeatherLightning count15km15m(int? count15km15m);

  PlaceWeatherLightning verifiedNearestKm30m(num? verifiedNearestKm30m);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherLightning(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherLightning(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherLightning call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    num? nearestKm15m,
    num? nearestKm30m,
    int? count15km15m,
    num? verifiedNearestKm30m,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherLightning.copyWith(...)` or call `instanceOfPlaceWeatherLightning.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherLightningCWProxyImpl
    implements _$PlaceWeatherLightningCWProxy {
  const _$PlaceWeatherLightningCWProxyImpl(this._value);

  final PlaceWeatherLightning _value;

  @override
  PlaceWeatherLightning status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  PlaceWeatherLightning asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherLightning statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherLightning nearestKm15m(num? nearestKm15m) =>
      call(nearestKm15m: nearestKm15m);

  @override
  PlaceWeatherLightning nearestKm30m(num? nearestKm30m) =>
      call(nearestKm30m: nearestKm30m);

  @override
  PlaceWeatherLightning count15km15m(int? count15km15m) =>
      call(count15km15m: count15km15m);

  @override
  PlaceWeatherLightning verifiedNearestKm30m(num? verifiedNearestKm30m) =>
      call(verifiedNearestKm30m: verifiedNearestKm30m);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherLightning(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherLightning(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherLightning call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? nearestKm15m = const $CopyWithPlaceholder(),
    Object? nearestKm30m = const $CopyWithPlaceholder(),
    Object? count15km15m = const $CopyWithPlaceholder(),
    Object? verifiedNearestKm30m = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherLightning(
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
      nearestKm15m: nearestKm15m == const $CopyWithPlaceholder()
          ? _value.nearestKm15m
          // ignore: cast_nullable_to_non_nullable
          : nearestKm15m as num?,
      nearestKm30m: nearestKm30m == const $CopyWithPlaceholder()
          ? _value.nearestKm30m
          // ignore: cast_nullable_to_non_nullable
          : nearestKm30m as num?,
      count15km15m: count15km15m == const $CopyWithPlaceholder()
          ? _value.count15km15m
          // ignore: cast_nullable_to_non_nullable
          : count15km15m as int?,
      verifiedNearestKm30m: verifiedNearestKm30m == const $CopyWithPlaceholder()
          ? _value.verifiedNearestKm30m
          // ignore: cast_nullable_to_non_nullable
          : verifiedNearestKm30m as num?,
    );
  }
}

extension $PlaceWeatherLightningCopyWith on PlaceWeatherLightning {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherLightning.copyWith(...)` or `instanceOfPlaceWeatherLightning.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherLightningCWProxy get copyWith =>
      _$PlaceWeatherLightningCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherLightning _$PlaceWeatherLightningFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherLightning', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = PlaceWeatherLightning(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    statusText: $checkedConvert('statusText', (v) => v as String?),
    nearestKm15m: $checkedConvert('nearestKm15m', (v) => v as num?),
    nearestKm30m: $checkedConvert('nearestKm30m', (v) => v as num?),
    count15km15m: $checkedConvert('count15km15m', (v) => (v as num?)?.toInt()),
    verifiedNearestKm30m: $checkedConvert(
      'verifiedNearestKm30m',
      (v) => v as num?,
    ),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherLightningToJson(
  PlaceWeatherLightning instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'nearestKm15m': ?instance.nearestKm15m,
  'nearestKm30m': ?instance.nearestKm30m,
  'count15km15m': ?instance.count15km15m,
  'verifiedNearestKm30m': ?instance.verifiedNearestKm30m,
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
