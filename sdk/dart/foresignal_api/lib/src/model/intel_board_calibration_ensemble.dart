//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_board_calibration_ensemble.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelBoardCalibrationEnsemble {
  /// Returns a new [IntelBoardCalibrationEnsemble] instance.
  IntelBoardCalibrationEnsemble({

     this.brierSkill,

     this.calibration,

    required  this.n,
  });

  @JsonKey(
    
    name: r'brierSkill',
    required: false,
    includeIfNull: false,
  )


  final num? brierSkill;



  @JsonKey(
    
    name: r'calibration',
    required: false,
    includeIfNull: false,
  )


  final String? calibration;



  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelBoardCalibrationEnsemble &&
      other.brierSkill == brierSkill &&
      other.calibration == calibration &&
      other.n == n;

    @override
    int get hashCode =>
        (brierSkill == null ? 0 : brierSkill.hashCode) +
        (calibration == null ? 0 : calibration.hashCode) +
        n.hashCode;

  factory IntelBoardCalibrationEnsemble.fromJson(Map<String, dynamic> json) => _$IntelBoardCalibrationEnsembleFromJson(json);

  Map<String, dynamic> toJson() => _$IntelBoardCalibrationEnsembleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

