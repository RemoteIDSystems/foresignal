// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'map_style.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MapStyleCWProxy {
  MapStyle version(MapStyleVersionEnum version);

  MapStyle name(String? name);

  MapStyle sources(Map<String, Map<String, Object>> sources);

  MapStyle sprite(String sprite);

  MapStyle layers(List<Map<String, Object>> layers);

  MapStyle metadata(MapStyleMetadata metadata);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MapStyle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MapStyle(...).copyWith(id: 12, name: "My name")
  /// ```
  MapStyle call({
    MapStyleVersionEnum version,
    String? name,
    Map<String, Map<String, Object>> sources,
    String sprite,
    List<Map<String, Object>> layers,
    MapStyleMetadata metadata,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMapStyle.copyWith(...)` or call `instanceOfMapStyle.copyWith.fieldName(value)` for a single field.
class _$MapStyleCWProxyImpl implements _$MapStyleCWProxy {
  const _$MapStyleCWProxyImpl(this._value);

  final MapStyle _value;

  @override
  MapStyle version(MapStyleVersionEnum version) => call(version: version);

  @override
  MapStyle name(String? name) => call(name: name);

  @override
  MapStyle sources(Map<String, Map<String, Object>> sources) =>
      call(sources: sources);

  @override
  MapStyle sprite(String sprite) => call(sprite: sprite);

  @override
  MapStyle layers(List<Map<String, Object>> layers) => call(layers: layers);

  @override
  MapStyle metadata(MapStyleMetadata metadata) => call(metadata: metadata);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MapStyle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MapStyle(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MapStyle call({
    Object? version = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? sprite = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
    Object? metadata = const $CopyWithPlaceholder(),
  }) {
    return MapStyle(
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as MapStyleVersionEnum,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      sources: sources == const $CopyWithPlaceholder() || sources == null
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as Map<String, Map<String, Object>>,
      sprite: sprite == const $CopyWithPlaceholder() || sprite == null
          ? _value.sprite
          // ignore: cast_nullable_to_non_nullable
          : sprite as String,
      layers: layers == const $CopyWithPlaceholder() || layers == null
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<Map<String, Object>>,
      metadata: metadata == const $CopyWithPlaceholder() || metadata == null
          ? _value.metadata
          // ignore: cast_nullable_to_non_nullable
          : metadata as MapStyleMetadata,
    );
  }
}

extension $MapStyleCopyWith on MapStyle {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMapStyle.copyWith(...)` or `instanceOfMapStyle.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MapStyleCWProxy get copyWith => _$MapStyleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MapStyle _$MapStyleFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MapStyle', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['version', 'sources', 'sprite', 'layers', 'metadata'],
  );
  final val = MapStyle(
    version: $checkedConvert(
      'version',
      (v) => $enumDecode(_$MapStyleVersionEnumEnumMap, v),
    ),
    name: $checkedConvert('name', (v) => v as String?),
    sources: $checkedConvert(
      'sources',
      (v) => (v as Map<String, dynamic>).map(
        (k, e) => MapEntry(
          k,
          (e as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
        ),
      ),
    ),
    sprite: $checkedConvert('sprite', (v) => v as String),
    layers: $checkedConvert(
      'layers',
      (v) => (v as List<dynamic>)
          .map(
            (e) => (e as Map<String, dynamic>).map(
              (k, e) => MapEntry(k, e as Object),
            ),
          )
          .toList(),
    ),
    metadata: $checkedConvert(
      'metadata',
      (v) => MapStyleMetadata.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$MapStyleToJson(MapStyle instance) => <String, dynamic>{
  'version': _$MapStyleVersionEnumEnumMap[instance.version]!,
  'name': ?instance.name,
  'sources': instance.sources,
  'sprite': instance.sprite,
  'layers': instance.layers,
  'metadata': instance.metadata.toJson(),
};

const _$MapStyleVersionEnumEnumMap = {MapStyleVersionEnum.number8: 8};
