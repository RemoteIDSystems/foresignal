//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_calibration_decomposition.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastCalibrationDecomposition {
  /// Returns a new [ForecastCalibrationDecomposition] instance.
  ForecastCalibrationDecomposition({

    required  this.score,

    required  this.miscalibration,

    required  this.discrimination,

    required  this.uncertainty,
  });

  @JsonKey(
    
    name: r'score',
    required: true,
    includeIfNull: false,
  )


  final num score;



  @JsonKey(
    
    name: r'miscalibration',
    required: true,
    includeIfNull: false,
  )


  final num miscalibration;



  @JsonKey(
    
    name: r'discrimination',
    required: true,
    includeIfNull: false,
  )


  final num discrimination;



  @JsonKey(
    
    name: r'uncertainty',
    required: true,
    includeIfNull: false,
  )


  final num uncertainty;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastCalibrationDecomposition &&
      other.score == score &&
      other.miscalibration == miscalibration &&
      other.discrimination == discrimination &&
      other.uncertainty == uncertainty;

    @override
    int get hashCode =>
        score.hashCode +
        miscalibration.hashCode +
        discrimination.hashCode +
        uncertainty.hashCode;

  factory ForecastCalibrationDecomposition.fromJson(Map<String, dynamic> json) => _$ForecastCalibrationDecompositionFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastCalibrationDecompositionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

