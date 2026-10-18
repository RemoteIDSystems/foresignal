//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'emergency_squawk_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EmergencySquawkPayload {
  /// Returns a new [EmergencySquawkPayload] instance.
  EmergencySquawkPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.hex,

     this.squawk,

     this.emergency,

    required  this.meaning,

    required  this.unverified,
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


  final EmergencySquawkPayloadStatusEnum status;



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
    
    name: r'squawk',
    required: false,
    includeIfNull: false,
  )


  final String? squawk;



  @JsonKey(
    
    name: r'emergency',
    required: false,
    includeIfNull: false,
  )


  final String? emergency;



  @JsonKey(
    
    name: r'meaning',
    required: true,
    includeIfNull: false,
  )


  final EmergencySquawkPayloadMeaningEnum meaning;



  @JsonKey(
    
    name: r'unverified',
    required: true,
    includeIfNull: false,
  )


  final bool unverified;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EmergencySquawkPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.hex == hex &&
      other.squawk == squawk &&
      other.emergency == emergency &&
      other.meaning == meaning &&
      other.unverified == unverified;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        hex.hashCode +
        (squawk == null ? 0 : squawk.hashCode) +
        (emergency == null ? 0 : emergency.hashCode) +
        meaning.hashCode +
        unverified.hashCode;

  factory EmergencySquawkPayload.fromJson(Map<String, dynamic> json) => _$EmergencySquawkPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$EmergencySquawkPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EmergencySquawkPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const EmergencySquawkPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EmergencySquawkPayloadMeaningEnum {
@JsonValue(r'unlawful interference')
unlawfulInterference(r'unlawful interference'),
@JsonValue(r'radio failure')
radioFailure(r'radio failure'),
@JsonValue(r'emergency')
emergency(r'emergency');

const EmergencySquawkPayloadMeaningEnum(this.value);

final String value;

@override
String toString() => value;
}


