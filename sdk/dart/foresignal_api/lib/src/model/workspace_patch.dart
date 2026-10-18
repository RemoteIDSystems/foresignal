//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'workspace_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WorkspacePatch {
  /// Returns a new [WorkspacePatch] instance.
  WorkspacePatch({

     this.name,

     this.members,

     this.savedViewIds,

     this.investigationId,

     this.private,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



      /// Principal id → role
  @JsonKey(
    
    name: r'members',
    required: false,
    includeIfNull: false,
  )


  final Map<String, WorkspacePatchMembersEnum>? members;



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
    required: false,
    includeIfNull: false,
  )


  final bool? private;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WorkspacePatch &&
      other.name == name &&
      other.members == members &&
      other.savedViewIds == savedViewIds &&
      other.investigationId == investigationId &&
      other.private == private;

    @override
    int get hashCode =>
        name.hashCode +
        members.hashCode +
        savedViewIds.hashCode +
        (investigationId == null ? 0 : investigationId.hashCode) +
        private.hashCode;

  factory WorkspacePatch.fromJson(Map<String, dynamic> json) => _$WorkspacePatchFromJson(json);

  Map<String, dynamic> toJson() => _$WorkspacePatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WorkspacePatchMembersEnum {
@JsonValue(r'viewer')
viewer(r'viewer'),
@JsonValue(r'editor')
editor(r'editor');

const WorkspacePatchMembersEnum(this.value);

final String value;

@override
String toString() => value;
}


