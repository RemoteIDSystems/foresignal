// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series_forecast_model_backtest_results_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesForecastModelBacktestResultsInnerCWProxy {
  SeriesForecastModelBacktestResultsInner method(
    SeriesForecastModelBacktestResultsInnerMethodEnum method,
  );

  SeriesForecastModelBacktestResultsInner horizon(int horizon);

  SeriesForecastModelBacktestResultsInner meanPinballLoss(num meanPinballLoss);

  SeriesForecastModelBacktestResultsInner intervalCoverageP05P95(
    num? intervalCoverageP05P95,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesForecastModelBacktestResultsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesForecastModelBacktestResultsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SeriesForecastModelBacktestResultsInner call({
    SeriesForecastModelBacktestResultsInnerMethodEnum method,
    int horizon,
    num meanPinballLoss,
    num? intervalCoverageP05P95,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeriesForecastModelBacktestResultsInner.copyWith(...)` or call `instanceOfSeriesForecastModelBacktestResultsInner.copyWith.fieldName(value)` for a single field.
class _$SeriesForecastModelBacktestResultsInnerCWProxyImpl
    implements _$SeriesForecastModelBacktestResultsInnerCWProxy {
  const _$SeriesForecastModelBacktestResultsInnerCWProxyImpl(this._value);

  final SeriesForecastModelBacktestResultsInner _value;

  @override
  SeriesForecastModelBacktestResultsInner method(
    SeriesForecastModelBacktestResultsInnerMethodEnum method,
  ) => call(method: method);

  @override
  SeriesForecastModelBacktestResultsInner horizon(int horizon) =>
      call(horizon: horizon);

  @override
  SeriesForecastModelBacktestResultsInner meanPinballLoss(
    num meanPinballLoss,
  ) => call(meanPinballLoss: meanPinballLoss);

  @override
  SeriesForecastModelBacktestResultsInner intervalCoverageP05P95(
    num? intervalCoverageP05P95,
  ) => call(intervalCoverageP05P95: intervalCoverageP05P95);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesForecastModelBacktestResultsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesForecastModelBacktestResultsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SeriesForecastModelBacktestResultsInner call({
    Object? method = const $CopyWithPlaceholder(),
    Object? horizon = const $CopyWithPlaceholder(),
    Object? meanPinballLoss = const $CopyWithPlaceholder(),
    Object? intervalCoverageP05P95 = const $CopyWithPlaceholder(),
  }) {
    return SeriesForecastModelBacktestResultsInner(
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as SeriesForecastModelBacktestResultsInnerMethodEnum,
      horizon: horizon == const $CopyWithPlaceholder() || horizon == null
          ? _value.horizon
          // ignore: cast_nullable_to_non_nullable
          : horizon as int,
      meanPinballLoss:
          meanPinballLoss == const $CopyWithPlaceholder() ||
              meanPinballLoss == null
          ? _value.meanPinballLoss
          // ignore: cast_nullable_to_non_nullable
          : meanPinballLoss as num,
      intervalCoverageP05P95:
          intervalCoverageP05P95 == const $CopyWithPlaceholder()
          ? _value.intervalCoverageP05P95
          // ignore: cast_nullable_to_non_nullable
          : intervalCoverageP05P95 as num?,
    );
  }
}

extension $SeriesForecastModelBacktestResultsInnerCopyWith
    on SeriesForecastModelBacktestResultsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeriesForecastModelBacktestResultsInner.copyWith(...)` or `instanceOfSeriesForecastModelBacktestResultsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesForecastModelBacktestResultsInnerCWProxy get copyWith =>
      _$SeriesForecastModelBacktestResultsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SeriesForecastModelBacktestResultsInner
_$SeriesForecastModelBacktestResultsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SeriesForecastModelBacktestResultsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(
        json,
        requiredKeys: const ['method', 'horizon', 'meanPinballLoss'],
      );
      final val = SeriesForecastModelBacktestResultsInner(
        method: $checkedConvert(
          'method',
          (v) => $enumDecode(
            _$SeriesForecastModelBacktestResultsInnerMethodEnumEnumMap,
            v,
          ),
        ),
        horizon: $checkedConvert('horizon', (v) => (v as num).toInt()),
        meanPinballLoss: $checkedConvert('meanPinballLoss', (v) => v as num),
        intervalCoverageP05P95: $checkedConvert(
          'intervalCoverageP05P95',
          (v) => v as num?,
        ),
      );
      return val;
    });

Map<String, dynamic> _$SeriesForecastModelBacktestResultsInnerToJson(
  SeriesForecastModelBacktestResultsInner instance,
) => <String, dynamic>{
  'method':
      _$SeriesForecastModelBacktestResultsInnerMethodEnumEnumMap[instance
          .method]!,
  'horizon': instance.horizon,
  'meanPinballLoss': instance.meanPinballLoss,
  'intervalCoverageP05P95': ?instance.intervalCoverageP05P95,
};

const _$SeriesForecastModelBacktestResultsInnerMethodEnumEnumMap = {
  SeriesForecastModelBacktestResultsInnerMethodEnum.seasonalNaive:
      'seasonal-naive',
  SeriesForecastModelBacktestResultsInnerMethodEnum.etsAnn: 'ets-ann',
  SeriesForecastModelBacktestResultsInnerMethodEnum.etsAaa: 'ets-aaa',
  SeriesForecastModelBacktestResultsInnerMethodEnum.nbGlm: 'nb-glm',
  SeriesForecastModelBacktestResultsInnerMethodEnum.dampedTrend: 'damped-trend',
};
