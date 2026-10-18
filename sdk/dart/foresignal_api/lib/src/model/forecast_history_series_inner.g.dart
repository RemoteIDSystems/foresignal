// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_history_series_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastHistorySeriesInnerCWProxy {
  ForecastHistorySeriesInner forecasterId(String forecasterId);

  ForecastHistorySeriesInner points(
    List<ForecastHistorySeriesInnerPointsInner> points,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastHistorySeriesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastHistorySeriesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastHistorySeriesInner call({
    String forecasterId,
    List<ForecastHistorySeriesInnerPointsInner> points,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastHistorySeriesInner.copyWith(...)` or call `instanceOfForecastHistorySeriesInner.copyWith.fieldName(value)` for a single field.
class _$ForecastHistorySeriesInnerCWProxyImpl
    implements _$ForecastHistorySeriesInnerCWProxy {
  const _$ForecastHistorySeriesInnerCWProxyImpl(this._value);

  final ForecastHistorySeriesInner _value;

  @override
  ForecastHistorySeriesInner forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  ForecastHistorySeriesInner points(
    List<ForecastHistorySeriesInnerPointsInner> points,
  ) => call(points: points);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastHistorySeriesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastHistorySeriesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastHistorySeriesInner call({
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? points = const $CopyWithPlaceholder(),
  }) {
    return ForecastHistorySeriesInner(
      forecasterId:
          forecasterId == const $CopyWithPlaceholder() || forecasterId == null
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String,
      points: points == const $CopyWithPlaceholder() || points == null
          ? _value.points
          // ignore: cast_nullable_to_non_nullable
          : points as List<ForecastHistorySeriesInnerPointsInner>,
    );
  }
}

extension $ForecastHistorySeriesInnerCopyWith on ForecastHistorySeriesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastHistorySeriesInner.copyWith(...)` or `instanceOfForecastHistorySeriesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastHistorySeriesInnerCWProxy get copyWith =>
      _$ForecastHistorySeriesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastHistorySeriesInner _$ForecastHistorySeriesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastHistorySeriesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['forecasterId', 'points']);
  final val = ForecastHistorySeriesInner(
    forecasterId: $checkedConvert('forecasterId', (v) => v as String),
    points: $checkedConvert(
      'points',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ForecastHistorySeriesInnerPointsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ForecastHistorySeriesInnerToJson(
  ForecastHistorySeriesInner instance,
) => <String, dynamic>{
  'forecasterId': instance.forecasterId,
  'points': instance.points.map((e) => e.toJson()).toList(),
};
