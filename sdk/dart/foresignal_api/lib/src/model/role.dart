//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'role.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Role {
  /// Returns a new [Role] instance.
  Role({

    required  this.id,

    required  this.name,

     this.description,

    required  this.builtIn,

    required  this.permissions,

     this.userCount,

     this.createdAt,

     this.updatedAt,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



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



      /// True for Admin/Operator/Editor/Viewer/Auditor; built-ins cannot be deleted and their permissions are fixed
  @JsonKey(
    
    name: r'builtIn',
    required: true,
    includeIfNull: false,
  )


  final bool builtIn;



      /// Permission keys granted by this role
  @JsonKey(
    
    name: r'permissions',
    required: true,
    includeIfNull: false,
  )


  final List<String> permissions;



      /// How many users hold this role
  @JsonKey(
    
    name: r'userCount',
    required: false,
    includeIfNull: false,
  )


  final int? userCount;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Role &&
      other.id == id &&
      other.name == name &&
      other.description == description &&
      other.builtIn == builtIn &&
      other.permissions == permissions &&
      other.userCount == userCount &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        (description == null ? 0 : description.hashCode) +
        builtIn.hashCode +
        permissions.hashCode +
        (userCount == null ? 0 : userCount.hashCode) +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory Role.fromJson(Map<String, dynamic> json) => _$RoleFromJson(json);

  Map<String, dynamic> toJson() => _$RoleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

