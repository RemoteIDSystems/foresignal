// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_tile_set.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcTileSetCWProxy {
  OgcTileSet title(String? title);

  OgcTileSet dataType(OgcTileSetDataTypeEnum dataType);

  OgcTileSet crs(String crs);

  OgcTileSet tileMatrixSetURI(String? tileMatrixSetURI);

  OgcTileSet tileMatrixSetLimits(
    List<OgcTileSetTileMatrixSetLimitsInner>? tileMatrixSetLimits,
  );

  OgcTileSet links(List<OgcLink> links);

  OgcTileSet attribution(String? attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileSet(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileSet(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcTileSet call({
    String? title,
    OgcTileSetDataTypeEnum dataType,
    String crs,
    String? tileMatrixSetURI,
    List<OgcTileSetTileMatrixSetLimitsInner>? tileMatrixSetLimits,
    List<OgcLink> links,
    String? attribution,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcTileSet.copyWith(...)` or call `instanceOfOgcTileSet.copyWith.fieldName(value)` for a single field.
class _$OgcTileSetCWProxyImpl implements _$OgcTileSetCWProxy {
  const _$OgcTileSetCWProxyImpl(this._value);

  final OgcTileSet _value;

  @override
  OgcTileSet title(String? title) => call(title: title);

  @override
  OgcTileSet dataType(OgcTileSetDataTypeEnum dataType) =>
      call(dataType: dataType);

  @override
  OgcTileSet crs(String crs) => call(crs: crs);

  @override
  OgcTileSet tileMatrixSetURI(String? tileMatrixSetURI) =>
      call(tileMatrixSetURI: tileMatrixSetURI);

  @override
  OgcTileSet tileMatrixSetLimits(
    List<OgcTileSetTileMatrixSetLimitsInner>? tileMatrixSetLimits,
  ) => call(tileMatrixSetLimits: tileMatrixSetLimits);

  @override
  OgcTileSet links(List<OgcLink> links) => call(links: links);

  @override
  OgcTileSet attribution(String? attribution) => call(attribution: attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileSet(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileSet(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcTileSet call({
    Object? title = const $CopyWithPlaceholder(),
    Object? dataType = const $CopyWithPlaceholder(),
    Object? crs = const $CopyWithPlaceholder(),
    Object? tileMatrixSetURI = const $CopyWithPlaceholder(),
    Object? tileMatrixSetLimits = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
  }) {
    return OgcTileSet(
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      dataType: dataType == const $CopyWithPlaceholder() || dataType == null
          ? _value.dataType
          // ignore: cast_nullable_to_non_nullable
          : dataType as OgcTileSetDataTypeEnum,
      crs: crs == const $CopyWithPlaceholder() || crs == null
          ? _value.crs
          // ignore: cast_nullable_to_non_nullable
          : crs as String,
      tileMatrixSetURI: tileMatrixSetURI == const $CopyWithPlaceholder()
          ? _value.tileMatrixSetURI
          // ignore: cast_nullable_to_non_nullable
          : tileMatrixSetURI as String?,
      tileMatrixSetLimits: tileMatrixSetLimits == const $CopyWithPlaceholder()
          ? _value.tileMatrixSetLimits
          // ignore: cast_nullable_to_non_nullable
          : tileMatrixSetLimits as List<OgcTileSetTileMatrixSetLimitsInner>?,
      links: links == const $CopyWithPlaceholder() || links == null
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<OgcLink>,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
    );
  }
}

extension $OgcTileSetCopyWith on OgcTileSet {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcTileSet.copyWith(...)` or `instanceOfOgcTileSet.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcTileSetCWProxy get copyWith => _$OgcTileSetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcTileSet _$OgcTileSetFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcTileSet', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['dataType', 'crs', 'links']);
      final val = OgcTileSet(
        title: $checkedConvert('title', (v) => v as String?),
        dataType: $checkedConvert(
          'dataType',
          (v) => $enumDecode(_$OgcTileSetDataTypeEnumEnumMap, v),
        ),
        crs: $checkedConvert('crs', (v) => v as String),
        tileMatrixSetURI: $checkedConvert(
          'tileMatrixSetURI',
          (v) => v as String?,
        ),
        tileMatrixSetLimits: $checkedConvert(
          'tileMatrixSetLimits',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => OgcTileSetTileMatrixSetLimitsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        links: $checkedConvert(
          'links',
          (v) => (v as List<dynamic>)
              .map((e) => OgcLink.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        attribution: $checkedConvert('attribution', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$OgcTileSetToJson(OgcTileSet instance) =>
    <String, dynamic>{
      'title': ?instance.title,
      'dataType': _$OgcTileSetDataTypeEnumEnumMap[instance.dataType]!,
      'crs': instance.crs,
      'tileMatrixSetURI': ?instance.tileMatrixSetURI,
      'tileMatrixSetLimits': ?instance.tileMatrixSetLimits
          ?.map((e) => e.toJson())
          .toList(),
      'links': instance.links.map((e) => e.toJson()).toList(),
      'attribution': ?instance.attribution,
    };

const _$OgcTileSetDataTypeEnumEnumMap = {OgcTileSetDataTypeEnum.map: 'map'};
