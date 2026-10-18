//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'felt_report_estimate_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FeltReportEstimatePayload {
  /// Returns a new [FeltReportEstimatePayload] instance.
  FeltReportEstimatePayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.eventId,

    required  this.placeId,

    required  this.mmi,

     this.distanceKm,

     this.feltLikely,

    required  this.method,

     this.coefficientSet,
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


  final FeltReportEstimatePayloadStatusEnum status;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef>? evidence;



  @JsonKey(
    
    name: r'eventId',
    required: true,
    includeIfNull: false,
  )


  final String eventId;



  @JsonKey(
    
    name: r'placeId',
    required: true,
    includeIfNull: false,
  )


  final String placeId;



  @JsonKey(
    
    name: r'mmi',
    required: true,
    includeIfNull: false,
  )


  final num mmi;



  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;



  @JsonKey(
    
    name: r'feltLikely',
    required: false,
    includeIfNull: false,
  )


  final bool? feltLikely;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final FeltReportEstimatePayloadMethodEnum method;



  @JsonKey(
    
    name: r'coefficientSet',
    required: false,
    includeIfNull: false,
  )


  final FeltReportEstimatePayloadCoefficientSetEnum? coefficientSet;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FeltReportEstimatePayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.eventId == eventId &&
      other.placeId == placeId &&
      other.mmi == mmi &&
      other.distanceKm == distanceKm &&
      other.feltLikely == feltLikely &&
      other.method == method &&
      other.coefficientSet == coefficientSet;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        eventId.hashCode +
        placeId.hashCode +
        mmi.hashCode +
        distanceKm.hashCode +
        feltLikely.hashCode +
        method.hashCode +
        (coefficientSet == null ? 0 : coefficientSet.hashCode);

  factory FeltReportEstimatePayload.fromJson(Map<String, dynamic> json) => _$FeltReportEstimatePayloadFromJson(json);

  Map<String, dynamic> toJson() => _$FeltReportEstimatePayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FeltReportEstimatePayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const FeltReportEstimatePayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum FeltReportEstimatePayloadMethodEnum {
@JsonValue(r'shakemap')
shakemap(r'shakemap'),
@JsonValue(r'ipe-aw2007')
ipeAw2007(r'ipe-aw2007');

const FeltReportEstimatePayloadMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


enum FeltReportEstimatePayloadCoefficientSetEnum {
@JsonValue(r'california')
california(r'california'),
@JsonValue(r'ceus')
ceus(r'ceus');

const FeltReportEstimatePayloadCoefficientSetEnum(this.value);

final String value;

@override
String toString() => value;
}


