//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/notification_preference_channel.dart';
import 'package:foresignal_api/src/model/quiet_hours.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_preference.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationPreference {
  /// Returns a new [NotificationPreference] instance.
  NotificationPreference({

    required  this.id,

     this.principalId,

    required  this.eventTypes,

    required  this.channel,

     this.quietHours,

    required  this.enabled,

     this.createdAt,

     this.updatedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'principalId',
    required: false,
    includeIfNull: false,
  )


  final String? principalId;



  @JsonKey(
    
    name: r'eventTypes',
    required: true,
    includeIfNull: false,
  )


  final List<NotificationPreferenceEventTypesEnum> eventTypes;



  @JsonKey(
    
    name: r'channel',
    required: true,
    includeIfNull: false,
  )


  final NotificationPreferenceChannel channel;



  @JsonKey(
    
    name: r'quietHours',
    required: false,
    includeIfNull: false,
  )


  final QuietHours? quietHours;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationPreference &&
      other.id == id &&
      other.principalId == principalId &&
      other.eventTypes == eventTypes &&
      other.channel == channel &&
      other.quietHours == quietHours &&
      other.enabled == enabled &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        principalId.hashCode +
        eventTypes.hashCode +
        channel.hashCode +
        (quietHours == null ? 0 : quietHours.hashCode) +
        enabled.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory NotificationPreference.fromJson(Map<String, dynamic> json) => _$NotificationPreferenceFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationPreferenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NotificationPreferenceEventTypesEnum {
@JsonValue(r'mention')
mention(r'mention'),
@JsonValue(r'share')
share(r'share'),
@JsonValue(r'report')
report(r'report'),
@JsonValue(r'chronicle')
chronicle(r'chronicle'),
@JsonValue(r'assessment')
assessment(r'assessment'),
@JsonValue(r'comment-reply')
commentReply(r'comment-reply');

const NotificationPreferenceEventTypesEnum(this.value);

final String value;

@override
String toString() => value;
}


