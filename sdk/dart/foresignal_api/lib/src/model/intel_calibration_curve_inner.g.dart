// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_calibration_curve_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelCalibrationCurveInnerCWProxy {
  IntelCalibrationCurveInner forecast(num? forecast);

  IntelCalibrationCurveInner calibrated(num? calibrated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCalibrationCurveInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCalibrationCurveInner(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelCalibrationCurveInner call({num? forecast, num? calibrated});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelCalibrationCurveInner.copyWith(...)` or call `instanceOfIntelCalibrationCurveInner.copyWith.fieldName(value)` for a single field.
class _$IntelCalibrationCurveInnerCWProxyImpl
    implements _$IntelCalibrationCurveInnerCWProxy {
  const _$IntelCalibrationCurveInnerCWProxyImpl(this._value);

  final IntelCalibrationCurveInner _value;

  @override
  IntelCalibrationCurveInner forecast(num? forecast) =>
      call(forecast: forecast);

  @override
  IntelCalibrationCurveInner calibrated(num? calibrated) =>
      call(calibrated: calibrated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCalibrationCurveInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCalibrationCurveInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelCalibrationCurveInner call({
    Object? forecast = const $CopyWithPlaceholder(),
    Object? calibrated = const $CopyWithPlaceholder(),
  }) {
    return IntelCalibrationCurveInner(
      forecast: forecast == const $CopyWithPlaceholder()
          ? _value.forecast
          // ignore: cast_nullable_to_non_nullable
          : forecast as num?,
      calibrated: calibrated == const $CopyWithPlaceholder()
          ? _value.calibrated
          // ignore: cast_nullable_to_non_nullable
          : calibrated as num?,
    );
  }
}

extension $IntelCalibrationCurveInnerCopyWith on IntelCalibrationCurveInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelCalibrationCurveInner.copyWith(...)` or `instanceOfIntelCalibrationCurveInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelCalibrationCurveInnerCWProxy get copyWith =>
      _$IntelCalibrationCurveInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelCalibrationCurveInner _$IntelCalibrationCurveInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelCalibrationCurveInner', json, ($checkedConvert) {
  final val = IntelCalibrationCurveInner(
    forecast: $checkedConvert('forecast', (v) => v as num?),
    calibrated: $checkedConvert('calibrated', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$IntelCalibrationCurveInnerToJson(
  IntelCalibrationCurveInner instance,
) => <String, dynamic>{
  'forecast': ?instance.forecast,
  'calibrated': ?instance.calibrated,
};
