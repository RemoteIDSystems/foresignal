// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tile_layer_index.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TileLayerIndexCWProxy {
  TileLayerIndex tilejson(TileLayerIndexTilejsonEnum tilejson);

  TileLayerIndex layers(List<TileLayerIndexLayersInner> layers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileLayerIndex(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileLayerIndex(...).copyWith(id: 12, name: "My name")
  /// ```
  TileLayerIndex call({
    TileLayerIndexTilejsonEnum tilejson,
    List<TileLayerIndexLayersInner> layers,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTileLayerIndex.copyWith(...)` or call `instanceOfTileLayerIndex.copyWith.fieldName(value)` for a single field.
class _$TileLayerIndexCWProxyImpl implements _$TileLayerIndexCWProxy {
  const _$TileLayerIndexCWProxyImpl(this._value);

  final TileLayerIndex _value;

  @override
  TileLayerIndex tilejson(TileLayerIndexTilejsonEnum tilejson) =>
      call(tilejson: tilejson);

  @override
  TileLayerIndex layers(List<TileLayerIndexLayersInner> layers) =>
      call(layers: layers);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileLayerIndex(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileLayerIndex(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TileLayerIndex call({
    Object? tilejson = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
  }) {
    return TileLayerIndex(
      tilejson: tilejson == const $CopyWithPlaceholder() || tilejson == null
          ? _value.tilejson
          // ignore: cast_nullable_to_non_nullable
          : tilejson as TileLayerIndexTilejsonEnum,
      layers: layers == const $CopyWithPlaceholder() || layers == null
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<TileLayerIndexLayersInner>,
    );
  }
}

extension $TileLayerIndexCopyWith on TileLayerIndex {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTileLayerIndex.copyWith(...)` or `instanceOfTileLayerIndex.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TileLayerIndexCWProxy get copyWith => _$TileLayerIndexCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TileLayerIndex _$TileLayerIndexFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TileLayerIndex', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['tilejson', 'layers']);
      final val = TileLayerIndex(
        tilejson: $checkedConvert(
          'tilejson',
          (v) => $enumDecode(_$TileLayerIndexTilejsonEnumEnumMap, v),
        ),
        layers: $checkedConvert(
          'layers',
          (v) => (v as List<dynamic>)
              .map(
                (e) => TileLayerIndexLayersInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TileLayerIndexToJson(TileLayerIndex instance) =>
    <String, dynamic>{
      'tilejson': _$TileLayerIndexTilejsonEnumEnumMap[instance.tilejson]!,
      'layers': instance.layers.map((e) => e.toJson()).toList(),
    };

const _$TileLayerIndexTilejsonEnumEnumMap = {
  TileLayerIndexTilejsonEnum.n3period0Period0: '3.0.0',
};
