//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/read_restriction.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pairing_code_created.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PairingCodeCreated {
  /// Returns a new [PairingCodeCreated] instance.
  PairingCodeCreated({

    required  this.id,

    required  this.name,

    required  this.scope,

     this.restrictions,

     this.principalId,

     this.privateAccess,

    required  this.createdAt,

    required  this.expiresAt,

     this.createdByTokenId,

     this.claimedAt,

     this.claimedTokenId,

    required  this.code,

    required  this.qrPayload,
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
    
    name: r'scope',
    required: true,
    includeIfNull: false,
  )


  final PairingCodeCreatedScopeEnum scope;



  @JsonKey(
    
    name: r'restrictions',
    required: false,
    includeIfNull: false,
  )


  final ReadRestriction? restrictions;



  @JsonKey(
    
    name: r'principalId',
    required: false,
    includeIfNull: false,
  )


  final String? principalId;



  @JsonKey(
    
    name: r'privateAccess',
    required: false,
    includeIfNull: false,
  )


  final bool? privateAccess;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;



  @JsonKey(
    
    name: r'createdByTokenId',
    required: false,
    includeIfNull: false,
  )


  final String? createdByTokenId;



  @JsonKey(
    
    name: r'claimedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? claimedAt;



  @JsonKey(
    
    name: r'claimedTokenId',
    required: false,
    includeIfNull: false,
  )


  final String? claimedTokenId;



      /// 8 Crockford base32 characters shown as XXXX-XXXX; shown once
  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final String code;



      /// `clientLinks.pair` template filled with `publicBaseUrl` and the code
  @JsonKey(
    
    name: r'qrPayload',
    required: true,
    includeIfNull: false,
  )


  final String qrPayload;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PairingCodeCreated &&
      other.id == id &&
      other.name == name &&
      other.scope == scope &&
      other.restrictions == restrictions &&
      other.principalId == principalId &&
      other.privateAccess == privateAccess &&
      other.createdAt == createdAt &&
      other.expiresAt == expiresAt &&
      other.createdByTokenId == createdByTokenId &&
      other.claimedAt == claimedAt &&
      other.claimedTokenId == claimedTokenId &&
      other.code == code &&
      other.qrPayload == qrPayload;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        scope.hashCode +
        (restrictions == null ? 0 : restrictions.hashCode) +
        (principalId == null ? 0 : principalId.hashCode) +
        privateAccess.hashCode +
        createdAt.hashCode +
        expiresAt.hashCode +
        (createdByTokenId == null ? 0 : createdByTokenId.hashCode) +
        (claimedAt == null ? 0 : claimedAt.hashCode) +
        (claimedTokenId == null ? 0 : claimedTokenId.hashCode) +
        code.hashCode +
        qrPayload.hashCode;

  factory PairingCodeCreated.fromJson(Map<String, dynamic> json) => _$PairingCodeCreatedFromJson(json);

  Map<String, dynamic> toJson() => _$PairingCodeCreatedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PairingCodeCreatedScopeEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent');

const PairingCodeCreatedScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


