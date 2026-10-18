//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dark_vessel_candidate_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DarkVesselCandidatePayload {
  /// Returns a new [DarkVesselCandidatePayload] instance.
  DarkVesselCandidatePayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.mmsi,

    required  this.gapStart,

    required  this.detectionId,

     this.distanceKm,

     this.timeOffsetMin,

     this.detectionLengthM,

    required  this.matchScore,

     this.label,
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


  final DarkVesselCandidatePayloadStatusEnum status;



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
    
    name: r'gapStart',
    required: true,
    includeIfNull: false,
  )


  final DateTime gapStart;



  @JsonKey(
    
    name: r'detectionId',
    required: true,
    includeIfNull: false,
  )


  final String detectionId;



  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;



  @JsonKey(
    
    name: r'timeOffsetMin',
    required: false,
    includeIfNull: false,
  )


  final num? timeOffsetMin;



  @JsonKey(
    
    name: r'detectionLengthM',
    required: false,
    includeIfNull: false,
  )


  final num? detectionLengthM;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'matchScore',
    required: true,
    includeIfNull: false,
  )


  final num matchScore;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final DarkVesselCandidatePayloadLabelEnum? label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DarkVesselCandidatePayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.mmsi == mmsi &&
      other.gapStart == gapStart &&
      other.detectionId == detectionId &&
      other.distanceKm == distanceKm &&
      other.timeOffsetMin == timeOffsetMin &&
      other.detectionLengthM == detectionLengthM &&
      other.matchScore == matchScore &&
      other.label == label;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        mmsi.hashCode +
        gapStart.hashCode +
        detectionId.hashCode +
        distanceKm.hashCode +
        timeOffsetMin.hashCode +
        (detectionLengthM == null ? 0 : detectionLengthM.hashCode) +
        matchScore.hashCode +
        label.hashCode;

  factory DarkVesselCandidatePayload.fromJson(Map<String, dynamic> json) => _$DarkVesselCandidatePayloadFromJson(json);

  Map<String, dynamic> toJson() => _$DarkVesselCandidatePayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DarkVesselCandidatePayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const DarkVesselCandidatePayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum DarkVesselCandidatePayloadLabelEnum {
@JsonValue(r'candidate; SAR detections are not identifications')
candidateSemicolonSARDetectionsAreNotIdentifications(r'candidate; SAR detections are not identifications');

const DarkVesselCandidatePayloadLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


