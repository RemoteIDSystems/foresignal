// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_history_series_inner_points_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastHistorySeriesInnerPointsInnerCWProxy {
  ForecastHistorySeriesInnerPointsInner date(DateTime date);

  ForecastHistorySeriesInnerPointsInner probability(num? probability);

  ForecastHistorySeriesInnerPointsInner distribution(
    Map<String, num>? distribution,
  );

  ForecastHistorySeriesInnerPointsInner quantiles(Quantiles? quantiles);

  ForecastHistorySeriesInnerPointsInner forecastId(String forecastId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastHistorySeriesInnerPointsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastHistorySeriesInnerPointsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastHistorySeriesInnerPointsInner call({
    DateTime date,
    num? probability,
    Map<String, num>? distribution,
    Quantiles? quantiles,
    String forecastId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastHistorySeriesInnerPointsInner.copyWith(...)` or call `instanceOfForecastHistorySeriesInnerPointsInner.copyWith.fieldName(value)` for a single field.
class _$ForecastHistorySeriesInnerPointsInnerCWProxyImpl
    implements _$ForecastHistorySeriesInnerPointsInnerCWProxy {
  const _$ForecastHistorySeriesInnerPointsInnerCWProxyImpl(this._value);

  final ForecastHistorySeriesInnerPointsInner _value;

  @override
  ForecastHistorySeriesInnerPointsInner date(DateTime date) => call(date: date);

  @override
  ForecastHistorySeriesInnerPointsInner probability(num? probability) =>
      call(probability: probability);

  @override
  ForecastHistorySeriesInnerPointsInner distribution(
    Map<String, num>? distribution,
  ) => call(distribution: distribution);

  @override
  ForecastHistorySeriesInnerPointsInner quantiles(Quantiles? quantiles) =>
      call(quantiles: quantiles);

  @override
  ForecastHistorySeriesInnerPointsInner forecastId(String forecastId) =>
      call(forecastId: forecastId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastHistorySeriesInnerPointsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastHistorySeriesInnerPointsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastHistorySeriesInnerPointsInner call({
    Object? date = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? distribution = const $CopyWithPlaceholder(),
    Object? quantiles = const $CopyWithPlaceholder(),
    Object? forecastId = const $CopyWithPlaceholder(),
  }) {
    return ForecastHistorySeriesInnerPointsInner(
      date: date == const $CopyWithPlaceholder() || date == null
          ? _value.date
          // ignore: cast_nullable_to_non_nullable
          : date as DateTime,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      distribution: distribution == const $CopyWithPlaceholder()
          ? _value.distribution
          // ignore: cast_nullable_to_non_nullable
          : distribution as Map<String, num>?,
      quantiles: quantiles == const $CopyWithPlaceholder()
          ? _value.quantiles
          // ignore: cast_nullable_to_non_nullable
          : quantiles as Quantiles?,
      forecastId:
          forecastId == const $CopyWithPlaceholder() || forecastId == null
          ? _value.forecastId
          // ignore: cast_nullable_to_non_nullable
          : forecastId as String,
    );
  }
}

extension $ForecastHistorySeriesInnerPointsInnerCopyWith
    on ForecastHistorySeriesInnerPointsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastHistorySeriesInnerPointsInner.copyWith(...)` or `instanceOfForecastHistorySeriesInnerPointsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastHistorySeriesInnerPointsInnerCWProxy get copyWith =>
      _$ForecastHistorySeriesInnerPointsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastHistorySeriesInnerPointsInner
_$ForecastHistorySeriesInnerPointsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ForecastHistorySeriesInnerPointsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['date', 'forecastId']);
      final val = ForecastHistorySeriesInnerPointsInner(
        date: $checkedConvert('date', (v) => DateTime.parse(v as String)),
        probability: $checkedConvert('probability', (v) => v as num?),
        distribution: $checkedConvert(
          'distribution',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as num),
          ),
        ),
        quantiles: $checkedConvert(
          'quantiles',
          (v) =>
              v == null ? null : Quantiles.fromJson(v as Map<String, dynamic>),
        ),
        forecastId: $checkedConvert('forecastId', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$ForecastHistorySeriesInnerPointsInnerToJson(
  ForecastHistorySeriesInnerPointsInner instance,
) => <String, dynamic>{
  'date': instance.date.toIso8601String(),
  'probability': ?instance.probability,
  'distribution': ?instance.distribution,
  'quantiles': ?instance.quantiles?.toJson(),
  'forecastId': instance.forecastId,
};
