//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'group_member_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GroupMemberInput {
  /// Returns a new [GroupMemberInput] instance.
  GroupMemberInput({

    required  this.principalId,

     this.role,
  });

      /// Principal id or handle
  @JsonKey(
    
    name: r'principalId',
    required: true,
    includeIfNull: false,
  )


  final String principalId;



  @JsonKey(
    
    name: r'role',
    required: false,
    includeIfNull: false,
  )


  final GroupMemberInputRoleEnum? role;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GroupMemberInput &&
      other.principalId == principalId &&
      other.role == role;

    @override
    int get hashCode =>
        principalId.hashCode +
        role.hashCode;

  factory GroupMemberInput.fromJson(Map<String, dynamic> json) => _$GroupMemberInputFromJson(json);

  Map<String, dynamic> toJson() => _$GroupMemberInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum GroupMemberInputRoleEnum {
@JsonValue(r'member')
member(r'member'),
@JsonValue(r'manager')
manager(r'manager');

const GroupMemberInputRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


