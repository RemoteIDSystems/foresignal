//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'login_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LoginRequest {
  /// Returns a new [LoginRequest] instance.
  LoginRequest({

    required  this.email,

    required  this.password,

     this.mfaCode,
  });

      /// The user's email (primary identity, normalized)
  @JsonKey(
    
    name: r'email',
    required: true,
    includeIfNull: false,
  )


  final String email;



  @JsonKey(
    
    name: r'password',
    required: true,
    includeIfNull: false,
  )


  final String password;



      /// TOTP or recovery code when MFA is enrolled
  @JsonKey(
    
    name: r'mfaCode',
    required: false,
    includeIfNull: false,
  )


  final String? mfaCode;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LoginRequest &&
      other.email == email &&
      other.password == password &&
      other.mfaCode == mfaCode;

    @override
    int get hashCode =>
        email.hashCode +
        password.hashCode +
        (mfaCode == null ? 0 : mfaCode.hashCode);

  factory LoginRequest.fromJson(Map<String, dynamic> json) => _$LoginRequestFromJson(json);

  Map<String, dynamic> toJson() => _$LoginRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

