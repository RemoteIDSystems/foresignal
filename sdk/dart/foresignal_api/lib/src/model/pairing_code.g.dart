// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pairing_code.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PairingCodeCWProxy {
  PairingCode id(String id);

  PairingCode name(String name);

  PairingCode scope(PairingCodeScopeEnum scope);

  PairingCode restrictions(ReadRestriction? restrictions);

  PairingCode principalId(String? principalId);

  PairingCode privateAccess(bool? privateAccess);

  PairingCode createdAt(DateTime createdAt);

  PairingCode expiresAt(DateTime expiresAt);

  PairingCode createdByTokenId(String? createdByTokenId);

  PairingCode claimedAt(DateTime? claimedAt);

  PairingCode claimedTokenId(String? claimedTokenId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PairingCode(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PairingCode(...).copyWith(id: 12, name: "My name")
  /// ```
  PairingCode call({
    String id,
    String name,
    PairingCodeScopeEnum scope,
    ReadRestriction? restrictions,
    String? principalId,
    bool? privateAccess,
    DateTime createdAt,
    DateTime expiresAt,
    String? createdByTokenId,
    DateTime? claimedAt,
    String? claimedTokenId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPairingCode.copyWith(...)` or call `instanceOfPairingCode.copyWith.fieldName(value)` for a single field.
class _$PairingCodeCWProxyImpl implements _$PairingCodeCWProxy {
  const _$PairingCodeCWProxyImpl(this._value);

  final PairingCode _value;

  @override
  PairingCode id(String id) => call(id: id);

  @override
  PairingCode name(String name) => call(name: name);

  @override
  PairingCode scope(PairingCodeScopeEnum scope) => call(scope: scope);

  @override
  PairingCode restrictions(ReadRestriction? restrictions) =>
      call(restrictions: restrictions);

  @override
  PairingCode principalId(String? principalId) =>
      call(principalId: principalId);

  @override
  PairingCode privateAccess(bool? privateAccess) =>
      call(privateAccess: privateAccess);

  @override
  PairingCode createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  PairingCode expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  @override
  PairingCode createdByTokenId(String? createdByTokenId) =>
      call(createdByTokenId: createdByTokenId);

  @override
  PairingCode claimedAt(DateTime? claimedAt) => call(claimedAt: claimedAt);

  @override
  PairingCode claimedTokenId(String? claimedTokenId) =>
      call(claimedTokenId: claimedTokenId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PairingCode(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PairingCode(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PairingCode call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? restrictions = const $CopyWithPlaceholder(),
    Object? principalId = const $CopyWithPlaceholder(),
    Object? privateAccess = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? createdByTokenId = const $CopyWithPlaceholder(),
    Object? claimedAt = const $CopyWithPlaceholder(),
    Object? claimedTokenId = const $CopyWithPlaceholder(),
  }) {
    return PairingCode(
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
          : scope as PairingCodeScopeEnum,
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
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      expiresAt: expiresAt == const $CopyWithPlaceholder() || expiresAt == null
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime,
      createdByTokenId: createdByTokenId == const $CopyWithPlaceholder()
          ? _value.createdByTokenId
          // ignore: cast_nullable_to_non_nullable
          : createdByTokenId as String?,
      claimedAt: claimedAt == const $CopyWithPlaceholder()
          ? _value.claimedAt
          // ignore: cast_nullable_to_non_nullable
          : claimedAt as DateTime?,
      claimedTokenId: claimedTokenId == const $CopyWithPlaceholder()
          ? _value.claimedTokenId
          // ignore: cast_nullable_to_non_nullable
          : claimedTokenId as String?,
    );
  }
}

extension $PairingCodeCopyWith on PairingCode {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPairingCode.copyWith(...)` or `instanceOfPairingCode.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PairingCodeCWProxy get copyWith => _$PairingCodeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PairingCode _$PairingCodeFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PairingCode', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'name', 'scope', 'createdAt', 'expiresAt'],
  );
  final val = PairingCode(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    scope: $checkedConvert(
      'scope',
      (v) => $enumDecode(_$PairingCodeScopeEnumEnumMap, v),
    ),
    restrictions: $checkedConvert(
      'restrictions',
      (v) => v == null
          ? null
          : ReadRestriction.fromJson(v as Map<String, dynamic>),
    ),
    principalId: $checkedConvert('principalId', (v) => v as String?),
    privateAccess: $checkedConvert('privateAccess', (v) => v as bool?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    expiresAt: $checkedConvert('expiresAt', (v) => DateTime.parse(v as String)),
    createdByTokenId: $checkedConvert('createdByTokenId', (v) => v as String?),
    claimedAt: $checkedConvert(
      'claimedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    claimedTokenId: $checkedConvert('claimedTokenId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PairingCodeToJson(PairingCode instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'scope': _$PairingCodeScopeEnumEnumMap[instance.scope]!,
      'restrictions': ?instance.restrictions?.toJson(),
      'principalId': ?instance.principalId,
      'privateAccess': ?instance.privateAccess,
      'createdAt': instance.createdAt.toIso8601String(),
      'expiresAt': instance.expiresAt.toIso8601String(),
      'createdByTokenId': ?instance.createdByTokenId,
      'claimedAt': ?instance.claimedAt?.toIso8601String(),
      'claimedTokenId': ?instance.claimedTokenId,
    };

const _$PairingCodeScopeEnumEnumMap = {
  PairingCodeScopeEnum.read: 'read',
  PairingCodeScopeEnum.agent: 'agent',
};
