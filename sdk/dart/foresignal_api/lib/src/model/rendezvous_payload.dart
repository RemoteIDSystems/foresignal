//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rendezvous_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RendezvousPayload {
  /// Returns a new [RendezvousPayload] instance.
  RendezvousPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.vessels,

     this.distanceNm,

    required  this.startedAt,

     this.durationMin,

     this.nearestPortKm,
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


  final RendezvousPayloadStatusEnum status;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef>? evidence;



      /// Sorted entity ids or MMSIs
  @JsonKey(
    
    name: r'vessels',
    required: true,
    includeIfNull: false,
  )


  final List<String> vessels;



  @JsonKey(
    
    name: r'distanceNm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceNm;



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
    
    name: r'nearestPortKm',
    required: false,
    includeIfNull: false,
  )


  final num? nearestPortKm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RendezvousPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.vessels == vessels &&
      other.distanceNm == distanceNm &&
      other.startedAt == startedAt &&
      other.durationMin == durationMin &&
      other.nearestPortKm == nearestPortKm;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        vessels.hashCode +
        distanceNm.hashCode +
        startedAt.hashCode +
        durationMin.hashCode +
        (nearestPortKm == null ? 0 : nearestPortKm.hashCode);

  factory RendezvousPayload.fromJson(Map<String, dynamic> json) => _$RendezvousPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$RendezvousPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RendezvousPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const RendezvousPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


