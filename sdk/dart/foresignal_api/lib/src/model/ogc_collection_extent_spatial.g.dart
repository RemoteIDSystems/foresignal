// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_collection_extent_spatial.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCollectionExtentSpatialCWProxy {
  OgcCollectionExtentSpatial bbox(List<List<num>>? bbox);

  OgcCollectionExtentSpatial crs(String? crs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCollectionExtentSpatial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCollectionExtentSpatial(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCollectionExtentSpatial call({List<List<num>>? bbox, String? crs});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCollectionExtentSpatial.copyWith(...)` or call `instanceOfOgcCollectionExtentSpatial.copyWith.fieldName(value)` for a single field.
class _$OgcCollectionExtentSpatialCWProxyImpl
    implements _$OgcCollectionExtentSpatialCWProxy {
  const _$OgcCollectionExtentSpatialCWProxyImpl(this._value);

  final OgcCollectionExtentSpatial _value;

  @override
  OgcCollectionExtentSpatial bbox(List<List<num>>? bbox) => call(bbox: bbox);

  @override
  OgcCollectionExtentSpatial crs(String? crs) => call(crs: crs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCollectionExtentSpatial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCollectionExtentSpatial(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCollectionExtentSpatial call({
    Object? bbox = const $CopyWithPlaceholder(),
    Object? crs = const $CopyWithPlaceholder(),
  }) {
    return OgcCollectionExtentSpatial(
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<List<num>>?,
      crs: crs == const $CopyWithPlaceholder()
          ? _value.crs
          // ignore: cast_nullable_to_non_nullable
          : crs as String?,
    );
  }
}

extension $OgcCollectionExtentSpatialCopyWith on OgcCollectionExtentSpatial {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCollectionExtentSpatial.copyWith(...)` or `instanceOfOgcCollectionExtentSpatial.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCollectionExtentSpatialCWProxy get copyWith =>
      _$OgcCollectionExtentSpatialCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCollectionExtentSpatial _$OgcCollectionExtentSpatialFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OgcCollectionExtentSpatial', json, ($checkedConvert) {
  final val = OgcCollectionExtentSpatial(
    bbox: $checkedConvert(
      'bbox',
      (v) => (v as List<dynamic>?)
          ?.map((e) => (e as List<dynamic>).map((e) => e as num).toList())
          .toList(),
    ),
    crs: $checkedConvert('crs', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$OgcCollectionExtentSpatialToJson(
  OgcCollectionExtentSpatial instance,
) => <String, dynamic>{'bbox': ?instance.bbox, 'crs': ?instance.crs};
