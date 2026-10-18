//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'nws_alert_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NwsAlertPayload {
  /// Returns a new [NwsAlertPayload] instance.
  NwsAlertPayload({

    required  this.event,

     this.eventCode,

     this.headline,

    required  this.severity,

     this.certainty,

     this.urgency,

     this.messageType,

     this.category,

     this.response,

     this.senderName,

     this.areaDesc,

     this.sent,

     this.effective,

     this.onset,

     this.expires,

     this.ends,

     this.ugc,

     this.same,

     this.vtec,

     this.motion,

     this.maxHailSizeIn,

     this.maxWindGustMph,

     this.tornadoDetection,

     this.thunderstormDamageThreat,

     this.description,

     this.instruction,

     this.web,

     this.geometrySource,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final String event;



  @JsonKey(
    
    name: r'eventCode',
    required: false,
    includeIfNull: false,
  )


  final String? eventCode;



  @JsonKey(
    
    name: r'headline',
    required: false,
    includeIfNull: false,
  )


  final String? headline;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final NwsAlertPayloadSeverityEnum severity;



  @JsonKey(
    
    name: r'certainty',
    required: false,
    includeIfNull: false,
  )


  final String? certainty;



  @JsonKey(
    
    name: r'urgency',
    required: false,
    includeIfNull: false,
  )


  final String? urgency;



  @JsonKey(
    
    name: r'messageType',
    required: false,
    includeIfNull: false,
  )


  final NwsAlertPayloadMessageTypeEnum? messageType;



  @JsonKey(
    
    name: r'category',
    required: false,
    includeIfNull: false,
  )


  final String? category;



  @JsonKey(
    
    name: r'response',
    required: false,
    includeIfNull: false,
  )


  final String? response;



  @JsonKey(
    
    name: r'senderName',
    required: false,
    includeIfNull: false,
  )


  final String? senderName;



  @JsonKey(
    
    name: r'areaDesc',
    required: false,
    includeIfNull: false,
  )


  final String? areaDesc;



  @JsonKey(
    
    name: r'sent',
    required: false,
    includeIfNull: false,
  )


  final DateTime? sent;



  @JsonKey(
    
    name: r'effective',
    required: false,
    includeIfNull: false,
  )


  final DateTime? effective;



  @JsonKey(
    
    name: r'onset',
    required: false,
    includeIfNull: false,
  )


  final DateTime? onset;



  @JsonKey(
    
    name: r'expires',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expires;



  @JsonKey(
    
    name: r'ends',
    required: false,
    includeIfNull: false,
  )


  final DateTime? ends;



  @JsonKey(
    
    name: r'ugc',
    required: false,
    includeIfNull: false,
  )


  final List<String>? ugc;



  @JsonKey(
    
    name: r'same',
    required: false,
    includeIfNull: false,
  )


  final List<String>? same;



  @JsonKey(
    
    name: r'vtec',
    required: false,
    includeIfNull: false,
  )


  final List<String>? vtec;



      /// eventMotionDescription
  @JsonKey(
    
    name: r'motion',
    required: false,
    includeIfNull: false,
  )


  final String? motion;



  @JsonKey(
    
    name: r'maxHailSizeIn',
    required: false,
    includeIfNull: false,
  )


  final num? maxHailSizeIn;



  @JsonKey(
    
    name: r'maxWindGustMph',
    required: false,
    includeIfNull: false,
  )


  final num? maxWindGustMph;



  @JsonKey(
    
    name: r'tornadoDetection',
    required: false,
    includeIfNull: false,
  )


  final String? tornadoDetection;



  @JsonKey(
    
    name: r'thunderstormDamageThreat',
    required: false,
    includeIfNull: false,
  )


  final String? thunderstormDamageThreat;



      /// Untrusted upstream text, ≤ 8 KB
  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



      /// Untrusted upstream text, ≤ 8 KB
  @JsonKey(
    
    name: r'instruction',
    required: false,
    includeIfNull: false,
  )


  final String? instruction;



  @JsonKey(
    
    name: r'web',
    required: false,
    includeIfNull: false,
  )


  final String? web;



  @JsonKey(
    
    name: r'geometrySource',
    required: false,
    includeIfNull: false,
  )


  final NwsAlertPayloadGeometrySourceEnum? geometrySource;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NwsAlertPayload &&
      other.event == event &&
      other.eventCode == eventCode &&
      other.headline == headline &&
      other.severity == severity &&
      other.certainty == certainty &&
      other.urgency == urgency &&
      other.messageType == messageType &&
      other.category == category &&
      other.response == response &&
      other.senderName == senderName &&
      other.areaDesc == areaDesc &&
      other.sent == sent &&
      other.effective == effective &&
      other.onset == onset &&
      other.expires == expires &&
      other.ends == ends &&
      other.ugc == ugc &&
      other.same == same &&
      other.vtec == vtec &&
      other.motion == motion &&
      other.maxHailSizeIn == maxHailSizeIn &&
      other.maxWindGustMph == maxWindGustMph &&
      other.tornadoDetection == tornadoDetection &&
      other.thunderstormDamageThreat == thunderstormDamageThreat &&
      other.description == description &&
      other.instruction == instruction &&
      other.web == web &&
      other.geometrySource == geometrySource;

    @override
    int get hashCode =>
        event.hashCode +
        (eventCode == null ? 0 : eventCode.hashCode) +
        headline.hashCode +
        severity.hashCode +
        certainty.hashCode +
        urgency.hashCode +
        messageType.hashCode +
        category.hashCode +
        response.hashCode +
        senderName.hashCode +
        areaDesc.hashCode +
        sent.hashCode +
        effective.hashCode +
        onset.hashCode +
        expires.hashCode +
        ends.hashCode +
        ugc.hashCode +
        same.hashCode +
        vtec.hashCode +
        motion.hashCode +
        maxHailSizeIn.hashCode +
        maxWindGustMph.hashCode +
        tornadoDetection.hashCode +
        thunderstormDamageThreat.hashCode +
        description.hashCode +
        instruction.hashCode +
        web.hashCode +
        geometrySource.hashCode;

  factory NwsAlertPayload.fromJson(Map<String, dynamic> json) => _$NwsAlertPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$NwsAlertPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NwsAlertPayloadSeverityEnum {
@JsonValue(r'Extreme')
extreme(r'Extreme'),
@JsonValue(r'Severe')
severe(r'Severe'),
@JsonValue(r'Moderate')
moderate(r'Moderate'),
@JsonValue(r'Minor')
minor(r'Minor'),
@JsonValue(r'Unknown')
unknown(r'Unknown');

const NwsAlertPayloadSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum NwsAlertPayloadMessageTypeEnum {
@JsonValue(r'Alert')
alert(r'Alert'),
@JsonValue(r'Update')
update(r'Update'),
@JsonValue(r'Cancel')
cancel(r'Cancel');

const NwsAlertPayloadMessageTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum NwsAlertPayloadGeometrySourceEnum {
@JsonValue(r'polygon')
polygon(r'polygon'),
@JsonValue(r'county-union')
countyUnion(r'county-union'),
@JsonValue(r'none')
none(r'none');

const NwsAlertPayloadGeometrySourceEnum(this.value);

final String value;

@override
String toString() => value;
}


