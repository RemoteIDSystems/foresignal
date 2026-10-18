//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/read_restriction.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pairing_code_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PairingCodeInput {
  /// Returns a new [PairingCodeInput] instance.
  PairingCodeInput({

    required  this.name,

    required  this.scope,

     this.restrictions,

     this.ttl,

     this.principalId,

     this.privateAccess = false,
  });

      /// Name given to the token created on claim
  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// `admin` is never pairable
  @JsonKey(
    
    name: r'scope',
    required: true,
    includeIfNull: false,
  )


  final PairingCodeInputScopeEnum scope;



  @JsonKey(
    
    name: r'restrictions',
    required: false,
    includeIfNull: false,
  )


  final ReadRestriction? restrictions;



      /// Code lifetime: default 10m, max 1h
  @JsonKey(
    
    name: r'ttl',
    required: false,
    includeIfNull: false,
  )


  final String? ttl;



      /// Principal that will own the token; default: a new principal named after the token
  @JsonKey(
    
    name: r'principalId',
    required: false,
    includeIfNull: false,
  )


  final String? principalId;



      /// Grant visibility of private sources (039) to the claimed token
  @JsonKey(
    defaultValue: false,
    name: r'privateAccess',
    required: false,
    includeIfNull: false,
  )


  final bool? privateAccess;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PairingCodeInput &&
      other.name == name &&
      other.scope == scope &&
      other.restrictions == restrictions &&
      other.ttl == ttl &&
      other.principalId == principalId &&
      other.privateAccess == privateAccess;

    @override
    int get hashCode =>
        name.hashCode +
        scope.hashCode +
        (restrictions == null ? 0 : restrictions.hashCode) +
        ttl.hashCode +
        (principalId == null ? 0 : principalId.hashCode) +
        privateAccess.hashCode;

  factory PairingCodeInput.fromJson(Map<String, dynamic> json) => _$PairingCodeInputFromJson(json);

  Map<String, dynamic> toJson() => _$PairingCodeInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `admin` is never pairable
enum PairingCodeInputScopeEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent');

const PairingCodeInputScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


