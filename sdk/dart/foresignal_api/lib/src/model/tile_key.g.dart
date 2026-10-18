// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tile_key.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TileKeyCWProxy {
  TileKey id(String id);

  TileKey name(String name);

  TileKey keyPrefix(String keyPrefix);

  TileKey prefixes(List<TileKeyPrefixesEnum> prefixes);

  TileKey restrictions(ReadRestriction? restrictions);

  TileKey createdAt(DateTime createdAt);

  TileKey expiresAt(DateTime expiresAt);

  TileKey lastUsedAt(DateTime? lastUsedAt);

  TileKey revokedAt(DateTime? revokedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileKey(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileKey(...).copyWith(id: 12, name: "My name")
  /// ```
  TileKey call({
    String id,
    String name,
    String keyPrefix,
    List<TileKeyPrefixesEnum> prefixes,
    ReadRestriction? restrictions,
    DateTime createdAt,
    DateTime expiresAt,
    DateTime? lastUsedAt,
    DateTime? revokedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTileKey.copyWith(...)` or call `instanceOfTileKey.copyWith.fieldName(value)` for a single field.
class _$TileKeyCWProxyImpl implements _$TileKeyCWProxy {
  const _$TileKeyCWProxyImpl(this._value);

  final TileKey _value;

  @override
  TileKey id(String id) => call(id: id);

  @override
  TileKey name(String name) => call(name: name);

  @override
  TileKey keyPrefix(String keyPrefix) => call(keyPrefix: keyPrefix);

  @override
  TileKey prefixes(List<TileKeyPrefixesEnum> prefixes) =>
      call(prefixes: prefixes);

  @override
  TileKey restrictions(ReadRestriction? restrictions) =>
      call(restrictions: restrictions);

  @override
  TileKey createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  TileKey expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  @override
  TileKey lastUsedAt(DateTime? lastUsedAt) => call(lastUsedAt: lastUsedAt);

  @override
  TileKey revokedAt(DateTime? revokedAt) => call(revokedAt: revokedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileKey(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileKey(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TileKey call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? keyPrefix = const $CopyWithPlaceholder(),
    Object? prefixes = const $CopyWithPlaceholder(),
    Object? restrictions = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? lastUsedAt = const $CopyWithPlaceholder(),
    Object? revokedAt = const $CopyWithPlaceholder(),
  }) {
    return TileKey(
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
          : prefixes as List<TileKeyPrefixesEnum>,
      restrictions: restrictions == const $CopyWithPlaceholder()
          ? _value.restrictions
          // ignore: cast_nullable_to_non_nullable
          : restrictions as ReadRestriction?,
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

extension $TileKeyCopyWith on TileKey {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTileKey.copyWith(...)` or `instanceOfTileKey.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TileKeyCWProxy get copyWith => _$TileKeyCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TileKey _$TileKeyFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TileKey', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'keyPrefix',
      'prefixes',
      'createdAt',
      'expiresAt',
    ],
  );
  final val = TileKey(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    keyPrefix: $checkedConvert('keyPrefix', (v) => v as String),
    prefixes: $checkedConvert(
      'prefixes',
      (v) => (v as List<dynamic>)
          .map((e) => $enumDecode(_$TileKeyPrefixesEnumEnumMap, e))
          .toList(),
    ),
    restrictions: $checkedConvert(
      'restrictions',
      (v) => v == null
          ? null
          : ReadRestriction.fromJson(v as Map<String, dynamic>),
    ),
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

Map<String, dynamic> _$TileKeyToJson(TileKey instance) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'keyPrefix': instance.keyPrefix,
  'prefixes': instance.prefixes
      .map((e) => _$TileKeyPrefixesEnumEnumMap[e]!)
      .toList(),
  'restrictions': ?instance.restrictions?.toJson(),
  'createdAt': instance.createdAt.toIso8601String(),
  'expiresAt': instance.expiresAt.toIso8601String(),
  'lastUsedAt': ?instance.lastUsedAt?.toIso8601String(),
  'revokedAt': ?instance.revokedAt?.toIso8601String(),
};

const _$TileKeyPrefixesEnumEnumMap = {
  TileKeyPrefixesEnum.slashV1SlashProductsSlash: '/v1/products/',
  TileKeyPrefixesEnum.slashV1SlashStyles: '/v1/styles',
  TileKeyPrefixesEnum.slashV1SlashMediaSlash: '/v1/media/',
  TileKeyPrefixesEnum.slashV1SlashTilesSlash: '/v1/tiles/',
  TileKeyPrefixesEnum.slashV1SlashMapsSlashStatic: '/v1/maps/static',
  TileKeyPrefixesEnum.slashV1SlashSample: '/v1/sample',
  TileKeyPrefixesEnum.slashV1SlashGroundStationsSlash: '/v1/ground-stations/',
  TileKeyPrefixesEnum.slashV1SlashChangesSlashStream: '/v1/changes/stream',
  TileKeyPrefixesEnum.slashV1SlashEventsSlashStream: '/v1/events/stream',
  TileKeyPrefixesEnum.slashV1SlashMapSlash: '/v1/map/',
};
