// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'weather_context_radar_nearest_cell_motion.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WeatherContextRadarNearestCellMotionCWProxy {
  WeatherContextRadarNearestCellMotion speedKmh(num? speedKmh);

  WeatherContextRadarNearestCellMotion bearingDeg(int? bearingDeg);

  WeatherContextRadarNearestCellMotion method(String? method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextRadarNearestCellMotion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextRadarNearestCellMotion(...).copyWith(id: 12, name: "My name")
  /// ```
  WeatherContextRadarNearestCellMotion call({
    num? speedKmh,
    int? bearingDeg,
    String? method,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWeatherContextRadarNearestCellMotion.copyWith(...)` or call `instanceOfWeatherContextRadarNearestCellMotion.copyWith.fieldName(value)` for a single field.
class _$WeatherContextRadarNearestCellMotionCWProxyImpl
    implements _$WeatherContextRadarNearestCellMotionCWProxy {
  const _$WeatherContextRadarNearestCellMotionCWProxyImpl(this._value);

  final WeatherContextRadarNearestCellMotion _value;

  @override
  WeatherContextRadarNearestCellMotion speedKmh(num? speedKmh) =>
      call(speedKmh: speedKmh);

  @override
  WeatherContextRadarNearestCellMotion bearingDeg(int? bearingDeg) =>
      call(bearingDeg: bearingDeg);

  @override
  WeatherContextRadarNearestCellMotion method(String? method) =>
      call(method: method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextRadarNearestCellMotion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextRadarNearestCellMotion(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WeatherContextRadarNearestCellMotion call({
    Object? speedKmh = const $CopyWithPlaceholder(),
    Object? bearingDeg = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
  }) {
    return WeatherContextRadarNearestCellMotion(
      speedKmh: speedKmh == const $CopyWithPlaceholder()
          ? _value.speedKmh
          // ignore: cast_nullable_to_non_nullable
          : speedKmh as num?,
      bearingDeg: bearingDeg == const $CopyWithPlaceholder()
          ? _value.bearingDeg
          // ignore: cast_nullable_to_non_nullable
          : bearingDeg as int?,
      method: method == const $CopyWithPlaceholder()
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as String?,
    );
  }
}

extension $WeatherContextRadarNearestCellMotionCopyWith
    on WeatherContextRadarNearestCellMotion {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWeatherContextRadarNearestCellMotion.copyWith(...)` or `instanceOfWeatherContextRadarNearestCellMotion.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WeatherContextRadarNearestCellMotionCWProxy get copyWith =>
      _$WeatherContextRadarNearestCellMotionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WeatherContextRadarNearestCellMotion
_$WeatherContextRadarNearestCellMotionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WeatherContextRadarNearestCellMotion', json, (
      $checkedConvert,
    ) {
      final val = WeatherContextRadarNearestCellMotion(
        speedKmh: $checkedConvert('speedKmh', (v) => v as num?),
        bearingDeg: $checkedConvert('bearingDeg', (v) => (v as num?)?.toInt()),
        method: $checkedConvert('method', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$WeatherContextRadarNearestCellMotionToJson(
  WeatherContextRadarNearestCellMotion instance,
) => <String, dynamic>{
  'speedKmh': ?instance.speedKmh,
  'bearingDeg': ?instance.bearingDeg,
  'method': ?instance.method,
};
