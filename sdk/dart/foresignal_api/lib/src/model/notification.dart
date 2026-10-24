//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Notification {
  /// Returns a new [Notification] instance.
  Notification({

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
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



      /// Expected to grow (`x-extensible-enum`): a client must accept a value not listed here.
  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final String event;



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


  final NotificationSeverityEnum severity;



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


  final NotificationAlertStateEnum? alertState;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Notification &&
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
      other.alertState == alertState;

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
        (alertState == null ? 0 : alertState.hashCode);

  factory Notification.fromJson(Map<String, dynamic> json) => _$NotificationFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NotificationSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const NotificationSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Live alert state
enum NotificationAlertStateEnum {
@JsonValue(r'firing')
firing(r'firing'),
@JsonValue(r'acknowledged')
acknowledged(r'acknowledged'),
@JsonValue(r'resolved')
resolved(r'resolved');

const NotificationAlertStateEnum(this.value);

final String value;

@override
String toString() => value;
}


