// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_numeric_calibration_quantile_reliability_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastNumericCalibrationQuantileReliabilityInnerCWProxy {
  ForecastNumericCalibrationQuantileReliabilityInner level(num? level);

  ForecastNumericCalibrationQuantileReliabilityInner quantile(String? quantile);

  ForecastNumericCalibrationQuantileReliabilityInner nominal(num? nominal);

  ForecastNumericCalibrationQuantileReliabilityInner empirical(num? empirical);

  ForecastNumericCalibrationQuantileReliabilityInner n(int? n);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastNumericCalibrationQuantileReliabilityInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastNumericCalibrationQuantileReliabilityInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastNumericCalibrationQuantileReliabilityInner call({
    num? level,
    String? quantile,
    num? nominal,
    num? empirical,
    int? n,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastNumericCalibrationQuantileReliabilityInner.copyWith(...)` or call `instanceOfForecastNumericCalibrationQuantileReliabilityInner.copyWith.fieldName(value)` for a single field.
class _$ForecastNumericCalibrationQuantileReliabilityInnerCWProxyImpl
    implements _$ForecastNumericCalibrationQuantileReliabilityInnerCWProxy {
  const _$ForecastNumericCalibrationQuantileReliabilityInnerCWProxyImpl(
    this._value,
  );

  final ForecastNumericCalibrationQuantileReliabilityInner _value;

  @override
  ForecastNumericCalibrationQuantileReliabilityInner level(num? level) =>
      call(level: level);

  @override
  ForecastNumericCalibrationQuantileReliabilityInner quantile(
    String? quantile,
  ) => call(quantile: quantile);

  @override
  ForecastNumericCalibrationQuantileReliabilityInner nominal(num? nominal) =>
      call(nominal: nominal);

  @override
  ForecastNumericCalibrationQuantileReliabilityInner empirical(
    num? empirical,
  ) => call(empirical: empirical);

  @override
  ForecastNumericCalibrationQuantileReliabilityInner n(int? n) => call(n: n);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastNumericCalibrationQuantileReliabilityInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastNumericCalibrationQuantileReliabilityInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastNumericCalibrationQuantileReliabilityInner call({
    Object? level = const $CopyWithPlaceholder(),
    Object? quantile = const $CopyWithPlaceholder(),
    Object? nominal = const $CopyWithPlaceholder(),
    Object? empirical = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
  }) {
    return ForecastNumericCalibrationQuantileReliabilityInner(
      level: level == const $CopyWithPlaceholder()
          ? _value.level
          // ignore: cast_nullable_to_non_nullable
          : level as num?,
      quantile: quantile == const $CopyWithPlaceholder()
          ? _value.quantile
          // ignore: cast_nullable_to_non_nullable
          : quantile as String?,
      nominal: nominal == const $CopyWithPlaceholder()
          ? _value.nominal
          // ignore: cast_nullable_to_non_nullable
          : nominal as num?,
      empirical: empirical == const $CopyWithPlaceholder()
          ? _value.empirical
          // ignore: cast_nullable_to_non_nullable
          : empirical as num?,
      n: n == const $CopyWithPlaceholder()
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int?,
    );
  }
}

extension $ForecastNumericCalibrationQuantileReliabilityInnerCopyWith
    on ForecastNumericCalibrationQuantileReliabilityInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastNumericCalibrationQuantileReliabilityInner.copyWith(...)` or `instanceOfForecastNumericCalibrationQuantileReliabilityInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastNumericCalibrationQuantileReliabilityInnerCWProxy get copyWith =>
      _$ForecastNumericCalibrationQuantileReliabilityInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastNumericCalibrationQuantileReliabilityInner
_$ForecastNumericCalibrationQuantileReliabilityInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate(
  'ForecastNumericCalibrationQuantileReliabilityInner',
  json,
  ($checkedConvert) {
    final val = ForecastNumericCalibrationQuantileReliabilityInner(
      level: $checkedConvert('level', (v) => v as num?),
      quantile: $checkedConvert('quantile', (v) => v as String?),
      nominal: $checkedConvert('nominal', (v) => v as num?),
      empirical: $checkedConvert('empirical', (v) => v as num?),
      n: $checkedConvert('n', (v) => (v as num?)?.toInt()),
    );
    return val;
  },
);

Map<String, dynamic> _$ForecastNumericCalibrationQuantileReliabilityInnerToJson(
  ForecastNumericCalibrationQuantileReliabilityInner instance,
) => <String, dynamic>{
  'level': ?instance.level,
  'quantile': ?instance.quantile,
  'nominal': ?instance.nominal,
  'empirical': ?instance.empirical,
  'n': ?instance.n,
};
