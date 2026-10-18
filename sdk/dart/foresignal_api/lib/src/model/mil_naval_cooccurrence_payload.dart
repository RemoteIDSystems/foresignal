//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mil_naval_cooccurrence_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MilNavalCooccurrencePayload {
  /// Returns a new [MilNavalCooccurrencePayload] instance.
  MilNavalCooccurrencePayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.cell,

    required  this.aircraftCount,

    required  this.vesselCount,

     this.aircraft,

     this.vessels,
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


  final MilNavalCooccurrencePayloadStatusEnum status;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef>? evidence;



  @JsonKey(
    
    name: r'cell',
    required: true,
    includeIfNull: false,
  )


  final String cell;



  @JsonKey(
    
    name: r'aircraftCount',
    required: true,
    includeIfNull: false,
  )


  final int aircraftCount;



  @JsonKey(
    
    name: r'vesselCount',
    required: true,
    includeIfNull: false,
  )


  final int vesselCount;



  @JsonKey(
    
    name: r'aircraft',
    required: false,
    includeIfNull: false,
  )


  final List<String>? aircraft;



  @JsonKey(
    
    name: r'vessels',
    required: false,
    includeIfNull: false,
  )


  final List<String>? vessels;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MilNavalCooccurrencePayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.cell == cell &&
      other.aircraftCount == aircraftCount &&
      other.vesselCount == vesselCount &&
      other.aircraft == aircraft &&
      other.vessels == vessels;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        cell.hashCode +
        aircraftCount.hashCode +
        vesselCount.hashCode +
        aircraft.hashCode +
        vessels.hashCode;

  factory MilNavalCooccurrencePayload.fromJson(Map<String, dynamic> json) => _$MilNavalCooccurrencePayloadFromJson(json);

  Map<String, dynamic> toJson() => _$MilNavalCooccurrencePayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MilNavalCooccurrencePayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const MilNavalCooccurrencePayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


