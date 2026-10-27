//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'role_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RoleInput {
  /// Returns a new [RoleInput] instance.
  RoleInput({

    required  this.name,

     this.description,

    required  this.permissions,
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
    
    name: r'permissions',
    required: true,
    includeIfNull: false,
  )


  final List<String> permissions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RoleInput &&
      other.name == name &&
      other.description == description &&
      other.permissions == permissions;

    @override
    int get hashCode =>
        name.hashCode +
        (description == null ? 0 : description.hashCode) +
        permissions.hashCode;

  factory RoleInput.fromJson(Map<String, dynamic> json) => _$RoleInputFromJson(json);

  Map<String, dynamic> toJson() => _$RoleInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

