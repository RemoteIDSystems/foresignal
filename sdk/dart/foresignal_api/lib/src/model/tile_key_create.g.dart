// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tile_key_create.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TileKeyCreateCWProxy {
  TileKeyCreate name(String name);

  TileKeyCreate prefixes(Set<TileKeyCreatePrefixesEnum> prefixes);

  TileKeyCreate ttl(String ttl);

  TileKeyCreate restrictions(ReadRestriction? restrictions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileKeyCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileKeyCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  TileKeyCreate call({
    String name,
    Set<TileKeyCreatePrefixesEnum> prefixes,
    String ttl,
    ReadRestriction? restrictions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTileKeyCreate.copyWith(...)` or call `instanceOfTileKeyCreate.copyWith.fieldName(value)` for a single field.
class _$TileKeyCreateCWProxyImpl implements _$TileKeyCreateCWProxy {
  const _$TileKeyCreateCWProxyImpl(this._value);

  final TileKeyCreate _value;

  @override
  TileKeyCreate name(String name) => call(name: name);

  @override
  TileKeyCreate prefixes(Set<TileKeyCreatePrefixesEnum> prefixes) =>
      call(prefixes: prefixes);

  @override
  TileKeyCreate ttl(String ttl) => call(ttl: ttl);

  @override
  TileKeyCreate restrictions(ReadRestriction? restrictions) =>
      call(restrictions: restrictions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileKeyCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileKeyCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TileKeyCreate call({
    Object? name = const $CopyWithPlaceholder(),
    Object? prefixes = const $CopyWithPlaceholder(),
    Object? ttl = const $CopyWithPlaceholder(),
    Object? restrictions = const $CopyWithPlaceholder(),
  }) {
    return TileKeyCreate(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      prefixes: prefixes == const $CopyWithPlaceholder() || prefixes == null
          ? _value.prefixes
          // ignore: cast_nullable_to_non_nullable
          : prefixes as Set<TileKeyCreatePrefixesEnum>,
      ttl: ttl == const $CopyWithPlaceholder() || ttl == null
          ? _value.ttl
          // ignore: cast_nullable_to_non_nullable
          : ttl as String,
      restrictions: restrictions == const $CopyWithPlaceholder()
          ? _value.restrictions
          // ignore: cast_nullable_to_non_nullable
          : restrictions as ReadRestriction?,
    );
  }
}

extension $TileKeyCreateCopyWith on TileKeyCreate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTileKeyCreate.copyWith(...)` or `instanceOfTileKeyCreate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TileKeyCreateCWProxy get copyWith => _$TileKeyCreateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TileKeyCreate _$TileKeyCreateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TileKeyCreate', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'prefixes', 'ttl']);
      final val = TileKeyCreate(
        name: $checkedConvert('name', (v) => v as String),
        prefixes: $checkedConvert(
          'prefixes',
          (v) => (v as List<dynamic>)
              .map((e) => $enumDecode(_$TileKeyCreatePrefixesEnumEnumMap, e))
              .toSet(),
        ),
        ttl: $checkedConvert('ttl', (v) => v as String),
        restrictions: $checkedConvert(
          'restrictions',
          (v) => v == null
              ? null
              : ReadRestriction.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TileKeyCreateToJson(TileKeyCreate instance) =>
    <String, dynamic>{
      'name': instance.name,
      'prefixes': instance.prefixes
          .map((e) => _$TileKeyCreatePrefixesEnumEnumMap[e]!)
          .toList(),
      'ttl': instance.ttl,
      'restrictions': ?instance.restrictions?.toJson(),
    };

const _$TileKeyCreatePrefixesEnumEnumMap = {
  TileKeyCreatePrefixesEnum.slashV1SlashProductsSlash: '/v1/products/',
  TileKeyCreatePrefixesEnum.slashV1SlashStyles: '/v1/styles',
  TileKeyCreatePrefixesEnum.slashV1SlashMediaSlash: '/v1/media/',
  TileKeyCreatePrefixesEnum.slashV1SlashTilesSlash: '/v1/tiles/',
  TileKeyCreatePrefixesEnum.slashV1SlashMapsSlashStatic: '/v1/maps/static',
  TileKeyCreatePrefixesEnum.slashV1SlashSample: '/v1/sample',
  TileKeyCreatePrefixesEnum.slashV1SlashGroundStationsSlash:
      '/v1/ground-stations/',
  TileKeyCreatePrefixesEnum.slashV1SlashChangesSlashStream:
      '/v1/changes/stream',
  TileKeyCreatePrefixesEnum.slashV1SlashEventsSlashStream: '/v1/events/stream',
  TileKeyCreatePrefixesEnum.slashV1SlashMapSlash: '/v1/map/',
};
