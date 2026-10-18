//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/severity.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cable_proximity_risk_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CableProximityRiskPayload {
  /// Returns a new [CableProximityRiskPayload] instance.
  CableProximityRiskPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.mmsi,

    required  this.cableId,

     this.cableName,

    required  this.startedAt,

     this.durationMin,

     this.cablesWithin2Km,

     this.possibleAnchorDrag,

     this.aisGapNearCable,

     this.severity,
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


  final CableProximityRiskPayloadStatusEnum status;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef>? evidence;



  @JsonKey(
    
    name: r'mmsi',
    required: true,
    includeIfNull: false,
  )


  final String mmsi;



  @JsonKey(
    
    name: r'cableId',
    required: true,
    includeIfNull: false,
  )


  final String cableId;



  @JsonKey(
    
    name: r'cableName',
    required: false,
    includeIfNull: false,
  )


  final String? cableName;



  @JsonKey(
    
    name: r'startedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime startedAt;



  @JsonKey(
    
    name: r'durationMin',
    required: false,
    includeIfNull: false,
  )


  final num? durationMin;



  @JsonKey(
    
    name: r'cablesWithin2Km',
    required: false,
    includeIfNull: false,
  )


  final int? cablesWithin2Km;



  @JsonKey(
    
    name: r'possibleAnchorDrag',
    required: false,
    includeIfNull: false,
  )


  final bool? possibleAnchorDrag;



  @JsonKey(
    
    name: r'aisGapNearCable',
    required: false,
    includeIfNull: false,
  )


  final bool? aisGapNearCable;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final Severity? severity;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CableProximityRiskPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.mmsi == mmsi &&
      other.cableId == cableId &&
      other.cableName == cableName &&
      other.startedAt == startedAt &&
      other.durationMin == durationMin &&
      other.cablesWithin2Km == cablesWithin2Km &&
      other.possibleAnchorDrag == possibleAnchorDrag &&
      other.aisGapNearCable == aisGapNearCable &&
      other.severity == severity;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        mmsi.hashCode +
        cableId.hashCode +
        (cableName == null ? 0 : cableName.hashCode) +
        startedAt.hashCode +
        durationMin.hashCode +
        cablesWithin2Km.hashCode +
        possibleAnchorDrag.hashCode +
        aisGapNearCable.hashCode +
        severity.hashCode;

  factory CableProximityRiskPayload.fromJson(Map<String, dynamic> json) => _$CableProximityRiskPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$CableProximityRiskPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CableProximityRiskPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const CableProximityRiskPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


