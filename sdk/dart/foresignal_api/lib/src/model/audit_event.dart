//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'audit_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AuditEvent {
  /// Returns a new [AuditEvent] instance.
  AuditEvent({

    required  this.id,

    required  this.at,

    required  this.actorKind,

     this.tokenId,

     this.tokenName,

     this.principalId,

     this.scope,

     this.method,

     this.path,

     this.operationId,

     this.status,

     this.targetType,

     this.targetId,

     this.remoteIp,

     this.detail,

     this.userId,

     this.userEmail,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



      /// `user` = a signed-in user (R2-013; `userId` and `userEmail` say who, `tokenId` is the session); `action` = a notification action capability (033); `chat` = a command from an allowlisted chat of an interactive channel (061; `tokenName` is the chat principal `chat:{channelId}:{chatId}[:{userId}]`); `mesh` = an `#E ACK` from an allowlisted sender on a private mesh channel (084; `tokenName` is `mesh:{sender}`)
  @JsonKey(
    
    name: r'actorKind',
    required: true,
    includeIfNull: false,
  )


  final AuditEventActorKindEnum actorKind;



  @JsonKey(
    
    name: r'tokenId',
    required: false,
    includeIfNull: false,
  )


  final String? tokenId;



  @JsonKey(
    
    name: r'tokenName',
    required: false,
    includeIfNull: false,
  )


  final String? tokenName;



      /// Principal owning the token (v1.3)
  @JsonKey(
    
    name: r'principalId',
    required: false,
    includeIfNull: false,
  )


  final String? principalId;



  @JsonKey(
    
    name: r'scope',
    required: false,
    includeIfNull: false,
  )


  final AuditEventScopeEnum? scope;



  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final String? method;



      /// Request path, query stripped
  @JsonKey(
    
    name: r'path',
    required: false,
    includeIfNull: false,
  )


  final String? path;



  @JsonKey(
    
    name: r'operationId',
    required: false,
    includeIfNull: false,
  )


  final String? operationId;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final int? status;



  @JsonKey(
    
    name: r'targetType',
    required: false,
    includeIfNull: false,
  )


  final String? targetType;



  @JsonKey(
    
    name: r'targetId',
    required: false,
    includeIfNull: false,
  )


  final String? targetId;



  @JsonKey(
    
    name: r'remoteIp',
    required: false,
    includeIfNull: false,
  )


  final String? remoteIp;



      /// Context (e.g. MCP tool name, argument names, duration, outcome); never secrets, token values or request bodies
  @JsonKey(
    
    name: r'detail',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? detail;



      /// The signed-in user who acted (actor kind `user`, R2-013), or null
  @JsonKey(
    
    name: r'userId',
    required: false,
    includeIfNull: false,
  )


  final String? userId;



      /// That user's email when the entry was written (kept after the user is renamed or deleted), or null
  @JsonKey(
    
    name: r'userEmail',
    required: false,
    includeIfNull: false,
  )


  final String? userEmail;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AuditEvent &&
      other.id == id &&
      other.at == at &&
      other.actorKind == actorKind &&
      other.tokenId == tokenId &&
      other.tokenName == tokenName &&
      other.principalId == principalId &&
      other.scope == scope &&
      other.method == method &&
      other.path == path &&
      other.operationId == operationId &&
      other.status == status &&
      other.targetType == targetType &&
      other.targetId == targetId &&
      other.remoteIp == remoteIp &&
      other.detail == detail &&
      other.userId == userId &&
      other.userEmail == userEmail;

    @override
    int get hashCode =>
        id.hashCode +
        at.hashCode +
        actorKind.hashCode +
        (tokenId == null ? 0 : tokenId.hashCode) +
        (tokenName == null ? 0 : tokenName.hashCode) +
        (principalId == null ? 0 : principalId.hashCode) +
        (scope == null ? 0 : scope.hashCode) +
        (method == null ? 0 : method.hashCode) +
        (path == null ? 0 : path.hashCode) +
        (operationId == null ? 0 : operationId.hashCode) +
        (status == null ? 0 : status.hashCode) +
        (targetType == null ? 0 : targetType.hashCode) +
        (targetId == null ? 0 : targetId.hashCode) +
        (remoteIp == null ? 0 : remoteIp.hashCode) +
        detail.hashCode +
        (userId == null ? 0 : userId.hashCode) +
        (userEmail == null ? 0 : userEmail.hashCode);

  factory AuditEvent.fromJson(Map<String, dynamic> json) => _$AuditEventFromJson(json);

  Map<String, dynamic> toJson() => _$AuditEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `user` = a signed-in user (R2-013; `userId` and `userEmail` say who, `tokenId` is the session); `action` = a notification action capability (033); `chat` = a command from an allowlisted chat of an interactive channel (061; `tokenName` is the chat principal `chat:{channelId}:{chatId}[:{userId}]`); `mesh` = an `#E ACK` from an allowlisted sender on a private mesh channel (084; `tokenName` is `mesh:{sender}`)
enum AuditEventActorKindEnum {
@JsonValue(r'token')
token(r'token'),
@JsonValue(r'user')
user(r'user'),
@JsonValue(r'ingest_key')
ingestKey(r'ingest_key'),
@JsonValue(r'pairing')
pairing(r'pairing'),
@JsonValue(r'system')
system(r'system'),
@JsonValue(r'action')
action(r'action'),
@JsonValue(r'chat')
chat(r'chat'),
@JsonValue(r'mesh')
mesh(r'mesh');

const AuditEventActorKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AuditEventScopeEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'admin')
admin(r'admin');

const AuditEventScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


