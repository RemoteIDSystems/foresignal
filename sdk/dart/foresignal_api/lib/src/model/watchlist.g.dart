// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'watchlist.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WatchlistCWProxy {
  Watchlist name(String name);

  Watchlist identity(WatchlistIdentityEnum identity);

  Watchlist normalize(WatchlistNormalizeEnum? normalize);

  Watchlist description(String? description);

  Watchlist id(String id);

  Watchlist itemCount(int itemCount);

  Watchlist createdAt(DateTime createdAt);

  Watchlist updatedAt(DateTime updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Watchlist(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Watchlist(...).copyWith(id: 12, name: "My name")
  /// ```
  Watchlist call({
    String name,
    WatchlistIdentityEnum identity,
    WatchlistNormalizeEnum? normalize,
    String? description,
    String id,
    int itemCount,
    DateTime createdAt,
    DateTime updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWatchlist.copyWith(...)` or call `instanceOfWatchlist.copyWith.fieldName(value)` for a single field.
class _$WatchlistCWProxyImpl implements _$WatchlistCWProxy {
  const _$WatchlistCWProxyImpl(this._value);

  final Watchlist _value;

  @override
  Watchlist name(String name) => call(name: name);

  @override
  Watchlist identity(WatchlistIdentityEnum identity) =>
      call(identity: identity);

  @override
  Watchlist normalize(WatchlistNormalizeEnum? normalize) =>
      call(normalize: normalize);

  @override
  Watchlist description(String? description) => call(description: description);

  @override
  Watchlist id(String id) => call(id: id);

  @override
  Watchlist itemCount(int itemCount) => call(itemCount: itemCount);

  @override
  Watchlist createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Watchlist updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Watchlist(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Watchlist(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Watchlist call({
    Object? name = const $CopyWithPlaceholder(),
    Object? identity = const $CopyWithPlaceholder(),
    Object? normalize = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? itemCount = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return Watchlist(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      identity: identity == const $CopyWithPlaceholder() || identity == null
          ? _value.identity
          // ignore: cast_nullable_to_non_nullable
          : identity as WatchlistIdentityEnum,
      normalize: normalize == const $CopyWithPlaceholder()
          ? _value.normalize
          // ignore: cast_nullable_to_non_nullable
          : normalize as WatchlistNormalizeEnum?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      itemCount: itemCount == const $CopyWithPlaceholder() || itemCount == null
          ? _value.itemCount
          // ignore: cast_nullable_to_non_nullable
          : itemCount as int,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
    );
  }
}

extension $WatchlistCopyWith on Watchlist {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWatchlist.copyWith(...)` or `instanceOfWatchlist.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WatchlistCWProxy get copyWith => _$WatchlistCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Watchlist _$WatchlistFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Watchlist', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'name',
      'identity',
      'id',
      'itemCount',
      'createdAt',
      'updatedAt',
    ],
  );
  final val = Watchlist(
    name: $checkedConvert('name', (v) => v as String),
    identity: $checkedConvert(
      'identity',
      (v) => $enumDecode(_$WatchlistIdentityEnumEnumMap, v),
    ),
    normalize: $checkedConvert(
      'normalize',
      (v) => $enumDecodeNullable(_$WatchlistNormalizeEnumEnumMap, v),
    ),
    description: $checkedConvert('description', (v) => v as String?),
    id: $checkedConvert('id', (v) => v as String),
    itemCount: $checkedConvert('itemCount', (v) => (v as num).toInt()),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$WatchlistToJson(Watchlist instance) => <String, dynamic>{
  'name': instance.name,
  'identity': _$WatchlistIdentityEnumEnumMap[instance.identity]!,
  'normalize': ?_$WatchlistNormalizeEnumEnumMap[instance.normalize],
  'description': ?instance.description,
  'id': instance.id,
  'itemCount': instance.itemCount,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': instance.updatedAt.toIso8601String(),
};

const _$WatchlistIdentityEnumEnumMap = {
  WatchlistIdentityEnum.icao24: 'icao24',
  WatchlistIdentityEnum.registration: 'registration',
  WatchlistIdentityEnum.callsign: 'callsign',
  WatchlistIdentityEnum.mmsi: 'mmsi',
  WatchlistIdentityEnum.imo: 'imo',
  WatchlistIdentityEnum.entity: 'entity',
  WatchlistIdentityEnum.custom: 'custom',
};

const _$WatchlistNormalizeEnumEnumMap = {
  WatchlistNormalizeEnum.upper: 'upper',
  WatchlistNormalizeEnum.lower: 'lower',
  WatchlistNormalizeEnum.digits: 'digits',
  WatchlistNormalizeEnum.none: 'none',
};
