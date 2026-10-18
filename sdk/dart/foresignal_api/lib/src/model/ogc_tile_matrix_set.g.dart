// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_tile_matrix_set.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcTileMatrixSetCWProxy {
  OgcTileMatrixSet id(OgcTileMatrixSetIdEnum id);

  OgcTileMatrixSet title(String? title);

  OgcTileMatrixSet uri(String? uri);

  OgcTileMatrixSet crs(String crs);

  OgcTileMatrixSet orderedAxes(List<String>? orderedAxes);

  OgcTileMatrixSet wellKnownScaleSet(String? wellKnownScaleSet);

  OgcTileMatrixSet tileMatrices(
    List<OgcTileMatrixSetTileMatricesInner> tileMatrices,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileMatrixSet(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileMatrixSet(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcTileMatrixSet call({
    OgcTileMatrixSetIdEnum id,
    String? title,
    String? uri,
    String crs,
    List<String>? orderedAxes,
    String? wellKnownScaleSet,
    List<OgcTileMatrixSetTileMatricesInner> tileMatrices,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcTileMatrixSet.copyWith(...)` or call `instanceOfOgcTileMatrixSet.copyWith.fieldName(value)` for a single field.
class _$OgcTileMatrixSetCWProxyImpl implements _$OgcTileMatrixSetCWProxy {
  const _$OgcTileMatrixSetCWProxyImpl(this._value);

  final OgcTileMatrixSet _value;

  @override
  OgcTileMatrixSet id(OgcTileMatrixSetIdEnum id) => call(id: id);

  @override
  OgcTileMatrixSet title(String? title) => call(title: title);

  @override
  OgcTileMatrixSet uri(String? uri) => call(uri: uri);

  @override
  OgcTileMatrixSet crs(String crs) => call(crs: crs);

  @override
  OgcTileMatrixSet orderedAxes(List<String>? orderedAxes) =>
      call(orderedAxes: orderedAxes);

  @override
  OgcTileMatrixSet wellKnownScaleSet(String? wellKnownScaleSet) =>
      call(wellKnownScaleSet: wellKnownScaleSet);

  @override
  OgcTileMatrixSet tileMatrices(
    List<OgcTileMatrixSetTileMatricesInner> tileMatrices,
  ) => call(tileMatrices: tileMatrices);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcTileMatrixSet(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcTileMatrixSet(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcTileMatrixSet call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? uri = const $CopyWithPlaceholder(),
    Object? crs = const $CopyWithPlaceholder(),
    Object? orderedAxes = const $CopyWithPlaceholder(),
    Object? wellKnownScaleSet = const $CopyWithPlaceholder(),
    Object? tileMatrices = const $CopyWithPlaceholder(),
  }) {
    return OgcTileMatrixSet(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as OgcTileMatrixSetIdEnum,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      uri: uri == const $CopyWithPlaceholder()
          ? _value.uri
          // ignore: cast_nullable_to_non_nullable
          : uri as String?,
      crs: crs == const $CopyWithPlaceholder() || crs == null
          ? _value.crs
          // ignore: cast_nullable_to_non_nullable
          : crs as String,
      orderedAxes: orderedAxes == const $CopyWithPlaceholder()
          ? _value.orderedAxes
          // ignore: cast_nullable_to_non_nullable
          : orderedAxes as List<String>?,
      wellKnownScaleSet: wellKnownScaleSet == const $CopyWithPlaceholder()
          ? _value.wellKnownScaleSet
          // ignore: cast_nullable_to_non_nullable
          : wellKnownScaleSet as String?,
      tileMatrices:
          tileMatrices == const $CopyWithPlaceholder() || tileMatrices == null
          ? _value.tileMatrices
          // ignore: cast_nullable_to_non_nullable
          : tileMatrices as List<OgcTileMatrixSetTileMatricesInner>,
    );
  }
}

extension $OgcTileMatrixSetCopyWith on OgcTileMatrixSet {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcTileMatrixSet.copyWith(...)` or `instanceOfOgcTileMatrixSet.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcTileMatrixSetCWProxy get copyWith => _$OgcTileMatrixSetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcTileMatrixSet _$OgcTileMatrixSetFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcTileMatrixSet', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'crs', 'tileMatrices']);
      final val = OgcTileMatrixSet(
        id: $checkedConvert(
          'id',
          (v) => $enumDecode(_$OgcTileMatrixSetIdEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String?),
        uri: $checkedConvert('uri', (v) => v as String?),
        crs: $checkedConvert('crs', (v) => v as String),
        orderedAxes: $checkedConvert(
          'orderedAxes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        wellKnownScaleSet: $checkedConvert(
          'wellKnownScaleSet',
          (v) => v as String?,
        ),
        tileMatrices: $checkedConvert(
          'tileMatrices',
          (v) => (v as List<dynamic>)
              .map(
                (e) => OgcTileMatrixSetTileMatricesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OgcTileMatrixSetToJson(OgcTileMatrixSet instance) =>
    <String, dynamic>{
      'id': _$OgcTileMatrixSetIdEnumEnumMap[instance.id]!,
      'title': ?instance.title,
      'uri': ?instance.uri,
      'crs': instance.crs,
      'orderedAxes': ?instance.orderedAxes,
      'wellKnownScaleSet': ?instance.wellKnownScaleSet,
      'tileMatrices': instance.tileMatrices.map((e) => e.toJson()).toList(),
    };

const _$OgcTileMatrixSetIdEnumEnumMap = {
  OgcTileMatrixSetIdEnum.webMercatorQuad: 'WebMercatorQuad',
  OgcTileMatrixSetIdEnum.worldCRS84Quad: 'WorldCRS84Quad',
};
