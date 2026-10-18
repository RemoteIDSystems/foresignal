// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_digest_now_alerts_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherDigestNowAlertsInnerCWProxy {
  PlaceWeatherDigestNowAlertsInner id(String id);

  PlaceWeatherDigestNowAlertsInner event(String event);

  PlaceWeatherDigestNowAlertsInner severity(String? severity);

  PlaceWeatherDigestNowAlertsInner expires(DateTime? expires);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNowAlertsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNowAlertsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherDigestNowAlertsInner call({
    String id,
    String event,
    String? severity,
    DateTime? expires,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherDigestNowAlertsInner.copyWith(...)` or call `instanceOfPlaceWeatherDigestNowAlertsInner.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherDigestNowAlertsInnerCWProxyImpl
    implements _$PlaceWeatherDigestNowAlertsInnerCWProxy {
  const _$PlaceWeatherDigestNowAlertsInnerCWProxyImpl(this._value);

  final PlaceWeatherDigestNowAlertsInner _value;

  @override
  PlaceWeatherDigestNowAlertsInner id(String id) => call(id: id);

  @override
  PlaceWeatherDigestNowAlertsInner event(String event) => call(event: event);

  @override
  PlaceWeatherDigestNowAlertsInner severity(String? severity) =>
      call(severity: severity);

  @override
  PlaceWeatherDigestNowAlertsInner expires(DateTime? expires) =>
      call(expires: expires);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNowAlertsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNowAlertsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherDigestNowAlertsInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? expires = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherDigestNowAlertsInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as String,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as String?,
      expires: expires == const $CopyWithPlaceholder()
          ? _value.expires
          // ignore: cast_nullable_to_non_nullable
          : expires as DateTime?,
    );
  }
}

extension $PlaceWeatherDigestNowAlertsInnerCopyWith
    on PlaceWeatherDigestNowAlertsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherDigestNowAlertsInner.copyWith(...)` or `instanceOfPlaceWeatherDigestNowAlertsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherDigestNowAlertsInnerCWProxy get copyWith =>
      _$PlaceWeatherDigestNowAlertsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherDigestNowAlertsInner _$PlaceWeatherDigestNowAlertsInnerFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('PlaceWeatherDigestNowAlertsInner', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'event']);
      final val = PlaceWeatherDigestNowAlertsInner(
        id: $checkedConvert('id', (v) => v as String),
        event: $checkedConvert('event', (v) => v as String),
        severity: $checkedConvert('severity', (v) => v as String?),
        expires: $checkedConvert(
          'expires',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PlaceWeatherDigestNowAlertsInnerToJson(
  PlaceWeatherDigestNowAlertsInner instance,
) => <String, dynamic>{
  'id': instance.id,
  'event': instance.event,
  'severity': ?instance.severity,
  'expires': ?instance.expires?.toIso8601String(),
};
