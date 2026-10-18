//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/workspace_context.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'workspace_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WorkspaceInput {
  /// Returns a new [WorkspaceInput] instance.
  WorkspaceInput({

    required  this.name,

     this.members,

     this.context,

     this.layout,

     this.savedViewIds,

     this.investigationId,

     this.private = false,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// Principal id → role
  @JsonKey(
    
    name: r'members',
    required: false,
    includeIfNull: false,
  )


  final Map<String, WorkspaceInputMembersEnum>? members;



  @JsonKey(
    
    name: r'context',
    required: false,
    includeIfNull: false,
  )


  final WorkspaceContext? context;



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
    defaultValue: false,
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WorkspaceInput &&
      other.name == name &&
      other.members == members &&
      other.context == context &&
      other.layout == layout &&
      other.savedViewIds == savedViewIds &&
      other.investigationId == investigationId &&
      other.private == private;

    @override
    int get hashCode =>
        name.hashCode +
        members.hashCode +
        context.hashCode +
        layout.hashCode +
        savedViewIds.hashCode +
        (investigationId == null ? 0 : investigationId.hashCode) +
        private.hashCode;

  factory WorkspaceInput.fromJson(Map<String, dynamic> json) => _$WorkspaceInputFromJson(json);

  Map<String, dynamic> toJson() => _$WorkspaceInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WorkspaceInputMembersEnum {
@JsonValue(r'viewer')
viewer(r'viewer'),
@JsonValue(r'editor')
editor(r'editor');

const WorkspaceInputMembersEnum(this.value);

final String value;

@override
String toString() => value;
}


