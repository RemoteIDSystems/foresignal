//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_preference_channel.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationPreferenceChannel {
  /// Returns a new [NotificationPreferenceChannel] instance.
  NotificationPreferenceChannel({

     this.channelId,

    required  this.type,

     this.config,

     this.secretFields,

     this.language,
  });

      /// Id of the 018 channel row owned by the principal
  @JsonKey(
    
    name: r'channelId',
    required: false,
    includeIfNull: false,
  )


  final String? channelId;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final NotificationPreferenceChannelTypeEnum type;



      /// Non-secret settings per the channel type's configFields
  @JsonKey(
    
    name: r'config',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? config;



  @JsonKey(
    
    name: r'secretFields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? secretFields;



      /// BCP 47 language for translated notifications (141)
  @JsonKey(
    
    name: r'language',
    required: false,
    includeIfNull: false,
  )


  final String? language;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationPreferenceChannel &&
      other.channelId == channelId &&
      other.type == type &&
      other.config == config &&
      other.secretFields == secretFields &&
      other.language == language;

    @override
    int get hashCode =>
        channelId.hashCode +
        type.hashCode +
        config.hashCode +
        secretFields.hashCode +
        (language == null ? 0 : language.hashCode);

  factory NotificationPreferenceChannel.fromJson(Map<String, dynamic> json) => _$NotificationPreferenceChannelFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationPreferenceChannelToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NotificationPreferenceChannelTypeEnum {
@JsonValue(r'webhook')
webhook(r'webhook'),
@JsonValue(r'slack')
slack(r'slack'),
@JsonValue(r'mattermost')
mattermost(r'mattermost'),
@JsonValue(r'discord')
discord(r'discord'),
@JsonValue(r'teams')
teams(r'teams'),
@JsonValue(r'google-chat')
googleChat(r'google-chat'),
@JsonValue(r'telegram')
telegram(r'telegram'),
@JsonValue(r'matrix')
matrix(r'matrix'),
@JsonValue(r'ntfy')
ntfy(r'ntfy'),
@JsonValue(r'gotify')
gotify(r'gotify'),
@JsonValue(r'pushover')
pushover(r'pushover'),
@JsonValue(r'email')
email(r'email'),
@JsonValue(r'webpush')
webpush(r'webpush');

const NotificationPreferenceChannelTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


