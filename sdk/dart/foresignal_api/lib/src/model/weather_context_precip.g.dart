// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'weather_context_precip.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WeatherContextPrecipCWProxy {
  WeatherContextPrecip status(WeatherSectionStatus status);

  WeatherContextPrecip asOf(DateTime? asOf);

  WeatherContextPrecip statusText(String? statusText);

  WeatherContextPrecip rateMmH(num? rateMmH);

  WeatherContextPrecip qpe1hMm(num? qpe1hMm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextPrecip(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextPrecip(...).copyWith(id: 12, name: "My name")
  /// ```
  WeatherContextPrecip call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    num? rateMmH,
    num? qpe1hMm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWeatherContextPrecip.copyWith(...)` or call `instanceOfWeatherContextPrecip.copyWith.fieldName(value)` for a single field.
class _$WeatherContextPrecipCWProxyImpl
    implements _$WeatherContextPrecipCWProxy {
  const _$WeatherContextPrecipCWProxyImpl(this._value);

  final WeatherContextPrecip _value;

  @override
  WeatherContextPrecip status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  WeatherContextPrecip asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  WeatherContextPrecip statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  WeatherContextPrecip rateMmH(num? rateMmH) => call(rateMmH: rateMmH);

  @override
  WeatherContextPrecip qpe1hMm(num? qpe1hMm) => call(qpe1hMm: qpe1hMm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextPrecip(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextPrecip(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WeatherContextPrecip call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? rateMmH = const $CopyWithPlaceholder(),
    Object? qpe1hMm = const $CopyWithPlaceholder(),
  }) {
    return WeatherContextPrecip(
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
      qpe1hMm: qpe1hMm == const $CopyWithPlaceholder()
          ? _value.qpe1hMm
          // ignore: cast_nullable_to_non_nullable
          : qpe1hMm as num?,
    );
  }
}

extension $WeatherContextPrecipCopyWith on WeatherContextPrecip {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWeatherContextPrecip.copyWith(...)` or `instanceOfWeatherContextPrecip.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WeatherContextPrecipCWProxy get copyWith =>
      _$WeatherContextPrecipCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WeatherContextPrecip _$WeatherContextPrecipFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('WeatherContextPrecip', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = WeatherContextPrecip(
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
    qpe1hMm: $checkedConvert('qpe1hMm', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$WeatherContextPrecipToJson(
  WeatherContextPrecip instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'rateMmH': ?instance.rateMmH,
  'qpe1hMm': ?instance.qpe1hMm,
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
