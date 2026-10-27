// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'api_token.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ApiTokenCWProxy {
  ApiToken id(String id);

  ApiToken name(String name);

  ApiToken scope(ApiTokenScopeEnum scope);

  ApiToken tokenPrefix(String tokenPrefix);

  ApiToken createdAt(DateTime createdAt);

  ApiToken lastUsedAt(DateTime? lastUsedAt);

  ApiToken expiresAt(DateTime? expiresAt);

  ApiToken revokedAt(DateTime? revokedAt);

  ApiToken bootstrap(bool? bootstrap);

  ApiToken restrictions(ReadRestriction? restrictions);

  ApiToken device(DeviceInfo? device);

  ApiToken createdVia(ApiTokenCreatedViaEnum? createdVia);

  ApiToken principalId(String? principalId);

  ApiToken privateAccess(bool? privateAccess);

  ApiToken licencePolicy(List<ApiTokenLicencePolicyEnum>? licencePolicy);

  ApiToken principalHandle(String? principalHandle);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ApiToken(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ApiToken(...).copyWith(id: 12, name: "My name")
  /// ```
  ApiToken call({
    String id,
    String name,
    ApiTokenScopeEnum scope,
    String tokenPrefix,
    DateTime createdAt,
    DateTime? lastUsedAt,
    DateTime? expiresAt,
    DateTime? revokedAt,
    bool? bootstrap,
    ReadRestriction? restrictions,
    DeviceInfo? device,
    ApiTokenCreatedViaEnum? createdVia,
    String? principalId,
    bool? privateAccess,
    List<ApiTokenLicencePolicyEnum>? licencePolicy,
    String? principalHandle,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfApiToken.copyWith(...)` or call `instanceOfApiToken.copyWith.fieldName(value)` for a single field.
class _$ApiTokenCWProxyImpl implements _$ApiTokenCWProxy {
  const _$ApiTokenCWProxyImpl(this._value);

  final ApiToken _value;

  @override
  ApiToken id(String id) => call(id: id);

  @override
  ApiToken name(String name) => call(name: name);

  @override
  ApiToken scope(ApiTokenScopeEnum scope) => call(scope: scope);

  @override
  ApiToken tokenPrefix(String tokenPrefix) => call(tokenPrefix: tokenPrefix);

  @override
  ApiToken createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  ApiToken lastUsedAt(DateTime? lastUsedAt) => call(lastUsedAt: lastUsedAt);

  @override
  ApiToken expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  @override
  ApiToken revokedAt(DateTime? revokedAt) => call(revokedAt: revokedAt);

  @override
  ApiToken bootstrap(bool? bootstrap) => call(bootstrap: bootstrap);

  @override
  ApiToken restrictions(ReadRestriction? restrictions) =>
      call(restrictions: restrictions);

  @override
  ApiToken device(DeviceInfo? device) => call(device: device);

  @override
  ApiToken createdVia(ApiTokenCreatedViaEnum? createdVia) =>
      call(createdVia: createdVia);

  @override
  ApiToken principalId(String? principalId) => call(principalId: principalId);

  @override
  ApiToken privateAccess(bool? privateAccess) =>
      call(privateAccess: privateAccess);

  @override
  ApiToken licencePolicy(List<ApiTokenLicencePolicyEnum>? licencePolicy) =>
      call(licencePolicy: licencePolicy);

  @override
  ApiToken principalHandle(String? principalHandle) =>
      call(principalHandle: principalHandle);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ApiToken(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ApiToken(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ApiToken call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? tokenPrefix = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? lastUsedAt = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? revokedAt = const $CopyWithPlaceholder(),
    Object? bootstrap = const $CopyWithPlaceholder(),
    Object? restrictions = const $CopyWithPlaceholder(),
    Object? device = const $CopyWithPlaceholder(),
    Object? createdVia = const $CopyWithPlaceholder(),
    Object? principalId = const $CopyWithPlaceholder(),
    Object? privateAccess = const $CopyWithPlaceholder(),
    Object? licencePolicy = const $CopyWithPlaceholder(),
    Object? principalHandle = const $CopyWithPlaceholder(),
  }) {
    return ApiToken(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      scope: scope == const $CopyWithPlaceholder() || scope == null
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as ApiTokenScopeEnum,
      tokenPrefix:
          tokenPrefix == const $CopyWithPlaceholder() || tokenPrefix == null
          ? _value.tokenPrefix
          // ignore: cast_nullable_to_non_nullable
          : tokenPrefix as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      lastUsedAt: lastUsedAt == const $CopyWithPlaceholder()
          ? _value.lastUsedAt
          // ignore: cast_nullable_to_non_nullable
          : lastUsedAt as DateTime?,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      revokedAt: revokedAt == const $CopyWithPlaceholder()
          ? _value.revokedAt
          // ignore: cast_nullable_to_non_nullable
          : revokedAt as DateTime?,
      bootstrap: bootstrap == const $CopyWithPlaceholder()
          ? _value.bootstrap
          // ignore: cast_nullable_to_non_nullable
          : bootstrap as bool?,
      restrictions: restrictions == const $CopyWithPlaceholder()
          ? _value.restrictions
          // ignore: cast_nullable_to_non_nullable
          : restrictions as ReadRestriction?,
      device: device == const $CopyWithPlaceholder()
          ? _value.device
          // ignore: cast_nullable_to_non_nullable
          : device as DeviceInfo?,
      createdVia: createdVia == const $CopyWithPlaceholder()
          ? _value.createdVia
          // ignore: cast_nullable_to_non_nullable
          : createdVia as ApiTokenCreatedViaEnum?,
      principalId: principalId == const $CopyWithPlaceholder()
          ? _value.principalId
          // ignore: cast_nullable_to_non_nullable
          : principalId as String?,
      privateAccess: privateAccess == const $CopyWithPlaceholder()
          ? _value.privateAccess
          // ignore: cast_nullable_to_non_nullable
          : privateAccess as bool?,
      licencePolicy: licencePolicy == const $CopyWithPlaceholder()
          ? _value.licencePolicy
          // ignore: cast_nullable_to_non_nullable
          : licencePolicy as List<ApiTokenLicencePolicyEnum>?,
      principalHandle: principalHandle == const $CopyWithPlaceholder()
          ? _value.principalHandle
          // ignore: cast_nullable_to_non_nullable
          : principalHandle as String?,
    );
  }
}

extension $ApiTokenCopyWith on ApiToken {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfApiToken.copyWith(...)` or `instanceOfApiToken.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ApiTokenCWProxy get copyWith => _$ApiTokenCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ApiToken _$ApiTokenFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ApiToken', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'name', 'scope', 'tokenPrefix', 'createdAt'],
  );
  final val = ApiToken(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    scope: $checkedConvert(
      'scope',
      (v) => $enumDecode(_$ApiTokenScopeEnumEnumMap, v),
    ),
    tokenPrefix: $checkedConvert('tokenPrefix', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    lastUsedAt: $checkedConvert(
      'lastUsedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    expiresAt: $checkedConvert(
      'expiresAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    revokedAt: $checkedConvert(
      'revokedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    bootstrap: $checkedConvert('bootstrap', (v) => v as bool?),
    restrictions: $checkedConvert(
      'restrictions',
      (v) => v == null
          ? null
          : ReadRestriction.fromJson(v as Map<String, dynamic>),
    ),
    device: $checkedConvert(
      'device',
      (v) => v == null ? null : DeviceInfo.fromJson(v as Map<String, dynamic>),
    ),
    createdVia: $checkedConvert(
      'createdVia',
      (v) => $enumDecodeNullable(_$ApiTokenCreatedViaEnumEnumMap, v),
    ),
    principalId: $checkedConvert('principalId', (v) => v as String?),
    privateAccess: $checkedConvert('privateAccess', (v) => v as bool?),
    licencePolicy: $checkedConvert(
      'licencePolicy',
      (v) => (v as List<dynamic>?)
          ?.map((e) => $enumDecode(_$ApiTokenLicencePolicyEnumEnumMap, e))
          .toList(),
    ),
    principalHandle: $checkedConvert('principalHandle', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ApiTokenToJson(ApiToken instance) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'scope': _$ApiTokenScopeEnumEnumMap[instance.scope]!,
  'tokenPrefix': instance.tokenPrefix,
  'createdAt': instance.createdAt.toIso8601String(),
  'lastUsedAt': ?instance.lastUsedAt?.toIso8601String(),
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
  'revokedAt': ?instance.revokedAt?.toIso8601String(),
  'bootstrap': ?instance.bootstrap,
  'restrictions': ?instance.restrictions?.toJson(),
  'device': ?instance.device?.toJson(),
  'createdVia': ?_$ApiTokenCreatedViaEnumEnumMap[instance.createdVia],
  'principalId': ?instance.principalId,
  'privateAccess': ?instance.privateAccess,
  'licencePolicy': ?instance.licencePolicy
      ?.map((e) => _$ApiTokenLicencePolicyEnumEnumMap[e]!)
      .toList(),
  'principalHandle': ?instance.principalHandle,
};

const _$ApiTokenScopeEnumEnumMap = {
  ApiTokenScopeEnum.read: 'read',
  ApiTokenScopeEnum.agent: 'agent',
  ApiTokenScopeEnum.admin: 'admin',
};

const _$ApiTokenCreatedViaEnumEnumMap = {
  ApiTokenCreatedViaEnum.api: 'api',
  ApiTokenCreatedViaEnum.pairing: 'pairing',
  ApiTokenCreatedViaEnum.bootstrap: 'bootstrap',
  ApiTokenCreatedViaEnum.self: 'self',
};

const _$ApiTokenLicencePolicyEnumEnumMap = {
  ApiTokenLicencePolicyEnum.noNc: 'no-nc',
  ApiTokenLicencePolicyEnum.noSa: 'no-sa',
  ApiTokenLicencePolicyEnum.redistributable: 'redistributable',
};
