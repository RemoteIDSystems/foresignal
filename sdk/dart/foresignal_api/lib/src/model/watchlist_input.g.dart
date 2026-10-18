// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'watchlist_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WatchlistInputCWProxy {
  WatchlistInput name(String name);

  WatchlistInput identity(WatchlistInputIdentityEnum identity);

  WatchlistInput normalize(WatchlistInputNormalizeEnum? normalize);

  WatchlistInput description(String? description);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistInput(...).copyWith(id: 12, name: "My name")
  /// ```
  WatchlistInput call({
    String name,
    WatchlistInputIdentityEnum identity,
    WatchlistInputNormalizeEnum? normalize,
    String? description,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWatchlistInput.copyWith(...)` or call `instanceOfWatchlistInput.copyWith.fieldName(value)` for a single field.
class _$WatchlistInputCWProxyImpl implements _$WatchlistInputCWProxy {
  const _$WatchlistInputCWProxyImpl(this._value);

  final WatchlistInput _value;

  @override
  WatchlistInput name(String name) => call(name: name);

  @override
  WatchlistInput identity(WatchlistInputIdentityEnum identity) =>
      call(identity: identity);

  @override
  WatchlistInput normalize(WatchlistInputNormalizeEnum? normalize) =>
      call(normalize: normalize);

  @override
  WatchlistInput description(String? description) =>
      call(description: description);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WatchlistInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? identity = const $CopyWithPlaceholder(),
    Object? normalize = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
  }) {
    return WatchlistInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      identity: identity == const $CopyWithPlaceholder() || identity == null
          ? _value.identity
          // ignore: cast_nullable_to_non_nullable
          : identity as WatchlistInputIdentityEnum,
      normalize: normalize == const $CopyWithPlaceholder()
          ? _value.normalize
          // ignore: cast_nullable_to_non_nullable
          : normalize as WatchlistInputNormalizeEnum?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
    );
  }
}

extension $WatchlistInputCopyWith on WatchlistInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWatchlistInput.copyWith(...)` or `instanceOfWatchlistInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WatchlistInputCWProxy get copyWith => _$WatchlistInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WatchlistInput _$WatchlistInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WatchlistInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'identity']);
      final val = WatchlistInput(
        name: $checkedConvert('name', (v) => v as String),
        identity: $checkedConvert(
          'identity',
          (v) => $enumDecode(_$WatchlistInputIdentityEnumEnumMap, v),
        ),
        normalize: $checkedConvert(
          'normalize',
          (v) => $enumDecodeNullable(_$WatchlistInputNormalizeEnumEnumMap, v),
        ),
        description: $checkedConvert('description', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$WatchlistInputToJson(WatchlistInput instance) =>
    <String, dynamic>{
      'name': instance.name,
      'identity': _$WatchlistInputIdentityEnumEnumMap[instance.identity]!,
      'normalize': ?_$WatchlistInputNormalizeEnumEnumMap[instance.normalize],
      'description': ?instance.description,
    };

const _$WatchlistInputIdentityEnumEnumMap = {
  WatchlistInputIdentityEnum.icao24: 'icao24',
  WatchlistInputIdentityEnum.registration: 'registration',
  WatchlistInputIdentityEnum.callsign: 'callsign',
  WatchlistInputIdentityEnum.mmsi: 'mmsi',
  WatchlistInputIdentityEnum.imo: 'imo',
  WatchlistInputIdentityEnum.entity: 'entity',
  WatchlistInputIdentityEnum.custom: 'custom',
};

const _$WatchlistInputNormalizeEnumEnumMap = {
  WatchlistInputNormalizeEnum.upper: 'upper',
  WatchlistInputNormalizeEnum.lower: 'lower',
  WatchlistInputNormalizeEnum.digits: 'digits',
  WatchlistInputNormalizeEnum.none: 'none',
};
