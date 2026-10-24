//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_numeric_calibration_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastNumericCalibrationSummary {
  /// Returns a new [ForecastNumericCalibrationSummary] instance.
  ForecastNumericCalibrationSummary({

     this.meanAbsoluteCoverageError,

     this.maxCoverageDeviation,
  });

      /// Mean |empirical − nominal| over the stated levels
  @JsonKey(
    
    name: r'meanAbsoluteCoverageError',
    required: false,
    includeIfNull: false,
  )


  final num? meanAbsoluteCoverageError;



  @JsonKey(
    
    name: r'maxCoverageDeviation',
    required: false,
    includeIfNull: false,
  )


  final num? maxCoverageDeviation;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastNumericCalibrationSummary &&
      other.meanAbsoluteCoverageError == meanAbsoluteCoverageError &&
      other.maxCoverageDeviation == maxCoverageDeviation;

    @override
    int get hashCode =>
        (meanAbsoluteCoverageError == null ? 0 : meanAbsoluteCoverageError.hashCode) +
        (maxCoverageDeviation == null ? 0 : maxCoverageDeviation.hashCode);

  factory ForecastNumericCalibrationSummary.fromJson(Map<String, dynamic> json) => _$ForecastNumericCalibrationSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastNumericCalibrationSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

