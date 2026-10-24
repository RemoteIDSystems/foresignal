//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_calibration_bins_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelCalibrationBinsInner {
  /// Returns a new [IntelCalibrationBinsInner] instance.
  IntelCalibrationBinsInner({

     this.lower,

     this.upper,

     this.n,

     this.meanForecast,

     this.observedFrequency,
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
    
    name: r'n',
    required: false,
    includeIfNull: false,
  )


  final int? n;



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
    bool operator ==(Object other) => identical(this, other) || other is IntelCalibrationBinsInner &&
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

  factory IntelCalibrationBinsInner.fromJson(Map<String, dynamic> json) => _$IntelCalibrationBinsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$IntelCalibrationBinsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

