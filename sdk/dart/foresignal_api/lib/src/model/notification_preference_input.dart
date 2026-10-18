//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/notification_preference_input_channel.dart';
import 'package:foresignal_api/src/model/quiet_hours.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_preference_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationPreferenceInput {
  /// Returns a new [NotificationPreferenceInput] instance.
  NotificationPreferenceInput({

     this.id,

    required  this.eventTypes,

    required  this.channel,

     this.quietHours,

     this.enabled = true,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'eventTypes',
    required: true,
    includeIfNull: false,
  )


  final List<NotificationPreferenceInputEventTypesEnum> eventTypes;



  @JsonKey(
    
    name: r'channel',
    required: true,
    includeIfNull: false,
  )


  final NotificationPreferenceInputChannel channel;



  @JsonKey(
    
    name: r'quietHours',
    required: false,
    includeIfNull: false,
  )


  final QuietHours? quietHours;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationPreferenceInput &&
      other.id == id &&
      other.eventTypes == eventTypes &&
      other.channel == channel &&
      other.quietHours == quietHours &&
      other.enabled == enabled;

    @override
    int get hashCode =>
        (id == null ? 0 : id.hashCode) +
        eventTypes.hashCode +
        channel.hashCode +
        (quietHours == null ? 0 : quietHours.hashCode) +
        enabled.hashCode;

  factory NotificationPreferenceInput.fromJson(Map<String, dynamic> json) => _$NotificationPreferenceInputFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationPreferenceInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NotificationPreferenceInputEventTypesEnum {
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

const NotificationPreferenceInputEventTypesEnum(this.value);

final String value;

@override
String toString() => value;
}


