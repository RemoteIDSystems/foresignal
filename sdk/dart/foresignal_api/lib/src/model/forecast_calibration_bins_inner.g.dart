// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_calibration_bins_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastCalibrationBinsInnerCWProxy {
  ForecastCalibrationBinsInner lower(num lower);

  ForecastCalibrationBinsInner upper(num upper);

  ForecastCalibrationBinsInner n(int n);

  ForecastCalibrationBinsInner meanForecast(num? meanForecast);

  ForecastCalibrationBinsInner observedFrequency(num? observedFrequency);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastCalibrationBinsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastCalibrationBinsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastCalibrationBinsInner call({
    num lower,
    num upper,
    int n,
    num? meanForecast,
    num? observedFrequency,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastCalibrationBinsInner.copyWith(...)` or call `instanceOfForecastCalibrationBinsInner.copyWith.fieldName(value)` for a single field.
class _$ForecastCalibrationBinsInnerCWProxyImpl
    implements _$ForecastCalibrationBinsInnerCWProxy {
  const _$ForecastCalibrationBinsInnerCWProxyImpl(this._value);

  final ForecastCalibrationBinsInner _value;

  @override
  ForecastCalibrationBinsInner lower(num lower) => call(lower: lower);

  @override
  ForecastCalibrationBinsInner upper(num upper) => call(upper: upper);

  @override
  ForecastCalibrationBinsInner n(int n) => call(n: n);

  @override
  ForecastCalibrationBinsInner meanForecast(num? meanForecast) =>
      call(meanForecast: meanForecast);

  @override
  ForecastCalibrationBinsInner observedFrequency(num? observedFrequency) =>
      call(observedFrequency: observedFrequency);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastCalibrationBinsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastCalibrationBinsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastCalibrationBinsInner call({
    Object? lower = const $CopyWithPlaceholder(),
    Object? upper = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? meanForecast = const $CopyWithPlaceholder(),
    Object? observedFrequency = const $CopyWithPlaceholder(),
  }) {
    return ForecastCalibrationBinsInner(
      lower: lower == const $CopyWithPlaceholder() || lower == null
          ? _value.lower
          // ignore: cast_nullable_to_non_nullable
          : lower as num,
      upper: upper == const $CopyWithPlaceholder() || upper == null
          ? _value.upper
          // ignore: cast_nullable_to_non_nullable
          : upper as num,
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
      meanForecast: meanForecast == const $CopyWithPlaceholder()
          ? _value.meanForecast
          // ignore: cast_nullable_to_non_nullable
          : meanForecast as num?,
      observedFrequency: observedFrequency == const $CopyWithPlaceholder()
          ? _value.observedFrequency
          // ignore: cast_nullable_to_non_nullable
          : observedFrequency as num?,
    );
  }
}

extension $ForecastCalibrationBinsInnerCopyWith
    on ForecastCalibrationBinsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastCalibrationBinsInner.copyWith(...)` or `instanceOfForecastCalibrationBinsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastCalibrationBinsInnerCWProxy get copyWith =>
      _$ForecastCalibrationBinsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastCalibrationBinsInner _$ForecastCalibrationBinsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastCalibrationBinsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['lower', 'upper', 'n']);
  final val = ForecastCalibrationBinsInner(
    lower: $checkedConvert('lower', (v) => v as num),
    upper: $checkedConvert('upper', (v) => v as num),
    n: $checkedConvert('n', (v) => (v as num).toInt()),
    meanForecast: $checkedConvert('meanForecast', (v) => v as num?),
    observedFrequency: $checkedConvert('observedFrequency', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$ForecastCalibrationBinsInnerToJson(
  ForecastCalibrationBinsInner instance,
) => <String, dynamic>{
  'lower': instance.lower,
  'upper': instance.upper,
  'n': instance.n,
  'meanForecast': ?instance.meanForecast,
  'observedFrequency': ?instance.observedFrequency,
};
