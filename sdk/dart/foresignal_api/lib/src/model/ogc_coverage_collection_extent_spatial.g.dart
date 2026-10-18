// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_coverage_collection_extent_spatial.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCoverageCollectionExtentSpatialCWProxy {
  OgcCoverageCollectionExtentSpatial bbox(List<List<num>>? bbox);

  OgcCoverageCollectionExtentSpatial crs(String? crs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionExtentSpatial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionExtentSpatial(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCoverageCollectionExtentSpatial call({List<List<num>>? bbox, String? crs});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCoverageCollectionExtentSpatial.copyWith(...)` or call `instanceOfOgcCoverageCollectionExtentSpatial.copyWith.fieldName(value)` for a single field.
class _$OgcCoverageCollectionExtentSpatialCWProxyImpl
    implements _$OgcCoverageCollectionExtentSpatialCWProxy {
  const _$OgcCoverageCollectionExtentSpatialCWProxyImpl(this._value);

  final OgcCoverageCollectionExtentSpatial _value;

  @override
  OgcCoverageCollectionExtentSpatial bbox(List<List<num>>? bbox) =>
      call(bbox: bbox);

  @override
  OgcCoverageCollectionExtentSpatial crs(String? crs) => call(crs: crs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionExtentSpatial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionExtentSpatial(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCoverageCollectionExtentSpatial call({
    Object? bbox = const $CopyWithPlaceholder(),
    Object? crs = const $CopyWithPlaceholder(),
  }) {
    return OgcCoverageCollectionExtentSpatial(
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

extension $OgcCoverageCollectionExtentSpatialCopyWith
    on OgcCoverageCollectionExtentSpatial {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCoverageCollectionExtentSpatial.copyWith(...)` or `instanceOfOgcCoverageCollectionExtentSpatial.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCoverageCollectionExtentSpatialCWProxy get copyWith =>
      _$OgcCoverageCollectionExtentSpatialCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCoverageCollectionExtentSpatial _$OgcCoverageCollectionExtentSpatialFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OgcCoverageCollectionExtentSpatial', json, (
  $checkedConvert,
) {
  final val = OgcCoverageCollectionExtentSpatial(
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

Map<String, dynamic> _$OgcCoverageCollectionExtentSpatialToJson(
  OgcCoverageCollectionExtentSpatial instance,
) => <String, dynamic>{'bbox': ?instance.bbox, 'crs': ?instance.crs};
