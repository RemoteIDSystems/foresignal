// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'map_style_metadata_layers_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MapStyleMetadataLayersInnerCWProxy {
  MapStyleMetadataLayersInner id(String id);

  MapStyleMetadataLayersInner kinds(List<String> kinds);

  MapStyleMetadataLayersInner attribution(String? attribution);

  MapStyleMetadataLayersInner minzoom(num? minzoom);

  MapStyleMetadataLayersInner aggBelowZoom(num? aggBelowZoom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MapStyleMetadataLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MapStyleMetadataLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  MapStyleMetadataLayersInner call({
    String id,
    List<String> kinds,
    String? attribution,
    num? minzoom,
    num? aggBelowZoom,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMapStyleMetadataLayersInner.copyWith(...)` or call `instanceOfMapStyleMetadataLayersInner.copyWith.fieldName(value)` for a single field.
class _$MapStyleMetadataLayersInnerCWProxyImpl
    implements _$MapStyleMetadataLayersInnerCWProxy {
  const _$MapStyleMetadataLayersInnerCWProxyImpl(this._value);

  final MapStyleMetadataLayersInner _value;

  @override
  MapStyleMetadataLayersInner id(String id) => call(id: id);

  @override
  MapStyleMetadataLayersInner kinds(List<String> kinds) => call(kinds: kinds);

  @override
  MapStyleMetadataLayersInner attribution(String? attribution) =>
      call(attribution: attribution);

  @override
  MapStyleMetadataLayersInner minzoom(num? minzoom) => call(minzoom: minzoom);

  @override
  MapStyleMetadataLayersInner aggBelowZoom(num? aggBelowZoom) =>
      call(aggBelowZoom: aggBelowZoom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MapStyleMetadataLayersInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MapStyleMetadataLayersInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MapStyleMetadataLayersInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? minzoom = const $CopyWithPlaceholder(),
    Object? aggBelowZoom = const $CopyWithPlaceholder(),
  }) {
    return MapStyleMetadataLayersInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      kinds: kinds == const $CopyWithPlaceholder() || kinds == null
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
      minzoom: minzoom == const $CopyWithPlaceholder()
          ? _value.minzoom
          // ignore: cast_nullable_to_non_nullable
          : minzoom as num?,
      aggBelowZoom: aggBelowZoom == const $CopyWithPlaceholder()
          ? _value.aggBelowZoom
          // ignore: cast_nullable_to_non_nullable
          : aggBelowZoom as num?,
    );
  }
}

extension $MapStyleMetadataLayersInnerCopyWith on MapStyleMetadataLayersInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMapStyleMetadataLayersInner.copyWith(...)` or `instanceOfMapStyleMetadataLayersInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MapStyleMetadataLayersInnerCWProxy get copyWith =>
      _$MapStyleMetadataLayersInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MapStyleMetadataLayersInner _$MapStyleMetadataLayersInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MapStyleMetadataLayersInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'kinds']);
  final val = MapStyleMetadataLayersInner(
    id: $checkedConvert('id', (v) => v as String),
    kinds: $checkedConvert(
      'kinds',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    attribution: $checkedConvert('attribution', (v) => v as String?),
    minzoom: $checkedConvert('minzoom', (v) => v as num?),
    aggBelowZoom: $checkedConvert('aggBelowZoom', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$MapStyleMetadataLayersInnerToJson(
  MapStyleMetadataLayersInner instance,
) => <String, dynamic>{
  'id': instance.id,
  'kinds': instance.kinds,
  'attribution': ?instance.attribution,
  'minzoom': ?instance.minzoom,
  'aggBelowZoom': ?instance.aggBelowZoom,
};
