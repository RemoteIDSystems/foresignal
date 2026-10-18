// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alert_context.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AlertContextCWProxy {
  AlertContext weather(WeatherContext? weather);

  AlertContext near(AlertContextNear? near);

  AlertContext anomaly(AnomalyObservation? anomaly);

  AlertContext anchor(AlertContextAnchor? anchor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertContext(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertContext(...).copyWith(id: 12, name: "My name")
  /// ```
  AlertContext call({
    WeatherContext? weather,
    AlertContextNear? near,
    AnomalyObservation? anomaly,
    AlertContextAnchor? anchor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAlertContext.copyWith(...)` or call `instanceOfAlertContext.copyWith.fieldName(value)` for a single field.
class _$AlertContextCWProxyImpl implements _$AlertContextCWProxy {
  const _$AlertContextCWProxyImpl(this._value);

  final AlertContext _value;

  @override
  AlertContext weather(WeatherContext? weather) => call(weather: weather);

  @override
  AlertContext near(AlertContextNear? near) => call(near: near);

  @override
  AlertContext anomaly(AnomalyObservation? anomaly) => call(anomaly: anomaly);

  @override
  AlertContext anchor(AlertContextAnchor? anchor) => call(anchor: anchor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertContext(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertContext(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AlertContext call({
    Object? weather = const $CopyWithPlaceholder(),
    Object? near = const $CopyWithPlaceholder(),
    Object? anomaly = const $CopyWithPlaceholder(),
    Object? anchor = const $CopyWithPlaceholder(),
  }) {
    return AlertContext(
      weather: weather == const $CopyWithPlaceholder()
          ? _value.weather
          // ignore: cast_nullable_to_non_nullable
          : weather as WeatherContext?,
      near: near == const $CopyWithPlaceholder()
          ? _value.near
          // ignore: cast_nullable_to_non_nullable
          : near as AlertContextNear?,
      anomaly: anomaly == const $CopyWithPlaceholder()
          ? _value.anomaly
          // ignore: cast_nullable_to_non_nullable
          : anomaly as AnomalyObservation?,
      anchor: anchor == const $CopyWithPlaceholder()
          ? _value.anchor
          // ignore: cast_nullable_to_non_nullable
          : anchor as AlertContextAnchor?,
    );
  }
}

extension $AlertContextCopyWith on AlertContext {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAlertContext.copyWith(...)` or `instanceOfAlertContext.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AlertContextCWProxy get copyWith => _$AlertContextCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AlertContext _$AlertContextFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AlertContext', json, ($checkedConvert) {
      final val = AlertContext(
        weather: $checkedConvert(
          'weather',
          (v) => v == null
              ? null
              : WeatherContext.fromJson(v as Map<String, dynamic>),
        ),
        near: $checkedConvert(
          'near',
          (v) => v == null
              ? null
              : AlertContextNear.fromJson(v as Map<String, dynamic>),
        ),
        anomaly: $checkedConvert(
          'anomaly',
          (v) => v == null
              ? null
              : AnomalyObservation.fromJson(v as Map<String, dynamic>),
        ),
        anchor: $checkedConvert(
          'anchor',
          (v) => v == null
              ? null
              : AlertContextAnchor.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$AlertContextToJson(AlertContext instance) =>
    <String, dynamic>{
      'weather': ?instance.weather?.toJson(),
      'near': ?instance.near?.toJson(),
      'anomaly': ?instance.anomaly?.toJson(),
      'anchor': ?instance.anchor?.toJson(),
    };
