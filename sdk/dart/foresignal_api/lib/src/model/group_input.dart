//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/group_member_input.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'group_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GroupInput {
  /// Returns a new [GroupInput] instance.
  GroupInput({

    required  this.name,

     this.description,

     this.members,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'members',
    required: false,
    includeIfNull: false,
  )


  final List<GroupMemberInput>? members;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GroupInput &&
      other.name == name &&
      other.description == description &&
      other.members == members;

    @override
    int get hashCode =>
        name.hashCode +
        (description == null ? 0 : description.hashCode) +
        members.hashCode;

  factory GroupInput.fromJson(Map<String, dynamic> json) => _$GroupInputFromJson(json);

  Map<String, dynamic> toJson() => _$GroupInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

