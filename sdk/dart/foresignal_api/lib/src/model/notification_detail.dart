//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/notification_link.dart';
import 'package:foresignal_api/src/model/notification_action.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_detail.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationDetail {
  /// Returns a new [NotificationDetail] instance.
  NotificationDetail({

    required  this.id,

    required  this.event,

     this.subtype,

    required  this.severity,

    required  this.title,

     this.body,

     this.alertId,

     this.briefingRunId,

     this.ruleId,

    required  this.createdAt,

    required  this.read,

     this.alertState,

     this.links,

     this.actions,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final NotificationDetailEventEnum event;



  @JsonKey(
    
    name: r'subtype',
    required: false,
    includeIfNull: false,
  )


  final String? subtype;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final NotificationDetailSeverityEnum severity;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



      /// Markdown
  @JsonKey(
    
    name: r'body',
    required: false,
    includeIfNull: false,
  )


  final String? body;



  @JsonKey(
    
    name: r'alertId',
    required: false,
    includeIfNull: false,
  )


  final int? alertId;



  @JsonKey(
    
    name: r'briefingRunId',
    required: false,
    includeIfNull: false,
  )


  final String? briefingRunId;



  @JsonKey(
    
    name: r'ruleId',
    required: false,
    includeIfNull: false,
  )


  final String? ruleId;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



      /// Read on this device; reading never acknowledges an alert
  @JsonKey(
    
    name: r'read',
    required: true,
    includeIfNull: false,
  )


  final bool read;



      /// Live alert state
  @JsonKey(
    
    name: r'alertState',
    required: false,
    includeIfNull: false,
  )


  final NotificationDetailAlertStateEnum? alertState;



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final List<NotificationLink>? links;



      /// Only for firing alerts
  @JsonKey(
    
    name: r'actions',
    required: false,
    includeIfNull: false,
  )


  final List<NotificationAction>? actions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationDetail &&
      other.id == id &&
      other.event == event &&
      other.subtype == subtype &&
      other.severity == severity &&
      other.title == title &&
      other.body == body &&
      other.alertId == alertId &&
      other.briefingRunId == briefingRunId &&
      other.ruleId == ruleId &&
      other.createdAt == createdAt &&
      other.read == read &&
      other.alertState == alertState &&
      other.links == links &&
      other.actions == actions;

    @override
    int get hashCode =>
        id.hashCode +
        event.hashCode +
        (subtype == null ? 0 : subtype.hashCode) +
        severity.hashCode +
        title.hashCode +
        body.hashCode +
        (alertId == null ? 0 : alertId.hashCode) +
        (briefingRunId == null ? 0 : briefingRunId.hashCode) +
        (ruleId == null ? 0 : ruleId.hashCode) +
        createdAt.hashCode +
        read.hashCode +
        (alertState == null ? 0 : alertState.hashCode) +
        links.hashCode +
        actions.hashCode;

  factory NotificationDetail.fromJson(Map<String, dynamic> json) => _$NotificationDetailFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationDetailToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NotificationDetailEventEnum {
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'briefing')
briefing(r'briefing'),
@JsonValue(r'system')
system(r'system');

const NotificationDetailEventEnum(this.value);

final String value;

@override
String toString() => value;
}


enum NotificationDetailSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const NotificationDetailSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Live alert state
enum NotificationDetailAlertStateEnum {
@JsonValue(r'firing')
firing(r'firing'),
@JsonValue(r'acknowledged')
acknowledged(r'acknowledged'),
@JsonValue(r'resolved')
resolved(r'resolved');

const NotificationDetailAlertStateEnum(this.value);

final String value;

@override
String toString() => value;
}


