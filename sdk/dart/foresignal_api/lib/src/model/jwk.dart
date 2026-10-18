//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'jwk.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Jwk {
  /// Returns a new [Jwk] instance.
  Jwk({

    required  this.kty,

    required  this.crv,

    required  this.x,

    required  this.kid,

    required  this.use,

    required  this.alg,
  });

  @JsonKey(
    
    name: r'kty',
    required: true,
    includeIfNull: false,
  )


  final JwkKtyEnum kty;



  @JsonKey(
    
    name: r'crv',
    required: true,
    includeIfNull: false,
  )


  final JwkCrvEnum crv;



      /// Base64url public key
  @JsonKey(
    
    name: r'x',
    required: true,
    includeIfNull: false,
  )


  final String x;



      /// RFC 7638 JWK thumbprint
  @JsonKey(
    
    name: r'kid',
    required: true,
    includeIfNull: false,
  )


  final String kid;



  @JsonKey(
    
    name: r'use',
    required: true,
    includeIfNull: false,
  )


  final JwkUseEnum use;



      /// Ed25519 (RFC 9864); EdDSA only when signing.legacyAlg=true
  @JsonKey(
    
    name: r'alg',
    required: true,
    includeIfNull: false,
  )


  final JwkAlgEnum alg;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Jwk &&
      other.kty == kty &&
      other.crv == crv &&
      other.x == x &&
      other.kid == kid &&
      other.use == use &&
      other.alg == alg;

    @override
    int get hashCode =>
        kty.hashCode +
        crv.hashCode +
        x.hashCode +
        kid.hashCode +
        use.hashCode +
        alg.hashCode;

  factory Jwk.fromJson(Map<String, dynamic> json) => _$JwkFromJson(json);

  Map<String, dynamic> toJson() => _$JwkToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum JwkKtyEnum {
@JsonValue(r'OKP')
OKP(r'OKP');

const JwkKtyEnum(this.value);

final String value;

@override
String toString() => value;
}


enum JwkCrvEnum {
@JsonValue(r'Ed25519')
ed25519(r'Ed25519');

const JwkCrvEnum(this.value);

final String value;

@override
String toString() => value;
}


enum JwkUseEnum {
@JsonValue(r'sig')
sig(r'sig');

const JwkUseEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Ed25519 (RFC 9864); EdDSA only when signing.legacyAlg=true
enum JwkAlgEnum {
@JsonValue(r'Ed25519')
ed25519(r'Ed25519'),
@JsonValue(r'EdDSA')
edDSA(r'EdDSA');

const JwkAlgEnum(this.value);

final String value;

@override
String toString() => value;
}


