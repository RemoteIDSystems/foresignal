//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/jwk.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'signing_key_info.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SigningKeyInfo {
  /// Returns a new [SigningKeyInfo] instance.
  SigningKeyInfo({

    required  this.kid,

    required  this.alg,

    required  this.status,

    required  this.createdAt,

     this.retiredAt,

     this.removeAfter,

    required  this.jwk,
  });

  @JsonKey(
    
    name: r'kid',
    required: true,
    includeIfNull: false,
  )


  final String kid;



  @JsonKey(
    
    name: r'alg',
    required: true,
    includeIfNull: false,
  )


  final SigningKeyInfoAlgEnum alg;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SigningKeyInfoStatusEnum status;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'retiredAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? retiredAt;



      /// When a retired key leaves the JWKS (retiredAt + signing.retainRetired)
  @JsonKey(
    
    name: r'removeAfter',
    required: false,
    includeIfNull: false,
  )


  final DateTime? removeAfter;



  @JsonKey(
    
    name: r'jwk',
    required: true,
    includeIfNull: false,
  )


  final Jwk jwk;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SigningKeyInfo &&
      other.kid == kid &&
      other.alg == alg &&
      other.status == status &&
      other.createdAt == createdAt &&
      other.retiredAt == retiredAt &&
      other.removeAfter == removeAfter &&
      other.jwk == jwk;

    @override
    int get hashCode =>
        kid.hashCode +
        alg.hashCode +
        status.hashCode +
        createdAt.hashCode +
        (retiredAt == null ? 0 : retiredAt.hashCode) +
        (removeAfter == null ? 0 : removeAfter.hashCode) +
        jwk.hashCode;

  factory SigningKeyInfo.fromJson(Map<String, dynamic> json) => _$SigningKeyInfoFromJson(json);

  Map<String, dynamic> toJson() => _$SigningKeyInfoToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SigningKeyInfoAlgEnum {
@JsonValue(r'Ed25519')
ed25519(r'Ed25519'),
@JsonValue(r'EdDSA')
edDSA(r'EdDSA');

const SigningKeyInfoAlgEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SigningKeyInfoStatusEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'retired')
retired(r'retired');

const SigningKeyInfoStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


