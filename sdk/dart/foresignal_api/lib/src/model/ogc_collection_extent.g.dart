// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_collection_extent.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcCollectionExtentCWProxy {
  OgcCollectionExtent spatial(OgcCollectionExtentSpatial? spatial);

  OgcCollectionExtent temporal(OgcCollectionExtentTemporal? temporal);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCollectionExtent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCollectionExtent(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcCollectionExtent call({
    OgcCollectionExtentSpatial? spatial,
    OgcCollectionExtentTemporal? temporal,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcCollectionExtent.copyWith(...)` or call `instanceOfOgcCollectionExtent.copyWith.fieldName(value)` for a single field.
class _$OgcCollectionExtentCWProxyImpl implements _$OgcCollectionExtentCWProxy {
  const _$OgcCollectionExtentCWProxyImpl(this._value);

  final OgcCollectionExtent _value;

  @override
  OgcCollectionExtent spatial(OgcCollectionExtentSpatial? spatial) =>
      call(spatial: spatial);

  @override
  OgcCollectionExtent temporal(OgcCollectionExtentTemporal? temporal) =>
      call(temporal: temporal);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcCollectionExtent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcCollectionExtent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcCollectionExtent call({
    Object? spatial = const $CopyWithPlaceholder(),
    Object? temporal = const $CopyWithPlaceholder(),
  }) {
    return OgcCollectionExtent(
      spatial: spatial == const $CopyWithPlaceholder()
          ? _value.spatial
          // ignore: cast_nullable_to_non_nullable
          : spatial as OgcCollectionExtentSpatial?,
      temporal: temporal == const $CopyWithPlaceholder()
          ? _value.temporal
          // ignore: cast_nullable_to_non_nullable
          : temporal as OgcCollectionExtentTemporal?,
    );
  }
}

extension $OgcCollectionExtentCopyWith on OgcCollectionExtent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcCollectionExtent.copyWith(...)` or `instanceOfOgcCollectionExtent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcCollectionExtentCWProxy get copyWith =>
      _$OgcCollectionExtentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcCollectionExtent _$OgcCollectionExtentFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcCollectionExtent', json, ($checkedConvert) {
      final val = OgcCollectionExtent(
        spatial: $checkedConvert(
          'spatial',
          (v) => v == null
              ? null
              : OgcCollectionExtentSpatial.fromJson(v as Map<String, dynamic>),
        ),
        temporal: $checkedConvert(
          'temporal',
          (v) => v == null
              ? null
              : OgcCollectionExtentTemporal.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OgcCollectionExtentToJson(
  OgcCollectionExtent instance,
) => <String, dynamic>{
  'spatial': ?instance.spatial?.toJson(),
  'temporal': ?instance.temporal?.toJson(),
};
