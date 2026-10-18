// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_precip.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherPrecipCWProxy {
  PlaceWeatherPrecip status(WeatherSectionStatus status);

  PlaceWeatherPrecip asOf(DateTime? asOf);

  PlaceWeatherPrecip statusText(String? statusText);

  PlaceWeatherPrecip rateMmH(num? rateMmH);

  PlaceWeatherPrecip qpe1hMmMax(num? qpe1hMmMax);

  PlaceWeatherPrecip qpe24hMmMax(num? qpe24hMmMax);

  PlaceWeatherPrecip ffgRatio1hPctMax(num? ffgRatio1hPctMax);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherPrecip(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherPrecip(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherPrecip call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    num? rateMmH,
    num? qpe1hMmMax,
    num? qpe24hMmMax,
    num? ffgRatio1hPctMax,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherPrecip.copyWith(...)` or call `instanceOfPlaceWeatherPrecip.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherPrecipCWProxyImpl implements _$PlaceWeatherPrecipCWProxy {
  const _$PlaceWeatherPrecipCWProxyImpl(this._value);

  final PlaceWeatherPrecip _value;

  @override
  PlaceWeatherPrecip status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  PlaceWeatherPrecip asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherPrecip statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherPrecip rateMmH(num? rateMmH) => call(rateMmH: rateMmH);

  @override
  PlaceWeatherPrecip qpe1hMmMax(num? qpe1hMmMax) =>
      call(qpe1hMmMax: qpe1hMmMax);

  @override
  PlaceWeatherPrecip qpe24hMmMax(num? qpe24hMmMax) =>
      call(qpe24hMmMax: qpe24hMmMax);

  @override
  PlaceWeatherPrecip ffgRatio1hPctMax(num? ffgRatio1hPctMax) =>
      call(ffgRatio1hPctMax: ffgRatio1hPctMax);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherPrecip(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherPrecip(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherPrecip call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? rateMmH = const $CopyWithPlaceholder(),
    Object? qpe1hMmMax = const $CopyWithPlaceholder(),
    Object? qpe24hMmMax = const $CopyWithPlaceholder(),
    Object? ffgRatio1hPctMax = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherPrecip(
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
      rateMmH: rateMmH == const $CopyWithPlaceholder()
          ? _value.rateMmH
          // ignore: cast_nullable_to_non_nullable
          : rateMmH as num?,
      qpe1hMmMax: qpe1hMmMax == const $CopyWithPlaceholder()
          ? _value.qpe1hMmMax
          // ignore: cast_nullable_to_non_nullable
          : qpe1hMmMax as num?,
      qpe24hMmMax: qpe24hMmMax == const $CopyWithPlaceholder()
          ? _value.qpe24hMmMax
          // ignore: cast_nullable_to_non_nullable
          : qpe24hMmMax as num?,
      ffgRatio1hPctMax: ffgRatio1hPctMax == const $CopyWithPlaceholder()
          ? _value.ffgRatio1hPctMax
          // ignore: cast_nullable_to_non_nullable
          : ffgRatio1hPctMax as num?,
    );
  }
}

extension $PlaceWeatherPrecipCopyWith on PlaceWeatherPrecip {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherPrecip.copyWith(...)` or `instanceOfPlaceWeatherPrecip.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherPrecipCWProxy get copyWith =>
      _$PlaceWeatherPrecipCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherPrecip _$PlaceWeatherPrecipFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlaceWeatherPrecip', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = PlaceWeatherPrecip(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        statusText: $checkedConvert('statusText', (v) => v as String?),
        rateMmH: $checkedConvert('rateMmH', (v) => v as num?),
        qpe1hMmMax: $checkedConvert('qpe1hMmMax', (v) => v as num?),
        qpe24hMmMax: $checkedConvert('qpe24hMmMax', (v) => v as num?),
        ffgRatio1hPctMax: $checkedConvert('ffgRatio1hPctMax', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$PlaceWeatherPrecipToJson(PlaceWeatherPrecip instance) =>
    <String, dynamic>{
      'status': _$WeatherSectionStatusEnumMap[instance.status]!,
      'asOf': ?instance.asOf?.toIso8601String(),
      'statusText': ?instance.statusText,
      'rateMmH': ?instance.rateMmH,
      'qpe1hMmMax': ?instance.qpe1hMmMax,
      'qpe24hMmMax': ?instance.qpe24hMmMax,
      'ffgRatio1hPctMax': ?instance.ffgRatio1hPctMax,
    };

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
