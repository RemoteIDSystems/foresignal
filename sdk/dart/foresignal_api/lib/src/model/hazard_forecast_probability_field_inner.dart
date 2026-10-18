//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hazard_forecast_probability_field_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HazardForecastProbabilityFieldInner {
  /// Returns a new [HazardForecastProbabilityFieldInner] instance.
  HazardForecastProbabilityFieldInner({

     this.band,

    required  this.pMin,

     this.pMax,

     this.geometry,
  });

  @JsonKey(
    
    name: r'band',
    required: false,
    includeIfNull: false,
  )


  final String? band;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pMin',
    required: true,
    includeIfNull: false,
  )


  final num pMin;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pMax',
    required: false,
    includeIfNull: false,
  )


  final num? pMax;



      /// The band's area (simplified to 0.02°); present on getProjectedImpact
  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HazardForecastProbabilityFieldInner &&
      other.band == band &&
      other.pMin == pMin &&
      other.pMax == pMax &&
      other.geometry == geometry;

    @override
    int get hashCode =>
        band.hashCode +
        pMin.hashCode +
        (pMax == null ? 0 : pMax.hashCode) +
        geometry.hashCode;

  factory HazardForecastProbabilityFieldInner.fromJson(Map<String, dynamic> json) => _$HazardForecastProbabilityFieldInnerFromJson(json);

  Map<String, dynamic> toJson() => _$HazardForecastProbabilityFieldInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

