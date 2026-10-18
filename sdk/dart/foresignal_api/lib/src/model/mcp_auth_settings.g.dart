// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mcp_auth_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$McpAuthSettingsCWProxy {
  McpAuthSettings mode(McpAuthSettingsModeEnum mode);

  McpAuthSettings issuer(String? issuer);

  McpAuthSettings audience(String? audience);

  McpAuthSettings jwksUri(String? jwksUri);

  McpAuthSettings scopeMap(Map<String, McpAuthSettingsScopeMapEnum>? scopeMap);

  McpAuthSettings scopeClaim(McpAuthSettingsScopeClaimEnum? scopeClaim);

  McpAuthSettings allowedAlgs(
    List<McpAuthSettingsAllowedAlgsEnum>? allowedAlgs,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `McpAuthSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// McpAuthSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  McpAuthSettings call({
    McpAuthSettingsModeEnum mode,
    String? issuer,
    String? audience,
    String? jwksUri,
    Map<String, McpAuthSettingsScopeMapEnum>? scopeMap,
    McpAuthSettingsScopeClaimEnum? scopeClaim,
    List<McpAuthSettingsAllowedAlgsEnum>? allowedAlgs,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMcpAuthSettings.copyWith(...)` or call `instanceOfMcpAuthSettings.copyWith.fieldName(value)` for a single field.
class _$McpAuthSettingsCWProxyImpl implements _$McpAuthSettingsCWProxy {
  const _$McpAuthSettingsCWProxyImpl(this._value);

  final McpAuthSettings _value;

  @override
  McpAuthSettings mode(McpAuthSettingsModeEnum mode) => call(mode: mode);

  @override
  McpAuthSettings issuer(String? issuer) => call(issuer: issuer);

  @override
  McpAuthSettings audience(String? audience) => call(audience: audience);

  @override
  McpAuthSettings jwksUri(String? jwksUri) => call(jwksUri: jwksUri);

  @override
  McpAuthSettings scopeMap(
    Map<String, McpAuthSettingsScopeMapEnum>? scopeMap,
  ) => call(scopeMap: scopeMap);

  @override
  McpAuthSettings scopeClaim(McpAuthSettingsScopeClaimEnum? scopeClaim) =>
      call(scopeClaim: scopeClaim);

  @override
  McpAuthSettings allowedAlgs(
    List<McpAuthSettingsAllowedAlgsEnum>? allowedAlgs,
  ) => call(allowedAlgs: allowedAlgs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `McpAuthSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// McpAuthSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  McpAuthSettings call({
    Object? mode = const $CopyWithPlaceholder(),
    Object? issuer = const $CopyWithPlaceholder(),
    Object? audience = const $CopyWithPlaceholder(),
    Object? jwksUri = const $CopyWithPlaceholder(),
    Object? scopeMap = const $CopyWithPlaceholder(),
    Object? scopeClaim = const $CopyWithPlaceholder(),
    Object? allowedAlgs = const $CopyWithPlaceholder(),
  }) {
    return McpAuthSettings(
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as McpAuthSettingsModeEnum,
      issuer: issuer == const $CopyWithPlaceholder()
          ? _value.issuer
          // ignore: cast_nullable_to_non_nullable
          : issuer as String?,
      audience: audience == const $CopyWithPlaceholder()
          ? _value.audience
          // ignore: cast_nullable_to_non_nullable
          : audience as String?,
      jwksUri: jwksUri == const $CopyWithPlaceholder()
          ? _value.jwksUri
          // ignore: cast_nullable_to_non_nullable
          : jwksUri as String?,
      scopeMap: scopeMap == const $CopyWithPlaceholder()
          ? _value.scopeMap
          // ignore: cast_nullable_to_non_nullable
          : scopeMap as Map<String, McpAuthSettingsScopeMapEnum>?,
      scopeClaim: scopeClaim == const $CopyWithPlaceholder()
          ? _value.scopeClaim
          // ignore: cast_nullable_to_non_nullable
          : scopeClaim as McpAuthSettingsScopeClaimEnum?,
      allowedAlgs: allowedAlgs == const $CopyWithPlaceholder()
          ? _value.allowedAlgs
          // ignore: cast_nullable_to_non_nullable
          : allowedAlgs as List<McpAuthSettingsAllowedAlgsEnum>?,
    );
  }
}

extension $McpAuthSettingsCopyWith on McpAuthSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMcpAuthSettings.copyWith(...)` or `instanceOfMcpAuthSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$McpAuthSettingsCWProxy get copyWith => _$McpAuthSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

McpAuthSettings _$McpAuthSettingsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('McpAuthSettings', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['mode']);
  final val = McpAuthSettings(
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecode(_$McpAuthSettingsModeEnumEnumMap, v),
    ),
    issuer: $checkedConvert('issuer', (v) => v as String?),
    audience: $checkedConvert('audience', (v) => v as String?),
    jwksUri: $checkedConvert('jwksUri', (v) => v as String?),
    scopeMap: $checkedConvert(
      'scopeMap',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) =>
            MapEntry(k, $enumDecode(_$McpAuthSettingsScopeMapEnumEnumMap, e)),
      ),
    ),
    scopeClaim: $checkedConvert(
      'scopeClaim',
      (v) => $enumDecodeNullable(_$McpAuthSettingsScopeClaimEnumEnumMap, v),
    ),
    allowedAlgs: $checkedConvert(
      'allowedAlgs',
      (v) => (v as List<dynamic>?)
          ?.map((e) => $enumDecode(_$McpAuthSettingsAllowedAlgsEnumEnumMap, e))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$McpAuthSettingsToJson(
  McpAuthSettings instance,
) => <String, dynamic>{
  'mode': _$McpAuthSettingsModeEnumEnumMap[instance.mode]!,
  'issuer': ?instance.issuer,
  'audience': ?instance.audience,
  'jwksUri': ?instance.jwksUri,
  'scopeMap': ?instance.scopeMap?.map(
    (k, e) => MapEntry(k, _$McpAuthSettingsScopeMapEnumEnumMap[e]!),
  ),
  'scopeClaim': ?_$McpAuthSettingsScopeClaimEnumEnumMap[instance.scopeClaim],
  'allowedAlgs': ?instance.allowedAlgs
      ?.map((e) => _$McpAuthSettingsAllowedAlgsEnumEnumMap[e]!)
      .toList(),
};

const _$McpAuthSettingsModeEnumEnumMap = {
  McpAuthSettingsModeEnum.token: 'token',
  McpAuthSettingsModeEnum.oauth: 'oauth',
  McpAuthSettingsModeEnum.both: 'both',
};

const _$McpAuthSettingsScopeMapEnumEnumMap = {
  McpAuthSettingsScopeMapEnum.read: 'read',
  McpAuthSettingsScopeMapEnum.agent: 'agent',
};

const _$McpAuthSettingsScopeClaimEnumEnumMap = {
  McpAuthSettingsScopeClaimEnum.scope: 'scope',
  McpAuthSettingsScopeClaimEnum.scp: 'scp',
};

const _$McpAuthSettingsAllowedAlgsEnumEnumMap = {
  McpAuthSettingsAllowedAlgsEnum.RS256: 'RS256',
  McpAuthSettingsAllowedAlgsEnum.ES256: 'ES256',
  McpAuthSettingsAllowedAlgsEnum.edDSA: 'EdDSA',
  McpAuthSettingsAllowedAlgsEnum.ed25519: 'Ed25519',
};
