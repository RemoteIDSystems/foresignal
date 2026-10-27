//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'user_create.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UserCreate {
  /// Returns a new [UserCreate] instance.
  UserCreate({

    required  this.email,

    required  this.fullName,

    required  this.roles,

     this.sendInvite = true,
  });

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



      /// Role ids to assign on creation
  @JsonKey(
    
    name: r'roles',
    required: true,
    includeIfNull: false,
  )


  final List<String> roles;



      /// Email an activation link; when false an admin retrieves the link
  @JsonKey(
    defaultValue: true,
    name: r'sendInvite',
    required: false,
    includeIfNull: false,
  )


  final bool? sendInvite;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UserCreate &&
      other.email == email &&
      other.fullName == fullName &&
      other.roles == roles &&
      other.sendInvite == sendInvite;

    @override
    int get hashCode =>
        email.hashCode +
        fullName.hashCode +
        roles.hashCode +
        sendInvite.hashCode;

  factory UserCreate.fromJson(Map<String, dynamic> json) => _$UserCreateFromJson(json);

  Map<String, dynamic> toJson() => _$UserCreateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

