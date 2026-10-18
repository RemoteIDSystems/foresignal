// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tile_json_vector_layers_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TileJsonVectorLayersInnerCWProxy {
  TileJsonVectorLayersInner id(String id);

  TileJsonVectorLayersInner description(String? description);

  TileJsonVectorLayersInner minzoom(int? minzoom);

  TileJsonVectorLayersInner maxzoom(int? maxzoom);

  TileJsonVectorLayersInner fields(Map<String, String> fields);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileJsonVectorLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileJsonVectorLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  TileJsonVectorLayersInner call({
    String id,
    String? description,
    int? minzoom,
    int? maxzoom,
    Map<String, String> fields,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTileJsonVectorLayersInner.copyWith(...)` or call `instanceOfTileJsonVectorLayersInner.copyWith.fieldName(value)` for a single field.
class _$TileJsonVectorLayersInnerCWProxyImpl
    implements _$TileJsonVectorLayersInnerCWProxy {
  const _$TileJsonVectorLayersInnerCWProxyImpl(this._value);

  final TileJsonVectorLayersInner _value;

  @override
  TileJsonVectorLayersInner id(String id) => call(id: id);

  @override
  TileJsonVectorLayersInner description(String? description) =>
      call(description: description);

  @override
  TileJsonVectorLayersInner minzoom(int? minzoom) => call(minzoom: minzoom);

  @override
  TileJsonVectorLayersInner maxzoom(int? maxzoom) => call(maxzoom: maxzoom);

  @override
  TileJsonVectorLayersInner fields(Map<String, String> fields) =>
      call(fields: fields);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileJsonVectorLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileJsonVectorLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TileJsonVectorLayersInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? minzoom = const $CopyWithPlaceholder(),
    Object? maxzoom = const $CopyWithPlaceholder(),
    Object? fields = const $CopyWithPlaceholder(),
  }) {
    return TileJsonVectorLayersInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      minzoom: minzoom == const $CopyWithPlaceholder()
          ? _value.minzoom
          // ignore: cast_nullable_to_non_nullable
          : minzoom as int?,
      maxzoom: maxzoom == const $CopyWithPlaceholder()
          ? _value.maxzoom
          // ignore: cast_nullable_to_non_nullable
          : maxzoom as int?,
      fields: fields == const $CopyWithPlaceholder() || fields == null
          ? _value.fields
          // ignore: cast_nullable_to_non_nullable
          : fields as Map<String, String>,
    );
  }
}

extension $TileJsonVectorLayersInnerCopyWith on TileJsonVectorLayersInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTileJsonVectorLayersInner.copyWith(...)` or `instanceOfTileJsonVectorLayersInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TileJsonVectorLayersInnerCWProxy get copyWith =>
      _$TileJsonVectorLayersInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TileJsonVectorLayersInner _$TileJsonVectorLayersInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TileJsonVectorLayersInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'fields']);
  final val = TileJsonVectorLayersInner(
    id: $checkedConvert('id', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String?),
    minzoom: $checkedConvert('minzoom', (v) => (v as num?)?.toInt()),
    maxzoom: $checkedConvert('maxzoom', (v) => (v as num?)?.toInt()),
    fields: $checkedConvert(
      'fields',
      (v) => Map<String, String>.from(v as Map),
    ),
  );
  return val;
});

Map<String, dynamic> _$TileJsonVectorLayersInnerToJson(
  TileJsonVectorLayersInner instance,
) => <String, dynamic>{
  'id': instance.id,
  'description': ?instance.description,
  'minzoom': ?instance.minzoom,
  'maxzoom': ?instance.maxzoom,
  'fields': instance.fields,
};
