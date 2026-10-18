// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'registration_token_created.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegistrationTokenCreatedCWProxy {
  RegistrationTokenCreated id(String id);

  RegistrationTokenCreated name(String name);

  RegistrationTokenCreated prefix(String prefix);

  RegistrationTokenCreated expiresAt(DateTime? expiresAt);

  RegistrationTokenCreated maxUses(int maxUses);

  RegistrationTokenCreated uses(int uses);

  RegistrationTokenCreated sourceIdPrefix(String? sourceIdPrefix);

  RegistrationTokenCreated createdAt(DateTime createdAt);

  RegistrationTokenCreated token(String token);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegistrationTokenCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegistrationTokenCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  RegistrationTokenCreated call({
    String id,
    String name,
    String prefix,
    DateTime? expiresAt,
    int maxUses,
    int uses,
    String? sourceIdPrefix,
    DateTime createdAt,
    String token,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegistrationTokenCreated.copyWith(...)` or call `instanceOfRegistrationTokenCreated.copyWith.fieldName(value)` for a single field.
class _$RegistrationTokenCreatedCWProxyImpl
    implements _$RegistrationTokenCreatedCWProxy {
  const _$RegistrationTokenCreatedCWProxyImpl(this._value);

  final RegistrationTokenCreated _value;

  @override
  RegistrationTokenCreated id(String id) => call(id: id);

  @override
  RegistrationTokenCreated name(String name) => call(name: name);

  @override
  RegistrationTokenCreated prefix(String prefix) => call(prefix: prefix);

  @override
  RegistrationTokenCreated expiresAt(DateTime? expiresAt) =>
      call(expiresAt: expiresAt);

  @override
  RegistrationTokenCreated maxUses(int maxUses) => call(maxUses: maxUses);

  @override
  RegistrationTokenCreated uses(int uses) => call(uses: uses);

  @override
  RegistrationTokenCreated sourceIdPrefix(String? sourceIdPrefix) =>
      call(sourceIdPrefix: sourceIdPrefix);

  @override
  RegistrationTokenCreated createdAt(DateTime createdAt) =>
      call(createdAt: createdAt);

  @override
  RegistrationTokenCreated token(String token) => call(token: token);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegistrationTokenCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegistrationTokenCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegistrationTokenCreated call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? prefix = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? maxUses = const $CopyWithPlaceholder(),
    Object? uses = const $CopyWithPlaceholder(),
    Object? sourceIdPrefix = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? token = const $CopyWithPlaceholder(),
  }) {
    return RegistrationTokenCreated(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      prefix: prefix == const $CopyWithPlaceholder() || prefix == null
          ? _value.prefix
          // ignore: cast_nullable_to_non_nullable
          : prefix as String,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      maxUses: maxUses == const $CopyWithPlaceholder() || maxUses == null
          ? _value.maxUses
          // ignore: cast_nullable_to_non_nullable
          : maxUses as int,
      uses: uses == const $CopyWithPlaceholder() || uses == null
          ? _value.uses
          // ignore: cast_nullable_to_non_nullable
          : uses as int,
      sourceIdPrefix: sourceIdPrefix == const $CopyWithPlaceholder()
          ? _value.sourceIdPrefix
          // ignore: cast_nullable_to_non_nullable
          : sourceIdPrefix as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      token: token == const $CopyWithPlaceholder() || token == null
          ? _value.token
          // ignore: cast_nullable_to_non_nullable
          : token as String,
    );
  }
}

extension $RegistrationTokenCreatedCopyWith on RegistrationTokenCreated {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegistrationTokenCreated.copyWith(...)` or `instanceOfRegistrationTokenCreated.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegistrationTokenCreatedCWProxy get copyWith =>
      _$RegistrationTokenCreatedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegistrationTokenCreated _$RegistrationTokenCreatedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RegistrationTokenCreated', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'prefix',
      'maxUses',
      'uses',
      'createdAt',
      'token',
    ],
  );
  final val = RegistrationTokenCreated(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    prefix: $checkedConvert('prefix', (v) => v as String),
    expiresAt: $checkedConvert(
      'expiresAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    maxUses: $checkedConvert('maxUses', (v) => (v as num).toInt()),
    uses: $checkedConvert('uses', (v) => (v as num).toInt()),
    sourceIdPrefix: $checkedConvert('sourceIdPrefix', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    token: $checkedConvert('token', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$RegistrationTokenCreatedToJson(
  RegistrationTokenCreated instance,
) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'prefix': instance.prefix,
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
  'maxUses': instance.maxUses,
  'uses': instance.uses,
  'sourceIdPrefix': ?instance.sourceIdPrefix,
  'createdAt': instance.createdAt.toIso8601String(),
  'token': instance.token,
};
