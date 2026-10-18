// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'registration_token.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegistrationTokenCWProxy {
  RegistrationToken id(String id);

  RegistrationToken name(String name);

  RegistrationToken prefix(String prefix);

  RegistrationToken expiresAt(DateTime? expiresAt);

  RegistrationToken maxUses(int maxUses);

  RegistrationToken uses(int uses);

  RegistrationToken sourceIdPrefix(String? sourceIdPrefix);

  RegistrationToken createdAt(DateTime createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegistrationToken(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegistrationToken(...).copyWith(id: 12, name: "My name")
  /// ```
  RegistrationToken call({
    String id,
    String name,
    String prefix,
    DateTime? expiresAt,
    int maxUses,
    int uses,
    String? sourceIdPrefix,
    DateTime createdAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegistrationToken.copyWith(...)` or call `instanceOfRegistrationToken.copyWith.fieldName(value)` for a single field.
class _$RegistrationTokenCWProxyImpl implements _$RegistrationTokenCWProxy {
  const _$RegistrationTokenCWProxyImpl(this._value);

  final RegistrationToken _value;

  @override
  RegistrationToken id(String id) => call(id: id);

  @override
  RegistrationToken name(String name) => call(name: name);

  @override
  RegistrationToken prefix(String prefix) => call(prefix: prefix);

  @override
  RegistrationToken expiresAt(DateTime? expiresAt) =>
      call(expiresAt: expiresAt);

  @override
  RegistrationToken maxUses(int maxUses) => call(maxUses: maxUses);

  @override
  RegistrationToken uses(int uses) => call(uses: uses);

  @override
  RegistrationToken sourceIdPrefix(String? sourceIdPrefix) =>
      call(sourceIdPrefix: sourceIdPrefix);

  @override
  RegistrationToken createdAt(DateTime createdAt) => call(createdAt: createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegistrationToken(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegistrationToken(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegistrationToken call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? prefix = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? maxUses = const $CopyWithPlaceholder(),
    Object? uses = const $CopyWithPlaceholder(),
    Object? sourceIdPrefix = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
  }) {
    return RegistrationToken(
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
    );
  }
}

extension $RegistrationTokenCopyWith on RegistrationToken {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegistrationToken.copyWith(...)` or `instanceOfRegistrationToken.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegistrationTokenCWProxy get copyWith =>
      _$RegistrationTokenCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegistrationToken _$RegistrationTokenFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RegistrationToken', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'name',
          'prefix',
          'maxUses',
          'uses',
          'createdAt',
        ],
      );
      final val = RegistrationToken(
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
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$RegistrationTokenToJson(RegistrationToken instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'prefix': instance.prefix,
      'expiresAt': ?instance.expiresAt?.toIso8601String(),
      'maxUses': instance.maxUses,
      'uses': instance.uses,
      'sourceIdPrefix': ?instance.sourceIdPrefix,
      'createdAt': instance.createdAt.toIso8601String(),
    };
