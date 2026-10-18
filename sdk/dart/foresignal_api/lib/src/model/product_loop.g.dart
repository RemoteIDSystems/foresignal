// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_loop.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductLoopCWProxy {
  ProductLoop crs(ProductLoopCrsEnum crs);

  ProductLoop bounds(List<num> bounds);

  ProductLoop coordinates(List<List<num>> coordinates);

  ProductLoop width(int width);

  ProductLoop height(int height);

  ProductLoop products(List<String>? products);

  ProductLoop runs(Map<String, String>? runs);

  ProductLoop frames(List<ProductLoopFramesInner> frames);

  ProductLoop legendUrl(String? legendUrl);

  ProductLoop attribution(List<Attribution>? attribution);

  ProductLoop serverTime(DateTime serverTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductLoop(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductLoop(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductLoop call({
    ProductLoopCrsEnum crs,
    List<num> bounds,
    List<List<num>> coordinates,
    int width,
    int height,
    List<String>? products,
    Map<String, String>? runs,
    List<ProductLoopFramesInner> frames,
    String? legendUrl,
    List<Attribution>? attribution,
    DateTime serverTime,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductLoop.copyWith(...)` or call `instanceOfProductLoop.copyWith.fieldName(value)` for a single field.
class _$ProductLoopCWProxyImpl implements _$ProductLoopCWProxy {
  const _$ProductLoopCWProxyImpl(this._value);

  final ProductLoop _value;

  @override
  ProductLoop crs(ProductLoopCrsEnum crs) => call(crs: crs);

  @override
  ProductLoop bounds(List<num> bounds) => call(bounds: bounds);

  @override
  ProductLoop coordinates(List<List<num>> coordinates) =>
      call(coordinates: coordinates);

  @override
  ProductLoop width(int width) => call(width: width);

  @override
  ProductLoop height(int height) => call(height: height);

  @override
  ProductLoop products(List<String>? products) => call(products: products);

  @override
  ProductLoop runs(Map<String, String>? runs) => call(runs: runs);

  @override
  ProductLoop frames(List<ProductLoopFramesInner> frames) =>
      call(frames: frames);

  @override
  ProductLoop legendUrl(String? legendUrl) => call(legendUrl: legendUrl);

  @override
  ProductLoop attribution(List<Attribution>? attribution) =>
      call(attribution: attribution);

  @override
  ProductLoop serverTime(DateTime serverTime) => call(serverTime: serverTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductLoop(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductLoop(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductLoop call({
    Object? crs = const $CopyWithPlaceholder(),
    Object? bounds = const $CopyWithPlaceholder(),
    Object? coordinates = const $CopyWithPlaceholder(),
    Object? width = const $CopyWithPlaceholder(),
    Object? height = const $CopyWithPlaceholder(),
    Object? products = const $CopyWithPlaceholder(),
    Object? runs = const $CopyWithPlaceholder(),
    Object? frames = const $CopyWithPlaceholder(),
    Object? legendUrl = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? serverTime = const $CopyWithPlaceholder(),
  }) {
    return ProductLoop(
      crs: crs == const $CopyWithPlaceholder() || crs == null
          ? _value.crs
          // ignore: cast_nullable_to_non_nullable
          : crs as ProductLoopCrsEnum,
      bounds: bounds == const $CopyWithPlaceholder() || bounds == null
          ? _value.bounds
          // ignore: cast_nullable_to_non_nullable
          : bounds as List<num>,
      coordinates:
          coordinates == const $CopyWithPlaceholder() || coordinates == null
          ? _value.coordinates
          // ignore: cast_nullable_to_non_nullable
          : coordinates as List<List<num>>,
      width: width == const $CopyWithPlaceholder() || width == null
          ? _value.width
          // ignore: cast_nullable_to_non_nullable
          : width as int,
      height: height == const $CopyWithPlaceholder() || height == null
          ? _value.height
          // ignore: cast_nullable_to_non_nullable
          : height as int,
      products: products == const $CopyWithPlaceholder()
          ? _value.products
          // ignore: cast_nullable_to_non_nullable
          : products as List<String>?,
      runs: runs == const $CopyWithPlaceholder()
          ? _value.runs
          // ignore: cast_nullable_to_non_nullable
          : runs as Map<String, String>?,
      frames: frames == const $CopyWithPlaceholder() || frames == null
          ? _value.frames
          // ignore: cast_nullable_to_non_nullable
          : frames as List<ProductLoopFramesInner>,
      legendUrl: legendUrl == const $CopyWithPlaceholder()
          ? _value.legendUrl
          // ignore: cast_nullable_to_non_nullable
          : legendUrl as String?,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as List<Attribution>?,
      serverTime:
          serverTime == const $CopyWithPlaceholder() || serverTime == null
          ? _value.serverTime
          // ignore: cast_nullable_to_non_nullable
          : serverTime as DateTime,
    );
  }
}

extension $ProductLoopCopyWith on ProductLoop {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductLoop.copyWith(...)` or `instanceOfProductLoop.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductLoopCWProxy get copyWith => _$ProductLoopCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductLoop _$ProductLoopFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProductLoop', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'crs',
          'bounds',
          'coordinates',
          'width',
          'height',
          'frames',
          'serverTime',
        ],
      );
      final val = ProductLoop(
        crs: $checkedConvert(
          'crs',
          (v) => $enumDecode(_$ProductLoopCrsEnumEnumMap, v),
        ),
        bounds: $checkedConvert(
          'bounds',
          (v) => (v as List<dynamic>).map((e) => e as num).toList(),
        ),
        coordinates: $checkedConvert(
          'coordinates',
          (v) => (v as List<dynamic>)
              .map((e) => (e as List<dynamic>).map((e) => e as num).toList())
              .toList(),
        ),
        width: $checkedConvert('width', (v) => (v as num).toInt()),
        height: $checkedConvert('height', (v) => (v as num).toInt()),
        products: $checkedConvert(
          'products',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        runs: $checkedConvert(
          'runs',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as String),
          ),
        ),
        frames: $checkedConvert(
          'frames',
          (v) => (v as List<dynamic>)
              .map(
                (e) =>
                    ProductLoopFramesInner.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
        legendUrl: $checkedConvert('legendUrl', (v) => v as String?),
        attribution: $checkedConvert(
          'attribution',
          (v) => (v as List<dynamic>?)
              ?.map((e) => Attribution.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        serverTime: $checkedConvert(
          'serverTime',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ProductLoopToJson(ProductLoop instance) =>
    <String, dynamic>{
      'crs': _$ProductLoopCrsEnumEnumMap[instance.crs]!,
      'bounds': instance.bounds,
      'coordinates': instance.coordinates,
      'width': instance.width,
      'height': instance.height,
      'products': ?instance.products,
      'runs': ?instance.runs,
      'frames': instance.frames.map((e) => e.toJson()).toList(),
      'legendUrl': ?instance.legendUrl,
      'attribution': ?instance.attribution?.map((e) => e.toJson()).toList(),
      'serverTime': instance.serverTime.toIso8601String(),
    };

const _$ProductLoopCrsEnumEnumMap = {
  ProductLoopCrsEnum.ePSGColon3857: 'EPSG:3857',
};
