//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/sample_status.dart';
import 'package:foresignal_api/src/model/sample_value_derived.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_value.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SampleValue {
  /// Returns a new [SampleValue] instance.
  SampleValue({

    required  this.productId,

     this.value,

     this.u,

     this.v,

     this.derived,

     this.units,

     this.label,

     this.frameKey,

     this.validTime,

     this.localTime,

     this.ageMinutes,

     this.run,

     this.step,

     this.method,

     this.coverage,

    required  this.status,

     this.statusText,

    required  this.attribution,
  });

  @JsonKey(
    
    name: r'productId',
    required: true,
    includeIfNull: false,
  )


  final String productId;



      /// Physical value (null for u/v products and non-ok statuses)
  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final num? value;



  @JsonKey(
    
    name: r'u',
    required: false,
    includeIfNull: false,
  )


  final num? u;



  @JsonKey(
    
    name: r'v',
    required: false,
    includeIfNull: false,
  )


  final num? v;



  @JsonKey(
    
    name: r'derived',
    required: false,
    includeIfNull: false,
  )


  final SampleValueDerived? derived;



  @JsonKey(
    
    name: r'units',
    required: false,
    includeIfNull: false,
  )


  final String? units;



      /// Human label, e.g. category or `52.5 dBZ`
  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



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



      /// Valid time in the place's timezone (else UTC), ISO-8601 with offset
  @JsonKey(
    
    name: r'localTime',
    required: false,
    includeIfNull: false,
  )


  final String? localTime;



  @JsonKey(
    
    name: r'ageMinutes',
    required: false,
    includeIfNull: false,
  )


  final num? ageMinutes;



  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



  @JsonKey(
    
    name: r'step',
    required: false,
    includeIfNull: false,
  )


  final int? step;



  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final SampleValueMethodEnum? method;



  @JsonKey(
    
    name: r'coverage',
    required: false,
    includeIfNull: false,
  )


  final SampleValueCoverageEnum? coverage;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SampleStatus status;



      /// Always present for non-ok statuses
  @JsonKey(
    
    name: r'statusText',
    required: false,
    includeIfNull: false,
  )


  final String? statusText;



  @JsonKey(
    
    name: r'attribution',
    required: true,
    includeIfNull: false,
  )


  final Attribution attribution;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SampleValue &&
      other.productId == productId &&
      other.value == value &&
      other.u == u &&
      other.v == v &&
      other.derived == derived &&
      other.units == units &&
      other.label == label &&
      other.frameKey == frameKey &&
      other.validTime == validTime &&
      other.localTime == localTime &&
      other.ageMinutes == ageMinutes &&
      other.run == run &&
      other.step == step &&
      other.method == method &&
      other.coverage == coverage &&
      other.status == status &&
      other.statusText == statusText &&
      other.attribution == attribution;

    @override
    int get hashCode =>
        productId.hashCode +
        (value == null ? 0 : value.hashCode) +
        (u == null ? 0 : u.hashCode) +
        (v == null ? 0 : v.hashCode) +
        (derived == null ? 0 : derived.hashCode) +
        (units == null ? 0 : units.hashCode) +
        (label == null ? 0 : label.hashCode) +
        (frameKey == null ? 0 : frameKey.hashCode) +
        (validTime == null ? 0 : validTime.hashCode) +
        (localTime == null ? 0 : localTime.hashCode) +
        (ageMinutes == null ? 0 : ageMinutes.hashCode) +
        (run == null ? 0 : run.hashCode) +
        (step == null ? 0 : step.hashCode) +
        method.hashCode +
        coverage.hashCode +
        status.hashCode +
        (statusText == null ? 0 : statusText.hashCode) +
        attribution.hashCode;

  factory SampleValue.fromJson(Map<String, dynamic> json) => _$SampleValueFromJson(json);

  Map<String, dynamic> toJson() => _$SampleValueToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SampleValueMethodEnum {
@JsonValue(r'nearest')
nearest(r'nearest'),
@JsonValue(r'bilinear')
bilinear(r'bilinear');

const SampleValueMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SampleValueCoverageEnum {
@JsonValue(r'CONUS')
CONUS(r'CONUS'),
@JsonValue(r'global')
global(r'global'),
@JsonValue(r'station-disc')
stationDisc(r'station-disc'),
@JsonValue(r'swath')
swath(r'swath');

const SampleValueCoverageEnum(this.value);

final String value;

@override
String toString() => value;
}


