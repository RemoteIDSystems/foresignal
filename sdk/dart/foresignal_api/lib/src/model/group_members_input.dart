//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/group_member_input.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'group_members_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GroupMembersInput {
  /// Returns a new [GroupMembersInput] instance.
  GroupMembersInput({

    required  this.members,
  });

  @JsonKey(
    
    name: r'members',
    required: true,
    includeIfNull: false,
  )


  final List<GroupMemberInput> members;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GroupMembersInput &&
      other.members == members;

    @override
    int get hashCode =>
        members.hashCode;

  factory GroupMembersInput.fromJson(Map<String, dynamic> json) => _$GroupMembersInputFromJson(json);

  Map<String, dynamic> toJson() => _$GroupMembersInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

