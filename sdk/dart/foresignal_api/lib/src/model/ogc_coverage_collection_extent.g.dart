// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_coverage_collection_extent.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCoverageCollectionExtentCWProxy {
  OgcCoverageCollectionExtent spatial(
    OgcCoverageCollectionExtentSpatial? spatial,
  );

  OgcCoverageCollectionExtent temporal(
    OgcCoverageCollectionExtentTemporal? temporal,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionExtent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionExtent(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCoverageCollectionExtent call({
    OgcCoverageCollectionExtentSpatial? spatial,
    OgcCoverageCollectionExtentTemporal? temporal,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCoverageCollectionExtent.copyWith(...)` or call `instanceOfOgcCoverageCollectionExtent.copyWith.fieldName(value)` for a single field.
class _$OgcCoverageCollectionExtentCWProxyImpl
    implements _$OgcCoverageCollectionExtentCWProxy {
  const _$OgcCoverageCollectionExtentCWProxyImpl(this._value);

  final OgcCoverageCollectionExtent _value;

  @override
  OgcCoverageCollectionExtent spatial(
    OgcCoverageCollectionExtentSpatial? spatial,
  ) => call(spatial: spatial);

  @override
  OgcCoverageCollectionExtent temporal(
    OgcCoverageCollectionExtentTemporal? temporal,
  ) => call(temporal: temporal);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCoverageCollectionExtent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCoverageCollectionExtent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCoverageCollectionExtent call({
    Object? spatial = const $CopyWithPlaceholder(),
    Object? temporal = const $CopyWithPlaceholder(),
  }) {
    return OgcCoverageCollectionExtent(
      spatial: spatial == const $CopyWithPlaceholder()
          ? _value.spatial
          // ignore: cast_nullable_to_non_nullable
          : spatial as OgcCoverageCollectionExtentSpatial?,
      temporal: temporal == const $CopyWithPlaceholder()
          ? _value.temporal
          // ignore: cast_nullable_to_non_nullable
          : temporal as OgcCoverageCollectionExtentTemporal?,
    );
  }
}

extension $OgcCoverageCollectionExtentCopyWith on OgcCoverageCollectionExtent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCoverageCollectionExtent.copyWith(...)` or `instanceOfOgcCoverageCollectionExtent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCoverageCollectionExtentCWProxy get copyWith =>
      _$OgcCoverageCollectionExtentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCoverageCollectionExtent _$OgcCoverageCollectionExtentFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OgcCoverageCollectionExtent', json, ($checkedConvert) {
  final val = OgcCoverageCollectionExtent(
    spatial: $checkedConvert(
      'spatial',
      (v) => v == null
          ? null
          : OgcCoverageCollectionExtentSpatial.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
    temporal: $checkedConvert(
      'temporal',
      (v) => v == null
          ? null
          : OgcCoverageCollectionExtentTemporal.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
  );
  return val;
});

Map<String, dynamic> _$OgcCoverageCollectionExtentToJson(
  OgcCoverageCollectionExtent instance,
) => <String, dynamic>{
  'spatial': ?instance.spatial?.toJson(),
  'temporal': ?instance.temporal?.toJson(),
};
