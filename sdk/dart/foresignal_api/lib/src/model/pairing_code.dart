//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/read_restriction.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pairing_code.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PairingCode {
  /// Returns a new [PairingCode] instance.
  PairingCode({

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


  final PairingCodeScopeEnum scope;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is PairingCode &&
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
      other.claimedTokenId == claimedTokenId;

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
        (claimedTokenId == null ? 0 : claimedTokenId.hashCode);

  factory PairingCode.fromJson(Map<String, dynamic> json) => _$PairingCodeFromJson(json);

  Map<String, dynamic> toJson() => _$PairingCodeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PairingCodeScopeEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent');

const PairingCodeScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


