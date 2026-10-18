// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_spatial.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductSpatialCWProxy {
  ProductSpatial bounds(List<num> bounds);

  ProductSpatial minZoom(int minZoom);

  ProductSpatial maxZoom(int maxZoom);

  ProductSpatial maxZoomForAnimation(int? maxZoomForAnimation);

  ProductSpatial center(List<num>? center);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductSpatial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductSpatial(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductSpatial call({
    List<num> bounds,
    int minZoom,
    int maxZoom,
    int? maxZoomForAnimation,
    List<num>? center,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductSpatial.copyWith(...)` or call `instanceOfProductSpatial.copyWith.fieldName(value)` for a single field.
class _$ProductSpatialCWProxyImpl implements _$ProductSpatialCWProxy {
  const _$ProductSpatialCWProxyImpl(this._value);

  final ProductSpatial _value;

  @override
  ProductSpatial bounds(List<num> bounds) => call(bounds: bounds);

  @override
  ProductSpatial minZoom(int minZoom) => call(minZoom: minZoom);

  @override
  ProductSpatial maxZoom(int maxZoom) => call(maxZoom: maxZoom);

  @override
  ProductSpatial maxZoomForAnimation(int? maxZoomForAnimation) =>
      call(maxZoomForAnimation: maxZoomForAnimation);

  @override
  ProductSpatial center(List<num>? center) => call(center: center);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductSpatial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductSpatial(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductSpatial call({
    Object? bounds = const $CopyWithPlaceholder(),
    Object? minZoom = const $CopyWithPlaceholder(),
    Object? maxZoom = const $CopyWithPlaceholder(),
    Object? maxZoomForAnimation = const $CopyWithPlaceholder(),
    Object? center = const $CopyWithPlaceholder(),
  }) {
    return ProductSpatial(
      bounds: bounds == const $CopyWithPlaceholder() || bounds == null
          ? _value.bounds
          // ignore: cast_nullable_to_non_nullable
          : bounds as List<num>,
      minZoom: minZoom == const $CopyWithPlaceholder() || minZoom == null
          ? _value.minZoom
          // ignore: cast_nullable_to_non_nullable
          : minZoom as int,
      maxZoom: maxZoom == const $CopyWithPlaceholder() || maxZoom == null
          ? _value.maxZoom
          // ignore: cast_nullable_to_non_nullable
          : maxZoom as int,
      maxZoomForAnimation: maxZoomForAnimation == const $CopyWithPlaceholder()
          ? _value.maxZoomForAnimation
          // ignore: cast_nullable_to_non_nullable
          : maxZoomForAnimation as int?,
      center: center == const $CopyWithPlaceholder()
          ? _value.center
          // ignore: cast_nullable_to_non_nullable
          : center as List<num>?,
    );
  }
}

extension $ProductSpatialCopyWith on ProductSpatial {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductSpatial.copyWith(...)` or `instanceOfProductSpatial.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductSpatialCWProxy get copyWith => _$ProductSpatialCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductSpatial _$ProductSpatialFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProductSpatial', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['bounds', 'minZoom', 'maxZoom']);
      final val = ProductSpatial(
        bounds: $checkedConvert(
          'bounds',
          (v) => (v as List<dynamic>).map((e) => e as num).toList(),
        ),
        minZoom: $checkedConvert('minZoom', (v) => (v as num).toInt()),
        maxZoom: $checkedConvert('maxZoom', (v) => (v as num).toInt()),
        maxZoomForAnimation: $checkedConvert(
          'maxZoomForAnimation',
          (v) => (v as num?)?.toInt(),
        ),
        center: $checkedConvert(
          'center',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ProductSpatialToJson(ProductSpatial instance) =>
    <String, dynamic>{
      'bounds': instance.bounds,
      'minZoom': instance.minZoom,
      'maxZoom': instance.maxZoom,
      'maxZoomForAnimation': ?instance.maxZoomForAnimation,
      'center': ?instance.center,
    };
