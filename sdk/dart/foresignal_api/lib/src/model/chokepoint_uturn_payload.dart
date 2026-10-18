//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chokepoint_uturn_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChokepointUturnPayload {
  /// Returns a new [ChokepointUturnPayload] instance.
  ChokepointUturnPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.mmsi,

    required  this.chokepointId,

    required  this.at,

    required  this.manoeuvre,

     this.courseChangeDeg,

     this.speedDropPct,
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


  final ChokepointUturnPayloadStatusEnum status;



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
    
    name: r'chokepointId',
    required: true,
    includeIfNull: false,
  )


  final String chokepointId;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'manoeuvre',
    required: true,
    includeIfNull: false,
  )


  final ChokepointUturnPayloadManoeuvreEnum manoeuvre;



  @JsonKey(
    
    name: r'courseChangeDeg',
    required: false,
    includeIfNull: false,
  )


  final num? courseChangeDeg;



  @JsonKey(
    
    name: r'speedDropPct',
    required: false,
    includeIfNull: false,
  )


  final num? speedDropPct;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChokepointUturnPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.mmsi == mmsi &&
      other.chokepointId == chokepointId &&
      other.at == at &&
      other.manoeuvre == manoeuvre &&
      other.courseChangeDeg == courseChangeDeg &&
      other.speedDropPct == speedDropPct;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        mmsi.hashCode +
        chokepointId.hashCode +
        at.hashCode +
        manoeuvre.hashCode +
        (courseChangeDeg == null ? 0 : courseChangeDeg.hashCode) +
        (speedDropPct == null ? 0 : speedDropPct.hashCode);

  factory ChokepointUturnPayload.fromJson(Map<String, dynamic> json) => _$ChokepointUturnPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$ChokepointUturnPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChokepointUturnPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const ChokepointUturnPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChokepointUturnPayloadManoeuvreEnum {
@JsonValue(r'course-change')
courseChange(r'course-change'),
@JsonValue(r'u-turn')
uTurn(r'u-turn'),
@JsonValue(r'speed-drop')
speedDrop(r'speed-drop');

const ChokepointUturnPayloadManoeuvreEnum(this.value);

final String value;

@override
String toString() => value;
}


