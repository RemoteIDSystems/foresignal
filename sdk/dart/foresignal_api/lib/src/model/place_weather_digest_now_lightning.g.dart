// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_digest_now_lightning.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherDigestNowLightningCWProxy {
  PlaceWeatherDigestNowLightning status(WeatherSectionStatus status);

  PlaceWeatherDigestNowLightning asOf(DateTime? asOf);

  PlaceWeatherDigestNowLightning statusText(String? statusText);

  PlaceWeatherDigestNowLightning nearestKm15m(num? nearestKm15m);

  PlaceWeatherDigestNowLightning count15km15m(int? count15km15m);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNowLightning(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNowLightning(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherDigestNowLightning call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    num? nearestKm15m,
    int? count15km15m,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherDigestNowLightning.copyWith(...)` or call `instanceOfPlaceWeatherDigestNowLightning.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherDigestNowLightningCWProxyImpl
    implements _$PlaceWeatherDigestNowLightningCWProxy {
  const _$PlaceWeatherDigestNowLightningCWProxyImpl(this._value);

  final PlaceWeatherDigestNowLightning _value;

  @override
  PlaceWeatherDigestNowLightning status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  PlaceWeatherDigestNowLightning asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherDigestNowLightning statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherDigestNowLightning nearestKm15m(num? nearestKm15m) =>
      call(nearestKm15m: nearestKm15m);

  @override
  PlaceWeatherDigestNowLightning count15km15m(int? count15km15m) =>
      call(count15km15m: count15km15m);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNowLightning(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNowLightning(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherDigestNowLightning call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? nearestKm15m = const $CopyWithPlaceholder(),
    Object? count15km15m = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherDigestNowLightning(
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
      count15km15m: count15km15m == const $CopyWithPlaceholder()
          ? _value.count15km15m
          // ignore: cast_nullable_to_non_nullable
          : count15km15m as int?,
    );
  }
}

extension $PlaceWeatherDigestNowLightningCopyWith
    on PlaceWeatherDigestNowLightning {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherDigestNowLightning.copyWith(...)` or `instanceOfPlaceWeatherDigestNowLightning.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherDigestNowLightningCWProxy get copyWith =>
      _$PlaceWeatherDigestNowLightningCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherDigestNowLightning _$PlaceWeatherDigestNowLightningFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherDigestNowLightning', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = PlaceWeatherDigestNowLightning(
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
    count15km15m: $checkedConvert('count15km15m', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherDigestNowLightningToJson(
  PlaceWeatherDigestNowLightning instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'nearestKm15m': ?instance.nearestKm15m,
  'count15km15m': ?instance.count15km15m,
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
