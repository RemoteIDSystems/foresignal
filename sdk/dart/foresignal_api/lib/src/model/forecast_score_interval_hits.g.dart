// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_score_interval_hits.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastScoreIntervalHitsCWProxy {
  ForecastScoreIntervalHits p05p95(bool? p05p95);

  ForecastScoreIntervalHits p25p75(bool? p25p75);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastScoreIntervalHits(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastScoreIntervalHits(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastScoreIntervalHits call({bool? p05p95, bool? p25p75});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastScoreIntervalHits.copyWith(...)` or call `instanceOfForecastScoreIntervalHits.copyWith.fieldName(value)` for a single field.
class _$ForecastScoreIntervalHitsCWProxyImpl
    implements _$ForecastScoreIntervalHitsCWProxy {
  const _$ForecastScoreIntervalHitsCWProxyImpl(this._value);

  final ForecastScoreIntervalHits _value;

  @override
  ForecastScoreIntervalHits p05p95(bool? p05p95) => call(p05p95: p05p95);

  @override
  ForecastScoreIntervalHits p25p75(bool? p25p75) => call(p25p75: p25p75);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastScoreIntervalHits(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastScoreIntervalHits(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastScoreIntervalHits call({
    Object? p05p95 = const $CopyWithPlaceholder(),
    Object? p25p75 = const $CopyWithPlaceholder(),
  }) {
    return ForecastScoreIntervalHits(
      p05p95: p05p95 == const $CopyWithPlaceholder()
          ? _value.p05p95
          // ignore: cast_nullable_to_non_nullable
          : p05p95 as bool?,
      p25p75: p25p75 == const $CopyWithPlaceholder()
          ? _value.p25p75
          // ignore: cast_nullable_to_non_nullable
          : p25p75 as bool?,
    );
  }
}

extension $ForecastScoreIntervalHitsCopyWith on ForecastScoreIntervalHits {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastScoreIntervalHits.copyWith(...)` or `instanceOfForecastScoreIntervalHits.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastScoreIntervalHitsCWProxy get copyWith =>
      _$ForecastScoreIntervalHitsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastScoreIntervalHits _$ForecastScoreIntervalHitsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastScoreIntervalHits', json, ($checkedConvert) {
  final val = ForecastScoreIntervalHits(
    p05p95: $checkedConvert('p05p95', (v) => v as bool?),
    p25p75: $checkedConvert('p25p75', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$ForecastScoreIntervalHitsToJson(
  ForecastScoreIntervalHits instance,
) => <String, dynamic>{'p05p95': ?instance.p05p95, 'p25p75': ?instance.p25p75};
