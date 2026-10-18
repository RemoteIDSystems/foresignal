//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:foresignal_api/src/model/holding_pattern_payload_all_of_nearest_airport.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'holding_pattern_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HoldingPatternPayload {
  /// Returns a new [HoldingPatternPayload] instance.
  HoldingPatternPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.hex,

    required  this.turnsDeg,

     this.durationMin,

     this.altitudeFt,

     this.nearestAirport,
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


  final HoldingPatternPayloadStatusEnum status;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef>? evidence;



  @JsonKey(
    
    name: r'hex',
    required: true,
    includeIfNull: false,
  )


  final String hex;



  @JsonKey(
    
    name: r'turnsDeg',
    required: true,
    includeIfNull: false,
  )


  final num turnsDeg;



  @JsonKey(
    
    name: r'durationMin',
    required: false,
    includeIfNull: false,
  )


  final num? durationMin;



  @JsonKey(
    
    name: r'altitudeFt',
    required: false,
    includeIfNull: false,
  )


  final num? altitudeFt;



  @JsonKey(
    
    name: r'nearestAirport',
    required: false,
    includeIfNull: false,
  )


  final HoldingPatternPayloadAllOfNearestAirport? nearestAirport;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HoldingPatternPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.hex == hex &&
      other.turnsDeg == turnsDeg &&
      other.durationMin == durationMin &&
      other.altitudeFt == altitudeFt &&
      other.nearestAirport == nearestAirport;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        hex.hashCode +
        turnsDeg.hashCode +
        durationMin.hashCode +
        altitudeFt.hashCode +
        (nearestAirport == null ? 0 : nearestAirport.hashCode);

  factory HoldingPatternPayload.fromJson(Map<String, dynamic> json) => _$HoldingPatternPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$HoldingPatternPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HoldingPatternPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const HoldingPatternPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


