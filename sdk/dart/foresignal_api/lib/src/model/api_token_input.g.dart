// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'api_token_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ApiTokenInputCWProxy {
  ApiTokenInput name(String name);

  ApiTokenInput scope(ApiTokenInputScopeEnum scope);

  ApiTokenInput expiresAt(DateTime? expiresAt);

  ApiTokenInput restrictions(ReadRestriction? restrictions);

  ApiTokenInput principalId(String? principalId);

  ApiTokenInput privateAccess(bool? privateAccess);

  ApiTokenInput licencePolicy(
    List<ApiTokenInputLicencePolicyEnum>? licencePolicy,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ApiTokenInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ApiTokenInput(...).copyWith(id: 12, name: "My name")
  /// ```
  ApiTokenInput call({
    String name,
    ApiTokenInputScopeEnum scope,
    DateTime? expiresAt,
    ReadRestriction? restrictions,
    String? principalId,
    bool? privateAccess,
    List<ApiTokenInputLicencePolicyEnum>? licencePolicy,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfApiTokenInput.copyWith(...)` or call `instanceOfApiTokenInput.copyWith.fieldName(value)` for a single field.
class _$ApiTokenInputCWProxyImpl implements _$ApiTokenInputCWProxy {
  const _$ApiTokenInputCWProxyImpl(this._value);

  final ApiTokenInput _value;

  @override
  ApiTokenInput name(String name) => call(name: name);

  @override
  ApiTokenInput scope(ApiTokenInputScopeEnum scope) => call(scope: scope);

  @override
  ApiTokenInput expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  @override
  ApiTokenInput restrictions(ReadRestriction? restrictions) =>
      call(restrictions: restrictions);

  @override
  ApiTokenInput principalId(String? principalId) =>
      call(principalId: principalId);

  @override
  ApiTokenInput privateAccess(bool? privateAccess) =>
      call(privateAccess: privateAccess);

  @override
  ApiTokenInput licencePolicy(
    List<ApiTokenInputLicencePolicyEnum>? licencePolicy,
  ) => call(licencePolicy: licencePolicy);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ApiTokenInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ApiTokenInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ApiTokenInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? restrictions = const $CopyWithPlaceholder(),
    Object? principalId = const $CopyWithPlaceholder(),
    Object? privateAccess = const $CopyWithPlaceholder(),
    Object? licencePolicy = const $CopyWithPlaceholder(),
  }) {
    return ApiTokenInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      scope: scope == const $CopyWithPlaceholder() || scope == null
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as ApiTokenInputScopeEnum,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      restrictions: restrictions == const $CopyWithPlaceholder()
          ? _value.restrictions
          // ignore: cast_nullable_to_non_nullable
          : restrictions as ReadRestriction?,
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
          : licencePolicy as List<ApiTokenInputLicencePolicyEnum>?,
    );
  }
}

extension $ApiTokenInputCopyWith on ApiTokenInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfApiTokenInput.copyWith(...)` or `instanceOfApiTokenInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ApiTokenInputCWProxy get copyWith => _$ApiTokenInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ApiTokenInput _$ApiTokenInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ApiTokenInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name', 'scope']);
  final val = ApiTokenInput(
    name: $checkedConvert('name', (v) => v as String),
    scope: $checkedConvert(
      'scope',
      (v) => $enumDecode(_$ApiTokenInputScopeEnumEnumMap, v),
    ),
    expiresAt: $checkedConvert(
      'expiresAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    restrictions: $checkedConvert(
      'restrictions',
      (v) => v == null
          ? null
          : ReadRestriction.fromJson(v as Map<String, dynamic>),
    ),
    principalId: $checkedConvert('principalId', (v) => v as String?),
    privateAccess: $checkedConvert('privateAccess', (v) => v as bool? ?? false),
    licencePolicy: $checkedConvert(
      'licencePolicy',
      (v) => (v as List<dynamic>?)
          ?.map((e) => $enumDecode(_$ApiTokenInputLicencePolicyEnumEnumMap, e))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ApiTokenInputToJson(ApiTokenInput instance) =>
    <String, dynamic>{
      'name': instance.name,
      'scope': _$ApiTokenInputScopeEnumEnumMap[instance.scope]!,
      'expiresAt': ?instance.expiresAt?.toIso8601String(),
      'restrictions': ?instance.restrictions?.toJson(),
      'principalId': ?instance.principalId,
      'privateAccess': ?instance.privateAccess,
      'licencePolicy': ?instance.licencePolicy
          ?.map((e) => _$ApiTokenInputLicencePolicyEnumEnumMap[e]!)
          .toList(),
    };

const _$ApiTokenInputScopeEnumEnumMap = {
  ApiTokenInputScopeEnum.read: 'read',
  ApiTokenInputScopeEnum.agent: 'agent',
  ApiTokenInputScopeEnum.admin: 'admin',
};

const _$ApiTokenInputLicencePolicyEnumEnumMap = {
  ApiTokenInputLicencePolicyEnum.noNc: 'no-nc',
  ApiTokenInputLicencePolicyEnum.noSa: 'no-sa',
  ApiTokenInputLicencePolicyEnum.redistributable: 'redistributable',
};
