//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/user_data_restriction.dart';
import 'package:foresignal_api/src/model/role_ref.dart';
import 'package:foresignal_api/src/model/user_link_issued.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'user.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class User {
  /// Returns a new [User] instance.
  User({

    required  this.id,

    required  this.email,

    required  this.fullName,

    required  this.status,

    required  this.roles,

     this.mfaEnabled = false,

     this.createdBy,

     this.createdAt,

     this.updatedAt,

     this.lastLoginAt,

     this.activation,

     this.dataRestriction,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Unique, normalized; the primary identity
  @JsonKey(
    
    name: r'email',
    required: true,
    includeIfNull: false,
  )


  final String email;



  @JsonKey(
    
    name: r'fullName',
    required: true,
    includeIfNull: false,
  )


  final String fullName;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final String status;



      /// Assigned roles; effective permissions are their union
  @JsonKey(
    
    name: r'roles',
    required: true,
    includeIfNull: false,
  )


  final List<RoleRef> roles;



  @JsonKey(
    defaultValue: false,
    name: r'mfaEnabled',
    required: false,
    includeIfNull: false,
  )


  final bool? mfaEnabled;



      /// Admin who created the account; null for the bootstrap first admin
  @JsonKey(
    
    name: r'createdBy',
    required: false,
    includeIfNull: false,
  )


  final String? createdBy;



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



  @JsonKey(
    
    name: r'lastLoginAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastLoginAt;



      /// Only on the `createUser` response: the activation link issued with the invite
  @JsonKey(
    
    name: r'activation',
    required: false,
    includeIfNull: false,
  )


  final UserLinkIssued? activation;



      /// The user's data restriction, or null when unrestricted (R2-013)
  @JsonKey(
    
    name: r'dataRestriction',
    required: false,
    includeIfNull: false,
  )


  final UserDataRestriction? dataRestriction;





    @override
    bool operator ==(Object other) => identical(this, other) || other is User &&
      other.id == id &&
      other.email == email &&
      other.fullName == fullName &&
      other.status == status &&
      other.roles == roles &&
      other.mfaEnabled == mfaEnabled &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.lastLoginAt == lastLoginAt &&
      other.activation == activation &&
      other.dataRestriction == dataRestriction;

    @override
    int get hashCode =>
        id.hashCode +
        email.hashCode +
        fullName.hashCode +
        status.hashCode +
        roles.hashCode +
        mfaEnabled.hashCode +
        (createdBy == null ? 0 : createdBy.hashCode) +
        createdAt.hashCode +
        updatedAt.hashCode +
        (lastLoginAt == null ? 0 : lastLoginAt.hashCode) +
        activation.hashCode +
        (dataRestriction == null ? 0 : dataRestriction.hashCode);

  factory User.fromJson(Map<String, dynamic> json) => _$UserFromJson(json);

  Map<String, dynamic> toJson() => _$UserToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

