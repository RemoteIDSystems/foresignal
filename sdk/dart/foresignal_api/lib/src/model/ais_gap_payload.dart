//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ais_gap_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AisGapPayload {
  /// Returns a new [AisGapPayload] instance.
  AisGapPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.mmsi,

     this.entityId,

    required  this.gapStart,

     this.lastSog,

     this.lastCog,

    required  this.open,

     this.gapEnd,

     this.gapMinutes,

     this.displacementKm,

     this.impliedKn,
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


  final AisGapPayloadStatusEnum status;



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
    
    name: r'entityId',
    required: false,
    includeIfNull: false,
  )


  final String? entityId;



  @JsonKey(
    
    name: r'gapStart',
    required: true,
    includeIfNull: false,
  )


  final DateTime gapStart;



  @JsonKey(
    
    name: r'lastSog',
    required: false,
    includeIfNull: false,
  )


  final num? lastSog;



  @JsonKey(
    
    name: r'lastCog',
    required: false,
    includeIfNull: false,
  )


  final num? lastCog;



  @JsonKey(
    
    name: r'open',
    required: true,
    includeIfNull: false,
  )


  final bool open;



  @JsonKey(
    
    name: r'gapEnd',
    required: false,
    includeIfNull: false,
  )


  final DateTime? gapEnd;



  @JsonKey(
    
    name: r'gapMinutes',
    required: false,
    includeIfNull: false,
  )


  final num? gapMinutes;



  @JsonKey(
    
    name: r'displacementKm',
    required: false,
    includeIfNull: false,
  )


  final num? displacementKm;



  @JsonKey(
    
    name: r'impliedKn',
    required: false,
    includeIfNull: false,
  )


  final num? impliedKn;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AisGapPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.mmsi == mmsi &&
      other.entityId == entityId &&
      other.gapStart == gapStart &&
      other.lastSog == lastSog &&
      other.lastCog == lastCog &&
      other.open == open &&
      other.gapEnd == gapEnd &&
      other.gapMinutes == gapMinutes &&
      other.displacementKm == displacementKm &&
      other.impliedKn == impliedKn;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        mmsi.hashCode +
        (entityId == null ? 0 : entityId.hashCode) +
        gapStart.hashCode +
        lastSog.hashCode +
        lastCog.hashCode +
        open.hashCode +
        (gapEnd == null ? 0 : gapEnd.hashCode) +
        (gapMinutes == null ? 0 : gapMinutes.hashCode) +
        (displacementKm == null ? 0 : displacementKm.hashCode) +
        (impliedKn == null ? 0 : impliedKn.hashCode);

  factory AisGapPayload.fromJson(Map<String, dynamic> json) => _$AisGapPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$AisGapPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AisGapPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const AisGapPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


