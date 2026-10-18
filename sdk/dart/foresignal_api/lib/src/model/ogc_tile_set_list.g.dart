// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_tile_set_list.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcTileSetListCWProxy {
  OgcTileSetList tilesets(List<OgcTileSetListTilesetsInner> tilesets);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileSetList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileSetList(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcTileSetList call({List<OgcTileSetListTilesetsInner> tilesets});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcTileSetList.copyWith(...)` or call `instanceOfOgcTileSetList.copyWith.fieldName(value)` for a single field.
class _$OgcTileSetListCWProxyImpl implements _$OgcTileSetListCWProxy {
  const _$OgcTileSetListCWProxyImpl(this._value);

  final OgcTileSetList _value;

  @override
  OgcTileSetList tilesets(List<OgcTileSetListTilesetsInner> tilesets) =>
      call(tilesets: tilesets);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileSetList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileSetList(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcTileSetList call({Object? tilesets = const $CopyWithPlaceholder()}) {
    return OgcTileSetList(
      tilesets: tilesets == const $CopyWithPlaceholder() || tilesets == null
          ? _value.tilesets
          // ignore: cast_nullable_to_non_nullable
          : tilesets as List<OgcTileSetListTilesetsInner>,
    );
  }
}

extension $OgcTileSetListCopyWith on OgcTileSetList {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcTileSetList.copyWith(...)` or `instanceOfOgcTileSetList.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcTileSetListCWProxy get copyWith => _$OgcTileSetListCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcTileSetList _$OgcTileSetListFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcTileSetList', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['tilesets']);
      final val = OgcTileSetList(
        tilesets: $checkedConvert(
          'tilesets',
          (v) => (v as List<dynamic>)
              .map(
                (e) => OgcTileSetListTilesetsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OgcTileSetListToJson(OgcTileSetList instance) =>
    <String, dynamic>{
      'tilesets': instance.tilesets.map((e) => e.toJson()).toList(),
    };
