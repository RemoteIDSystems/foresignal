//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hf_propagation_anomaly_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HfPropagationAnomalyPayload {
  /// Returns a new [HfPropagationAnomalyPayload] instance.
  HfPropagationAnomalyPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.band,

    required  this.pathClass,

    required  this.utcHour,

     this.spotCount,

     this.baselineSpotCount,

     this.spotDropPct,

     this.medianSnrDb,

     this.baselineMedianSnrDb,

     this.snrDropDb,

     this.sunlit,

     this.flareConcurrent,
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


  final HfPropagationAnomalyPayloadStatusEnum status;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef>? evidence;



  @JsonKey(
    
    name: r'band',
    required: true,
    includeIfNull: false,
  )


  final String band;



  @JsonKey(
    
    name: r'pathClass',
    required: true,
    includeIfNull: false,
  )


  final String pathClass;



  @JsonKey(
    
    name: r'utcHour',
    required: true,
    includeIfNull: false,
  )


  final DateTime utcHour;



  @JsonKey(
    
    name: r'spotCount',
    required: false,
    includeIfNull: false,
  )


  final int? spotCount;



  @JsonKey(
    
    name: r'baselineSpotCount',
    required: false,
    includeIfNull: false,
  )


  final num? baselineSpotCount;



  @JsonKey(
    
    name: r'spotDropPct',
    required: false,
    includeIfNull: false,
  )


  final num? spotDropPct;



  @JsonKey(
    
    name: r'medianSnrDb',
    required: false,
    includeIfNull: false,
  )


  final num? medianSnrDb;



  @JsonKey(
    
    name: r'baselineMedianSnrDb',
    required: false,
    includeIfNull: false,
  )


  final num? baselineMedianSnrDb;



  @JsonKey(
    
    name: r'snrDropDb',
    required: false,
    includeIfNull: false,
  )


  final num? snrDropDb;



  @JsonKey(
    
    name: r'sunlit',
    required: false,
    includeIfNull: false,
  )


  final bool? sunlit;



      /// Concurrent SWPC X-ray class when known
  @JsonKey(
    
    name: r'flareConcurrent',
    required: false,
    includeIfNull: false,
  )


  final String? flareConcurrent;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HfPropagationAnomalyPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.band == band &&
      other.pathClass == pathClass &&
      other.utcHour == utcHour &&
      other.spotCount == spotCount &&
      other.baselineSpotCount == baselineSpotCount &&
      other.spotDropPct == spotDropPct &&
      other.medianSnrDb == medianSnrDb &&
      other.baselineMedianSnrDb == baselineMedianSnrDb &&
      other.snrDropDb == snrDropDb &&
      other.sunlit == sunlit &&
      other.flareConcurrent == flareConcurrent;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        band.hashCode +
        pathClass.hashCode +
        utcHour.hashCode +
        spotCount.hashCode +
        baselineSpotCount.hashCode +
        spotDropPct.hashCode +
        (medianSnrDb == null ? 0 : medianSnrDb.hashCode) +
        (baselineMedianSnrDb == null ? 0 : baselineMedianSnrDb.hashCode) +
        (snrDropDb == null ? 0 : snrDropDb.hashCode) +
        sunlit.hashCode +
        (flareConcurrent == null ? 0 : flareConcurrent.hashCode);

  factory HfPropagationAnomalyPayload.fromJson(Map<String, dynamic> json) => _$HfPropagationAnomalyPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$HfPropagationAnomalyPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HfPropagationAnomalyPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const HfPropagationAnomalyPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


