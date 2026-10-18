//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'aircraft_fusion_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AircraftFusionPayload {
  /// Returns a new [AircraftFusionPayload] instance.
  AircraftFusionPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.hex,

    required  this.sources,

    required  this.primarySource,

     this.positionSource,
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


  final AircraftFusionPayloadStatusEnum status;



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
    
    name: r'sources',
    required: true,
    includeIfNull: false,
  )


  final List<String> sources;



  @JsonKey(
    
    name: r'primarySource',
    required: true,
    includeIfNull: false,
  )


  final String primarySource;



  @JsonKey(
    
    name: r'positionSource',
    required: false,
    includeIfNull: false,
  )


  final AircraftFusionPayloadPositionSourceEnum? positionSource;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AircraftFusionPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.hex == hex &&
      other.sources == sources &&
      other.primarySource == primarySource &&
      other.positionSource == positionSource;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        hex.hashCode +
        sources.hashCode +
        primarySource.hashCode +
        positionSource.hashCode;

  factory AircraftFusionPayload.fromJson(Map<String, dynamic> json) => _$AircraftFusionPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$AircraftFusionPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AircraftFusionPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const AircraftFusionPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AircraftFusionPayloadPositionSourceEnum {
@JsonValue(r'adsb')
adsb(r'adsb'),
@JsonValue(r'mlat')
mlat(r'mlat'),
@JsonValue(r'tisb')
tisb(r'tisb'),
@JsonValue(r'other')
other(r'other');

const AircraftFusionPayloadPositionSourceEnum(this.value);

final String value;

@override
String toString() => value;
}


