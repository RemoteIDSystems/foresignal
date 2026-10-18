//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/read_restriction.dart';
import 'package:foresignal_api/src/model/device_info.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'api_token_created.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ApiTokenCreated {
  /// Returns a new [ApiTokenCreated] instance.
  ApiTokenCreated({

    required  this.id,

    required  this.name,

    required  this.scope,

    required  this.tokenPrefix,

    required  this.createdAt,

     this.lastUsedAt,

     this.expiresAt,

     this.revokedAt,

     this.bootstrap,

     this.restrictions,

     this.device,

     this.createdVia,

     this.principalId,

     this.privateAccess,

     this.licencePolicy,

     this.principalHandle,

    required  this.token,
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


  final ApiTokenCreatedScopeEnum scope;



  @JsonKey(
    
    name: r'tokenPrefix',
    required: true,
    includeIfNull: false,
  )


  final String tokenPrefix;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'lastUsedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastUsedAt;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;



  @JsonKey(
    
    name: r'revokedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? revokedAt;



  @JsonKey(
    
    name: r'bootstrap',
    required: false,
    includeIfNull: false,
  )


  final bool? bootstrap;



      /// Data and endpoint restrictions (data restrictions enforced from 035)
  @JsonKey(
    
    name: r'restrictions',
    required: false,
    includeIfNull: false,
  )


  final ReadRestriction? restrictions;



  @JsonKey(
    
    name: r'device',
    required: false,
    includeIfNull: false,
  )


  final DeviceInfo? device;



  @JsonKey(
    
    name: r'createdVia',
    required: false,
    includeIfNull: false,
  )


  final ApiTokenCreatedCreatedViaEnum? createdVia;



      /// Owning principal (every token belongs to one principal)
  @JsonKey(
    
    name: r'principalId',
    required: false,
    includeIfNull: false,
  )


  final String? principalId;



      /// May see private sources (039)
  @JsonKey(
    
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


  final List<ApiTokenCreatedLicencePolicyEnum>? licencePolicy;



  @JsonKey(
    
    name: r'principalHandle',
    required: false,
    includeIfNull: false,
  )


  final String? principalHandle;



      /// Shown once
  @JsonKey(
    
    name: r'token',
    required: true,
    includeIfNull: false,
  )


  final String token;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ApiTokenCreated &&
      other.id == id &&
      other.name == name &&
      other.scope == scope &&
      other.tokenPrefix == tokenPrefix &&
      other.createdAt == createdAt &&
      other.lastUsedAt == lastUsedAt &&
      other.expiresAt == expiresAt &&
      other.revokedAt == revokedAt &&
      other.bootstrap == bootstrap &&
      other.restrictions == restrictions &&
      other.device == device &&
      other.createdVia == createdVia &&
      other.principalId == principalId &&
      other.privateAccess == privateAccess &&
      other.licencePolicy == licencePolicy &&
      other.principalHandle == principalHandle &&
      other.token == token;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        scope.hashCode +
        tokenPrefix.hashCode +
        createdAt.hashCode +
        (lastUsedAt == null ? 0 : lastUsedAt.hashCode) +
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        (revokedAt == null ? 0 : revokedAt.hashCode) +
        bootstrap.hashCode +
        (restrictions == null ? 0 : restrictions.hashCode) +
        (device == null ? 0 : device.hashCode) +
        createdVia.hashCode +
        principalId.hashCode +
        privateAccess.hashCode +
        licencePolicy.hashCode +
        principalHandle.hashCode +
        token.hashCode;

  factory ApiTokenCreated.fromJson(Map<String, dynamic> json) => _$ApiTokenCreatedFromJson(json);

  Map<String, dynamic> toJson() => _$ApiTokenCreatedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ApiTokenCreatedScopeEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'admin')
admin(r'admin');

const ApiTokenCreatedScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ApiTokenCreatedCreatedViaEnum {
@JsonValue(r'api')
api(r'api'),
@JsonValue(r'pairing')
pairing(r'pairing'),
@JsonValue(r'bootstrap')
bootstrap(r'bootstrap');

const ApiTokenCreatedCreatedViaEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ApiTokenCreatedLicencePolicyEnum {
@JsonValue(r'no-nc')
noNc(r'no-nc'),
@JsonValue(r'no-sa')
noSa(r'no-sa'),
@JsonValue(r'redistributable')
redistributable(r'redistributable');

const ApiTokenCreatedLicencePolicyEnum(this.value);

final String value;

@override
String toString() => value;
}


