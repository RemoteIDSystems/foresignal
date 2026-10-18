// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series_forecast_forecasts_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesForecastForecastsInnerCWProxy {
  SeriesForecastForecastsInner horizon(
    SeriesForecastForecastsInnerHorizonEnum horizon,
  );

  SeriesForecastForecastsInner targetPeriod(TimeRange targetPeriod);

  SeriesForecastForecastsInner quantiles(Quantiles quantiles);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesForecastForecastsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesForecastForecastsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SeriesForecastForecastsInner call({
    SeriesForecastForecastsInnerHorizonEnum horizon,
    TimeRange targetPeriod,
    Quantiles quantiles,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeriesForecastForecastsInner.copyWith(...)` or call `instanceOfSeriesForecastForecastsInner.copyWith.fieldName(value)` for a single field.
class _$SeriesForecastForecastsInnerCWProxyImpl
    implements _$SeriesForecastForecastsInnerCWProxy {
  const _$SeriesForecastForecastsInnerCWProxyImpl(this._value);

  final SeriesForecastForecastsInner _value;

  @override
  SeriesForecastForecastsInner horizon(
    SeriesForecastForecastsInnerHorizonEnum horizon,
  ) => call(horizon: horizon);

  @override
  SeriesForecastForecastsInner targetPeriod(TimeRange targetPeriod) =>
      call(targetPeriod: targetPeriod);

  @override
  SeriesForecastForecastsInner quantiles(Quantiles quantiles) =>
      call(quantiles: quantiles);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesForecastForecastsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesForecastForecastsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SeriesForecastForecastsInner call({
    Object? horizon = const $CopyWithPlaceholder(),
    Object? targetPeriod = const $CopyWithPlaceholder(),
    Object? quantiles = const $CopyWithPlaceholder(),
  }) {
    return SeriesForecastForecastsInner(
      horizon: horizon == const $CopyWithPlaceholder() || horizon == null
          ? _value.horizon
          // ignore: cast_nullable_to_non_nullable
          : horizon as SeriesForecastForecastsInnerHorizonEnum,
      targetPeriod:
          targetPeriod == const $CopyWithPlaceholder() || targetPeriod == null
          ? _value.targetPeriod
          // ignore: cast_nullable_to_non_nullable
          : targetPeriod as TimeRange,
      quantiles: quantiles == const $CopyWithPlaceholder() || quantiles == null
          ? _value.quantiles
          // ignore: cast_nullable_to_non_nullable
          : quantiles as Quantiles,
    );
  }
}

extension $SeriesForecastForecastsInnerCopyWith
    on SeriesForecastForecastsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeriesForecastForecastsInner.copyWith(...)` or `instanceOfSeriesForecastForecastsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesForecastForecastsInnerCWProxy get copyWith =>
      _$SeriesForecastForecastsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SeriesForecastForecastsInner _$SeriesForecastForecastsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SeriesForecastForecastsInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['horizon', 'targetPeriod', 'quantiles'],
  );
  final val = SeriesForecastForecastsInner(
    horizon: $checkedConvert(
      'horizon',
      (v) => $enumDecode(_$SeriesForecastForecastsInnerHorizonEnumEnumMap, v),
    ),
    targetPeriod: $checkedConvert(
      'targetPeriod',
      (v) => TimeRange.fromJson(v as Map<String, dynamic>),
    ),
    quantiles: $checkedConvert(
      'quantiles',
      (v) => Quantiles.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$SeriesForecastForecastsInnerToJson(
  SeriesForecastForecastsInner instance,
) => <String, dynamic>{
  'horizon':
      _$SeriesForecastForecastsInnerHorizonEnumEnumMap[instance.horizon]!,
  'targetPeriod': instance.targetPeriod.toJson(),
  'quantiles': instance.quantiles.toJson(),
};

const _$SeriesForecastForecastsInnerHorizonEnumEnumMap = {
  SeriesForecastForecastsInnerHorizonEnum.number1: 1,
  SeriesForecastForecastsInnerHorizonEnum.number7: 7,
  SeriesForecastForecastsInnerHorizonEnum.number28: 28,
};
