//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mcp_auth_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class McpAuthSettings {
  /// Returns a new [McpAuthSettings] instance.
  McpAuthSettings({

    required  this.mode,

     this.issuer,

     this.audience,

     this.jwksUri,

     this.scopeMap,

     this.scopeClaim,

     this.allowedAlgs,
  });

      /// `token`: Foresignal tokens only (open reads apply as on REST). `oauth`: identity-provider access tokens only. `both`: either. With `oauth` or `both`, `/mcp` always needs a credential
  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final McpAuthSettingsModeEnum mode;



      /// Issuer identifier, compared exactly with the token's `iss`; required with `oauth` or `both`
  @JsonKey(
    
    name: r'issuer',
    required: false,
    includeIfNull: false,
  )


  final String? issuer;



      /// Default `{publicBaseUrl}/mcp` (RFC 8707)
  @JsonKey(
    
    name: r'audience',
    required: false,
    includeIfNull: false,
  )


  final String? audience;



      /// Optional; else RFC 8414 then OIDC discovery from the issuer through the 035 URL policy
  @JsonKey(
    
    name: r'jwksUri',
    required: false,
    includeIfNull: false,
  )


  final String? jwksUri;



      /// IdP scope → Foresignal scope; `admin` is never mappable
  @JsonKey(
    
    name: r'scopeMap',
    required: false,
    includeIfNull: false,
  )


  final Map<String, McpAuthSettingsScopeMapEnum>? scopeMap;



      /// Claim carrying the scopes; a space-separated string or an array of strings is read from either claim
  @JsonKey(
    
    name: r'scopeClaim',
    required: false,
    includeIfNull: false,
  )


  final McpAuthSettingsScopeClaimEnum? scopeClaim;



      /// Accepted JWS algorithms; `none` and HMAC algorithms are never accepted
  @JsonKey(
    
    name: r'allowedAlgs',
    required: false,
    includeIfNull: false,
  )


  final List<McpAuthSettingsAllowedAlgsEnum>? allowedAlgs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is McpAuthSettings &&
      other.mode == mode &&
      other.issuer == issuer &&
      other.audience == audience &&
      other.jwksUri == jwksUri &&
      other.scopeMap == scopeMap &&
      other.scopeClaim == scopeClaim &&
      other.allowedAlgs == allowedAlgs;

    @override
    int get hashCode =>
        mode.hashCode +
        (issuer == null ? 0 : issuer.hashCode) +
        (audience == null ? 0 : audience.hashCode) +
        (jwksUri == null ? 0 : jwksUri.hashCode) +
        scopeMap.hashCode +
        scopeClaim.hashCode +
        allowedAlgs.hashCode;

  factory McpAuthSettings.fromJson(Map<String, dynamic> json) => _$McpAuthSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$McpAuthSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `token`: Foresignal tokens only (open reads apply as on REST). `oauth`: identity-provider access tokens only. `both`: either. With `oauth` or `both`, `/mcp` always needs a credential
enum McpAuthSettingsModeEnum {
@JsonValue(r'token')
token(r'token'),
@JsonValue(r'oauth')
oauth(r'oauth'),
@JsonValue(r'both')
both(r'both');

const McpAuthSettingsModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum McpAuthSettingsScopeMapEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent');

const McpAuthSettingsScopeMapEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Claim carrying the scopes; a space-separated string or an array of strings is read from either claim
enum McpAuthSettingsScopeClaimEnum {
@JsonValue(r'scope')
scope(r'scope'),
@JsonValue(r'scp')
scp(r'scp');

const McpAuthSettingsScopeClaimEnum(this.value);

final String value;

@override
String toString() => value;
}


enum McpAuthSettingsAllowedAlgsEnum {
@JsonValue(r'RS256')
RS256(r'RS256'),
@JsonValue(r'ES256')
ES256(r'ES256'),
@JsonValue(r'EdDSA')
edDSA(r'EdDSA'),
@JsonValue(r'Ed25519')
ed25519(r'Ed25519');

const McpAuthSettingsAllowedAlgsEnum(this.value);

final String value;

@override
String toString() => value;
}


