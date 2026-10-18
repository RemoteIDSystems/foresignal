//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/derived_payload_common_detector.dart';
import 'package:foresignal_api/src/model/derived_evidence_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'port_call_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PortCallPayload {
  /// Returns a new [PortCallPayload] instance.
  PortCallPayload({

    required  this.automated,

    required  this.notAWarning,

    required  this.detector,

     this.confidence,

    required  this.status,

     this.evidence,

    required  this.mmsi,

    required  this.portNumber,

     this.portName,

    required  this.arrival,

     this.departure,

     this.durationH,

     this.departureUnknown,
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


  final PortCallPayloadStatusEnum status;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<DerivedEvidenceRef>? evidence;



  @JsonKey(
    
    name: r'mmsi',
    required: true,
    includeIfNull: false,
  )


  final String mmsi;



  @JsonKey(
    
    name: r'portNumber',
    required: true,
    includeIfNull: false,
  )


  final String portNumber;



  @JsonKey(
    
    name: r'portName',
    required: false,
    includeIfNull: false,
  )


  final String? portName;



  @JsonKey(
    
    name: r'arrival',
    required: true,
    includeIfNull: false,
  )


  final DateTime arrival;



  @JsonKey(
    
    name: r'departure',
    required: false,
    includeIfNull: false,
  )


  final DateTime? departure;



  @JsonKey(
    
    name: r'durationH',
    required: false,
    includeIfNull: false,
  )


  final num? durationH;



      /// True when the departure time could not be observed; never guessed
  @JsonKey(
    
    name: r'departureUnknown',
    required: false,
    includeIfNull: false,
  )


  final bool? departureUnknown;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PortCallPayload &&
      other.automated == automated &&
      other.notAWarning == notAWarning &&
      other.detector == detector &&
      other.confidence == confidence &&
      other.status == status &&
      other.evidence == evidence &&
      other.mmsi == mmsi &&
      other.portNumber == portNumber &&
      other.portName == portName &&
      other.arrival == arrival &&
      other.departure == departure &&
      other.durationH == durationH &&
      other.departureUnknown == departureUnknown;

    @override
    int get hashCode =>
        automated.hashCode +
        notAWarning.hashCode +
        detector.hashCode +
        confidence.hashCode +
        status.hashCode +
        evidence.hashCode +
        mmsi.hashCode +
        portNumber.hashCode +
        (portName == null ? 0 : portName.hashCode) +
        arrival.hashCode +
        (departure == null ? 0 : departure.hashCode) +
        (durationH == null ? 0 : durationH.hashCode) +
        departureUnknown.hashCode;

  factory PortCallPayload.fromJson(Map<String, dynamic> json) => _$PortCallPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$PortCallPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PortCallPayloadStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient-data')
insufficientData(r'insufficient-data'),
@JsonValue(r'no-coverage')
noCoverage(r'no-coverage'),
@JsonValue(r'degraded-input')
degradedInput(r'degraded-input');

const PortCallPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


