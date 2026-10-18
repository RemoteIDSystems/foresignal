//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/notification_action_params.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_action.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationAction {
  /// Returns a new [NotificationAction] instance.
  NotificationAction({

    required  this.action,

     this.label,

     this.method,

    required  this.url,

     this.confirmUrl,

     this.params,

    required  this.expiresAt,
  });

  @JsonKey(
    
    name: r'action',
    required: true,
    includeIfNull: false,
  )


  final NotificationActionActionEnum action;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final NotificationActionMethodEnum? method;



      /// <publicBaseUrl>/v1/actions/eac_…
  @JsonKey(
    
    name: r'url',
    required: true,
    includeIfNull: false,
  )


  final String url;



      /// Same URL; GET shows the confirm page
  @JsonKey(
    
    name: r'confirmUrl',
    required: false,
    includeIfNull: false,
  )


  final String? confirmUrl;



  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final NotificationActionParams? params;



  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationAction &&
      other.action == action &&
      other.label == label &&
      other.method == method &&
      other.url == url &&
      other.confirmUrl == confirmUrl &&
      other.params == params &&
      other.expiresAt == expiresAt;

    @override
    int get hashCode =>
        action.hashCode +
        label.hashCode +
        method.hashCode +
        url.hashCode +
        confirmUrl.hashCode +
        params.hashCode +
        expiresAt.hashCode;

  factory NotificationAction.fromJson(Map<String, dynamic> json) => _$NotificationActionFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationActionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NotificationActionActionEnum {
@JsonValue(r'ack')
ack(r'ack'),
@JsonValue(r'snooze')
snooze(r'snooze'),
@JsonValue(r'mute')
mute(r'mute'),
@JsonValue(r'feedback:useful')
feedbackColonUseful(r'feedback:useful'),
@JsonValue(r'feedback:noise')
feedbackColonNoise(r'feedback:noise');

const NotificationActionActionEnum(this.value);

final String value;

@override
String toString() => value;
}


enum NotificationActionMethodEnum {
@JsonValue(r'POST')
POST(r'POST');

const NotificationActionMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


