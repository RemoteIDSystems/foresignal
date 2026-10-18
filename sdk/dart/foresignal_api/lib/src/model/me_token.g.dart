// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'me_token.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MeTokenCWProxy {
  MeToken id(String id);

  MeToken name(String name);

  MeToken scope(MeTokenScopeEnum scope);

  MeToken tokenPrefix(String? tokenPrefix);

  MeToken expiresAt(DateTime? expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MeToken(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MeToken(...).copyWith(id: 12, name: "My name")
  /// ```
  MeToken call({
    String id,
    String name,
    MeTokenScopeEnum scope,
    String? tokenPrefix,
    DateTime? expiresAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMeToken.copyWith(...)` or call `instanceOfMeToken.copyWith.fieldName(value)` for a single field.
class _$MeTokenCWProxyImpl implements _$MeTokenCWProxy {
  const _$MeTokenCWProxyImpl(this._value);

  final MeToken _value;

  @override
  MeToken id(String id) => call(id: id);

  @override
  MeToken name(String name) => call(name: name);

  @override
  MeToken scope(MeTokenScopeEnum scope) => call(scope: scope);

  @override
  MeToken tokenPrefix(String? tokenPrefix) => call(tokenPrefix: tokenPrefix);

  @override
  MeToken expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MeToken(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MeToken(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MeToken call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? tokenPrefix = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return MeToken(
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
          : scope as MeTokenScopeEnum,
      tokenPrefix: tokenPrefix == const $CopyWithPlaceholder()
          ? _value.tokenPrefix
          // ignore: cast_nullable_to_non_nullable
          : tokenPrefix as String?,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
    );
  }
}

extension $MeTokenCopyWith on MeToken {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMeToken.copyWith(...)` or `instanceOfMeToken.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MeTokenCWProxy get copyWith => _$MeTokenCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MeToken _$MeTokenFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MeToken', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'name', 'scope']);
      final val = MeToken(
        id: $checkedConvert('id', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        scope: $checkedConvert(
          'scope',
          (v) => $enumDecode(_$MeTokenScopeEnumEnumMap, v),
        ),
        tokenPrefix: $checkedConvert('tokenPrefix', (v) => v as String?),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MeTokenToJson(MeToken instance) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'scope': _$MeTokenScopeEnumEnumMap[instance.scope]!,
  'tokenPrefix': ?instance.tokenPrefix,
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
};

const _$MeTokenScopeEnumEnumMap = {
  MeTokenScopeEnum.read: 'read',
  MeTokenScopeEnum.agent: 'agent',
  MeTokenScopeEnum.admin: 'admin',
};
