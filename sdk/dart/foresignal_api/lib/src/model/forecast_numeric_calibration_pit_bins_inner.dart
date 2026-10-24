//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_numeric_calibration_pit_bins_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastNumericCalibrationPitBinsInner {
  /// Returns a new [ForecastNumericCalibrationPitBinsInner] instance.
  ForecastNumericCalibrationPitBinsInner({

     this.lower,

     this.upper,

     this.count,

     this.frequency,

     this.expected,
  });

  @JsonKey(
    
    name: r'lower',
    required: false,
    includeIfNull: false,
  )


  final num? lower;



  @JsonKey(
    
    name: r'upper',
    required: false,
    includeIfNull: false,
  )


  final num? upper;



  @JsonKey(
    
    name: r'count',
    required: false,
    includeIfNull: false,
  )


  final int? count;



  @JsonKey(
    
    name: r'frequency',
    required: false,
    includeIfNull: false,
  )


  final num? frequency;



      /// The bin width; a calibrated forecaster's frequency is near it
  @JsonKey(
    
    name: r'expected',
    required: false,
    includeIfNull: false,
  )


  final num? expected;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastNumericCalibrationPitBinsInner &&
      other.lower == lower &&
      other.upper == upper &&
      other.count == count &&
      other.frequency == frequency &&
      other.expected == expected;

    @override
    int get hashCode =>
        lower.hashCode +
        upper.hashCode +
        count.hashCode +
        (frequency == null ? 0 : frequency.hashCode) +
        expected.hashCode;

  factory ForecastNumericCalibrationPitBinsInner.fromJson(Map<String, dynamic> json) => _$ForecastNumericCalibrationPitBinsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastNumericCalibrationPitBinsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

