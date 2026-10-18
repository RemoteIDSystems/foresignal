//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'jamming_cell_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class JammingCellPayload {
  /// Returns a new [JammingCellPayload] instance.
  JammingCellPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.level,

     this.grid,

    required  this.cell,

     this.aircraft,

     this.badAircraft,

     this.ratio,

     this.windowMinutes = 15,
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


  final JammingCellPayloadStatusEnum status;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef>? evidence;



  @JsonKey(
    
    name: r'level',
    required: true,
    includeIfNull: false,
  )


  final JammingCellPayloadLevelEnum level;



  @JsonKey(
    
    name: r'grid',
    required: false,
    includeIfNull: false,
  )


  final JammingCellPayloadGridEnum? grid;



  @JsonKey(
    
    name: r'cell',
    required: true,
    includeIfNull: false,
  )


  final String cell;



      /// Distinct airborne ADS-B aircraft in the window
  @JsonKey(
    
    name: r'aircraft',
    required: false,
    includeIfNull: false,
  )


  final int? aircraft;



      /// Aircraft with NACp < 8 or NIC < 7
  @JsonKey(
    
    name: r'badAircraft',
    required: false,
    includeIfNull: false,
  )


  final int? badAircraft;



  @JsonKey(
    
    name: r'ratio',
    required: false,
    includeIfNull: false,
  )


  final num? ratio;



  @JsonKey(
    defaultValue: 15,
    name: r'windowMinutes',
    required: false,
    includeIfNull: false,
  )


  final int? windowMinutes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is JammingCellPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.level == level &&
      other.grid == grid &&
      other.cell == cell &&
      other.aircraft == aircraft &&
      other.badAircraft == badAircraft &&
      other.ratio == ratio &&
      other.windowMinutes == windowMinutes;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        level.hashCode +
        grid.hashCode +
        cell.hashCode +
        aircraft.hashCode +
        badAircraft.hashCode +
        ratio.hashCode +
        windowMinutes.hashCode;

  factory JammingCellPayload.fromJson(Map<String, dynamic> json) => _$JammingCellPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$JammingCellPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum JammingCellPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const JammingCellPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum JammingCellPayloadLevelEnum {
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'high')
high(r'high');

const JammingCellPayloadLevelEnum(this.value);

final String value;

@override
String toString() => value;
}


enum JammingCellPayloadGridEnum {
@JsonValue(r'h3')
h3(r'h3'),
@JsonValue(r'deg1')
deg1(r'deg1');

const JammingCellPayloadGridEnum(this.value);

final String value;

@override
String toString() => value;
}


