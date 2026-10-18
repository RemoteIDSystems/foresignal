// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_digest_next_gfs.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherDigestNextGfsCWProxy {
  PlaceWeatherDigestNextGfs status(WeatherSectionStatus status);

  PlaceWeatherDigestNextGfs asOf(DateTime? asOf);

  PlaceWeatherDigestNextGfs statusText(String? statusText);

  PlaceWeatherDigestNextGfs run(String? run);

  PlaceWeatherDigestNextGfs gustMaxMs(num? gustMaxMs);

  PlaceWeatherDigestNextGfs precipMm(num? precipMm);

  PlaceWeatherDigestNextGfs validTime(DateTime? validTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNextGfs(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNextGfs(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherDigestNextGfs call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    String? run,
    num? gustMaxMs,
    num? precipMm,
    DateTime? validTime,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherDigestNextGfs.copyWith(...)` or call `instanceOfPlaceWeatherDigestNextGfs.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherDigestNextGfsCWProxyImpl
    implements _$PlaceWeatherDigestNextGfsCWProxy {
  const _$PlaceWeatherDigestNextGfsCWProxyImpl(this._value);

  final PlaceWeatherDigestNextGfs _value;

  @override
  PlaceWeatherDigestNextGfs status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  PlaceWeatherDigestNextGfs asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherDigestNextGfs statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherDigestNextGfs run(String? run) => call(run: run);

  @override
  PlaceWeatherDigestNextGfs gustMaxMs(num? gustMaxMs) =>
      call(gustMaxMs: gustMaxMs);

  @override
  PlaceWeatherDigestNextGfs precipMm(num? precipMm) => call(precipMm: precipMm);

  @override
  PlaceWeatherDigestNextGfs validTime(DateTime? validTime) =>
      call(validTime: validTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNextGfs(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNextGfs(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherDigestNextGfs call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? gustMaxMs = const $CopyWithPlaceholder(),
    Object? precipMm = const $CopyWithPlaceholder(),
    Object? validTime = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherDigestNextGfs(
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
      validTime: validTime == const $CopyWithPlaceholder()
          ? _value.validTime
          // ignore: cast_nullable_to_non_nullable
          : validTime as DateTime?,
    );
  }
}

extension $PlaceWeatherDigestNextGfsCopyWith on PlaceWeatherDigestNextGfs {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherDigestNextGfs.copyWith(...)` or `instanceOfPlaceWeatherDigestNextGfs.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherDigestNextGfsCWProxy get copyWith =>
      _$PlaceWeatherDigestNextGfsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherDigestNextGfs _$PlaceWeatherDigestNextGfsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherDigestNextGfs', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = PlaceWeatherDigestNextGfs(
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
    validTime: $checkedConvert(
      'validTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherDigestNextGfsToJson(
  PlaceWeatherDigestNextGfs instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'run': ?instance.run,
  'gustMaxMs': ?instance.gustMaxMs,
  'precipMm': ?instance.precipMm,
  'validTime': ?instance.validTime?.toIso8601String(),
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
