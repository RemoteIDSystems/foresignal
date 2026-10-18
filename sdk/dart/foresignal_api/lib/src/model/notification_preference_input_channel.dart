//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_preference_input_channel.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationPreferenceInputChannel {
  /// Returns a new [NotificationPreferenceInputChannel] instance.
  NotificationPreferenceInputChannel({

    required  this.type,

     this.config,

     this.secrets,

     this.language,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final NotificationPreferenceInputChannelTypeEnum type;



  @JsonKey(
    
    name: r'config',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? config;



      /// Write-only secret settings; omitted secrets keep their stored values
  @JsonKey(
    
    name: r'secrets',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? secrets;



      /// BCP 47 (141)
  @JsonKey(
    
    name: r'language',
    required: false,
    includeIfNull: false,
  )


  final String? language;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationPreferenceInputChannel &&
      other.type == type &&
      other.config == config &&
      other.secrets == secrets &&
      other.language == language;

    @override
    int get hashCode =>
        type.hashCode +
        config.hashCode +
        secrets.hashCode +
        (language == null ? 0 : language.hashCode);

  factory NotificationPreferenceInputChannel.fromJson(Map<String, dynamic> json) => _$NotificationPreferenceInputChannelFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationPreferenceInputChannelToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NotificationPreferenceInputChannelTypeEnum {
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

const NotificationPreferenceInputChannelTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


