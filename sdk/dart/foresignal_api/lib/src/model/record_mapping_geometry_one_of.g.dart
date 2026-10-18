// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_mapping_geometry_one_of.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordMappingGeometryOneOfCWProxy {
  RecordMappingGeometryOneOf lon(MappingValueSpec lon);

  RecordMappingGeometryOneOf lat(MappingValueSpec lat);

  RecordMappingGeometryOneOf fallback(List<num>? fallback);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingGeometryOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingGeometryOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordMappingGeometryOneOf call({
    MappingValueSpec lon,
    MappingValueSpec lat,
    List<num>? fallback,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordMappingGeometryOneOf.copyWith(...)` or call `instanceOfRecordMappingGeometryOneOf.copyWith.fieldName(value)` for a single field.
class _$RecordMappingGeometryOneOfCWProxyImpl
    implements _$RecordMappingGeometryOneOfCWProxy {
  const _$RecordMappingGeometryOneOfCWProxyImpl(this._value);

  final RecordMappingGeometryOneOf _value;

  @override
  RecordMappingGeometryOneOf lon(MappingValueSpec lon) => call(lon: lon);

  @override
  RecordMappingGeometryOneOf lat(MappingValueSpec lat) => call(lat: lat);

  @override
  RecordMappingGeometryOneOf fallback(List<num>? fallback) =>
      call(fallback: fallback);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingGeometryOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingGeometryOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordMappingGeometryOneOf call({
    Object? lon = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
    Object? fallback = const $CopyWithPlaceholder(),
  }) {
    return RecordMappingGeometryOneOf(
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
    );
  }
}

extension $RecordMappingGeometryOneOfCopyWith on RecordMappingGeometryOneOf {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordMappingGeometryOneOf.copyWith(...)` or `instanceOfRecordMappingGeometryOneOf.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordMappingGeometryOneOfCWProxy get copyWith =>
      _$RecordMappingGeometryOneOfCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordMappingGeometryOneOf _$RecordMappingGeometryOneOfFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RecordMappingGeometryOneOf', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['lon', 'lat']);
  final val = RecordMappingGeometryOneOf(
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
  );
  return val;
});

Map<String, dynamic> _$RecordMappingGeometryOneOfToJson(
  RecordMappingGeometryOneOf instance,
) => <String, dynamic>{
  'lon': instance.lon.toJson(),
  'lat': instance.lat.toJson(),
  'fallback': ?instance.fallback,
};
