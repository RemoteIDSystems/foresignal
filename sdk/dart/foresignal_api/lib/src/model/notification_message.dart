//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attachment.dart';
import 'package:foresignal_api/src/model/notification_link.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_message.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationMessage {
  /// Returns a new [NotificationMessage] instance.
  NotificationMessage({

    required  this.id,

    required  this.event,

    required  this.subtype,

    required  this.title,

     this.shortTitle,

     this.bodyMarkdown,

    required  this.severity,

     this.url,

     this.links,

     this.data,

     this.tags,

     this.attachments,

     this.updateOf,

     this.origin,

     this.createdAt,

     this.exercise = false,
  });

      /// Notification id
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



      /// Grows as the server adds events: a client must accept a value not listed here.
  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final String event;



      /// e.g. alert.fired, alert.acknowledged, alert.snoozed, alert.resolved, alert.digest (a quiet-hours, pause, backlog or grouping digest), system.source_failing, system.rule_storm, system.delivery_failed, system.push_provider_failed (032), briefing.run (029)
  @JsonKey(
    
    name: r'subtype',
    required: true,
    includeIfNull: false,
  )


  final String subtype;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



      /// ≤ 40 chars, for watches, mesh and push
  @JsonKey(
    
    name: r'shortTitle',
    required: false,
    includeIfNull: false,
  )


  final String? shortTitle;



      /// Foresignal markdown subset
  @JsonKey(
    
    name: r'bodyMarkdown',
    required: false,
    includeIfNull: false,
  )


  final String? bodyMarkdown;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final NotificationMessageSeverityEnum severity;



      /// API link, e.g. `{publicBaseUrl}/v1/alerts/{id}`
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


  final List<NotificationLink>? links;



      /// Full event data (alert or briefing-run JSON). Alert data carries `context` (056): the computed alert context with `context.weather` (`WeatherContext`, its `summary` sentence and sections) when the alert has one
  @JsonKey(
    
    name: r'data',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? data;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



      /// Image attachments as media references (filled by 055)
  @JsonKey(
    
    name: r'attachments',
    required: false,
    includeIfNull: false,
  )


  final List<Attachment>? attachments;



      /// Notification this one updates (lifecycle updates)
  @JsonKey(
    
    name: r'updateOf',
    required: false,
    includeIfNull: false,
  )


  final int? updateOf;



      /// Install id of the sending instance (loop guard)
  @JsonKey(
    
    name: r'origin',
    required: false,
    includeIfNull: false,
  )


  final String? origin;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



      /// Title prefixed [EXERCISE] and exercise footer appended
  @JsonKey(
    defaultValue: false,
    name: r'exercise',
    required: false,
    includeIfNull: false,
  )


  final bool? exercise;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationMessage &&
      other.id == id &&
      other.event == event &&
      other.subtype == subtype &&
      other.title == title &&
      other.shortTitle == shortTitle &&
      other.bodyMarkdown == bodyMarkdown &&
      other.severity == severity &&
      other.url == url &&
      other.links == links &&
      other.data == data &&
      other.tags == tags &&
      other.attachments == attachments &&
      other.updateOf == updateOf &&
      other.origin == origin &&
      other.createdAt == createdAt &&
      other.exercise == exercise;

    @override
    int get hashCode =>
        id.hashCode +
        event.hashCode +
        subtype.hashCode +
        title.hashCode +
        (shortTitle == null ? 0 : shortTitle.hashCode) +
        (bodyMarkdown == null ? 0 : bodyMarkdown.hashCode) +
        severity.hashCode +
        (url == null ? 0 : url.hashCode) +
        links.hashCode +
        (data == null ? 0 : data.hashCode) +
        tags.hashCode +
        attachments.hashCode +
        (updateOf == null ? 0 : updateOf.hashCode) +
        origin.hashCode +
        createdAt.hashCode +
        exercise.hashCode;

  factory NotificationMessage.fromJson(Map<String, dynamic> json) => _$NotificationMessageFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationMessageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NotificationMessageSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const NotificationMessageSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


