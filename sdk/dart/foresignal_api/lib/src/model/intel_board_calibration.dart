//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/intel_board_calibration_operator.dart';
import 'package:foresignal_api/src/model/intel_board_calibration_ensemble.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_board_calibration.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelBoardCalibration {
  /// Returns a new [IntelBoardCalibration] instance.
  IntelBoardCalibration({

     this.window = 'P90D',

     this.ensemble,

     this.operator_,
  });

  @JsonKey(
    defaultValue: 'P90D',
    name: r'window',
    required: false,
    includeIfNull: false,
  )


  final String? window;



  @JsonKey(
    
    name: r'ensemble',
    required: false,
    includeIfNull: false,
  )


  final IntelBoardCalibrationEnsemble? ensemble;



  @JsonKey(
    
    name: r'operator',
    required: false,
    includeIfNull: false,
  )


  final IntelBoardCalibrationOperator? operator_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelBoardCalibration &&
      other.window == window &&
      other.ensemble == ensemble &&
      other.operator_ == operator_;

    @override
    int get hashCode =>
        window.hashCode +
        (ensemble == null ? 0 : ensemble.hashCode) +
        (operator_ == null ? 0 : operator_.hashCode);

  factory IntelBoardCalibration.fromJson(Map<String, dynamic> json) => _$IntelBoardCalibrationFromJson(json);

  Map<String, dynamic> toJson() => _$IntelBoardCalibrationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

