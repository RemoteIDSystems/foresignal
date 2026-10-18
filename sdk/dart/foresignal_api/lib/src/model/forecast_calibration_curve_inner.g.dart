// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_calibration_curve_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastCalibrationCurveInnerCWProxy {
  ForecastCalibrationCurveInner forecast(num forecast);

  ForecastCalibrationCurveInner calibrated(num calibrated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastCalibrationCurveInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastCalibrationCurveInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastCalibrationCurveInner call({num forecast, num calibrated});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastCalibrationCurveInner.copyWith(...)` or call `instanceOfForecastCalibrationCurveInner.copyWith.fieldName(value)` for a single field.
class _$ForecastCalibrationCurveInnerCWProxyImpl
    implements _$ForecastCalibrationCurveInnerCWProxy {
  const _$ForecastCalibrationCurveInnerCWProxyImpl(this._value);

  final ForecastCalibrationCurveInner _value;

  @override
  ForecastCalibrationCurveInner forecast(num forecast) =>
      call(forecast: forecast);

  @override
  ForecastCalibrationCurveInner calibrated(num calibrated) =>
      call(calibrated: calibrated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastCalibrationCurveInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastCalibrationCurveInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastCalibrationCurveInner call({
    Object? forecast = const $CopyWithPlaceholder(),
    Object? calibrated = const $CopyWithPlaceholder(),
  }) {
    return ForecastCalibrationCurveInner(
      forecast: forecast == const $CopyWithPlaceholder() || forecast == null
          ? _value.forecast
          // ignore: cast_nullable_to_non_nullable
          : forecast as num,
      calibrated:
          calibrated == const $CopyWithPlaceholder() || calibrated == null
          ? _value.calibrated
          // ignore: cast_nullable_to_non_nullable
          : calibrated as num,
    );
  }
}

extension $ForecastCalibrationCurveInnerCopyWith
    on ForecastCalibrationCurveInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastCalibrationCurveInner.copyWith(...)` or `instanceOfForecastCalibrationCurveInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastCalibrationCurveInnerCWProxy get copyWith =>
      _$ForecastCalibrationCurveInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastCalibrationCurveInner _$ForecastCalibrationCurveInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastCalibrationCurveInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['forecast', 'calibrated']);
  final val = ForecastCalibrationCurveInner(
    forecast: $checkedConvert('forecast', (v) => v as num),
    calibrated: $checkedConvert('calibrated', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$ForecastCalibrationCurveInnerToJson(
  ForecastCalibrationCurveInner instance,
) => <String, dynamic>{
  'forecast': instance.forecast,
  'calibrated': instance.calibrated,
};
