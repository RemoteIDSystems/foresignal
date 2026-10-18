//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/notification.dart';
import 'package:foresignal_api/src/model/notification_badge.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stream_notifications200_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StreamNotifications200Response {
  /// Returns a new [StreamNotifications200Response] instance.
  StreamNotifications200Response({

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

    required  this.unread,

    required  this.unreadCritical,

     this.latestId,
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


  final StreamNotifications200ResponseEventEnum event;



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


  final StreamNotifications200ResponseSeverityEnum severity;



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


  final StreamNotifications200ResponseAlertStateEnum? alertState;



  @JsonKey(
    
    name: r'unread',
    required: true,
    includeIfNull: false,
  )


  final int unread;



  @JsonKey(
    
    name: r'unreadCritical',
    required: true,
    includeIfNull: false,
  )


  final int unreadCritical;



  @JsonKey(
    
    name: r'latestId',
    required: false,
    includeIfNull: false,
  )


  final int? latestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StreamNotifications200Response &&
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
      other.unread == unread &&
      other.unreadCritical == unreadCritical &&
      other.latestId == latestId;

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
        unread.hashCode +
        unreadCritical.hashCode +
        (latestId == null ? 0 : latestId.hashCode);

  factory StreamNotifications200Response.fromJson(Map<String, dynamic> json) => _$StreamNotifications200ResponseFromJson(json);

  Map<String, dynamic> toJson() => _$StreamNotifications200ResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StreamNotifications200ResponseEventEnum {
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'briefing')
briefing(r'briefing'),
@JsonValue(r'system')
system(r'system');

const StreamNotifications200ResponseEventEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StreamNotifications200ResponseSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const StreamNotifications200ResponseSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Live alert state
enum StreamNotifications200ResponseAlertStateEnum {
@JsonValue(r'firing')
firing(r'firing'),
@JsonValue(r'acknowledged')
acknowledged(r'acknowledged'),
@JsonValue(r'resolved')
resolved(r'resolved');

const StreamNotifications200ResponseAlertStateEnum(this.value);

final String value;

@override
String toString() => value;
}


