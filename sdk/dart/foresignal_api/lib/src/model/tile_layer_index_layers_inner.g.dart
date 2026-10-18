// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tile_layer_index_layers_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TileLayerIndexLayersInnerCWProxy {
  TileLayerIndexLayersInner id(String id);

  TileLayerIndexLayersInner type(TileLayerIndexLayersInnerTypeEnum type);

  TileLayerIndexLayersInner name(String name);

  TileLayerIndexLayersInner tilejson(String tilejson);

  TileLayerIndexLayersInner aggBelowZoom(int? aggBelowZoom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileLayerIndexLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileLayerIndexLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  TileLayerIndexLayersInner call({
    String id,
    TileLayerIndexLayersInnerTypeEnum type,
    String name,
    String tilejson,
    int? aggBelowZoom,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTileLayerIndexLayersInner.copyWith(...)` or call `instanceOfTileLayerIndexLayersInner.copyWith.fieldName(value)` for a single field.
class _$TileLayerIndexLayersInnerCWProxyImpl
    implements _$TileLayerIndexLayersInnerCWProxy {
  const _$TileLayerIndexLayersInnerCWProxyImpl(this._value);

  final TileLayerIndexLayersInner _value;

  @override
  TileLayerIndexLayersInner id(String id) => call(id: id);

  @override
  TileLayerIndexLayersInner type(TileLayerIndexLayersInnerTypeEnum type) =>
      call(type: type);

  @override
  TileLayerIndexLayersInner name(String name) => call(name: name);

  @override
  TileLayerIndexLayersInner tilejson(String tilejson) =>
      call(tilejson: tilejson);

  @override
  TileLayerIndexLayersInner aggBelowZoom(int? aggBelowZoom) =>
      call(aggBelowZoom: aggBelowZoom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileLayerIndexLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileLayerIndexLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TileLayerIndexLayersInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? tilejson = const $CopyWithPlaceholder(),
    Object? aggBelowZoom = const $CopyWithPlaceholder(),
  }) {
    return TileLayerIndexLayersInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as TileLayerIndexLayersInnerTypeEnum,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      tilejson: tilejson == const $CopyWithPlaceholder() || tilejson == null
          ? _value.tilejson
          // ignore: cast_nullable_to_non_nullable
          : tilejson as String,
      aggBelowZoom: aggBelowZoom == const $CopyWithPlaceholder()
          ? _value.aggBelowZoom
          // ignore: cast_nullable_to_non_nullable
          : aggBelowZoom as int?,
    );
  }
}

extension $TileLayerIndexLayersInnerCopyWith on TileLayerIndexLayersInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTileLayerIndexLayersInner.copyWith(...)` or `instanceOfTileLayerIndexLayersInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TileLayerIndexLayersInnerCWProxy get copyWith =>
      _$TileLayerIndexLayersInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TileLayerIndexLayersInner _$TileLayerIndexLayersInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TileLayerIndexLayersInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'type', 'name', 'tilejson']);
  final val = TileLayerIndexLayersInner(
    id: $checkedConvert('id', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$TileLayerIndexLayersInnerTypeEnumEnumMap, v),
    ),
    name: $checkedConvert('name', (v) => v as String),
    tilejson: $checkedConvert('tilejson', (v) => v as String),
    aggBelowZoom: $checkedConvert('aggBelowZoom', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$TileLayerIndexLayersInnerToJson(
  TileLayerIndexLayersInner instance,
) => <String, dynamic>{
  'id': instance.id,
  'type': _$TileLayerIndexLayersInnerTypeEnumEnumMap[instance.type]!,
  'name': instance.name,
  'tilejson': instance.tilejson,
  'aggBelowZoom': ?instance.aggBelowZoom,
};

const _$TileLayerIndexLayersInnerTypeEnumEnumMap = {
  TileLayerIndexLayersInnerTypeEnum.source_: 'source',
  TileLayerIndexLayersInnerTypeEnum.kind: 'kind',
};
