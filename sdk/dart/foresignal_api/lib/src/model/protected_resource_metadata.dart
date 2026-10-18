//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'protected_resource_metadata.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProtectedResourceMetadata {
  /// Returns a new [ProtectedResourceMetadata] instance.
  ProtectedResourceMetadata({

    required  this.resource,

    required  this.authorizationServers,

     this.scopesSupported,

    required  this.bearerMethodsSupported,

     this.resourceName,

     this.resourceDocumentation,
  });

      /// Canonical resource URI, default `{publicBaseUrl}/mcp`
  @JsonKey(
    
    name: r'resource',
    required: true,
    includeIfNull: false,
  )


  final String resource;



      /// The identity provider's issuer (`mcp.auth.issuer`); empty while none is configured
  @JsonKey(
    
    name: r'authorization_servers',
    required: true,
    includeIfNull: false,
  )


  final List<String> authorizationServers;



      /// The identity-provider scopes of `mcp.auth.scopeMap` that map to `read` or `agent`, sorted
  @JsonKey(
    
    name: r'scopes_supported',
    required: false,
    includeIfNull: false,
  )


  final List<String>? scopesSupported;



  @JsonKey(
    
    name: r'bearer_methods_supported',
    required: true,
    includeIfNull: false,
  )


  final List<ProtectedResourceMetadataBearerMethodsSupportedEnum> bearerMethodsSupported;



  @JsonKey(
    
    name: r'resource_name',
    required: false,
    includeIfNull: false,
  )


  final ProtectedResourceMetadataResourceNameEnum? resourceName;



      /// The API documentation, `{base}/docs`
  @JsonKey(
    
    name: r'resource_documentation',
    required: false,
    includeIfNull: false,
  )


  final String? resourceDocumentation;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProtectedResourceMetadata &&
      other.resource == resource &&
      other.authorizationServers == authorizationServers &&
      other.scopesSupported == scopesSupported &&
      other.bearerMethodsSupported == bearerMethodsSupported &&
      other.resourceName == resourceName &&
      other.resourceDocumentation == resourceDocumentation;

    @override
    int get hashCode =>
        resource.hashCode +
        authorizationServers.hashCode +
        scopesSupported.hashCode +
        bearerMethodsSupported.hashCode +
        resourceName.hashCode +
        (resourceDocumentation == null ? 0 : resourceDocumentation.hashCode);

  factory ProtectedResourceMetadata.fromJson(Map<String, dynamic> json) => _$ProtectedResourceMetadataFromJson(json);

  Map<String, dynamic> toJson() => _$ProtectedResourceMetadataToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ProtectedResourceMetadataBearerMethodsSupportedEnum {
@JsonValue(r'header')
header(r'header');

const ProtectedResourceMetadataBearerMethodsSupportedEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ProtectedResourceMetadataResourceNameEnum {
@JsonValue(r'Foresignal')
foresignal(r'Foresignal');

const ProtectedResourceMetadataResourceNameEnum(this.value);

final String value;

@override
String toString() => value;
}


