//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/read_restriction.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'api_token_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ApiTokenInput {
  /// Returns a new [ApiTokenInput] instance.
  ApiTokenInput({

    required  this.name,

    required  this.scope,

     this.expiresAt,

     this.restrictions,

     this.principalId,

     this.privateAccess = false,

     this.licencePolicy,
  });

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


  final ApiTokenInputScopeEnum scope;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;



      /// Rejected on admin tokens (400 RESTRICTION_NOT_ALLOWED, 035)
  @JsonKey(
    
    name: r'restrictions',
    required: false,
    includeIfNull: false,
  )


  final ReadRestriction? restrictions;



      /// Owning principal (id or handle); default: a new principal named after the token; not accepted by createMyToken, whose tokens are always the caller's (R2-025)
  @JsonKey(
    
    name: r'principalId',
    required: false,
    includeIfNull: false,
  )


  final String? principalId;



      /// Grant visibility of private sources (039)
  @JsonKey(
    defaultValue: false,
    name: r'privateAccess',
    required: false,
    includeIfNull: false,
  )


  final bool? privateAccess;



      /// Licence policy floor applied to every read with this token; requests may add flags but never remove these
  @JsonKey(
    
    name: r'licencePolicy',
    required: false,
    includeIfNull: false,
  )


  final List<ApiTokenInputLicencePolicyEnum>? licencePolicy;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ApiTokenInput &&
      other.name == name &&
      other.scope == scope &&
      other.expiresAt == expiresAt &&
      other.restrictions == restrictions &&
      other.principalId == principalId &&
      other.privateAccess == privateAccess &&
      other.licencePolicy == licencePolicy;

    @override
    int get hashCode =>
        name.hashCode +
        scope.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        (restrictions == null ? 0 : restrictions.hashCode) +
        (principalId == null ? 0 : principalId.hashCode) +
        privateAccess.hashCode +
        licencePolicy.hashCode;

  factory ApiTokenInput.fromJson(Map<String, dynamic> json) => _$ApiTokenInputFromJson(json);

  Map<String, dynamic> toJson() => _$ApiTokenInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ApiTokenInputScopeEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'admin')
admin(r'admin');

const ApiTokenInputScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ApiTokenInputLicencePolicyEnum {
@JsonValue(r'no-nc')
noNc(r'no-nc'),
@JsonValue(r'no-sa')
noSa(r'no-sa'),
@JsonValue(r'redistributable')
redistributable(r'redistributable');

const ApiTokenInputLicencePolicyEnum(this.value);

final String value;

@override
String toString() => value;
}


