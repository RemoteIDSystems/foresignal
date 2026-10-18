// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'feed_key.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FeedKeyCWProxy {
  FeedKey id(String id);

  FeedKey name(String name);

  FeedKey keyPrefix(String keyPrefix);

  FeedKey prefixes(List<String> prefixes);

  FeedKey restrictions(ReadRestriction restrictions);

  FeedKey privateAccess(bool? privateAccess);

  FeedKey createdAt(DateTime createdAt);

  FeedKey expiresAt(DateTime expiresAt);

  FeedKey lastUsedAt(DateTime? lastUsedAt);

  FeedKey revokedAt(DateTime? revokedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FeedKey(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FeedKey(...).copyWith(id: 12, name: "My name")
  /// ```
  FeedKey call({
    String id,
    String name,
    String keyPrefix,
    List<String> prefixes,
    ReadRestriction restrictions,
    bool? privateAccess,
    DateTime createdAt,
    DateTime expiresAt,
    DateTime? lastUsedAt,
    DateTime? revokedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFeedKey.copyWith(...)` or call `instanceOfFeedKey.copyWith.fieldName(value)` for a single field.
class _$FeedKeyCWProxyImpl implements _$FeedKeyCWProxy {
  const _$FeedKeyCWProxyImpl(this._value);

  final FeedKey _value;

  @override
  FeedKey id(String id) => call(id: id);

  @override
  FeedKey name(String name) => call(name: name);

  @override
  FeedKey keyPrefix(String keyPrefix) => call(keyPrefix: keyPrefix);

  @override
  FeedKey prefixes(List<String> prefixes) => call(prefixes: prefixes);

  @override
  FeedKey restrictions(ReadRestriction restrictions) =>
      call(restrictions: restrictions);

  @override
  FeedKey privateAccess(bool? privateAccess) =>
      call(privateAccess: privateAccess);

  @override
  FeedKey createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  FeedKey expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  @override
  FeedKey lastUsedAt(DateTime? lastUsedAt) => call(lastUsedAt: lastUsedAt);

  @override
  FeedKey revokedAt(DateTime? revokedAt) => call(revokedAt: revokedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FeedKey(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FeedKey(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FeedKey call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? keyPrefix = const $CopyWithPlaceholder(),
    Object? prefixes = const $CopyWithPlaceholder(),
    Object? restrictions = const $CopyWithPlaceholder(),
    Object? privateAccess = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? lastUsedAt = const $CopyWithPlaceholder(),
    Object? revokedAt = const $CopyWithPlaceholder(),
  }) {
    return FeedKey(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      keyPrefix: keyPrefix == const $CopyWithPlaceholder() || keyPrefix == null
          ? _value.keyPrefix
          // ignore: cast_nullable_to_non_nullable
          : keyPrefix as String,
      prefixes: prefixes == const $CopyWithPlaceholder() || prefixes == null
          ? _value.prefixes
          // ignore: cast_nullable_to_non_nullable
          : prefixes as List<String>,
      restrictions:
          restrictions == const $CopyWithPlaceholder() || restrictions == null
          ? _value.restrictions
          // ignore: cast_nullable_to_non_nullable
          : restrictions as ReadRestriction,
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
      lastUsedAt: lastUsedAt == const $CopyWithPlaceholder()
          ? _value.lastUsedAt
          // ignore: cast_nullable_to_non_nullable
          : lastUsedAt as DateTime?,
      revokedAt: revokedAt == const $CopyWithPlaceholder()
          ? _value.revokedAt
          // ignore: cast_nullable_to_non_nullable
          : revokedAt as DateTime?,
    );
  }
}

extension $FeedKeyCopyWith on FeedKey {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFeedKey.copyWith(...)` or `instanceOfFeedKey.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FeedKeyCWProxy get copyWith => _$FeedKeyCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FeedKey _$FeedKeyFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FeedKey', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'keyPrefix',
      'prefixes',
      'restrictions',
      'createdAt',
      'expiresAt',
    ],
  );
  final val = FeedKey(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    keyPrefix: $checkedConvert('keyPrefix', (v) => v as String),
    prefixes: $checkedConvert(
      'prefixes',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    restrictions: $checkedConvert(
      'restrictions',
      (v) => ReadRestriction.fromJson(v as Map<String, dynamic>),
    ),
    privateAccess: $checkedConvert('privateAccess', (v) => v as bool?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    expiresAt: $checkedConvert('expiresAt', (v) => DateTime.parse(v as String)),
    lastUsedAt: $checkedConvert(
      'lastUsedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    revokedAt: $checkedConvert(
      'revokedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$FeedKeyToJson(FeedKey instance) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'keyPrefix': instance.keyPrefix,
  'prefixes': instance.prefixes,
  'restrictions': instance.restrictions.toJson(),
  'privateAccess': ?instance.privateAccess,
  'createdAt': instance.createdAt.toIso8601String(),
  'expiresAt': instance.expiresAt.toIso8601String(),
  'lastUsedAt': ?instance.lastUsedAt?.toIso8601String(),
  'revokedAt': ?instance.revokedAt?.toIso8601String(),
};
