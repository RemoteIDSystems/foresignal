// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_export_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductExportRequestCWProxy {
  ProductExportRequest format(ProductExportRequestFormatEnum format);

  ProductExportRequest frameKey(String? frameKey);

  ProductExportRequest frameKeys(List<String>? frameKeys);

  ProductExportRequest bbox(List<num>? bbox);

  ProductExportRequest placeId(String? placeId);

  ProductExportRequest radiusKm(num? radiusKm);

  ProductExportRequest minZoom(int? minZoom);

  ProductExportRequest maxZoom(int? maxZoom);

  ProductExportRequest style(String? style);

  ProductExportRequest tms(ProductExportRequestTmsEnum? tms);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductExportRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductExportRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductExportRequest call({
    ProductExportRequestFormatEnum format,
    String? frameKey,
    List<String>? frameKeys,
    List<num>? bbox,
    String? placeId,
    num? radiusKm,
    int? minZoom,
    int? maxZoom,
    String? style,
    ProductExportRequestTmsEnum? tms,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductExportRequest.copyWith(...)` or call `instanceOfProductExportRequest.copyWith.fieldName(value)` for a single field.
class _$ProductExportRequestCWProxyImpl
    implements _$ProductExportRequestCWProxy {
  const _$ProductExportRequestCWProxyImpl(this._value);

  final ProductExportRequest _value;

  @override
  ProductExportRequest format(ProductExportRequestFormatEnum format) =>
      call(format: format);

  @override
  ProductExportRequest frameKey(String? frameKey) => call(frameKey: frameKey);

  @override
  ProductExportRequest frameKeys(List<String>? frameKeys) =>
      call(frameKeys: frameKeys);

  @override
  ProductExportRequest bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  ProductExportRequest placeId(String? placeId) => call(placeId: placeId);

  @override
  ProductExportRequest radiusKm(num? radiusKm) => call(radiusKm: radiusKm);

  @override
  ProductExportRequest minZoom(int? minZoom) => call(minZoom: minZoom);

  @override
  ProductExportRequest maxZoom(int? maxZoom) => call(maxZoom: maxZoom);

  @override
  ProductExportRequest style(String? style) => call(style: style);

  @override
  ProductExportRequest tms(ProductExportRequestTmsEnum? tms) => call(tms: tms);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductExportRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductExportRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductExportRequest call({
    Object? format = const $CopyWithPlaceholder(),
    Object? frameKey = const $CopyWithPlaceholder(),
    Object? frameKeys = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? radiusKm = const $CopyWithPlaceholder(),
    Object? minZoom = const $CopyWithPlaceholder(),
    Object? maxZoom = const $CopyWithPlaceholder(),
    Object? style = const $CopyWithPlaceholder(),
    Object? tms = const $CopyWithPlaceholder(),
  }) {
    return ProductExportRequest(
      format: format == const $CopyWithPlaceholder() || format == null
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as ProductExportRequestFormatEnum,
      frameKey: frameKey == const $CopyWithPlaceholder()
          ? _value.frameKey
          // ignore: cast_nullable_to_non_nullable
          : frameKey as String?,
      frameKeys: frameKeys == const $CopyWithPlaceholder()
          ? _value.frameKeys
          // ignore: cast_nullable_to_non_nullable
          : frameKeys as List<String>?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      radiusKm: radiusKm == const $CopyWithPlaceholder()
          ? _value.radiusKm
          // ignore: cast_nullable_to_non_nullable
          : radiusKm as num?,
      minZoom: minZoom == const $CopyWithPlaceholder()
          ? _value.minZoom
          // ignore: cast_nullable_to_non_nullable
          : minZoom as int?,
      maxZoom: maxZoom == const $CopyWithPlaceholder()
          ? _value.maxZoom
          // ignore: cast_nullable_to_non_nullable
          : maxZoom as int?,
      style: style == const $CopyWithPlaceholder()
          ? _value.style
          // ignore: cast_nullable_to_non_nullable
          : style as String?,
      tms: tms == const $CopyWithPlaceholder()
          ? _value.tms
          // ignore: cast_nullable_to_non_nullable
          : tms as ProductExportRequestTmsEnum?,
    );
  }
}

extension $ProductExportRequestCopyWith on ProductExportRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductExportRequest.copyWith(...)` or `instanceOfProductExportRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductExportRequestCWProxy get copyWith =>
      _$ProductExportRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductExportRequest _$ProductExportRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProductExportRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['format']);
  final val = ProductExportRequest(
    format: $checkedConvert(
      'format',
      (v) => $enumDecode(_$ProductExportRequestFormatEnumEnumMap, v),
    ),
    frameKey: $checkedConvert('frameKey', (v) => v as String?),
    frameKeys: $checkedConvert(
      'frameKeys',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    bbox: $checkedConvert(
      'bbox',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
    placeId: $checkedConvert('placeId', (v) => v as String?),
    radiusKm: $checkedConvert('radiusKm', (v) => v as num?),
    minZoom: $checkedConvert('minZoom', (v) => (v as num?)?.toInt()),
    maxZoom: $checkedConvert('maxZoom', (v) => (v as num?)?.toInt()),
    style: $checkedConvert('style', (v) => v as String?),
    tms: $checkedConvert(
      'tms',
      (v) => $enumDecodeNullable(_$ProductExportRequestTmsEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$ProductExportRequestToJson(
  ProductExportRequest instance,
) => <String, dynamic>{
  'format': _$ProductExportRequestFormatEnumEnumMap[instance.format]!,
  'frameKey': ?instance.frameKey,
  'frameKeys': ?instance.frameKeys,
  'bbox': ?instance.bbox,
  'placeId': ?instance.placeId,
  'radiusKm': ?instance.radiusKm,
  'minZoom': ?instance.minZoom,
  'maxZoom': ?instance.maxZoom,
  'style': ?instance.style,
  'tms': ?_$ProductExportRequestTmsEnumEnumMap[instance.tms],
};

const _$ProductExportRequestFormatEnumEnumMap = {
  ProductExportRequestFormatEnum.mbtiles: 'mbtiles',
  ProductExportRequestFormatEnum.pmtiles: 'pmtiles',
  ProductExportRequestFormatEnum.cog: 'cog',
};

const _$ProductExportRequestTmsEnumEnumMap = {
  ProductExportRequestTmsEnum.webMercatorQuad: 'WebMercatorQuad',
  ProductExportRequestTmsEnum.worldCRS84Quad: 'WorldCRS84Quad',
};
