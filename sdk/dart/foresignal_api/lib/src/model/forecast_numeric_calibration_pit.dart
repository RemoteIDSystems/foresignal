//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/forecast_numeric_calibration_pit_bins_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_numeric_calibration_pit.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastNumericCalibrationPit {
  /// Returns a new [ForecastNumericCalibrationPit] instance.
  ForecastNumericCalibrationPit({

     this.n,

     this.bins,
  });

  @JsonKey(
    
    name: r'n',
    required: false,
    includeIfNull: false,
  )


  final int? n;



  @JsonKey(
    
    name: r'bins',
    required: false,
    includeIfNull: false,
  )


  final List<ForecastNumericCalibrationPitBinsInner>? bins;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastNumericCalibrationPit &&
      other.n == n &&
      other.bins == bins;

    @override
    int get hashCode =>
        n.hashCode +
        bins.hashCode;

  factory ForecastNumericCalibrationPit.fromJson(Map<String, dynamic> json) => _$ForecastNumericCalibrationPitFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastNumericCalibrationPitToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

