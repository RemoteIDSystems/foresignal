//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'role_assignment.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RoleAssignment {
  /// Returns a new [RoleAssignment] instance.
  RoleAssignment({

    required  this.roles,
  });

  @JsonKey(
    
    name: r'roles',
    required: true,
    includeIfNull: false,
  )


  final List<String> roles;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RoleAssignment &&
      other.roles == roles;

    @override
    int get hashCode =>
        roles.hashCode;

  factory RoleAssignment.fromJson(Map<String, dynamic> json) => _$RoleAssignmentFromJson(json);

  Map<String, dynamic> toJson() => _$RoleAssignmentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

