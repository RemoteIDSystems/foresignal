//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_calibration_bins_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastCalibrationBinsInner {
  /// Returns a new [ForecastCalibrationBinsInner] instance.
  ForecastCalibrationBinsInner({

    required  this.lower,

    required  this.upper,

    required  this.n,

     this.meanForecast,

     this.observedFrequency,
  });

  @JsonKey(
    
    name: r'lower',
    required: true,
    includeIfNull: false,
  )


  final num lower;



  @JsonKey(
    
    name: r'upper',
    required: true,
    includeIfNull: false,
  )


  final num upper;



  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;



  @JsonKey(
    
    name: r'meanForecast',
    required: false,
    includeIfNull: false,
  )


  final num? meanForecast;



  @JsonKey(
    
    name: r'observedFrequency',
    required: false,
    includeIfNull: false,
  )


  final num? observedFrequency;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastCalibrationBinsInner &&
      other.lower == lower &&
      other.upper == upper &&
      other.n == n &&
      other.meanForecast == meanForecast &&
      other.observedFrequency == observedFrequency;

    @override
    int get hashCode =>
        lower.hashCode +
        upper.hashCode +
        n.hashCode +
        (meanForecast == null ? 0 : meanForecast.hashCode) +
        (observedFrequency == null ? 0 : observedFrequency.hashCode);

  factory ForecastCalibrationBinsInner.fromJson(Map<String, dynamic> json) => _$ForecastCalibrationBinsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastCalibrationBinsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

