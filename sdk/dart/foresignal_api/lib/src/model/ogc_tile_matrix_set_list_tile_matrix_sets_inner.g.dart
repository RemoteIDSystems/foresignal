// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_tile_matrix_set_list_tile_matrix_sets_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcTileMatrixSetListTileMatrixSetsInnerCWProxy {
  OgcTileMatrixSetListTileMatrixSetsInner id(
    OgcTileMatrixSetListTileMatrixSetsInnerIdEnum id,
  );

  OgcTileMatrixSetListTileMatrixSetsInner title(String? title);

  OgcTileMatrixSetListTileMatrixSetsInner uri(String? uri);

  OgcTileMatrixSetListTileMatrixSetsInner links(List<OgcLink> links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileMatrixSetListTileMatrixSetsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileMatrixSetListTileMatrixSetsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcTileMatrixSetListTileMatrixSetsInner call({
    OgcTileMatrixSetListTileMatrixSetsInnerIdEnum id,
    String? title,
    String? uri,
    List<OgcLink> links,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcTileMatrixSetListTileMatrixSetsInner.copyWith(...)` or call `instanceOfOgcTileMatrixSetListTileMatrixSetsInner.copyWith.fieldName(value)` for a single field.
class _$OgcTileMatrixSetListTileMatrixSetsInnerCWProxyImpl
    implements _$OgcTileMatrixSetListTileMatrixSetsInnerCWProxy {
  const _$OgcTileMatrixSetListTileMatrixSetsInnerCWProxyImpl(this._value);

  final OgcTileMatrixSetListTileMatrixSetsInner _value;

  @override
  OgcTileMatrixSetListTileMatrixSetsInner id(
    OgcTileMatrixSetListTileMatrixSetsInnerIdEnum id,
  ) => call(id: id);

  @override
  OgcTileMatrixSetListTileMatrixSetsInner title(String? title) =>
      call(title: title);

  @override
  OgcTileMatrixSetListTileMatrixSetsInner uri(String? uri) => call(uri: uri);

  @override
  OgcTileMatrixSetListTileMatrixSetsInner links(List<OgcLink> links) =>
      call(links: links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileMatrixSetListTileMatrixSetsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileMatrixSetListTileMatrixSetsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcTileMatrixSetListTileMatrixSetsInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? uri = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
  }) {
    return OgcTileMatrixSetListTileMatrixSetsInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as OgcTileMatrixSetListTileMatrixSetsInnerIdEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      uri: uri == const $CopyWithPlaceholder()
          ? _value.uri
          // ignore: cast_nullable_to_non_nullable
          : uri as String?,
      links: links == const $CopyWithPlaceholder() || links == null
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<OgcLink>,
    );
  }
}

extension $OgcTileMatrixSetListTileMatrixSetsInnerCopyWith
    on OgcTileMatrixSetListTileMatrixSetsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcTileMatrixSetListTileMatrixSetsInner.copyWith(...)` or `instanceOfOgcTileMatrixSetListTileMatrixSetsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcTileMatrixSetListTileMatrixSetsInnerCWProxy get copyWith =>
      _$OgcTileMatrixSetListTileMatrixSetsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcTileMatrixSetListTileMatrixSetsInner
_$OgcTileMatrixSetListTileMatrixSetsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcTileMatrixSetListTileMatrixSetsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['id', 'links']);
      final val = OgcTileMatrixSetListTileMatrixSetsInner(
        id: $checkedConvert(
          'id',
          (v) => $enumDecode(
            _$OgcTileMatrixSetListTileMatrixSetsInnerIdEnumEnumMap,
            v,
          ),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        uri: $checkedConvert('uri', (v) => v as String?),
        links: $checkedConvert(
          'links',
          (v) => (v as List<dynamic>)
              .map((e) => OgcLink.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OgcTileMatrixSetListTileMatrixSetsInnerToJson(
  OgcTileMatrixSetListTileMatrixSetsInner instance,
) => <String, dynamic>{
  'id': _$OgcTileMatrixSetListTileMatrixSetsInnerIdEnumEnumMap[instance.id]!,
  'title': ?instance.title,
  'uri': ?instance.uri,
  'links': instance.links.map((e) => e.toJson()).toList(),
};

const _$OgcTileMatrixSetListTileMatrixSetsInnerIdEnumEnumMap = {
  OgcTileMatrixSetListTileMatrixSetsInnerIdEnum.webMercatorQuad:
      'WebMercatorQuad',
  OgcTileMatrixSetListTileMatrixSetsInnerIdEnum.worldCRS84Quad:
      'WorldCRS84Quad',
};
