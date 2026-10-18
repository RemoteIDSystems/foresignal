// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pairing_code_created.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PairingCodeCreatedCWProxy {
  PairingCodeCreated id(String id);

  PairingCodeCreated name(String name);

  PairingCodeCreated scope(PairingCodeCreatedScopeEnum scope);

  PairingCodeCreated restrictions(ReadRestriction? restrictions);

  PairingCodeCreated principalId(String? principalId);

  PairingCodeCreated privateAccess(bool? privateAccess);

  PairingCodeCreated createdAt(DateTime createdAt);

  PairingCodeCreated expiresAt(DateTime expiresAt);

  PairingCodeCreated createdByTokenId(String? createdByTokenId);

  PairingCodeCreated claimedAt(DateTime? claimedAt);

  PairingCodeCreated claimedTokenId(String? claimedTokenId);

  PairingCodeCreated code(String code);

  PairingCodeCreated qrPayload(String qrPayload);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PairingCodeCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PairingCodeCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  PairingCodeCreated call({
    String id,
    String name,
    PairingCodeCreatedScopeEnum scope,
    ReadRestriction? restrictions,
    String? principalId,
    bool? privateAccess,
    DateTime createdAt,
    DateTime expiresAt,
    String? createdByTokenId,
    DateTime? claimedAt,
    String? claimedTokenId,
    String code,
    String qrPayload,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPairingCodeCreated.copyWith(...)` or call `instanceOfPairingCodeCreated.copyWith.fieldName(value)` for a single field.
class _$PairingCodeCreatedCWProxyImpl implements _$PairingCodeCreatedCWProxy {
  const _$PairingCodeCreatedCWProxyImpl(this._value);

  final PairingCodeCreated _value;

  @override
  PairingCodeCreated id(String id) => call(id: id);

  @override
  PairingCodeCreated name(String name) => call(name: name);

  @override
  PairingCodeCreated scope(PairingCodeCreatedScopeEnum scope) =>
      call(scope: scope);

  @override
  PairingCodeCreated restrictions(ReadRestriction? restrictions) =>
      call(restrictions: restrictions);

  @override
  PairingCodeCreated principalId(String? principalId) =>
      call(principalId: principalId);

  @override
  PairingCodeCreated privateAccess(bool? privateAccess) =>
      call(privateAccess: privateAccess);

  @override
  PairingCodeCreated createdAt(DateTime createdAt) =>
      call(createdAt: createdAt);

  @override
  PairingCodeCreated expiresAt(DateTime expiresAt) =>
      call(expiresAt: expiresAt);

  @override
  PairingCodeCreated createdByTokenId(String? createdByTokenId) =>
      call(createdByTokenId: createdByTokenId);

  @override
  PairingCodeCreated claimedAt(DateTime? claimedAt) =>
      call(claimedAt: claimedAt);

  @override
  PairingCodeCreated claimedTokenId(String? claimedTokenId) =>
      call(claimedTokenId: claimedTokenId);

  @override
  PairingCodeCreated code(String code) => call(code: code);

  @override
  PairingCodeCreated qrPayload(String qrPayload) => call(qrPayload: qrPayload);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PairingCodeCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PairingCodeCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PairingCodeCreated call({
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
    Object? code = const $CopyWithPlaceholder(),
    Object? qrPayload = const $CopyWithPlaceholder(),
  }) {
    return PairingCodeCreated(
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
          : scope as PairingCodeCreatedScopeEnum,
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
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String,
      qrPayload: qrPayload == const $CopyWithPlaceholder() || qrPayload == null
          ? _value.qrPayload
          // ignore: cast_nullable_to_non_nullable
          : qrPayload as String,
    );
  }
}

extension $PairingCodeCreatedCopyWith on PairingCodeCreated {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPairingCodeCreated.copyWith(...)` or `instanceOfPairingCodeCreated.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PairingCodeCreatedCWProxy get copyWith =>
      _$PairingCodeCreatedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PairingCodeCreated _$PairingCodeCreatedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PairingCodeCreated', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'scope',
      'createdAt',
      'expiresAt',
      'code',
      'qrPayload',
    ],
  );
  final val = PairingCodeCreated(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    scope: $checkedConvert(
      'scope',
      (v) => $enumDecode(_$PairingCodeCreatedScopeEnumEnumMap, v),
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
    code: $checkedConvert('code', (v) => v as String),
    qrPayload: $checkedConvert('qrPayload', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$PairingCodeCreatedToJson(PairingCodeCreated instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'scope': _$PairingCodeCreatedScopeEnumEnumMap[instance.scope]!,
      'restrictions': ?instance.restrictions?.toJson(),
      'principalId': ?instance.principalId,
      'privateAccess': ?instance.privateAccess,
      'createdAt': instance.createdAt.toIso8601String(),
      'expiresAt': instance.expiresAt.toIso8601String(),
      'createdByTokenId': ?instance.createdByTokenId,
      'claimedAt': ?instance.claimedAt?.toIso8601String(),
      'claimedTokenId': ?instance.claimedTokenId,
      'code': instance.code,
      'qrPayload': instance.qrPayload,
    };

const _$PairingCodeCreatedScopeEnumEnumMap = {
  PairingCodeCreatedScopeEnum.read: 'read',
  PairingCodeCreatedScopeEnum.agent: 'agent',
};
