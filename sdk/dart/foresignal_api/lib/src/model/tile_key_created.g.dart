// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tile_key_created.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TileKeyCreatedCWProxy {
  TileKeyCreated id(String id);

  TileKeyCreated name(String name);

  TileKeyCreated keyPrefix(String keyPrefix);

  TileKeyCreated prefixes(List<TileKeyCreatedPrefixesEnum> prefixes);

  TileKeyCreated restrictions(ReadRestriction? restrictions);

  TileKeyCreated createdAt(DateTime createdAt);

  TileKeyCreated expiresAt(DateTime expiresAt);

  TileKeyCreated lastUsedAt(DateTime? lastUsedAt);

  TileKeyCreated revokedAt(DateTime? revokedAt);

  TileKeyCreated key(String key);

  TileKeyCreated urlBase(String urlBase);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileKeyCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileKeyCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  TileKeyCreated call({
    String id,
    String name,
    String keyPrefix,
    List<TileKeyCreatedPrefixesEnum> prefixes,
    ReadRestriction? restrictions,
    DateTime createdAt,
    DateTime expiresAt,
    DateTime? lastUsedAt,
    DateTime? revokedAt,
    String key,
    String urlBase,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTileKeyCreated.copyWith(...)` or call `instanceOfTileKeyCreated.copyWith.fieldName(value)` for a single field.
class _$TileKeyCreatedCWProxyImpl implements _$TileKeyCreatedCWProxy {
  const _$TileKeyCreatedCWProxyImpl(this._value);

  final TileKeyCreated _value;

  @override
  TileKeyCreated id(String id) => call(id: id);

  @override
  TileKeyCreated name(String name) => call(name: name);

  @override
  TileKeyCreated keyPrefix(String keyPrefix) => call(keyPrefix: keyPrefix);

  @override
  TileKeyCreated prefixes(List<TileKeyCreatedPrefixesEnum> prefixes) =>
      call(prefixes: prefixes);

  @override
  TileKeyCreated restrictions(ReadRestriction? restrictions) =>
      call(restrictions: restrictions);

  @override
  TileKeyCreated createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  TileKeyCreated expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  @override
  TileKeyCreated lastUsedAt(DateTime? lastUsedAt) =>
      call(lastUsedAt: lastUsedAt);

  @override
  TileKeyCreated revokedAt(DateTime? revokedAt) => call(revokedAt: revokedAt);

  @override
  TileKeyCreated key(String key) => call(key: key);

  @override
  TileKeyCreated urlBase(String urlBase) => call(urlBase: urlBase);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileKeyCreated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileKeyCreated(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TileKeyCreated call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? keyPrefix = const $CopyWithPlaceholder(),
    Object? prefixes = const $CopyWithPlaceholder(),
    Object? restrictions = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? lastUsedAt = const $CopyWithPlaceholder(),
    Object? revokedAt = const $CopyWithPlaceholder(),
    Object? key = const $CopyWithPlaceholder(),
    Object? urlBase = const $CopyWithPlaceholder(),
  }) {
    return TileKeyCreated(
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
          : prefixes as List<TileKeyCreatedPrefixesEnum>,
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
      key: key == const $CopyWithPlaceholder() || key == null
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as String,
      urlBase: urlBase == const $CopyWithPlaceholder() || urlBase == null
          ? _value.urlBase
          // ignore: cast_nullable_to_non_nullable
          : urlBase as String,
    );
  }
}

extension $TileKeyCreatedCopyWith on TileKeyCreated {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTileKeyCreated.copyWith(...)` or `instanceOfTileKeyCreated.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TileKeyCreatedCWProxy get copyWith => _$TileKeyCreatedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TileKeyCreated _$TileKeyCreatedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TileKeyCreated', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'keyPrefix',
      'prefixes',
      'createdAt',
      'expiresAt',
      'key',
      'urlBase',
    ],
  );
  final val = TileKeyCreated(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    keyPrefix: $checkedConvert('keyPrefix', (v) => v as String),
    prefixes: $checkedConvert(
      'prefixes',
      (v) => (v as List<dynamic>)
          .map((e) => $enumDecode(_$TileKeyCreatedPrefixesEnumEnumMap, e))
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
    key: $checkedConvert('key', (v) => v as String),
    urlBase: $checkedConvert('urlBase', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$TileKeyCreatedToJson(TileKeyCreated instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'keyPrefix': instance.keyPrefix,
      'prefixes': instance.prefixes
          .map((e) => _$TileKeyCreatedPrefixesEnumEnumMap[e]!)
          .toList(),
      'restrictions': ?instance.restrictions?.toJson(),
      'createdAt': instance.createdAt.toIso8601String(),
      'expiresAt': instance.expiresAt.toIso8601String(),
      'lastUsedAt': ?instance.lastUsedAt?.toIso8601String(),
      'revokedAt': ?instance.revokedAt?.toIso8601String(),
      'key': instance.key,
      'urlBase': instance.urlBase,
    };

const _$TileKeyCreatedPrefixesEnumEnumMap = {
  TileKeyCreatedPrefixesEnum.slashV1SlashProductsSlash: '/v1/products/',
  TileKeyCreatedPrefixesEnum.slashV1SlashStyles: '/v1/styles',
  TileKeyCreatedPrefixesEnum.slashV1SlashMediaSlash: '/v1/media/',
  TileKeyCreatedPrefixesEnum.slashV1SlashTilesSlash: '/v1/tiles/',
  TileKeyCreatedPrefixesEnum.slashV1SlashMapsSlashStatic: '/v1/maps/static',
  TileKeyCreatedPrefixesEnum.slashV1SlashSample: '/v1/sample',
  TileKeyCreatedPrefixesEnum.slashV1SlashGroundStationsSlash:
      '/v1/ground-stations/',
  TileKeyCreatedPrefixesEnum.slashV1SlashChangesSlashStream:
      '/v1/changes/stream',
  TileKeyCreatedPrefixesEnum.slashV1SlashEventsSlashStream: '/v1/events/stream',
  TileKeyCreatedPrefixesEnum.slashV1SlashMapSlash: '/v1/map/',
};
