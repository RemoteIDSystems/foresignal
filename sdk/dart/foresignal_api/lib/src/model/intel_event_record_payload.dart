//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_event_record_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelEventRecordPayload {
  /// Returns a new [IntelEventRecordPayload] instance.
  IntelEventRecordPayload({

    required  this.type,

    required  this.severity,

    required  this.status,

    required  this.confidence,

     this.label,

     this.evidenceQuality,

     this.memberCount,

     this.signalFamilies,

    required  this.title,

    required  this.automated,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final IntelEventRecordPayloadTypeEnum type;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final Severity severity;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final IntelEventRecordPayloadStatusEnum status;



  @JsonKey(
    
    name: r'confidence',
    required: true,
    includeIfNull: false,
  )


  final num confidence;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final IntelEventRecordPayloadLabelEnum? label;



  @JsonKey(
    
    name: r'evidenceQuality',
    required: false,
    includeIfNull: false,
  )


  final IntelEventRecordPayloadEvidenceQualityEnum? evidenceQuality;



  @JsonKey(
    
    name: r'memberCount',
    required: false,
    includeIfNull: false,
  )


  final int? memberCount;



  @JsonKey(
    
    name: r'signalFamilies',
    required: false,
    includeIfNull: false,
  )


  final List<String>? signalFamilies;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'automated',
    required: true,
    includeIfNull: false,
  )


  final bool automated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelEventRecordPayload &&
      other.type == type &&
      other.severity == severity &&
      other.status == status &&
      other.confidence == confidence &&
      other.label == label &&
      other.evidenceQuality == evidenceQuality &&
      other.memberCount == memberCount &&
      other.signalFamilies == signalFamilies &&
      other.title == title &&
      other.automated == automated;

    @override
    int get hashCode =>
        type.hashCode +
        severity.hashCode +
        status.hashCode +
        confidence.hashCode +
        label.hashCode +
        evidenceQuality.hashCode +
        memberCount.hashCode +
        signalFamilies.hashCode +
        title.hashCode +
        automated.hashCode;

  factory IntelEventRecordPayload.fromJson(Map<String, dynamic> json) => _$IntelEventRecordPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$IntelEventRecordPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelEventRecordPayloadTypeEnum {
@JsonValue(r'maritime-disruption')
maritimeDisruption(r'maritime-disruption'),
@JsonValue(r'aviation-anomaly')
aviationAnomaly(r'aviation-anomaly'),
@JsonValue(r'infrastructure-disruption')
infrastructureDisruption(r'infrastructure-disruption'),
@JsonValue(r'weather-impact')
weatherImpact(r'weather-impact'),
@JsonValue(r'seismic')
seismic(r'seismic'),
@JsonValue(r'custom')
custom(r'custom');

const IntelEventRecordPayloadTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelEventRecordPayloadStatusEnum {
@JsonValue(r'emerging')
emerging(r'emerging'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'resolved')
resolved(r'resolved'),
@JsonValue(r'dismissed')
dismissed(r'dismissed');

const IntelEventRecordPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelEventRecordPayloadLabelEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'moderate')
moderate(r'moderate'),
@JsonValue(r'high')
high(r'high');

const IntelEventRecordPayloadLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelEventRecordPayloadEvidenceQualityEnum {
@JsonValue(r'strong')
strong(r'strong'),
@JsonValue(r'moderate')
moderate(r'moderate'),
@JsonValue(r'weak')
weak(r'weak'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const IntelEventRecordPayloadEvidenceQualityEnum(this.value);

final String value;

@override
String toString() => value;
}


