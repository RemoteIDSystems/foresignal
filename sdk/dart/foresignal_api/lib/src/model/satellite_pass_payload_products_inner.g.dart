// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'satellite_pass_payload_products_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SatellitePassPayloadProductsInnerCWProxy {
  SatellitePassPayloadProductsInner productId(String? productId);

  SatellitePassPayloadProductsInner frameKey(String? frameKey);

  SatellitePassPayloadProductsInner composite(String composite);

  SatellitePassPayloadProductsInner thumbMediaId(String? thumbMediaId);

  SatellitePassPayloadProductsInner tilesTemplate(String? tilesTemplate);

  SatellitePassPayloadProductsInner instrument(String? instrument);

  SatellitePassPayloadProductsInner mediaId(String? mediaId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SatellitePassPayloadProductsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SatellitePassPayloadProductsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SatellitePassPayloadProductsInner call({
    String? productId,
    String? frameKey,
    String composite,
    String? thumbMediaId,
    String? tilesTemplate,
    String? instrument,
    String? mediaId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSatellitePassPayloadProductsInner.copyWith(...)` or call `instanceOfSatellitePassPayloadProductsInner.copyWith.fieldName(value)` for a single field.
class _$SatellitePassPayloadProductsInnerCWProxyImpl
    implements _$SatellitePassPayloadProductsInnerCWProxy {
  const _$SatellitePassPayloadProductsInnerCWProxyImpl(this._value);

  final SatellitePassPayloadProductsInner _value;

  @override
  SatellitePassPayloadProductsInner productId(String? productId) =>
      call(productId: productId);

  @override
  SatellitePassPayloadProductsInner frameKey(String? frameKey) =>
      call(frameKey: frameKey);

  @override
  SatellitePassPayloadProductsInner composite(String composite) =>
      call(composite: composite);

  @override
  SatellitePassPayloadProductsInner thumbMediaId(String? thumbMediaId) =>
      call(thumbMediaId: thumbMediaId);

  @override
  SatellitePassPayloadProductsInner tilesTemplate(String? tilesTemplate) =>
      call(tilesTemplate: tilesTemplate);

  @override
  SatellitePassPayloadProductsInner instrument(String? instrument) =>
      call(instrument: instrument);

  @override
  SatellitePassPayloadProductsInner mediaId(String? mediaId) =>
      call(mediaId: mediaId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SatellitePassPayloadProductsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SatellitePassPayloadProductsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SatellitePassPayloadProductsInner call({
    Object? productId = const $CopyWithPlaceholder(),
    Object? frameKey = const $CopyWithPlaceholder(),
    Object? composite = const $CopyWithPlaceholder(),
    Object? thumbMediaId = const $CopyWithPlaceholder(),
    Object? tilesTemplate = const $CopyWithPlaceholder(),
    Object? instrument = const $CopyWithPlaceholder(),
    Object? mediaId = const $CopyWithPlaceholder(),
  }) {
    return SatellitePassPayloadProductsInner(
      productId: productId == const $CopyWithPlaceholder()
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String?,
      frameKey: frameKey == const $CopyWithPlaceholder()
          ? _value.frameKey
          // ignore: cast_nullable_to_non_nullable
          : frameKey as String?,
      composite: composite == const $CopyWithPlaceholder() || composite == null
          ? _value.composite
          // ignore: cast_nullable_to_non_nullable
          : composite as String,
      thumbMediaId: thumbMediaId == const $CopyWithPlaceholder()
          ? _value.thumbMediaId
          // ignore: cast_nullable_to_non_nullable
          : thumbMediaId as String?,
      tilesTemplate: tilesTemplate == const $CopyWithPlaceholder()
          ? _value.tilesTemplate
          // ignore: cast_nullable_to_non_nullable
          : tilesTemplate as String?,
      instrument: instrument == const $CopyWithPlaceholder()
          ? _value.instrument
          // ignore: cast_nullable_to_non_nullable
          : instrument as String?,
      mediaId: mediaId == const $CopyWithPlaceholder()
          ? _value.mediaId
          // ignore: cast_nullable_to_non_nullable
          : mediaId as String?,
    );
  }
}

extension $SatellitePassPayloadProductsInnerCopyWith
    on SatellitePassPayloadProductsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSatellitePassPayloadProductsInner.copyWith(...)` or `instanceOfSatellitePassPayloadProductsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SatellitePassPayloadProductsInnerCWProxy get copyWith =>
      _$SatellitePassPayloadProductsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SatellitePassPayloadProductsInner _$SatellitePassPayloadProductsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SatellitePassPayloadProductsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['composite']);
  final val = SatellitePassPayloadProductsInner(
    productId: $checkedConvert('productId', (v) => v as String?),
    frameKey: $checkedConvert('frameKey', (v) => v as String?),
    composite: $checkedConvert('composite', (v) => v as String),
    thumbMediaId: $checkedConvert('thumbMediaId', (v) => v as String?),
    tilesTemplate: $checkedConvert('tilesTemplate', (v) => v as String?),
    instrument: $checkedConvert('instrument', (v) => v as String?),
    mediaId: $checkedConvert('mediaId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SatellitePassPayloadProductsInnerToJson(
  SatellitePassPayloadProductsInner instance,
) => <String, dynamic>{
  'productId': ?instance.productId,
  'frameKey': ?instance.frameKey,
  'composite': instance.composite,
  'thumbMediaId': ?instance.thumbMediaId,
  'tilesTemplate': ?instance.tilesTemplate,
  'instrument': ?instance.instrument,
  'mediaId': ?instance.mediaId,
};
