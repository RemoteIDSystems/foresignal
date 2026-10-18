//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'group_member.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GroupMember {
  /// Returns a new [GroupMember] instance.
  GroupMember({

    required  this.principal,

    required  this.role,

     this.addedAt,

     this.addedBy,
  });

  @JsonKey(
    
    name: r'principal',
    required: true,
    includeIfNull: false,
  )


  final PrincipalSummary principal;



  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final GroupMemberRoleEnum role;



  @JsonKey(
    
    name: r'addedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? addedAt;



  @JsonKey(
    
    name: r'addedBy',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? addedBy;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GroupMember &&
      other.principal == principal &&
      other.role == role &&
      other.addedAt == addedAt &&
      other.addedBy == addedBy;

    @override
    int get hashCode =>
        principal.hashCode +
        role.hashCode +
        addedAt.hashCode +
        (addedBy == null ? 0 : addedBy.hashCode);

  factory GroupMember.fromJson(Map<String, dynamic> json) => _$GroupMemberFromJson(json);

  Map<String, dynamic> toJson() => _$GroupMemberToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum GroupMemberRoleEnum {
@JsonValue(r'member')
member(r'member'),
@JsonValue(r'manager')
manager(r'manager');

const GroupMemberRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


