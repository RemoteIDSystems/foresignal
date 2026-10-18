// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pack_spec.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PackSpecCWProxy {
  PackSpec name(String name);

  PackSpec bbox(List<num>? bbox);

  PackSpec placeId(String? placeId);

  PackSpec minZoom(int minZoom);

  PackSpec maxZoom(int maxZoom);

  PackSpec vector(PackSpecVector? vector);

  PackSpec raster(PackSpecRaster? raster);

  PackSpec includePrivate(bool? includePrivate);

  PackSpec personalUse(bool? personalUse);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackSpec(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackSpec(...).copyWith(id: 12, name: "My name")
  /// ```
  PackSpec call({
    String name,
    List<num>? bbox,
    String? placeId,
    int minZoom,
    int maxZoom,
    PackSpecVector? vector,
    PackSpecRaster? raster,
    bool? includePrivate,
    bool? personalUse,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPackSpec.copyWith(...)` or call `instanceOfPackSpec.copyWith.fieldName(value)` for a single field.
class _$PackSpecCWProxyImpl implements _$PackSpecCWProxy {
  const _$PackSpecCWProxyImpl(this._value);

  final PackSpec _value;

  @override
  PackSpec name(String name) => call(name: name);

  @override
  PackSpec bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  PackSpec placeId(String? placeId) => call(placeId: placeId);

  @override
  PackSpec minZoom(int minZoom) => call(minZoom: minZoom);

  @override
  PackSpec maxZoom(int maxZoom) => call(maxZoom: maxZoom);

  @override
  PackSpec vector(PackSpecVector? vector) => call(vector: vector);

  @override
  PackSpec raster(PackSpecRaster? raster) => call(raster: raster);

  @override
  PackSpec includePrivate(bool? includePrivate) =>
      call(includePrivate: includePrivate);

  @override
  PackSpec personalUse(bool? personalUse) => call(personalUse: personalUse);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackSpec(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackSpec(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PackSpec call({
    Object? name = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? minZoom = const $CopyWithPlaceholder(),
    Object? maxZoom = const $CopyWithPlaceholder(),
    Object? vector = const $CopyWithPlaceholder(),
    Object? raster = const $CopyWithPlaceholder(),
    Object? includePrivate = const $CopyWithPlaceholder(),
    Object? personalUse = const $CopyWithPlaceholder(),
  }) {
    return PackSpec(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      minZoom: minZoom == const $CopyWithPlaceholder() || minZoom == null
          ? _value.minZoom
          // ignore: cast_nullable_to_non_nullable
          : minZoom as int,
      maxZoom: maxZoom == const $CopyWithPlaceholder() || maxZoom == null
          ? _value.maxZoom
          // ignore: cast_nullable_to_non_nullable
          : maxZoom as int,
      vector: vector == const $CopyWithPlaceholder()
          ? _value.vector
          // ignore: cast_nullable_to_non_nullable
          : vector as PackSpecVector?,
      raster: raster == const $CopyWithPlaceholder()
          ? _value.raster
          // ignore: cast_nullable_to_non_nullable
          : raster as PackSpecRaster?,
      includePrivate: includePrivate == const $CopyWithPlaceholder()
          ? _value.includePrivate
          // ignore: cast_nullable_to_non_nullable
          : includePrivate as bool?,
      personalUse: personalUse == const $CopyWithPlaceholder()
          ? _value.personalUse
          // ignore: cast_nullable_to_non_nullable
          : personalUse as bool?,
    );
  }
}

extension $PackSpecCopyWith on PackSpec {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPackSpec.copyWith(...)` or `instanceOfPackSpec.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PackSpecCWProxy get copyWith => _$PackSpecCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PackSpec _$PackSpecFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PackSpec', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name', 'minZoom', 'maxZoom']);
  final val = PackSpec(
    name: $checkedConvert('name', (v) => v as String),
    bbox: $checkedConvert(
      'bbox',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
    placeId: $checkedConvert('placeId', (v) => v as String?),
    minZoom: $checkedConvert('minZoom', (v) => (v as num).toInt()),
    maxZoom: $checkedConvert('maxZoom', (v) => (v as num).toInt()),
    vector: $checkedConvert(
      'vector',
      (v) =>
          v == null ? null : PackSpecVector.fromJson(v as Map<String, dynamic>),
    ),
    raster: $checkedConvert(
      'raster',
      (v) =>
          v == null ? null : PackSpecRaster.fromJson(v as Map<String, dynamic>),
    ),
    includePrivate: $checkedConvert(
      'includePrivate',
      (v) => v as bool? ?? false,
    ),
    personalUse: $checkedConvert('personalUse', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$PackSpecToJson(PackSpec instance) => <String, dynamic>{
  'name': instance.name,
  'bbox': ?instance.bbox,
  'placeId': ?instance.placeId,
  'minZoom': instance.minZoom,
  'maxZoom': instance.maxZoom,
  'vector': ?instance.vector?.toJson(),
  'raster': ?instance.raster?.toJson(),
  'includePrivate': ?instance.includePrivate,
  'personalUse': ?instance.personalUse,
};
