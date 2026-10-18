//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/severity.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'holdover_fire_risk_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HoldoverFireRiskPayload {
  /// Returns a new [HoldoverFireRiskPayload] instance.
  HoldoverFireRiskPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.cell,

    required  this.utcDay,

    required  this.flashCount,

     this.qpe72hMm,

     this.precipSource,

     this.relativeHumidityPct,

    required  this.ignitionConfirmed,

     this.ignitionDelayH,

     this.watchUntil,

     this.severity,
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


  final HoldoverFireRiskPayloadStatusEnum status;



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
    
    name: r'utcDay',
    required: true,
    includeIfNull: false,
  )


  final DateTime utcDay;



  @JsonKey(
    
    name: r'flashCount',
    required: true,
    includeIfNull: false,
  )


  final int flashCount;



  @JsonKey(
    
    name: r'qpe72hMm',
    required: false,
    includeIfNull: false,
  )


  final num? qpe72hMm;



  @JsonKey(
    
    name: r'precipSource',
    required: false,
    includeIfNull: false,
  )


  final HoldoverFireRiskPayloadPrecipSourceEnum? precipSource;



  @JsonKey(
    
    name: r'relativeHumidityPct',
    required: false,
    includeIfNull: false,
  )


  final num? relativeHumidityPct;



  @JsonKey(
    
    name: r'ignitionConfirmed',
    required: true,
    includeIfNull: false,
  )


  final bool ignitionConfirmed;



  @JsonKey(
    
    name: r'ignitionDelayH',
    required: false,
    includeIfNull: false,
  )


  final num? ignitionDelayH;



  @JsonKey(
    
    name: r'watchUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? watchUntil;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final Severity? severity;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HoldoverFireRiskPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.cell == cell &&
      other.utcDay == utcDay &&
      other.flashCount == flashCount &&
      other.qpe72hMm == qpe72hMm &&
      other.precipSource == precipSource &&
      other.relativeHumidityPct == relativeHumidityPct &&
      other.ignitionConfirmed == ignitionConfirmed &&
      other.ignitionDelayH == ignitionDelayH &&
      other.watchUntil == watchUntil &&
      other.severity == severity;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        cell.hashCode +
        utcDay.hashCode +
        flashCount.hashCode +
        (qpe72hMm == null ? 0 : qpe72hMm.hashCode) +
        precipSource.hashCode +
        (relativeHumidityPct == null ? 0 : relativeHumidityPct.hashCode) +
        ignitionConfirmed.hashCode +
        (ignitionDelayH == null ? 0 : ignitionDelayH.hashCode) +
        watchUntil.hashCode +
        severity.hashCode;

  factory HoldoverFireRiskPayload.fromJson(Map<String, dynamic> json) => _$HoldoverFireRiskPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$HoldoverFireRiskPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HoldoverFireRiskPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const HoldoverFireRiskPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum HoldoverFireRiskPayloadPrecipSourceEnum {
@JsonValue(r'mrms')
mrms(r'mrms'),
@JsonValue(r'model')
model(r'model');

const HoldoverFireRiskPayloadPrecipSourceEnum(this.value);

final String value;

@override
String toString() => value;
}


