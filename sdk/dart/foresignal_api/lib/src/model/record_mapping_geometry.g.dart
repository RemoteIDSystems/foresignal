// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_mapping_geometry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordMappingGeometryCWProxy {
  RecordMappingGeometry lon(MappingValueSpec lon);

  RecordMappingGeometry lat(MappingValueSpec lat);

  RecordMappingGeometry fallback(List<num>? fallback);

  RecordMappingGeometry geojson(String geojson);

  RecordMappingGeometry const_(List<num> const_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingGeometry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingGeometry(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordMappingGeometry call({
    MappingValueSpec lon,
    MappingValueSpec lat,
    List<num>? fallback,
    String geojson,
    List<num> const_,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordMappingGeometry.copyWith(...)` or call `instanceOfRecordMappingGeometry.copyWith.fieldName(value)` for a single field.
class _$RecordMappingGeometryCWProxyImpl
    implements _$RecordMappingGeometryCWProxy {
  const _$RecordMappingGeometryCWProxyImpl(this._value);

  final RecordMappingGeometry _value;

  @override
  RecordMappingGeometry lon(MappingValueSpec lon) => call(lon: lon);

  @override
  RecordMappingGeometry lat(MappingValueSpec lat) => call(lat: lat);

  @override
  RecordMappingGeometry fallback(List<num>? fallback) =>
      call(fallback: fallback);

  @override
  RecordMappingGeometry geojson(String geojson) => call(geojson: geojson);

  @override
  RecordMappingGeometry const_(List<num> const_) => call(const_: const_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingGeometry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingGeometry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordMappingGeometry call({
    Object? lon = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
    Object? fallback = const $CopyWithPlaceholder(),
    Object? geojson = const $CopyWithPlaceholder(),
    Object? const_ = const $CopyWithPlaceholder(),
  }) {
    return RecordMappingGeometry(
      lon: lon == const $CopyWithPlaceholder() || lon == null
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as MappingValueSpec,
      lat: lat == const $CopyWithPlaceholder() || lat == null
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as MappingValueSpec,
      fallback: fallback == const $CopyWithPlaceholder()
          ? _value.fallback
          // ignore: cast_nullable_to_non_nullable
          : fallback as List<num>?,
      geojson: geojson == const $CopyWithPlaceholder() || geojson == null
          ? _value.geojson
          // ignore: cast_nullable_to_non_nullable
          : geojson as String,
      const_: const_ == const $CopyWithPlaceholder() || const_ == null
          ? _value.const_
          // ignore: cast_nullable_to_non_nullable
          : const_ as List<num>,
    );
  }
}

extension $RecordMappingGeometryCopyWith on RecordMappingGeometry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordMappingGeometry.copyWith(...)` or `instanceOfRecordMappingGeometry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordMappingGeometryCWProxy get copyWith =>
      _$RecordMappingGeometryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordMappingGeometry _$RecordMappingGeometryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RecordMappingGeometry', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['lon', 'lat', 'geojson', 'const']);
  final val = RecordMappingGeometry(
    lon: $checkedConvert(
      'lon',
      (v) => MappingValueSpec.fromJson(v as Map<String, dynamic>),
    ),
    lat: $checkedConvert(
      'lat',
      (v) => MappingValueSpec.fromJson(v as Map<String, dynamic>),
    ),
    fallback: $checkedConvert(
      'fallback',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
    geojson: $checkedConvert('geojson', (v) => v as String),
    const_: $checkedConvert(
      'const',
      (v) => (v as List<dynamic>).map((e) => e as num).toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'const_': 'const'});

Map<String, dynamic> _$RecordMappingGeometryToJson(
  RecordMappingGeometry instance,
) => <String, dynamic>{
  'lon': instance.lon.toJson(),
  'lat': instance.lat.toJson(),
  'fallback': ?instance.fallback,
  'geojson': instance.geojson,
  'const': instance.const_,
};
