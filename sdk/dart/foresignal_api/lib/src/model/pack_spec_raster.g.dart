// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pack_spec_raster.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PackSpecRasterCWProxy {
  PackSpecRaster productId(String productId);

  PackSpecRaster frame(String? frame);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackSpecRaster(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackSpecRaster(...).copyWith(id: 12, name: "My name")
  /// ```
  PackSpecRaster call({String productId, String? frame});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPackSpecRaster.copyWith(...)` or call `instanceOfPackSpecRaster.copyWith.fieldName(value)` for a single field.
class _$PackSpecRasterCWProxyImpl implements _$PackSpecRasterCWProxy {
  const _$PackSpecRasterCWProxyImpl(this._value);

  final PackSpecRaster _value;

  @override
  PackSpecRaster productId(String productId) => call(productId: productId);

  @override
  PackSpecRaster frame(String? frame) => call(frame: frame);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackSpecRaster(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackSpecRaster(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PackSpecRaster call({
    Object? productId = const $CopyWithPlaceholder(),
    Object? frame = const $CopyWithPlaceholder(),
  }) {
    return PackSpecRaster(
      productId: productId == const $CopyWithPlaceholder() || productId == null
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String,
      frame: frame == const $CopyWithPlaceholder()
          ? _value.frame
          // ignore: cast_nullable_to_non_nullable
          : frame as String?,
    );
  }
}

extension $PackSpecRasterCopyWith on PackSpecRaster {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPackSpecRaster.copyWith(...)` or `instanceOfPackSpecRaster.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PackSpecRasterCWProxy get copyWith => _$PackSpecRasterCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PackSpecRaster _$PackSpecRasterFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PackSpecRaster', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['productId']);
      final val = PackSpecRaster(
        productId: $checkedConvert('productId', (v) => v as String),
        frame: $checkedConvert('frame', (v) => v as String? ?? 'latest'),
      );
      return val;
    });

Map<String, dynamic> _$PackSpecRasterToJson(PackSpecRaster instance) =>
    <String, dynamic>{
      'productId': instance.productId,
      'frame': ?instance.frame,
    };
