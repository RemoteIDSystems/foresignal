// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'protected_resource_metadata.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProtectedResourceMetadataCWProxy {
  ProtectedResourceMetadata resource(String resource);

  ProtectedResourceMetadata authorizationServers(
    List<String> authorizationServers,
  );

  ProtectedResourceMetadata scopesSupported(List<String>? scopesSupported);

  ProtectedResourceMetadata bearerMethodsSupported(
    List<ProtectedResourceMetadataBearerMethodsSupportedEnum>
    bearerMethodsSupported,
  );

  ProtectedResourceMetadata resourceName(
    ProtectedResourceMetadataResourceNameEnum? resourceName,
  );

  ProtectedResourceMetadata resourceDocumentation(
    String? resourceDocumentation,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProtectedResourceMetadata(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProtectedResourceMetadata(...).copyWith(id: 12, name: "My name")
  /// ```
  ProtectedResourceMetadata call({
    String resource,
    List<String> authorizationServers,
    List<String>? scopesSupported,
    List<ProtectedResourceMetadataBearerMethodsSupportedEnum>
    bearerMethodsSupported,
    ProtectedResourceMetadataResourceNameEnum? resourceName,
    String? resourceDocumentation,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProtectedResourceMetadata.copyWith(...)` or call `instanceOfProtectedResourceMetadata.copyWith.fieldName(value)` for a single field.
class _$ProtectedResourceMetadataCWProxyImpl
    implements _$ProtectedResourceMetadataCWProxy {
  const _$ProtectedResourceMetadataCWProxyImpl(this._value);

  final ProtectedResourceMetadata _value;

  @override
  ProtectedResourceMetadata resource(String resource) =>
      call(resource: resource);

  @override
  ProtectedResourceMetadata authorizationServers(
    List<String> authorizationServers,
  ) => call(authorizationServers: authorizationServers);

  @override
  ProtectedResourceMetadata scopesSupported(List<String>? scopesSupported) =>
      call(scopesSupported: scopesSupported);

  @override
  ProtectedResourceMetadata bearerMethodsSupported(
    List<ProtectedResourceMetadataBearerMethodsSupportedEnum>
    bearerMethodsSupported,
  ) => call(bearerMethodsSupported: bearerMethodsSupported);

  @override
  ProtectedResourceMetadata resourceName(
    ProtectedResourceMetadataResourceNameEnum? resourceName,
  ) => call(resourceName: resourceName);

  @override
  ProtectedResourceMetadata resourceDocumentation(
    String? resourceDocumentation,
  ) => call(resourceDocumentation: resourceDocumentation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProtectedResourceMetadata(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProtectedResourceMetadata(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProtectedResourceMetadata call({
    Object? resource = const $CopyWithPlaceholder(),
    Object? authorizationServers = const $CopyWithPlaceholder(),
    Object? scopesSupported = const $CopyWithPlaceholder(),
    Object? bearerMethodsSupported = const $CopyWithPlaceholder(),
    Object? resourceName = const $CopyWithPlaceholder(),
    Object? resourceDocumentation = const $CopyWithPlaceholder(),
  }) {
    return ProtectedResourceMetadata(
      resource: resource == const $CopyWithPlaceholder() || resource == null
          ? _value.resource
          // ignore: cast_nullable_to_non_nullable
          : resource as String,
      authorizationServers:
          authorizationServers == const $CopyWithPlaceholder() ||
              authorizationServers == null
          ? _value.authorizationServers
          // ignore: cast_nullable_to_non_nullable
          : authorizationServers as List<String>,
      scopesSupported: scopesSupported == const $CopyWithPlaceholder()
          ? _value.scopesSupported
          // ignore: cast_nullable_to_non_nullable
          : scopesSupported as List<String>?,
      bearerMethodsSupported:
          bearerMethodsSupported == const $CopyWithPlaceholder() ||
              bearerMethodsSupported == null
          ? _value.bearerMethodsSupported
          // ignore: cast_nullable_to_non_nullable
          : bearerMethodsSupported
                as List<ProtectedResourceMetadataBearerMethodsSupportedEnum>,
      resourceName: resourceName == const $CopyWithPlaceholder()
          ? _value.resourceName
          // ignore: cast_nullable_to_non_nullable
          : resourceName as ProtectedResourceMetadataResourceNameEnum?,
      resourceDocumentation:
          resourceDocumentation == const $CopyWithPlaceholder()
          ? _value.resourceDocumentation
          // ignore: cast_nullable_to_non_nullable
          : resourceDocumentation as String?,
    );
  }
}

extension $ProtectedResourceMetadataCopyWith on ProtectedResourceMetadata {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProtectedResourceMetadata.copyWith(...)` or `instanceOfProtectedResourceMetadata.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProtectedResourceMetadataCWProxy get copyWith =>
      _$ProtectedResourceMetadataCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProtectedResourceMetadata _$ProtectedResourceMetadataFromJson(
  Map<String, dynamic> json,
) => $checkedCreate(
  'ProtectedResourceMetadata',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const [
        'resource',
        'authorization_servers',
        'bearer_methods_supported',
      ],
    );
    final val = ProtectedResourceMetadata(
      resource: $checkedConvert('resource', (v) => v as String),
      authorizationServers: $checkedConvert(
        'authorization_servers',
        (v) => (v as List<dynamic>).map((e) => e as String).toList(),
      ),
      scopesSupported: $checkedConvert(
        'scopes_supported',
        (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
      ),
      bearerMethodsSupported: $checkedConvert(
        'bearer_methods_supported',
        (v) => (v as List<dynamic>)
            .map(
              (e) => $enumDecode(
                _$ProtectedResourceMetadataBearerMethodsSupportedEnumEnumMap,
                e,
              ),
            )
            .toList(),
      ),
      resourceName: $checkedConvert(
        'resource_name',
        (v) => $enumDecodeNullable(
          _$ProtectedResourceMetadataResourceNameEnumEnumMap,
          v,
        ),
      ),
      resourceDocumentation: $checkedConvert(
        'resource_documentation',
        (v) => v as String?,
      ),
    );
    return val;
  },
  fieldKeyMap: const {
    'authorizationServers': 'authorization_servers',
    'scopesSupported': 'scopes_supported',
    'bearerMethodsSupported': 'bearer_methods_supported',
    'resourceName': 'resource_name',
    'resourceDocumentation': 'resource_documentation',
  },
);

Map<String, dynamic> _$ProtectedResourceMetadataToJson(
  ProtectedResourceMetadata instance,
) => <String, dynamic>{
  'resource': instance.resource,
  'authorization_servers': instance.authorizationServers,
  'scopes_supported': ?instance.scopesSupported,
  'bearer_methods_supported': instance.bearerMethodsSupported
      .map(
        (e) => _$ProtectedResourceMetadataBearerMethodsSupportedEnumEnumMap[e]!,
      )
      .toList(),
  'resource_name':
      ?_$ProtectedResourceMetadataResourceNameEnumEnumMap[instance
          .resourceName],
  'resource_documentation': ?instance.resourceDocumentation,
};

const _$ProtectedResourceMetadataBearerMethodsSupportedEnumEnumMap = {
  ProtectedResourceMetadataBearerMethodsSupportedEnum.header: 'header',
};

const _$ProtectedResourceMetadataResourceNameEnumEnumMap = {
  ProtectedResourceMetadataResourceNameEnum.foresignal: 'Foresignal',
};
