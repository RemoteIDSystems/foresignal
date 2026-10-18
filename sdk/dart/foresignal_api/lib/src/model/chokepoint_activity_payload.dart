//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chokepoint_activity_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChokepointActivityPayload {
  /// Returns a new [ChokepointActivityPayload] instance.
  ChokepointActivityPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.chokepointId,

     this.name,

    required  this.hourStart,

     this.transits,

     this.uTurns,

     this.stopped,

     this.coverageStatus,
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


  final ChokepointActivityPayloadStatusEnum status;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef>? evidence;



  @JsonKey(
    
    name: r'chokepointId',
    required: true,
    includeIfNull: false,
  )


  final String chokepointId;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'hourStart',
    required: true,
    includeIfNull: false,
  )


  final DateTime hourStart;



  @JsonKey(
    
    name: r'transits',
    required: false,
    includeIfNull: false,
  )


  final int? transits;



  @JsonKey(
    
    name: r'uTurns',
    required: false,
    includeIfNull: false,
  )


  final int? uTurns;



  @JsonKey(
    
    name: r'stopped',
    required: false,
    includeIfNull: false,
  )


  final int? stopped;



  @JsonKey(
    
    name: r'coverageStatus',
    required: false,
    includeIfNull: false,
  )


  final ChokepointActivityPayloadCoverageStatusEnum? coverageStatus;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChokepointActivityPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.chokepointId == chokepointId &&
      other.name == name &&
      other.hourStart == hourStart &&
      other.transits == transits &&
      other.uTurns == uTurns &&
      other.stopped == stopped &&
      other.coverageStatus == coverageStatus;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        chokepointId.hashCode +
        name.hashCode +
        hourStart.hashCode +
        transits.hashCode +
        uTurns.hashCode +
        stopped.hashCode +
        coverageStatus.hashCode;

  factory ChokepointActivityPayload.fromJson(Map<String, dynamic> json) => _$ChokepointActivityPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$ChokepointActivityPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChokepointActivityPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const ChokepointActivityPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChokepointActivityPayloadCoverageStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const ChokepointActivityPayloadCoverageStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


