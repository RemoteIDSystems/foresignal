// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series_forecast_model_backtest.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesForecastModelBacktestCWProxy {
  SeriesForecastModelBacktest origins(int origins);

  SeriesForecastModelBacktest spacing(String? spacing);

  SeriesForecastModelBacktest selectionHorizon(int? selectionHorizon);

  SeriesForecastModelBacktest results(
    List<SeriesForecastModelBacktestResultsInner> results,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesForecastModelBacktest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesForecastModelBacktest(...).copyWith(id: 12, name: "My name")
  /// ```
  SeriesForecastModelBacktest call({
    int origins,
    String? spacing,
    int? selectionHorizon,
    List<SeriesForecastModelBacktestResultsInner> results,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeriesForecastModelBacktest.copyWith(...)` or call `instanceOfSeriesForecastModelBacktest.copyWith.fieldName(value)` for a single field.
class _$SeriesForecastModelBacktestCWProxyImpl
    implements _$SeriesForecastModelBacktestCWProxy {
  const _$SeriesForecastModelBacktestCWProxyImpl(this._value);

  final SeriesForecastModelBacktest _value;

  @override
  SeriesForecastModelBacktest origins(int origins) => call(origins: origins);

  @override
  SeriesForecastModelBacktest spacing(String? spacing) =>
      call(spacing: spacing);

  @override
  SeriesForecastModelBacktest selectionHorizon(int? selectionHorizon) =>
      call(selectionHorizon: selectionHorizon);

  @override
  SeriesForecastModelBacktest results(
    List<SeriesForecastModelBacktestResultsInner> results,
  ) => call(results: results);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesForecastModelBacktest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesForecastModelBacktest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SeriesForecastModelBacktest call({
    Object? origins = const $CopyWithPlaceholder(),
    Object? spacing = const $CopyWithPlaceholder(),
    Object? selectionHorizon = const $CopyWithPlaceholder(),
    Object? results = const $CopyWithPlaceholder(),
  }) {
    return SeriesForecastModelBacktest(
      origins: origins == const $CopyWithPlaceholder() || origins == null
          ? _value.origins
          // ignore: cast_nullable_to_non_nullable
          : origins as int,
      spacing: spacing == const $CopyWithPlaceholder()
          ? _value.spacing
          // ignore: cast_nullable_to_non_nullable
          : spacing as String?,
      selectionHorizon: selectionHorizon == const $CopyWithPlaceholder()
          ? _value.selectionHorizon
          // ignore: cast_nullable_to_non_nullable
          : selectionHorizon as int?,
      results: results == const $CopyWithPlaceholder() || results == null
          ? _value.results
          // ignore: cast_nullable_to_non_nullable
          : results as List<SeriesForecastModelBacktestResultsInner>,
    );
  }
}

extension $SeriesForecastModelBacktestCopyWith on SeriesForecastModelBacktest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeriesForecastModelBacktest.copyWith(...)` or `instanceOfSeriesForecastModelBacktest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesForecastModelBacktestCWProxy get copyWith =>
      _$SeriesForecastModelBacktestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SeriesForecastModelBacktest _$SeriesForecastModelBacktestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SeriesForecastModelBacktest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['origins', 'results']);
  final val = SeriesForecastModelBacktest(
    origins: $checkedConvert('origins', (v) => (v as num).toInt()),
    spacing: $checkedConvert('spacing', (v) => v as String?),
    selectionHorizon: $checkedConvert(
      'selectionHorizon',
      (v) => (v as num?)?.toInt(),
    ),
    results: $checkedConvert(
      'results',
      (v) => (v as List<dynamic>)
          .map(
            (e) => SeriesForecastModelBacktestResultsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SeriesForecastModelBacktestToJson(
  SeriesForecastModelBacktest instance,
) => <String, dynamic>{
  'origins': instance.origins,
  'spacing': ?instance.spacing,
  'selectionHorizon': ?instance.selectionHorizon,
  'results': instance.results.map((e) => e.toJson()).toList(),
};
