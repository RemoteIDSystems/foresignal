// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_tile_set_list_tilesets_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcTileSetListTilesetsInnerCWProxy {
  OgcTileSetListTilesetsInner title(String? title);

  OgcTileSetListTilesetsInner dataType(
    OgcTileSetListTilesetsInnerDataTypeEnum dataType,
  );

  OgcTileSetListTilesetsInner crs(String? crs);

  OgcTileSetListTilesetsInner tileMatrixSetURI(String? tileMatrixSetURI);

  OgcTileSetListTilesetsInner links(List<OgcLink> links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileSetListTilesetsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileSetListTilesetsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcTileSetListTilesetsInner call({
    String? title,
    OgcTileSetListTilesetsInnerDataTypeEnum dataType,
    String? crs,
    String? tileMatrixSetURI,
    List<OgcLink> links,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcTileSetListTilesetsInner.copyWith(...)` or call `instanceOfOgcTileSetListTilesetsInner.copyWith.fieldName(value)` for a single field.
class _$OgcTileSetListTilesetsInnerCWProxyImpl
    implements _$OgcTileSetListTilesetsInnerCWProxy {
  const _$OgcTileSetListTilesetsInnerCWProxyImpl(this._value);

  final OgcTileSetListTilesetsInner _value;

  @override
  OgcTileSetListTilesetsInner title(String? title) => call(title: title);

  @override
  OgcTileSetListTilesetsInner dataType(
    OgcTileSetListTilesetsInnerDataTypeEnum dataType,
  ) => call(dataType: dataType);

  @override
  OgcTileSetListTilesetsInner crs(String? crs) => call(crs: crs);

  @override
  OgcTileSetListTilesetsInner tileMatrixSetURI(String? tileMatrixSetURI) =>
      call(tileMatrixSetURI: tileMatrixSetURI);

  @override
  OgcTileSetListTilesetsInner links(List<OgcLink> links) => call(links: links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileSetListTilesetsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileSetListTilesetsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcTileSetListTilesetsInner call({
    Object? title = const $CopyWithPlaceholder(),
    Object? dataType = const $CopyWithPlaceholder(),
    Object? crs = const $CopyWithPlaceholder(),
    Object? tileMatrixSetURI = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
  }) {
    return OgcTileSetListTilesetsInner(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      dataType: dataType == const $CopyWithPlaceholder() || dataType == null
          ? _value.dataType
          // ignore: cast_nullable_to_non_nullable
          : dataType as OgcTileSetListTilesetsInnerDataTypeEnum,
      crs: crs == const $CopyWithPlaceholder()
          ? _value.crs
          // ignore: cast_nullable_to_non_nullable
          : crs as String?,
      tileMatrixSetURI: tileMatrixSetURI == const $CopyWithPlaceholder()
          ? _value.tileMatrixSetURI
          // ignore: cast_nullable_to_non_nullable
          : tileMatrixSetURI as String?,
      links: links == const $CopyWithPlaceholder() || links == null
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<OgcLink>,
    );
  }
}

extension $OgcTileSetListTilesetsInnerCopyWith on OgcTileSetListTilesetsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcTileSetListTilesetsInner.copyWith(...)` or `instanceOfOgcTileSetListTilesetsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcTileSetListTilesetsInnerCWProxy get copyWith =>
      _$OgcTileSetListTilesetsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcTileSetListTilesetsInner _$OgcTileSetListTilesetsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OgcTileSetListTilesetsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['dataType', 'links']);
  final val = OgcTileSetListTilesetsInner(
    title: $checkedConvert('title', (v) => v as String?),
    dataType: $checkedConvert(
      'dataType',
      (v) => $enumDecode(_$OgcTileSetListTilesetsInnerDataTypeEnumEnumMap, v),
    ),
    crs: $checkedConvert('crs', (v) => v as String?),
    tileMatrixSetURI: $checkedConvert('tileMatrixSetURI', (v) => v as String?),
    links: $checkedConvert(
      'links',
      (v) => (v as List<dynamic>)
          .map((e) => OgcLink.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$OgcTileSetListTilesetsInnerToJson(
  OgcTileSetListTilesetsInner instance,
) => <String, dynamic>{
  'title': ?instance.title,
  'dataType':
      _$OgcTileSetListTilesetsInnerDataTypeEnumEnumMap[instance.dataType]!,
  'crs': ?instance.crs,
  'tileMatrixSetURI': ?instance.tileMatrixSetURI,
  'links': instance.links.map((e) => e.toJson()).toList(),
};

const _$OgcTileSetListTilesetsInnerDataTypeEnumEnumMap = {
  OgcTileSetListTilesetsInnerDataTypeEnum.map: 'map',
};
