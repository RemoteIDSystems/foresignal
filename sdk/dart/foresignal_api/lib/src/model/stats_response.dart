//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/stats_response_thresholds_inner.dart';
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/sample_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsResponse {
  /// Returns a new [StatsResponse] instance.
  StatsResponse({

    required  this.productId,

     this.frameKey,

     this.validTime,

     this.run,

     this.ageMinutes,

     this.units,

    required  this.status,

     this.statusText,

     this.min,

     this.max,

     this.mean,

     this.p95,

     this.coveragePct,

     this.areaKm2,

    required  this.thresholds,

    required  this.attribution,
  });

  @JsonKey(
    
    name: r'productId',
    required: true,
    includeIfNull: false,
  )


  final String productId;



  @JsonKey(
    
    name: r'frameKey',
    required: false,
    includeIfNull: false,
  )


  final String? frameKey;



  @JsonKey(
    
    name: r'validTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTime;



  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



  @JsonKey(
    
    name: r'ageMinutes',
    required: false,
    includeIfNull: false,
  )


  final num? ageMinutes;



  @JsonKey(
    
    name: r'units',
    required: false,
    includeIfNull: false,
  )


  final String? units;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SampleStatus status;



  @JsonKey(
    
    name: r'statusText',
    required: false,
    includeIfNull: false,
  )


  final String? statusText;



  @JsonKey(
    
    name: r'min',
    required: false,
    includeIfNull: false,
  )


  final num? min;



  @JsonKey(
    
    name: r'max',
    required: false,
    includeIfNull: false,
  )


  final num? max;



  @JsonKey(
    
    name: r'mean',
    required: false,
    includeIfNull: false,
  )


  final num? mean;



  @JsonKey(
    
    name: r'p95',
    required: false,
    includeIfNull: false,
  )


  final num? p95;



  @JsonKey(
    
    name: r'coveragePct',
    required: false,
    includeIfNull: false,
  )


  final num? coveragePct;



      /// Area of the query geometry (spherical cell areas)
  @JsonKey(
    
    name: r'areaKm2',
    required: false,
    includeIfNull: false,
  )


  final num? areaKm2;



  @JsonKey(
    
    name: r'thresholds',
    required: true,
    includeIfNull: false,
  )


  final List<StatsResponseThresholdsInner> thresholds;



  @JsonKey(
    
    name: r'attribution',
    required: true,
    includeIfNull: false,
  )


  final Attribution attribution;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsResponse &&
      other.productId == productId &&
      other.frameKey == frameKey &&
      other.validTime == validTime &&
      other.run == run &&
      other.ageMinutes == ageMinutes &&
      other.units == units &&
      other.status == status &&
      other.statusText == statusText &&
      other.min == min &&
      other.max == max &&
      other.mean == mean &&
      other.p95 == p95 &&
      other.coveragePct == coveragePct &&
      other.areaKm2 == areaKm2 &&
      other.thresholds == thresholds &&
      other.attribution == attribution;

    @override
    int get hashCode =>
        productId.hashCode +
        (frameKey == null ? 0 : frameKey.hashCode) +
        (validTime == null ? 0 : validTime.hashCode) +
        (run == null ? 0 : run.hashCode) +
        (ageMinutes == null ? 0 : ageMinutes.hashCode) +
        (units == null ? 0 : units.hashCode) +
        status.hashCode +
        (statusText == null ? 0 : statusText.hashCode) +
        (min == null ? 0 : min.hashCode) +
        (max == null ? 0 : max.hashCode) +
        (mean == null ? 0 : mean.hashCode) +
        (p95 == null ? 0 : p95.hashCode) +
        (coveragePct == null ? 0 : coveragePct.hashCode) +
        areaKm2.hashCode +
        thresholds.hashCode +
        attribution.hashCode;

  factory StatsResponse.fromJson(Map<String, dynamic> json) => _$StatsResponseFromJson(json);

  Map<String, dynamic> toJson() => _$StatsResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

