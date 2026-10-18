// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_digest_now.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherDigestNowCWProxy {
  PlaceWeatherDigestNow radar(PlaceWeatherDigestNowRadar? radar);

  PlaceWeatherDigestNow lightning(PlaceWeatherDigestNowLightning? lightning);

  PlaceWeatherDigestNow alerts(List<PlaceWeatherDigestNowAlertsInner>? alerts);

  PlaceWeatherDigestNow alertsStatus(
    PlaceWeatherDigestNowAlertsStatus? alertsStatus,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNow(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherDigestNow call({
    PlaceWeatherDigestNowRadar? radar,
    PlaceWeatherDigestNowLightning? lightning,
    List<PlaceWeatherDigestNowAlertsInner>? alerts,
    PlaceWeatherDigestNowAlertsStatus? alertsStatus,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherDigestNow.copyWith(...)` or call `instanceOfPlaceWeatherDigestNow.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherDigestNowCWProxyImpl
    implements _$PlaceWeatherDigestNowCWProxy {
  const _$PlaceWeatherDigestNowCWProxyImpl(this._value);

  final PlaceWeatherDigestNow _value;

  @override
  PlaceWeatherDigestNow radar(PlaceWeatherDigestNowRadar? radar) =>
      call(radar: radar);

  @override
  PlaceWeatherDigestNow lightning(PlaceWeatherDigestNowLightning? lightning) =>
      call(lightning: lightning);

  @override
  PlaceWeatherDigestNow alerts(
    List<PlaceWeatherDigestNowAlertsInner>? alerts,
  ) => call(alerts: alerts);

  @override
  PlaceWeatherDigestNow alertsStatus(
    PlaceWeatherDigestNowAlertsStatus? alertsStatus,
  ) => call(alertsStatus: alertsStatus);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNow(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNow(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherDigestNow call({
    Object? radar = const $CopyWithPlaceholder(),
    Object? lightning = const $CopyWithPlaceholder(),
    Object? alerts = const $CopyWithPlaceholder(),
    Object? alertsStatus = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherDigestNow(
      radar: radar == const $CopyWithPlaceholder()
          ? _value.radar
          // ignore: cast_nullable_to_non_nullable
          : radar as PlaceWeatherDigestNowRadar?,
      lightning: lightning == const $CopyWithPlaceholder()
          ? _value.lightning
          // ignore: cast_nullable_to_non_nullable
          : lightning as PlaceWeatherDigestNowLightning?,
      alerts: alerts == const $CopyWithPlaceholder()
          ? _value.alerts
          // ignore: cast_nullable_to_non_nullable
          : alerts as List<PlaceWeatherDigestNowAlertsInner>?,
      alertsStatus: alertsStatus == const $CopyWithPlaceholder()
          ? _value.alertsStatus
          // ignore: cast_nullable_to_non_nullable
          : alertsStatus as PlaceWeatherDigestNowAlertsStatus?,
    );
  }
}

extension $PlaceWeatherDigestNowCopyWith on PlaceWeatherDigestNow {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherDigestNow.copyWith(...)` or `instanceOfPlaceWeatherDigestNow.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherDigestNowCWProxy get copyWith =>
      _$PlaceWeatherDigestNowCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherDigestNow _$PlaceWeatherDigestNowFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherDigestNow', json, ($checkedConvert) {
  final val = PlaceWeatherDigestNow(
    radar: $checkedConvert(
      'radar',
      (v) => v == null
          ? null
          : PlaceWeatherDigestNowRadar.fromJson(v as Map<String, dynamic>),
    ),
    lightning: $checkedConvert(
      'lightning',
      (v) => v == null
          ? null
          : PlaceWeatherDigestNowLightning.fromJson(v as Map<String, dynamic>),
    ),
    alerts: $checkedConvert(
      'alerts',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => PlaceWeatherDigestNowAlertsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    alertsStatus: $checkedConvert(
      'alertsStatus',
      (v) => v == null
          ? null
          : PlaceWeatherDigestNowAlertsStatus.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherDigestNowToJson(
  PlaceWeatherDigestNow instance,
) => <String, dynamic>{
  'radar': ?instance.radar?.toJson(),
  'lightning': ?instance.lightning?.toJson(),
  'alerts': ?instance.alerts?.map((e) => e.toJson()).toList(),
  'alertsStatus': ?instance.alertsStatus?.toJson(),
};
