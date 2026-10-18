//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/anomaly_observation_baseline.dart';
import 'package:foresignal_api/src/model/anomaly_observation_evidence.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'anomaly_observation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnomalyObservation {
  /// Returns a new [AnomalyObservation] instance.
  AnomalyObservation({

    required  this.id,

    required  this.ruleId,

    required  this.groupKey,

    required  this.bucketStart,

    required  this.bucketEnd,

    required  this.observed,

    required  this.expected,

     this.exposure,

     this.pValue,

     this.score,

     this.direction,

    required  this.classification,

     this.lowConfidence,

    required  this.baseline,

    required  this.detector,

     this.evidence,

    required  this.watermarkSeq,

     this.alertId,

     this.manifestId,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'groupKey',
    required: true,
    includeIfNull: false,
  )


  final String groupKey;



  @JsonKey(
    
    name: r'bucketStart',
    required: true,
    includeIfNull: false,
  )


  final DateTime bucketStart;



  @JsonKey(
    
    name: r'bucketEnd',
    required: true,
    includeIfNull: false,
  )


  final DateTime bucketEnd;



  @JsonKey(
    
    name: r'observed',
    required: true,
    includeIfNull: false,
  )


  final int observed;



      /// λ = slot mean × coverage ratio
  @JsonKey(
    
    name: r'expected',
    required: true,
    includeIfNull: false,
  )


  final num expected;



      /// Coverage ratio applied to the expectation
  @JsonKey(
    
    name: r'exposure',
    required: false,
    includeIfNull: false,
  )


  final num? exposure;



  @JsonKey(
    
    name: r'pValue',
    required: false,
    includeIfNull: false,
  )


  final num? pValue;



      /// Anscombe-transformed z
  @JsonKey(
    
    name: r'score',
    required: false,
    includeIfNull: false,
  )


  final num? score;



  @JsonKey(
    
    name: r'direction',
    required: false,
    includeIfNull: false,
  )


  final AnomalyObservationDirectionEnum? direction;



  @JsonKey(
    
    name: r'classification',
    required: true,
    includeIfNull: false,
  )


  final AnomalyObservationClassificationEnum classification;



  @JsonKey(
    
    name: r'lowConfidence',
    required: false,
    includeIfNull: false,
  )


  final bool? lowConfidence;



  @JsonKey(
    
    name: r'baseline',
    required: true,
    includeIfNull: false,
  )


  final AnomalyObservationBaseline baseline;



  @JsonKey(
    
    name: r'detector',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef detector;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final AnomalyObservationEvidence? evidence;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: true,
    includeIfNull: false,
  )


  final int watermarkSeq;



  @JsonKey(
    
    name: r'alertId',
    required: false,
    includeIfNull: false,
  )


  final int? alertId;



      /// The analysis manifest of this output (prompt 116); null for outputs written before it
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnomalyObservation &&
      other.id == id &&
      other.ruleId == ruleId &&
      other.groupKey == groupKey &&
      other.bucketStart == bucketStart &&
      other.bucketEnd == bucketEnd &&
      other.observed == observed &&
      other.expected == expected &&
      other.exposure == exposure &&
      other.pValue == pValue &&
      other.score == score &&
      other.direction == direction &&
      other.classification == classification &&
      other.lowConfidence == lowConfidence &&
      other.baseline == baseline &&
      other.detector == detector &&
      other.evidence == evidence &&
      other.watermarkSeq == watermarkSeq &&
      other.alertId == alertId &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        id.hashCode +
        ruleId.hashCode +
        groupKey.hashCode +
        bucketStart.hashCode +
        bucketEnd.hashCode +
        observed.hashCode +
        expected.hashCode +
        exposure.hashCode +
        (pValue == null ? 0 : pValue.hashCode) +
        (score == null ? 0 : score.hashCode) +
        (direction == null ? 0 : direction.hashCode) +
        classification.hashCode +
        lowConfidence.hashCode +
        baseline.hashCode +
        detector.hashCode +
        evidence.hashCode +
        watermarkSeq.hashCode +
        (alertId == null ? 0 : alertId.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory AnomalyObservation.fromJson(Map<String, dynamic> json) => _$AnomalyObservationFromJson(json);

  Map<String, dynamic> toJson() => _$AnomalyObservationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnomalyObservationDirectionEnum {
@JsonValue(r'up')
up(r'up'),
@JsonValue(r'down')
down(r'down');

const AnomalyObservationDirectionEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnomalyObservationClassificationEnum {
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

const AnomalyObservationClassificationEnum(this.value);

final String value;

@override
String toString() => value;
}


