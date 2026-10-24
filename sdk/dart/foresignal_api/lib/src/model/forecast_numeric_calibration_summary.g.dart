// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_numeric_calibration_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastNumericCalibrationSummaryCWProxy {
  ForecastNumericCalibrationSummary meanAbsoluteCoverageError(
    num? meanAbsoluteCoverageError,
  );

  ForecastNumericCalibrationSummary maxCoverageDeviation(
    num? maxCoverageDeviation,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastNumericCalibrationSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastNumericCalibrationSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastNumericCalibrationSummary call({
    num? meanAbsoluteCoverageError,
    num? maxCoverageDeviation,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastNumericCalibrationSummary.copyWith(...)` or call `instanceOfForecastNumericCalibrationSummary.copyWith.fieldName(value)` for a single field.
class _$ForecastNumericCalibrationSummaryCWProxyImpl
    implements _$ForecastNumericCalibrationSummaryCWProxy {
  const _$ForecastNumericCalibrationSummaryCWProxyImpl(this._value);

  final ForecastNumericCalibrationSummary _value;

  @override
  ForecastNumericCalibrationSummary meanAbsoluteCoverageError(
    num? meanAbsoluteCoverageError,
  ) => call(meanAbsoluteCoverageError: meanAbsoluteCoverageError);

  @override
  ForecastNumericCalibrationSummary maxCoverageDeviation(
    num? maxCoverageDeviation,
  ) => call(maxCoverageDeviation: maxCoverageDeviation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastNumericCalibrationSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastNumericCalibrationSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastNumericCalibrationSummary call({
    Object? meanAbsoluteCoverageError = const $CopyWithPlaceholder(),
    Object? maxCoverageDeviation = const $CopyWithPlaceholder(),
  }) {
    return ForecastNumericCalibrationSummary(
      meanAbsoluteCoverageError:
          meanAbsoluteCoverageError == const $CopyWithPlaceholder()
          ? _value.meanAbsoluteCoverageError
          // ignore: cast_nullable_to_non_nullable
          : meanAbsoluteCoverageError as num?,
      maxCoverageDeviation: maxCoverageDeviation == const $CopyWithPlaceholder()
          ? _value.maxCoverageDeviation
          // ignore: cast_nullable_to_non_nullable
          : maxCoverageDeviation as num?,
    );
  }
}

extension $ForecastNumericCalibrationSummaryCopyWith
    on ForecastNumericCalibrationSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastNumericCalibrationSummary.copyWith(...)` or `instanceOfForecastNumericCalibrationSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastNumericCalibrationSummaryCWProxy get copyWith =>
      _$ForecastNumericCalibrationSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastNumericCalibrationSummary _$ForecastNumericCalibrationSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastNumericCalibrationSummary', json, (
  $checkedConvert,
) {
  final val = ForecastNumericCalibrationSummary(
    meanAbsoluteCoverageError: $checkedConvert(
      'meanAbsoluteCoverageError',
      (v) => v as num?,
    ),
    maxCoverageDeviation: $checkedConvert(
      'maxCoverageDeviation',
      (v) => v as num?,
    ),
  );
  return val;
});

Map<String, dynamic> _$ForecastNumericCalibrationSummaryToJson(
  ForecastNumericCalibrationSummary instance,
) => <String, dynamic>{
  'meanAbsoluteCoverageError': ?instance.meanAbsoluteCoverageError,
  'maxCoverageDeviation': ?instance.maxCoverageDeviation,
};
