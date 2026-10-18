//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/notification_settings_chatops.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationSettings {
  /// Returns a new [NotificationSettings] instance.
  NotificationSettings({

     this.pausedUntil,

     this.pauseBypassSeverity,

     this.chatops,

     this.imageLinks = false,

     this.defaultLanguage = 'en',
  });

      /// Pause all deliveries until this time; paused deliveries are released as digests
  @JsonKey(
    
    name: r'pausedUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? pausedUntil;



  @JsonKey(
    
    name: r'pauseBypassSeverity',
    required: false,
    includeIfNull: false,
  )


  final NotificationSettingsPauseBypassSeverityEnum? pauseBypassSeverity;



  @JsonKey(
    
    name: r'chatops',
    required: false,
    includeIfNull: false,
  )


  final NotificationSettingsChatops? chatops;



      /// 108: add feed-key image links to map attachments for channels that cannot take attachments; requires an https publicBaseUrl (400 IMAGE_LINKS_REQUIRE_HTTPS)
  @JsonKey(
    defaultValue: false,
    name: r'imageLinks',
    required: false,
    includeIfNull: false,
  )


  final bool? imageLinks;



      /// 141 (`notify.defaultLanguage`): BCP 47 language notifications are written in; a channel whose `language` differs gets the title and body translated
  @JsonKey(
    defaultValue: 'en',
    name: r'defaultLanguage',
    required: false,
    includeIfNull: false,
  )


  final String? defaultLanguage;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationSettings &&
      other.pausedUntil == pausedUntil &&
      other.pauseBypassSeverity == pauseBypassSeverity &&
      other.chatops == chatops &&
      other.imageLinks == imageLinks &&
      other.defaultLanguage == defaultLanguage;

    @override
    int get hashCode =>
        (pausedUntil == null ? 0 : pausedUntil.hashCode) +
        (pauseBypassSeverity == null ? 0 : pauseBypassSeverity.hashCode) +
        chatops.hashCode +
        imageLinks.hashCode +
        defaultLanguage.hashCode;

  factory NotificationSettings.fromJson(Map<String, dynamic> json) => _$NotificationSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NotificationSettingsPauseBypassSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const NotificationSettingsPauseBypassSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


