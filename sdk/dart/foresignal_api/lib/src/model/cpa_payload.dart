//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cpa_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CpaPayload {
  /// Returns a new [CpaPayload] instance.
  CpaPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.vessels,

    required  this.cpaNm,

    required  this.tcpaMin,
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


  final CpaPayloadStatusEnum status;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef>? evidence;



  @JsonKey(
    
    name: r'vessels',
    required: true,
    includeIfNull: false,
  )


  final List<String> vessels;



  @JsonKey(
    
    name: r'cpaNm',
    required: true,
    includeIfNull: false,
  )


  final num cpaNm;



  @JsonKey(
    
    name: r'tcpaMin',
    required: true,
    includeIfNull: false,
  )


  final num tcpaMin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CpaPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.vessels == vessels &&
      other.cpaNm == cpaNm &&
      other.tcpaMin == tcpaMin;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        vessels.hashCode +
        cpaNm.hashCode +
        tcpaMin.hashCode;

  factory CpaPayload.fromJson(Map<String, dynamic> json) => _$CpaPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$CpaPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CpaPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const CpaPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


