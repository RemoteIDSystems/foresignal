// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'places_aoi.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlacesAoiCWProxy {
  PlacesAoi version(int version);

  PlacesAoi geometry(Geometry? geometry);

  PlacesAoi envelope(List<num> envelope);

  PlacesAoi placeCount(int placeCount);

  PlacesAoi bufferKm(num? bufferKm);

  PlacesAoi isDefault(bool isDefault);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlacesAoi(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlacesAoi(...).copyWith(id: 12, name: "My name")
  /// ```
  PlacesAoi call({
    int version,
    Geometry? geometry,
    List<num> envelope,
    int placeCount,
    num? bufferKm,
    bool isDefault,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlacesAoi.copyWith(...)` or call `instanceOfPlacesAoi.copyWith.fieldName(value)` for a single field.
class _$PlacesAoiCWProxyImpl implements _$PlacesAoiCWProxy {
  const _$PlacesAoiCWProxyImpl(this._value);

  final PlacesAoi _value;

  @override
  PlacesAoi version(int version) => call(version: version);

  @override
  PlacesAoi geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  PlacesAoi envelope(List<num> envelope) => call(envelope: envelope);

  @override
  PlacesAoi placeCount(int placeCount) => call(placeCount: placeCount);

  @override
  PlacesAoi bufferKm(num? bufferKm) => call(bufferKm: bufferKm);

  @override
  PlacesAoi isDefault(bool isDefault) => call(isDefault: isDefault);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlacesAoi(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlacesAoi(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlacesAoi call({
    Object? version = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? envelope = const $CopyWithPlaceholder(),
    Object? placeCount = const $CopyWithPlaceholder(),
    Object? bufferKm = const $CopyWithPlaceholder(),
    Object? isDefault = const $CopyWithPlaceholder(),
  }) {
    return PlacesAoi(
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      envelope: envelope == const $CopyWithPlaceholder() || envelope == null
          ? _value.envelope
          // ignore: cast_nullable_to_non_nullable
          : envelope as List<num>,
      placeCount:
          placeCount == const $CopyWithPlaceholder() || placeCount == null
          ? _value.placeCount
          // ignore: cast_nullable_to_non_nullable
          : placeCount as int,
      bufferKm: bufferKm == const $CopyWithPlaceholder()
          ? _value.bufferKm
          // ignore: cast_nullable_to_non_nullable
          : bufferKm as num?,
      isDefault: isDefault == const $CopyWithPlaceholder() || isDefault == null
          ? _value.isDefault
          // ignore: cast_nullable_to_non_nullable
          : isDefault as bool,
    );
  }
}

extension $PlacesAoiCopyWith on PlacesAoi {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlacesAoi.copyWith(...)` or `instanceOfPlacesAoi.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlacesAoiCWProxy get copyWith => _$PlacesAoiCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlacesAoi _$PlacesAoiFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlacesAoi', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['version', 'envelope', 'placeCount', 'isDefault'],
      );
      final val = PlacesAoi(
        version: $checkedConvert('version', (v) => (v as num).toInt()),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        envelope: $checkedConvert(
          'envelope',
          (v) => (v as List<dynamic>).map((e) => e as num).toList(),
        ),
        placeCount: $checkedConvert('placeCount', (v) => (v as num).toInt()),
        bufferKm: $checkedConvert('bufferKm', (v) => v as num?),
        isDefault: $checkedConvert('isDefault', (v) => v as bool),
      );
      return val;
    });

Map<String, dynamic> _$PlacesAoiToJson(PlacesAoi instance) => <String, dynamic>{
  'version': instance.version,
  'geometry': ?instance.geometry?.toJson(),
  'envelope': instance.envelope,
  'placeCount': instance.placeCount,
  'bufferKm': ?instance.bufferKm,
  'isDefault': instance.isDefault,
};
