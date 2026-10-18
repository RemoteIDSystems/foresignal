// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_forecast.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherForecastCWProxy {
  PlaceWeatherForecast gfs(PlaceWeatherForecastGfs? gfs);

  PlaceWeatherForecast nws(PlaceWeatherForecastNws? nws);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherForecast(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherForecast(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherForecast call({
    PlaceWeatherForecastGfs? gfs,
    PlaceWeatherForecastNws? nws,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherForecast.copyWith(...)` or call `instanceOfPlaceWeatherForecast.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherForecastCWProxyImpl
    implements _$PlaceWeatherForecastCWProxy {
  const _$PlaceWeatherForecastCWProxyImpl(this._value);

  final PlaceWeatherForecast _value;

  @override
  PlaceWeatherForecast gfs(PlaceWeatherForecastGfs? gfs) => call(gfs: gfs);

  @override
  PlaceWeatherForecast nws(PlaceWeatherForecastNws? nws) => call(nws: nws);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherForecast(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherForecast(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherForecast call({
    Object? gfs = const $CopyWithPlaceholder(),
    Object? nws = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherForecast(
      gfs: gfs == const $CopyWithPlaceholder()
          ? _value.gfs
          // ignore: cast_nullable_to_non_nullable
          : gfs as PlaceWeatherForecastGfs?,
      nws: nws == const $CopyWithPlaceholder()
          ? _value.nws
          // ignore: cast_nullable_to_non_nullable
          : nws as PlaceWeatherForecastNws?,
    );
  }
}

extension $PlaceWeatherForecastCopyWith on PlaceWeatherForecast {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherForecast.copyWith(...)` or `instanceOfPlaceWeatherForecast.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherForecastCWProxy get copyWith =>
      _$PlaceWeatherForecastCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherForecast _$PlaceWeatherForecastFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherForecast', json, ($checkedConvert) {
  final val = PlaceWeatherForecast(
    gfs: $checkedConvert(
      'gfs',
      (v) => v == null
          ? null
          : PlaceWeatherForecastGfs.fromJson(v as Map<String, dynamic>),
    ),
    nws: $checkedConvert(
      'nws',
      (v) => v == null
          ? null
          : PlaceWeatherForecastNws.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherForecastToJson(
  PlaceWeatherForecast instance,
) => <String, dynamic>{
  'gfs': ?instance.gfs?.toJson(),
  'nws': ?instance.nws?.toJson(),
};
