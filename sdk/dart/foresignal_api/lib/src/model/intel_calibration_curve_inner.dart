//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_calibration_curve_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelCalibrationCurveInner {
  /// Returns a new [IntelCalibrationCurveInner] instance.
  IntelCalibrationCurveInner({

     this.forecast,

     this.calibrated,
  });

  @JsonKey(
    
    name: r'forecast',
    required: false,
    includeIfNull: false,
  )


  final num? forecast;



  @JsonKey(
    
    name: r'calibrated',
    required: false,
    includeIfNull: false,
  )


  final num? calibrated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelCalibrationCurveInner &&
      other.forecast == forecast &&
      other.calibrated == calibrated;

    @override
    int get hashCode =>
        forecast.hashCode +
        calibrated.hashCode;

  factory IntelCalibrationCurveInner.fromJson(Map<String, dynamic> json) => _$IntelCalibrationCurveInnerFromJson(json);

  Map<String, dynamic> toJson() => _$IntelCalibrationCurveInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

