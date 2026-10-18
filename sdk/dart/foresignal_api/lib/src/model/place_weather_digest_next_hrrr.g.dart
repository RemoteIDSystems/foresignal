// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_digest_next_hrrr.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherDigestNextHrrrCWProxy {
  PlaceWeatherDigestNextHrrr status(WeatherSectionStatus status);

  PlaceWeatherDigestNextHrrr asOf(DateTime? asOf);

  PlaceWeatherDigestNextHrrr statusText(String? statusText);

  PlaceWeatherDigestNextHrrr run(String? run);

  PlaceWeatherDigestNextHrrr gustMaxMs(num? gustMaxMs);

  PlaceWeatherDigestNextHrrr precipMm(num? precipMm);

  PlaceWeatherDigestNextHrrr smokeMaxUgM3(num? smokeMaxUgM3);

  PlaceWeatherDigestNextHrrr validTime(DateTime? validTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNextHrrr(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNextHrrr(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherDigestNextHrrr call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    String? run,
    num? gustMaxMs,
    num? precipMm,
    num? smokeMaxUgM3,
    DateTime? validTime,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherDigestNextHrrr.copyWith(...)` or call `instanceOfPlaceWeatherDigestNextHrrr.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherDigestNextHrrrCWProxyImpl
    implements _$PlaceWeatherDigestNextHrrrCWProxy {
  const _$PlaceWeatherDigestNextHrrrCWProxyImpl(this._value);

  final PlaceWeatherDigestNextHrrr _value;

  @override
  PlaceWeatherDigestNextHrrr status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  PlaceWeatherDigestNextHrrr asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherDigestNextHrrr statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherDigestNextHrrr run(String? run) => call(run: run);

  @override
  PlaceWeatherDigestNextHrrr gustMaxMs(num? gustMaxMs) =>
      call(gustMaxMs: gustMaxMs);

  @override
  PlaceWeatherDigestNextHrrr precipMm(num? precipMm) =>
      call(precipMm: precipMm);

  @override
  PlaceWeatherDigestNextHrrr smokeMaxUgM3(num? smokeMaxUgM3) =>
      call(smokeMaxUgM3: smokeMaxUgM3);

  @override
  PlaceWeatherDigestNextHrrr validTime(DateTime? validTime) =>
      call(validTime: validTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNextHrrr(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNextHrrr(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherDigestNextHrrr call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? gustMaxMs = const $CopyWithPlaceholder(),
    Object? precipMm = const $CopyWithPlaceholder(),
    Object? smokeMaxUgM3 = const $CopyWithPlaceholder(),
    Object? validTime = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherDigestNextHrrr(
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
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      gustMaxMs: gustMaxMs == const $CopyWithPlaceholder()
          ? _value.gustMaxMs
          // ignore: cast_nullable_to_non_nullable
          : gustMaxMs as num?,
      precipMm: precipMm == const $CopyWithPlaceholder()
          ? _value.precipMm
          // ignore: cast_nullable_to_non_nullable
          : precipMm as num?,
      smokeMaxUgM3: smokeMaxUgM3 == const $CopyWithPlaceholder()
          ? _value.smokeMaxUgM3
          // ignore: cast_nullable_to_non_nullable
          : smokeMaxUgM3 as num?,
      validTime: validTime == const $CopyWithPlaceholder()
          ? _value.validTime
          // ignore: cast_nullable_to_non_nullable
          : validTime as DateTime?,
    );
  }
}

extension $PlaceWeatherDigestNextHrrrCopyWith on PlaceWeatherDigestNextHrrr {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherDigestNextHrrr.copyWith(...)` or `instanceOfPlaceWeatherDigestNextHrrr.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherDigestNextHrrrCWProxy get copyWith =>
      _$PlaceWeatherDigestNextHrrrCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherDigestNextHrrr _$PlaceWeatherDigestNextHrrrFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherDigestNextHrrr', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = PlaceWeatherDigestNextHrrr(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    statusText: $checkedConvert('statusText', (v) => v as String?),
    run: $checkedConvert('run', (v) => v as String?),
    gustMaxMs: $checkedConvert('gustMaxMs', (v) => v as num?),
    precipMm: $checkedConvert('precipMm', (v) => v as num?),
    smokeMaxUgM3: $checkedConvert('smokeMaxUgM3', (v) => v as num?),
    validTime: $checkedConvert(
      'validTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherDigestNextHrrrToJson(
  PlaceWeatherDigestNextHrrr instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'run': ?instance.run,
  'gustMaxMs': ?instance.gustMaxMs,
  'precipMm': ?instance.precipMm,
  'smokeMaxUgM3': ?instance.smokeMaxUgM3,
  'validTime': ?instance.validTime?.toIso8601String(),
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
