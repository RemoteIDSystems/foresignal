// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_pack_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlacePackRequestCWProxy {
  PlacePackRequest products(List<String>? products);

  PlacePackRequest frames(int? frames);

  PlacePackRequest radiusKm(num? radiusKm);

  PlacePackRequest maxZoom(int? maxZoom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlacePackRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlacePackRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  PlacePackRequest call({
    List<String>? products,
    int? frames,
    num? radiusKm,
    int? maxZoom,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlacePackRequest.copyWith(...)` or call `instanceOfPlacePackRequest.copyWith.fieldName(value)` for a single field.
class _$PlacePackRequestCWProxyImpl implements _$PlacePackRequestCWProxy {
  const _$PlacePackRequestCWProxyImpl(this._value);

  final PlacePackRequest _value;

  @override
  PlacePackRequest products(List<String>? products) => call(products: products);

  @override
  PlacePackRequest frames(int? frames) => call(frames: frames);

  @override
  PlacePackRequest radiusKm(num? radiusKm) => call(radiusKm: radiusKm);

  @override
  PlacePackRequest maxZoom(int? maxZoom) => call(maxZoom: maxZoom);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlacePackRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlacePackRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlacePackRequest call({
    Object? products = const $CopyWithPlaceholder(),
    Object? frames = const $CopyWithPlaceholder(),
    Object? radiusKm = const $CopyWithPlaceholder(),
    Object? maxZoom = const $CopyWithPlaceholder(),
  }) {
    return PlacePackRequest(
      products: products == const $CopyWithPlaceholder()
          ? _value.products
          // ignore: cast_nullable_to_non_nullable
          : products as List<String>?,
      frames: frames == const $CopyWithPlaceholder()
          ? _value.frames
          // ignore: cast_nullable_to_non_nullable
          : frames as int?,
      radiusKm: radiusKm == const $CopyWithPlaceholder()
          ? _value.radiusKm
          // ignore: cast_nullable_to_non_nullable
          : radiusKm as num?,
      maxZoom: maxZoom == const $CopyWithPlaceholder()
          ? _value.maxZoom
          // ignore: cast_nullable_to_non_nullable
          : maxZoom as int?,
    );
  }
}

extension $PlacePackRequestCopyWith on PlacePackRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlacePackRequest.copyWith(...)` or `instanceOfPlacePackRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlacePackRequestCWProxy get copyWith => _$PlacePackRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlacePackRequest _$PlacePackRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlacePackRequest', json, ($checkedConvert) {
      final val = PlacePackRequest(
        products: $checkedConvert(
          'products',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        frames: $checkedConvert('frames', (v) => (v as num?)?.toInt() ?? 12),
        radiusKm: $checkedConvert('radiusKm', (v) => v as num? ?? 150),
        maxZoom: $checkedConvert('maxZoom', (v) => (v as num?)?.toInt() ?? 8),
      );
      return val;
    });

Map<String, dynamic> _$PlacePackRequestToJson(PlacePackRequest instance) =>
    <String, dynamic>{
      'products': ?instance.products,
      'frames': ?instance.frames,
      'radiusKm': ?instance.radiusKm,
      'maxZoom': ?instance.maxZoom,
    };
