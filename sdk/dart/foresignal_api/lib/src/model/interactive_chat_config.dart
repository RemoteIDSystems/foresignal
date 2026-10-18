//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/allowed_chat.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'interactive_chat_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InteractiveChatConfig {
  /// Returns a new [InteractiveChatConfig] instance.
  InteractiveChatConfig({

     this.interactive = false,

     this.allowedChats,

     this.askRateLimitPerHour = 10,

     this.commandRateLimit = 30,

     this.takeOverWebhook = false,

     this.discoveryMode = false,
  });

      /// Accept commands from `allowedChats` (`/alerts`, `/alert`, `/ack`, `/resolve`, `/snooze`, `/ask`, `/brief`, `/status`, `/sources`, `/help`, plus `/confirm` and `/reject` codes on Matrix). Telegram needs a bot used only by this Foresignal instance; Matrix rooms must be unencrypted.
  @JsonKey(
    defaultValue: false,
    name: r'interactive',
    required: false,
    includeIfNull: false,
  )


  final bool? interactive;



      /// The chats that may command, each mapped to `read` or `agent` scope (never `admin`). Messages from any other chat or user are ignored silently. Telegram groups (negative ids) must list `userIds`; a Matrix room with more than two members and no `userIds` is ignored when the connector starts.
  @JsonKey(
    
    name: r'allowedChats',
    required: false,
    includeIfNull: false,
  )


  final List<AllowedChat>? allowedChats;



      /// `/ask` questions per chat per hour (0 turns `/ask` off for chats); the LLM budget applies as well
          // minimum: 0
          // maximum: 100000
  @JsonKey(
    defaultValue: 10,
    name: r'askRateLimitPerHour',
    required: false,
    includeIfNull: false,
  )


  final int? askRateLimitPerHour;



      /// Commands per chat per 10 minutes
          // minimum: 1
          // maximum: 100000
  @JsonKey(
    defaultValue: 30,
    name: r'commandRateLimit',
    required: false,
    includeIfNull: false,
  )


  final int? commandRateLimit;



      /// Telegram: delete a foreign webhook at start
  @JsonKey(
    defaultValue: false,
    name: r'takeOverWebhook',
    required: false,
    includeIfNull: false,
  )


  final bool? takeOverWebhook;



      /// `/whoami` answers any chat with its ids; auto-off after 10 minutes
  @JsonKey(
    defaultValue: false,
    name: r'discoveryMode',
    required: false,
    includeIfNull: false,
  )


  final bool? discoveryMode;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InteractiveChatConfig &&
      other.interactive == interactive &&
      other.allowedChats == allowedChats &&
      other.askRateLimitPerHour == askRateLimitPerHour &&
      other.commandRateLimit == commandRateLimit &&
      other.takeOverWebhook == takeOverWebhook &&
      other.discoveryMode == discoveryMode;

    @override
    int get hashCode =>
        interactive.hashCode +
        allowedChats.hashCode +
        askRateLimitPerHour.hashCode +
        commandRateLimit.hashCode +
        takeOverWebhook.hashCode +
        discoveryMode.hashCode;

  factory InteractiveChatConfig.fromJson(Map<String, dynamic> json) => _$InteractiveChatConfigFromJson(json);

  Map<String, dynamic> toJson() => _$InteractiveChatConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

