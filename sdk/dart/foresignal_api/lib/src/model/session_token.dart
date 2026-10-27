//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'session_token.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SessionToken {
  /// Returns a new [SessionToken] instance.
  SessionToken({

    required  this.token,

     this.tokenPrefix,

    required  this.userId,

     this.issuedAt,

    required  this.expiresAt,

     this.idleExpiresAt,

     this.mfaRequired = false,

     this.scopes,

     this.permissions,
  });

      /// The session bearer; shown once, stored hashed server-side. Empty while mfaRequired: no session was issued (R2-024)
  @JsonKey(
    
    name: r'token',
    required: true,
    includeIfNull: false,
  )


  final String token;



      /// First characters, for display in a session list
  @JsonKey(
    
    name: r'tokenPrefix',
    required: false,
    includeIfNull: false,
  )


  final String? tokenPrefix;



  @JsonKey(
    
    name: r'userId',
    required: true,
    includeIfNull: false,
  )


  final String userId;



  @JsonKey(
    
    name: r'issuedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? issuedAt;



      /// Absolute expiry; null while mfaRequired (no session was issued, R2-024)
  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: true,
  )


  final DateTime? expiresAt;



      /// Idle expiry; refreshed on use
  @JsonKey(
    
    name: r'idleExpiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? idleExpiresAt;



      /// True when MFA is enrolled and a code must still be supplied to complete login: no session was issued, and the client signs in again with mfaCode (R2-024)
  @JsonKey(
    defaultValue: false,
    name: r'mfaRequired',
    required: false,
    includeIfNull: false,
  )


  final bool? mfaRequired;



      /// Effective coarse scopes the session carries
  @JsonKey(
    
    name: r'scopes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? scopes;



      /// Effective permission keys (union of the user's roles)
  @JsonKey(
    
    name: r'permissions',
    required: false,
    includeIfNull: false,
  )


  final List<String>? permissions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SessionToken &&
      other.token == token &&
      other.tokenPrefix == tokenPrefix &&
      other.userId == userId &&
      other.issuedAt == issuedAt &&
      other.expiresAt == expiresAt &&
      other.idleExpiresAt == idleExpiresAt &&
      other.mfaRequired == mfaRequired &&
      other.scopes == scopes &&
      other.permissions == permissions;

    @override
    int get hashCode =>
        token.hashCode +
        tokenPrefix.hashCode +
        userId.hashCode +
        issuedAt.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        (idleExpiresAt == null ? 0 : idleExpiresAt.hashCode) +
        mfaRequired.hashCode +
        scopes.hashCode +
        permissions.hashCode;

  factory SessionToken.fromJson(Map<String, dynamic> json) => _$SessionTokenFromJson(json);

  Map<String, dynamic> toJson() => _$SessionTokenToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

