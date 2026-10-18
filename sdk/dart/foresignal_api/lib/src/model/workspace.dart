//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/workspace_context.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'workspace.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Workspace {
  /// Returns a new [Workspace] instance.
  Workspace({

    required  this.id,

     this.ref,

    required  this.name,

    required  this.owner,

    required  this.members,

     this.callerRole,

    required  this.context,

    required  this.contextVersion,

     this.layout,

     this.savedViewIds,

     this.investigationId,

    required  this.private,

    required  this.createdAt,

    required  this.updatedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// Principal id
  @JsonKey(
    
    name: r'owner',
    required: true,
    includeIfNull: false,
  )


  final String owner;



      /// Principal id → role
  @JsonKey(
    
    name: r'members',
    required: true,
    includeIfNull: false,
  )


  final Map<String, WorkspaceMembersEnum> members;



  @JsonKey(
    
    name: r'callerRole',
    required: false,
    includeIfNull: false,
  )


  final WorkspaceCallerRoleEnum? callerRole;



  @JsonKey(
    
    name: r'context',
    required: true,
    includeIfNull: false,
  )


  final WorkspaceContext context;



  @JsonKey(
    
    name: r'contextVersion',
    required: true,
    includeIfNull: false,
  )


  final int contextVersion;



      /// Opaque client JSON (≤ 64 KB); never interpreted by Foresignal
  @JsonKey(
    
    name: r'layout',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? layout;



  @JsonKey(
    
    name: r'savedViewIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? savedViewIds;



  @JsonKey(
    
    name: r'investigationId',
    required: false,
    includeIfNull: false,
  )


  final String? investigationId;



  @JsonKey(
    
    name: r'private',
    required: true,
    includeIfNull: false,
  )


  final bool private;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Workspace &&
      other.id == id &&
      other.ref == ref &&
      other.name == name &&
      other.owner == owner &&
      other.members == members &&
      other.callerRole == callerRole &&
      other.context == context &&
      other.contextVersion == contextVersion &&
      other.layout == layout &&
      other.savedViewIds == savedViewIds &&
      other.investigationId == investigationId &&
      other.private == private &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        name.hashCode +
        owner.hashCode +
        members.hashCode +
        callerRole.hashCode +
        context.hashCode +
        contextVersion.hashCode +
        layout.hashCode +
        savedViewIds.hashCode +
        (investigationId == null ? 0 : investigationId.hashCode) +
        private.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory Workspace.fromJson(Map<String, dynamic> json) => _$WorkspaceFromJson(json);

  Map<String, dynamic> toJson() => _$WorkspaceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WorkspaceMembersEnum {
@JsonValue(r'viewer')
viewer(r'viewer'),
@JsonValue(r'editor')
editor(r'editor');

const WorkspaceMembersEnum(this.value);

final String value;

@override
String toString() => value;
}


enum WorkspaceCallerRoleEnum {
@JsonValue(r'owner')
owner(r'owner'),
@JsonValue(r'editor')
editor(r'editor'),
@JsonValue(r'viewer')
viewer(r'viewer');

const WorkspaceCallerRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


