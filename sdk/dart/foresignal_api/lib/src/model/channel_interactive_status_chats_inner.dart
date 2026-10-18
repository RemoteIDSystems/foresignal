//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'channel_interactive_status_chats_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChannelInteractiveStatusChatsInner {
  /// Returns a new [ChannelInteractiveStatusChatsInner] instance.
  ChannelInteractiveStatusChatsInner({

    required  this.chatId,

     this.name,

    required  this.scope,

     this.userIds,

     this.privateAccess,

    required  this.active,

     this.reason,
  });

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



  @JsonKey(
    
    name: r'scope',
    required: true,
    includeIfNull: false,
  )


  final ChannelInteractiveStatusChatsInnerScopeEnum scope;



  @JsonKey(
    
    name: r'userIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? userIds;



  @JsonKey(
    
    name: r'privateAccess',
    required: false,
    includeIfNull: false,
  )


  final bool? privateAccess;



      /// false when ignored (e.g. an encrypted Matrix room)
  @JsonKey(
    
    name: r'active',
    required: true,
    includeIfNull: false,
  )


  final bool active;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChannelInteractiveStatusChatsInner &&
      other.chatId == chatId &&
      other.name == name &&
      other.scope == scope &&
      other.userIds == userIds &&
      other.privateAccess == privateAccess &&
      other.active == active &&
      other.reason == reason;

    @override
    int get hashCode =>
        chatId.hashCode +
        (name == null ? 0 : name.hashCode) +
        scope.hashCode +
        userIds.hashCode +
        privateAccess.hashCode +
        active.hashCode +
        (reason == null ? 0 : reason.hashCode);

  factory ChannelInteractiveStatusChatsInner.fromJson(Map<String, dynamic> json) => _$ChannelInteractiveStatusChatsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChannelInteractiveStatusChatsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChannelInteractiveStatusChatsInnerScopeEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent');

const ChannelInteractiveStatusChatsInnerScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


