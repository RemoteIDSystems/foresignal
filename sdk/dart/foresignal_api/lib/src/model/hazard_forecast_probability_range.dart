//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hazard_forecast_probability_range.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HazardForecastProbabilityRange {
  /// Returns a new [HazardForecastProbabilityRange] instance.
  HazardForecastProbabilityRange({

    required  this.low,

     this.high,
  });

          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'low',
    required: true,
    includeIfNull: false,
  )


  final num low;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'high',
    required: false,
    includeIfNull: false,
  )


  final num? high;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HazardForecastProbabilityRange &&
      other.low == low &&
      other.high == high;

    @override
    int get hashCode =>
        low.hashCode +
        (high == null ? 0 : high.hashCode);

  factory HazardForecastProbabilityRange.fromJson(Map<String, dynamic> json) => _$HazardForecastProbabilityRangeFromJson(json);

  Map<String, dynamic> toJson() => _$HazardForecastProbabilityRangeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

