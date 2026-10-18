// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_digest_next_nws.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherDigestNextNwsCWProxy {
  PlaceWeatherDigestNextNws status(WeatherSectionStatus status);

  PlaceWeatherDigestNextNws asOf(DateTime? asOf);

  PlaceWeatherDigestNextNws statusText(String? statusText);

  PlaceWeatherDigestNextNws popMax24h(int? popMax24h);

  PlaceWeatherDigestNextNws qpf24hMm(num? qpf24hMm);

  PlaceWeatherDigestNextNws gustMaxMs24h(num? gustMaxMs24h);

  PlaceWeatherDigestNextNws tMaxC24h(num? tMaxC24h);

  PlaceWeatherDigestNextNws tMinC24h(num? tMinC24h);

  PlaceWeatherDigestNextNws updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNextNws(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNextNws(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherDigestNextNws call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    int? popMax24h,
    num? qpf24hMm,
    num? gustMaxMs24h,
    num? tMaxC24h,
    num? tMinC24h,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherDigestNextNws.copyWith(...)` or call `instanceOfPlaceWeatherDigestNextNws.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherDigestNextNwsCWProxyImpl
    implements _$PlaceWeatherDigestNextNwsCWProxy {
  const _$PlaceWeatherDigestNextNwsCWProxyImpl(this._value);

  final PlaceWeatherDigestNextNws _value;

  @override
  PlaceWeatherDigestNextNws status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  PlaceWeatherDigestNextNws asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherDigestNextNws statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  PlaceWeatherDigestNextNws popMax24h(int? popMax24h) =>
      call(popMax24h: popMax24h);

  @override
  PlaceWeatherDigestNextNws qpf24hMm(num? qpf24hMm) => call(qpf24hMm: qpf24hMm);

  @override
  PlaceWeatherDigestNextNws gustMaxMs24h(num? gustMaxMs24h) =>
      call(gustMaxMs24h: gustMaxMs24h);

  @override
  PlaceWeatherDigestNextNws tMaxC24h(num? tMaxC24h) => call(tMaxC24h: tMaxC24h);

  @override
  PlaceWeatherDigestNextNws tMinC24h(num? tMinC24h) => call(tMinC24h: tMinC24h);

  @override
  PlaceWeatherDigestNextNws updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNextNws(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNextNws(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherDigestNextNws call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? popMax24h = const $CopyWithPlaceholder(),
    Object? qpf24hMm = const $CopyWithPlaceholder(),
    Object? gustMaxMs24h = const $CopyWithPlaceholder(),
    Object? tMaxC24h = const $CopyWithPlaceholder(),
    Object? tMinC24h = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherDigestNextNws(
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
      tMaxC24h: tMaxC24h == const $CopyWithPlaceholder()
          ? _value.tMaxC24h
          // ignore: cast_nullable_to_non_nullable
          : tMaxC24h as num?,
      tMinC24h: tMinC24h == const $CopyWithPlaceholder()
          ? _value.tMinC24h
          // ignore: cast_nullable_to_non_nullable
          : tMinC24h as num?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $PlaceWeatherDigestNextNwsCopyWith on PlaceWeatherDigestNextNws {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherDigestNextNws.copyWith(...)` or `instanceOfPlaceWeatherDigestNextNws.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherDigestNextNwsCWProxy get copyWith =>
      _$PlaceWeatherDigestNextNwsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherDigestNextNws _$PlaceWeatherDigestNextNwsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherDigestNextNws', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = PlaceWeatherDigestNextNws(
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
    tMaxC24h: $checkedConvert('tMaxC24h', (v) => v as num?),
    tMinC24h: $checkedConvert('tMinC24h', (v) => v as num?),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherDigestNextNwsToJson(
  PlaceWeatherDigestNextNws instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'popMax24h': ?instance.popMax24h,
  'qpf24hMm': ?instance.qpf24hMm,
  'gustMaxMs24h': ?instance.gustMaxMs24h,
  'tMaxC24h': ?instance.tMaxC24h,
  'tMinC24h': ?instance.tMinC24h,
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
