//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'anomaly_record_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnomalyRecordPayload {
  /// Returns a new [AnomalyRecordPayload] instance.
  AnomalyRecordPayload({

    required  this.ruleId,

     this.name,

    required  this.groupKey,

     this.state,

     this.observed,

     this.expected,

     this.pValue,

    required  this.classification,

     this.detector,

     this.automated,
  });

  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'groupKey',
    required: true,
    includeIfNull: false,
  )


  final String groupKey;



  @JsonKey(
    
    name: r'state',
    required: false,
    includeIfNull: false,
  )


  final AnomalyRecordPayloadStateEnum? state;



  @JsonKey(
    
    name: r'observed',
    required: false,
    includeIfNull: false,
  )


  final int? observed;



  @JsonKey(
    
    name: r'expected',
    required: false,
    includeIfNull: false,
  )


  final num? expected;



  @JsonKey(
    
    name: r'pValue',
    required: false,
    includeIfNull: false,
  )


  final num? pValue;



  @JsonKey(
    
    name: r'classification',
    required: true,
    includeIfNull: false,
  )


  final AnomalyRecordPayloadClassificationEnum classification;



  @JsonKey(
    
    name: r'detector',
    required: false,
    includeIfNull: false,
  )


  final DetectorRef? detector;



  @JsonKey(
    
    name: r'automated',
    required: false,
    includeIfNull: false,
  )


  final bool? automated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnomalyRecordPayload &&
      other.ruleId == ruleId &&
      other.name == name &&
      other.groupKey == groupKey &&
      other.state == state &&
      other.observed == observed &&
      other.expected == expected &&
      other.pValue == pValue &&
      other.classification == classification &&
      other.detector == detector &&
      other.automated == automated;

    @override
    int get hashCode =>
        ruleId.hashCode +
        name.hashCode +
        groupKey.hashCode +
        state.hashCode +
        observed.hashCode +
        expected.hashCode +
        (pValue == null ? 0 : pValue.hashCode) +
        classification.hashCode +
        detector.hashCode +
        automated.hashCode;

  factory AnomalyRecordPayload.fromJson(Map<String, dynamic> json) => _$AnomalyRecordPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$AnomalyRecordPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnomalyRecordPayloadStateEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'clear')
clear(r'clear'),
@JsonValue(r'unknown')
unknown(r'unknown'),
@JsonValue(r'learning')
learning(r'learning');

const AnomalyRecordPayloadStateEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnomalyRecordPayloadClassificationEnum {
@JsonValue(r'activity-up')
activityUp(r'activity-up'),
@JsonValue(r'activity-down')
activityDown(r'activity-down'),
@JsonValue(r'coverage-drop')
coverageDrop(r'coverage-drop'),
@JsonValue(r'insufficient-coverage')
insufficientCoverage(r'insufficient-coverage'),
@JsonValue(r'learning')
learning(r'learning'),
@JsonValue(r'normal')
normal(r'normal');

const AnomalyRecordPayloadClassificationEnum(this.value);

final String value;

@override
String toString() => value;
}


