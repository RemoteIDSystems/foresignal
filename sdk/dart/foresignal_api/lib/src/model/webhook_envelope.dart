//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/notification_message.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'webhook_envelope.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WebhookEnvelope {
  /// Returns a new [WebhookEnvelope] instance.
  WebhookEnvelope({

    required  this.event,

    required  this.subtype,

    required  this.message,

     this.data,

    required  this.foresignalOrigin,

     this.exercise = false,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final WebhookEnvelopeEventEnum event;



  @JsonKey(
    
    name: r'subtype',
    required: true,
    includeIfNull: false,
  )


  final String subtype;



  @JsonKey(
    
    name: r'message',
    required: true,
    includeIfNull: false,
  )


  final NotificationMessage message;



  @JsonKey(
    
    name: r'data',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? data;



      /// Install id of the sending instance
  @JsonKey(
    
    name: r'foresignalOrigin',
    required: true,
    includeIfNull: false,
  )


  final String foresignalOrigin;



      /// Exercise delivery; also header X-Foresignal-Exercise: true
  @JsonKey(
    defaultValue: false,
    name: r'exercise',
    required: false,
    includeIfNull: false,
  )


  final bool? exercise;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WebhookEnvelope &&
      other.event == event &&
      other.subtype == subtype &&
      other.message == message &&
      other.data == data &&
      other.foresignalOrigin == foresignalOrigin &&
      other.exercise == exercise;

    @override
    int get hashCode =>
        event.hashCode +
        subtype.hashCode +
        message.hashCode +
        (data == null ? 0 : data.hashCode) +
        foresignalOrigin.hashCode +
        exercise.hashCode;

  factory WebhookEnvelope.fromJson(Map<String, dynamic> json) => _$WebhookEnvelopeFromJson(json);

  Map<String, dynamic> toJson() => _$WebhookEnvelopeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WebhookEnvelopeEventEnum {
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'briefing')
briefing(r'briefing'),
@JsonValue(r'system')
system(r'system'),
@JsonValue(r'test')
test(r'test');

const WebhookEnvelopeEventEnum(this.value);

final String value;

@override
String toString() => value;
}


