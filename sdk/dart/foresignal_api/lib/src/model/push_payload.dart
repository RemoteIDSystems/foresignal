//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/push_payload_links.dart';
import 'package:foresignal_api/src/model/push_payload_actions_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushPayload {
  /// Returns a new [PushPayload] instance.
  PushPayload({

    required  this.v,

    required  this.event,

     this.id,

     this.alertId,

     this.ruleId,

     this.severity,

     this.title,

     this.shortTitle,

     this.body,

     this.url,

     this.links,

     this.tags,

     this.lang,

     this.tag,

     this.silent,

     this.ts,

     this.actions,

     this.confirmUrl,

     this.subscriptionId,

     this.code,

     this.exercise = false,
  });

  @JsonKey(
    
    name: r'v',
    required: true,
    includeIfNull: false,
  )


  final PushPayloadVEnum v;



  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final PushPayloadEventEnum event;



      /// Persisted notification id (018); alert id if absent
  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final int? id;



  @JsonKey(
    
    name: r'alertId',
    required: false,
    includeIfNull: false,
  )


  final int? alertId;



  @JsonKey(
    
    name: r'ruleId',
    required: false,
    includeIfNull: false,
  )


  final String? ruleId;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final PushPayloadSeverityEnum? severity;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'shortTitle',
    required: false,
    includeIfNull: false,
  )


  final String? shortTitle;



      /// Plain text; briefings send a 200-char excerpt; system events title and code only
  @JsonKey(
    
    name: r'body',
    required: false,
    includeIfNull: false,
  )


  final String? body;



      /// Alias of links.api
  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final PushPayloadLinks? links;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



  @JsonKey(
    
    name: r'lang',
    required: false,
    includeIfNull: false,
  )


  final String? lang;



      /// foresignal-alert-<alertId> or foresignal-<event>-<id>; repeats replace each other in the OS
  @JsonKey(
    
    name: r'tag',
    required: false,
    includeIfNull: false,
  )


  final String? tag;



  @JsonKey(
    
    name: r'silent',
    required: false,
    includeIfNull: false,
  )


  final bool? silent;



  @JsonKey(
    
    name: r'ts',
    required: false,
    includeIfNull: false,
  )


  final int? ts;



  @JsonKey(
    
    name: r'actions',
    required: false,
    includeIfNull: false,
  )


  final List<PushPayloadActionsInner>? actions;



      /// Confirm page when the browser has no action buttons (033)
  @JsonKey(
    
    name: r'confirmUrl',
    required: false,
    includeIfNull: false,
  )


  final String? confirmUrl;



      /// verify only
  @JsonKey(
    
    name: r'subscriptionId',
    required: false,
    includeIfNull: false,
  )


  final String? subscriptionId;



      /// verify only: 16 random bytes, base64url
  @JsonKey(
    
    name: r'code',
    required: false,
    includeIfNull: false,
  )


  final String? code;



      /// Exercise; urgency forced to normal
  @JsonKey(
    defaultValue: false,
    name: r'exercise',
    required: false,
    includeIfNull: false,
  )


  final bool? exercise;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushPayload &&
      other.v == v &&
      other.event == event &&
      other.id == id &&
      other.alertId == alertId &&
      other.ruleId == ruleId &&
      other.severity == severity &&
      other.title == title &&
      other.shortTitle == shortTitle &&
      other.body == body &&
      other.url == url &&
      other.links == links &&
      other.tags == tags &&
      other.lang == lang &&
      other.tag == tag &&
      other.silent == silent &&
      other.ts == ts &&
      other.actions == actions &&
      other.confirmUrl == confirmUrl &&
      other.subscriptionId == subscriptionId &&
      other.code == code &&
      other.exercise == exercise;

    @override
    int get hashCode =>
        v.hashCode +
        event.hashCode +
        id.hashCode +
        (alertId == null ? 0 : alertId.hashCode) +
        (ruleId == null ? 0 : ruleId.hashCode) +
        severity.hashCode +
        title.hashCode +
        shortTitle.hashCode +
        body.hashCode +
        url.hashCode +
        links.hashCode +
        tags.hashCode +
        lang.hashCode +
        tag.hashCode +
        silent.hashCode +
        ts.hashCode +
        actions.hashCode +
        confirmUrl.hashCode +
        subscriptionId.hashCode +
        code.hashCode +
        exercise.hashCode;

  factory PushPayload.fromJson(Map<String, dynamic> json) => _$PushPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$PushPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PushPayloadVEnum {
@JsonValue(1)
number1('1');

const PushPayloadVEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PushPayloadEventEnum {
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'briefing')
briefing(r'briefing'),
@JsonValue(r'system')
system(r'system'),
@JsonValue(r'verify')
verify(r'verify');

const PushPayloadEventEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PushPayloadSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const PushPayloadSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


