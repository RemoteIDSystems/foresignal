//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'corroboration_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CorroborationPayload {
  /// Returns a new [CorroborationPayload] instance.
  CorroborationPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.kindFamily,

    required  this.members,

    required  this.independentSources,

    required  this.credibility,

     this.reliability,
  });

  @JsonKey(
    
    name: r'automated',
    required: true,
    includeIfNull: false,
  )


  final bool automated;



  @JsonKey(
    
    name: r'notAWarning',
    required: true,
    includeIfNull: false,
  )


  final bool notAWarning;



  @JsonKey(
    
    name: r'detector',
    required: true,
    includeIfNull: false,
  )


  final DerivedPayloadCommonDetector detector;



      /// Banded to 0.1; a score, not a calibrated probability
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final num? confidence;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final CorroborationPayloadStatusEnum status;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef>? evidence;



  @JsonKey(
    
    name: r'kindFamily',
    required: true,
    includeIfNull: false,
  )


  final CorroborationPayloadKindFamilyEnum kindFamily;



  @JsonKey(
    
    name: r'members',
    required: true,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef> members;



  @JsonKey(
    
    name: r'independentSources',
    required: true,
    includeIfNull: false,
  )


  final int independentSources;



      /// Admiralty information credibility; 6 means unknown, never false
          // minimum: 1
          // maximum: 6
  @JsonKey(
    
    name: r'credibility',
    required: true,
    includeIfNull: false,
  )


  final int credibility;



      /// Source reliability from 109 grades; F means unknown
  @JsonKey(
    
    name: r'reliability',
    required: false,
    includeIfNull: false,
  )


  final CorroborationPayloadReliabilityEnum? reliability;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CorroborationPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.kindFamily == kindFamily &&
      other.members == members &&
      other.independentSources == independentSources &&
      other.credibility == credibility &&
      other.reliability == reliability;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        kindFamily.hashCode +
        members.hashCode +
        independentSources.hashCode +
        credibility.hashCode +
        reliability.hashCode;

  factory CorroborationPayload.fromJson(Map<String, dynamic> json) => _$CorroborationPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$CorroborationPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CorroborationPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const CorroborationPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum CorroborationPayloadKindFamilyEnum {
@JsonValue(r'earthquake')
earthquake(r'earthquake'),
@JsonValue(r'fire')
fire(r'fire'),
@JsonValue(r'conflict')
conflict(r'conflict'),
@JsonValue(r'outage')
outage(r'outage');

const CorroborationPayloadKindFamilyEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Source reliability from 109 grades; F means unknown
enum CorroborationPayloadReliabilityEnum {
@JsonValue(r'A')
A(r'A'),
@JsonValue(r'B')
B(r'B'),
@JsonValue(r'C')
C(r'C'),
@JsonValue(r'D')
D(r'D'),
@JsonValue(r'E')
E(r'E'),
@JsonValue(r'F')
F(r'F');

const CorroborationPayloadReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


