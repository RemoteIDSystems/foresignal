//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_calibration_curve_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastCalibrationCurveInner {
  /// Returns a new [ForecastCalibrationCurveInner] instance.
  ForecastCalibrationCurveInner({

    required  this.forecast,

    required  this.calibrated,
  });

  @JsonKey(
    
    name: r'forecast',
    required: true,
    includeIfNull: false,
  )


  final num forecast;



  @JsonKey(
    
    name: r'calibrated',
    required: true,
    includeIfNull: false,
  )


  final num calibrated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastCalibrationCurveInner &&
      other.forecast == forecast &&
      other.calibrated == calibrated;

    @override
    int get hashCode =>
        forecast.hashCode +
        calibrated.hashCode;

  factory ForecastCalibrationCurveInner.fromJson(Map<String, dynamic> json) => _$ForecastCalibrationCurveInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastCalibrationCurveInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

