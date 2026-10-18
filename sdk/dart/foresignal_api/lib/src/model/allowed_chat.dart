//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'allowed_chat.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AllowedChat {
  /// Returns a new [AllowedChat] instance.
  AllowedChat({

    required  this.chatId,

     this.name,

    required  this.scope,

     this.userIds,

     this.privateAccess = false,
  });

      /// Telegram numeric chat id as a string (negative for groups) or Matrix `!room:server`
  @JsonKey(
    
    name: r'chatId',
    required: true,
    includeIfNull: false,
  )


  final String chatId;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



      /// `read`: /help, /status, /alerts, /alert, /ask (proposed actions listed, not executable), /brief, /sources. `agent`: also /ack, /resolve, /snooze, /brief run and confirming proposed actions. Checked against the same AccessPolicy rule as the REST operation each command mirrors.
  @JsonKey(
    
    name: r'scope',
    required: true,
    includeIfNull: false,
  )


  final AllowedChatScopeEnum scope;



      /// Allowed senders (Telegram user ids or Matrix MXIDs)
  @JsonKey(
    
    name: r'userIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? userIds;



      /// The 039 private grant: the chat may see private sources (Ask still follows the LLM egress rules)
  @JsonKey(
    defaultValue: false,
    name: r'privateAccess',
    required: false,
    includeIfNull: false,
  )


  final bool? privateAccess;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AllowedChat &&
      other.chatId == chatId &&
      other.name == name &&
      other.scope == scope &&
      other.userIds == userIds &&
      other.privateAccess == privateAccess;

    @override
    int get hashCode =>
        chatId.hashCode +
        name.hashCode +
        scope.hashCode +
        userIds.hashCode +
        privateAccess.hashCode;

  factory AllowedChat.fromJson(Map<String, dynamic> json) => _$AllowedChatFromJson(json);

  Map<String, dynamic> toJson() => _$AllowedChatToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `read`: /help, /status, /alerts, /alert, /ask (proposed actions listed, not executable), /brief, /sources. `agent`: also /ack, /resolve, /snooze, /brief run and confirming proposed actions. Checked against the same AccessPolicy rule as the REST operation each command mirrors.
enum AllowedChatScopeEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent');

const AllowedChatScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


