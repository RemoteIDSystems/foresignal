// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'api_token_created.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ApiTokenCreatedCWProxy {
  ApiTokenCreated id(String id);

  ApiTokenCreated name(String name);

  ApiTokenCreated scope(ApiTokenCreatedScopeEnum scope);

  ApiTokenCreated tokenPrefix(String tokenPrefix);

  ApiTokenCreated createdAt(DateTime createdAt);

  ApiTokenCreated lastUsedAt(DateTime? lastUsedAt);

  ApiTokenCreated expiresAt(DateTime? expiresAt);

  ApiTokenCreated revokedAt(DateTime? revokedAt);

  ApiTokenCreated bootstrap(bool? bootstrap);

  ApiTokenCreated restrictions(ReadRestriction? restrictions);

  ApiTokenCreated device(DeviceInfo? device);

  ApiTokenCreated createdVia(ApiTokenCreatedCreatedViaEnum? createdVia);

  ApiTokenCreated principalId(String? principalId);

  ApiTokenCreated privateAccess(bool? privateAccess);

  ApiTokenCreated licencePolicy(
    List<ApiTokenCreatedLicencePolicyEnum>? licencePolicy,
  );

  ApiTokenCreated principalHandle(String? principalHandle);

  ApiTokenCreated token(String token);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ApiTokenCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ApiTokenCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  ApiTokenCreated call({
    String id,
    String name,
    ApiTokenCreatedScopeEnum scope,
    String tokenPrefix,
    DateTime createdAt,
    DateTime? lastUsedAt,
    DateTime? expiresAt,
    DateTime? revokedAt,
    bool? bootstrap,
    ReadRestriction? restrictions,
    DeviceInfo? device,
    ApiTokenCreatedCreatedViaEnum? createdVia,
    String? principalId,
    bool? privateAccess,
    List<ApiTokenCreatedLicencePolicyEnum>? licencePolicy,
    String? principalHandle,
    String token,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfApiTokenCreated.copyWith(...)` or call `instanceOfApiTokenCreated.copyWith.fieldName(value)` for a single field.
class _$ApiTokenCreatedCWProxyImpl implements _$ApiTokenCreatedCWProxy {
  const _$ApiTokenCreatedCWProxyImpl(this._value);

  final ApiTokenCreated _value;

  @override
  ApiTokenCreated id(String id) => call(id: id);

  @override
  ApiTokenCreated name(String name) => call(name: name);

  @override
  ApiTokenCreated scope(ApiTokenCreatedScopeEnum scope) => call(scope: scope);

  @override
  ApiTokenCreated tokenPrefix(String tokenPrefix) =>
      call(tokenPrefix: tokenPrefix);

  @override
  ApiTokenCreated createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  ApiTokenCreated lastUsedAt(DateTime? lastUsedAt) =>
      call(lastUsedAt: lastUsedAt);

  @override
  ApiTokenCreated expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  @override
  ApiTokenCreated revokedAt(DateTime? revokedAt) => call(revokedAt: revokedAt);

  @override
  ApiTokenCreated bootstrap(bool? bootstrap) => call(bootstrap: bootstrap);

  @override
  ApiTokenCreated restrictions(ReadRestriction? restrictions) =>
      call(restrictions: restrictions);

  @override
  ApiTokenCreated device(DeviceInfo? device) => call(device: device);

  @override
  ApiTokenCreated createdVia(ApiTokenCreatedCreatedViaEnum? createdVia) =>
      call(createdVia: createdVia);

  @override
  ApiTokenCreated principalId(String? principalId) =>
      call(principalId: principalId);

  @override
  ApiTokenCreated privateAccess(bool? privateAccess) =>
      call(privateAccess: privateAccess);

  @override
  ApiTokenCreated licencePolicy(
    List<ApiTokenCreatedLicencePolicyEnum>? licencePolicy,
  ) => call(licencePolicy: licencePolicy);

  @override
  ApiTokenCreated principalHandle(String? principalHandle) =>
      call(principalHandle: principalHandle);

  @override
  ApiTokenCreated token(String token) => call(token: token);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ApiTokenCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ApiTokenCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ApiTokenCreated call({
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
    Object? token = const $CopyWithPlaceholder(),
  }) {
    return ApiTokenCreated(
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
          : scope as ApiTokenCreatedScopeEnum,
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
          : createdVia as ApiTokenCreatedCreatedViaEnum?,
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
          : licencePolicy as List<ApiTokenCreatedLicencePolicyEnum>?,
      principalHandle: principalHandle == const $CopyWithPlaceholder()
          ? _value.principalHandle
          // ignore: cast_nullable_to_non_nullable
          : principalHandle as String?,
      token: token == const $CopyWithPlaceholder() || token == null
          ? _value.token
          // ignore: cast_nullable_to_non_nullable
          : token as String,
    );
  }
}

extension $ApiTokenCreatedCopyWith on ApiTokenCreated {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfApiTokenCreated.copyWith(...)` or `instanceOfApiTokenCreated.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ApiTokenCreatedCWProxy get copyWith => _$ApiTokenCreatedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ApiTokenCreated _$ApiTokenCreatedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ApiTokenCreated', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'scope',
      'tokenPrefix',
      'createdAt',
      'token',
    ],
  );
  final val = ApiTokenCreated(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    scope: $checkedConvert(
      'scope',
      (v) => $enumDecode(_$ApiTokenCreatedScopeEnumEnumMap, v),
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
      (v) => $enumDecodeNullable(_$ApiTokenCreatedCreatedViaEnumEnumMap, v),
    ),
    principalId: $checkedConvert('principalId', (v) => v as String?),
    privateAccess: $checkedConvert('privateAccess', (v) => v as bool?),
    licencePolicy: $checkedConvert(
      'licencePolicy',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => $enumDecode(_$ApiTokenCreatedLicencePolicyEnumEnumMap, e),
          )
          .toList(),
    ),
    principalHandle: $checkedConvert('principalHandle', (v) => v as String?),
    token: $checkedConvert('token', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$ApiTokenCreatedToJson(
  ApiTokenCreated instance,
) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'scope': _$ApiTokenCreatedScopeEnumEnumMap[instance.scope]!,
  'tokenPrefix': instance.tokenPrefix,
  'createdAt': instance.createdAt.toIso8601String(),
  'lastUsedAt': ?instance.lastUsedAt?.toIso8601String(),
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
  'revokedAt': ?instance.revokedAt?.toIso8601String(),
  'bootstrap': ?instance.bootstrap,
  'restrictions': ?instance.restrictions?.toJson(),
  'device': ?instance.device?.toJson(),
  'createdVia': ?_$ApiTokenCreatedCreatedViaEnumEnumMap[instance.createdVia],
  'principalId': ?instance.principalId,
  'privateAccess': ?instance.privateAccess,
  'licencePolicy': ?instance.licencePolicy
      ?.map((e) => _$ApiTokenCreatedLicencePolicyEnumEnumMap[e]!)
      .toList(),
  'principalHandle': ?instance.principalHandle,
  'token': instance.token,
};

const _$ApiTokenCreatedScopeEnumEnumMap = {
  ApiTokenCreatedScopeEnum.read: 'read',
  ApiTokenCreatedScopeEnum.agent: 'agent',
  ApiTokenCreatedScopeEnum.admin: 'admin',
};

const _$ApiTokenCreatedCreatedViaEnumEnumMap = {
  ApiTokenCreatedCreatedViaEnum.api: 'api',
  ApiTokenCreatedCreatedViaEnum.pairing: 'pairing',
  ApiTokenCreatedCreatedViaEnum.bootstrap: 'bootstrap',
  ApiTokenCreatedCreatedViaEnum.self: 'self',
};

const _$ApiTokenCreatedLicencePolicyEnumEnumMap = {
  ApiTokenCreatedLicencePolicyEnum.noNc: 'no-nc',
  ApiTokenCreatedLicencePolicyEnum.noSa: 'no-sa',
  ApiTokenCreatedLicencePolicyEnum.redistributable: 'redistributable',
};
