// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'feed_key_created.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FeedKeyCreatedCWProxy {
  FeedKeyCreated id(String id);

  FeedKeyCreated name(String name);

  FeedKeyCreated keyPrefix(String keyPrefix);

  FeedKeyCreated prefixes(List<String> prefixes);

  FeedKeyCreated restrictions(ReadRestriction restrictions);

  FeedKeyCreated privateAccess(bool? privateAccess);

  FeedKeyCreated createdAt(DateTime createdAt);

  FeedKeyCreated expiresAt(DateTime expiresAt);

  FeedKeyCreated lastUsedAt(DateTime? lastUsedAt);

  FeedKeyCreated revokedAt(DateTime? revokedAt);

  FeedKeyCreated key(String key);

  FeedKeyCreated urls(List<FeedKeyCreatedAllOfUrls> urls);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FeedKeyCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FeedKeyCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  FeedKeyCreated call({
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
    String key,
    List<FeedKeyCreatedAllOfUrls> urls,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFeedKeyCreated.copyWith(...)` or call `instanceOfFeedKeyCreated.copyWith.fieldName(value)` for a single field.
class _$FeedKeyCreatedCWProxyImpl implements _$FeedKeyCreatedCWProxy {
  const _$FeedKeyCreatedCWProxyImpl(this._value);

  final FeedKeyCreated _value;

  @override
  FeedKeyCreated id(String id) => call(id: id);

  @override
  FeedKeyCreated name(String name) => call(name: name);

  @override
  FeedKeyCreated keyPrefix(String keyPrefix) => call(keyPrefix: keyPrefix);

  @override
  FeedKeyCreated prefixes(List<String> prefixes) => call(prefixes: prefixes);

  @override
  FeedKeyCreated restrictions(ReadRestriction restrictions) =>
      call(restrictions: restrictions);

  @override
  FeedKeyCreated privateAccess(bool? privateAccess) =>
      call(privateAccess: privateAccess);

  @override
  FeedKeyCreated createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  FeedKeyCreated expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  @override
  FeedKeyCreated lastUsedAt(DateTime? lastUsedAt) =>
      call(lastUsedAt: lastUsedAt);

  @override
  FeedKeyCreated revokedAt(DateTime? revokedAt) => call(revokedAt: revokedAt);

  @override
  FeedKeyCreated key(String key) => call(key: key);

  @override
  FeedKeyCreated urls(List<FeedKeyCreatedAllOfUrls> urls) => call(urls: urls);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FeedKeyCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FeedKeyCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FeedKeyCreated call({
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
    Object? key = const $CopyWithPlaceholder(),
    Object? urls = const $CopyWithPlaceholder(),
  }) {
    return FeedKeyCreated(
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
      key: key == const $CopyWithPlaceholder() || key == null
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as String,
      urls: urls == const $CopyWithPlaceholder() || urls == null
          ? _value.urls
          // ignore: cast_nullable_to_non_nullable
          : urls as List<FeedKeyCreatedAllOfUrls>,
    );
  }
}

extension $FeedKeyCreatedCopyWith on FeedKeyCreated {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFeedKeyCreated.copyWith(...)` or `instanceOfFeedKeyCreated.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FeedKeyCreatedCWProxy get copyWith => _$FeedKeyCreatedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FeedKeyCreated _$FeedKeyCreatedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FeedKeyCreated', json, ($checkedConvert) {
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
      'key',
      'urls',
    ],
  );
  final val = FeedKeyCreated(
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
    key: $checkedConvert('key', (v) => v as String),
    urls: $checkedConvert(
      'urls',
      (v) => (v as List<dynamic>)
          .map(
            (e) => FeedKeyCreatedAllOfUrls.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$FeedKeyCreatedToJson(FeedKeyCreated instance) =>
    <String, dynamic>{
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
      'key': instance.key,
      'urls': instance.urls.map((e) => e.toJson()).toList(),
    };
