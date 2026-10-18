// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_digest_now_alerts_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherDigestNowAlertsStatusCWProxy {
  PlaceWeatherDigestNowAlertsStatus status(WeatherSectionStatus status);

  PlaceWeatherDigestNowAlertsStatus asOf(DateTime? asOf);

  PlaceWeatherDigestNowAlertsStatus statusText(String? statusText);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNowAlertsStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNowAlertsStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherDigestNowAlertsStatus call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherDigestNowAlertsStatus.copyWith(...)` or call `instanceOfPlaceWeatherDigestNowAlertsStatus.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherDigestNowAlertsStatusCWProxyImpl
    implements _$PlaceWeatherDigestNowAlertsStatusCWProxy {
  const _$PlaceWeatherDigestNowAlertsStatusCWProxyImpl(this._value);

  final PlaceWeatherDigestNowAlertsStatus _value;

  @override
  PlaceWeatherDigestNowAlertsStatus status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  PlaceWeatherDigestNowAlertsStatus asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  PlaceWeatherDigestNowAlertsStatus statusText(String? statusText) =>
      call(statusText: statusText);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNowAlertsStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNowAlertsStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherDigestNowAlertsStatus call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherDigestNowAlertsStatus(
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
    );
  }
}

extension $PlaceWeatherDigestNowAlertsStatusCopyWith
    on PlaceWeatherDigestNowAlertsStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherDigestNowAlertsStatus.copyWith(...)` or `instanceOfPlaceWeatherDigestNowAlertsStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherDigestNowAlertsStatusCWProxy get copyWith =>
      _$PlaceWeatherDigestNowAlertsStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherDigestNowAlertsStatus _$PlaceWeatherDigestNowAlertsStatusFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherDigestNowAlertsStatus', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['status']);
  final val = PlaceWeatherDigestNowAlertsStatus(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
    ),
    asOf: $checkedConvert(
      'asOf',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    statusText: $checkedConvert('statusText', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherDigestNowAlertsStatusToJson(
  PlaceWeatherDigestNowAlertsStatus instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
};

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
