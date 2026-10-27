//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'setup_request_admin.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SetupRequestAdmin {
  /// Returns a new [SetupRequestAdmin] instance.
  SetupRequestAdmin({

    required  this.email,

    required  this.fullName,

    required  this.password,
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



  @JsonKey(
    
    name: r'password',
    required: true,
    includeIfNull: false,
  )


  final String password;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SetupRequestAdmin &&
      other.email == email &&
      other.fullName == fullName &&
      other.password == password;

    @override
    int get hashCode =>
        email.hashCode +
        fullName.hashCode +
        password.hashCode;

  factory SetupRequestAdmin.fromJson(Map<String, dynamic> json) => _$SetupRequestAdminFromJson(json);

  Map<String, dynamic> toJson() => _$SetupRequestAdminToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

