//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'loiter_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LoiterPayload {
  /// Returns a new [LoiterPayload] instance.
  LoiterPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.entity,

     this.fixes,

    required  this.startedAt,

     this.durationMin,

     this.radiusNm,
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


  final LoiterPayloadStatusEnum status;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef>? evidence;



      /// 098 entity id when linked, otherwise the hex or MMSI
  @JsonKey(
    
    name: r'entity',
    required: true,
    includeIfNull: false,
  )


  final String entity;



  @JsonKey(
    
    name: r'fixes',
    required: false,
    includeIfNull: false,
  )


  final int? fixes;



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
    
    name: r'radiusNm',
    required: false,
    includeIfNull: false,
  )


  final num? radiusNm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LoiterPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.entity == entity &&
      other.fixes == fixes &&
      other.startedAt == startedAt &&
      other.durationMin == durationMin &&
      other.radiusNm == radiusNm;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        entity.hashCode +
        fixes.hashCode +
        startedAt.hashCode +
        durationMin.hashCode +
        radiusNm.hashCode;

  factory LoiterPayload.fromJson(Map<String, dynamic> json) => _$LoiterPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$LoiterPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LoiterPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const LoiterPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


