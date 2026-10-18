// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_mapping_geometry_one_of1.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordMappingGeometryOneOf1CWProxy {
  RecordMappingGeometryOneOf1 geojson(String geojson);

  RecordMappingGeometryOneOf1 fallback(List<num>? fallback);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingGeometryOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingGeometryOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordMappingGeometryOneOf1 call({String geojson, List<num>? fallback});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordMappingGeometryOneOf1.copyWith(...)` or call `instanceOfRecordMappingGeometryOneOf1.copyWith.fieldName(value)` for a single field.
class _$RecordMappingGeometryOneOf1CWProxyImpl
    implements _$RecordMappingGeometryOneOf1CWProxy {
  const _$RecordMappingGeometryOneOf1CWProxyImpl(this._value);

  final RecordMappingGeometryOneOf1 _value;

  @override
  RecordMappingGeometryOneOf1 geojson(String geojson) => call(geojson: geojson);

  @override
  RecordMappingGeometryOneOf1 fallback(List<num>? fallback) =>
      call(fallback: fallback);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMappingGeometryOneOf1(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMappingGeometryOneOf1(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordMappingGeometryOneOf1 call({
    Object? geojson = const $CopyWithPlaceholder(),
    Object? fallback = const $CopyWithPlaceholder(),
  }) {
    return RecordMappingGeometryOneOf1(
      geojson: geojson == const $CopyWithPlaceholder() || geojson == null
          ? _value.geojson
          // ignore: cast_nullable_to_non_nullable
          : geojson as String,
      fallback: fallback == const $CopyWithPlaceholder()
          ? _value.fallback
          // ignore: cast_nullable_to_non_nullable
          : fallback as List<num>?,
    );
  }
}

extension $RecordMappingGeometryOneOf1CopyWith on RecordMappingGeometryOneOf1 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordMappingGeometryOneOf1.copyWith(...)` or `instanceOfRecordMappingGeometryOneOf1.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordMappingGeometryOneOf1CWProxy get copyWith =>
      _$RecordMappingGeometryOneOf1CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordMappingGeometryOneOf1 _$RecordMappingGeometryOneOf1FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RecordMappingGeometryOneOf1', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['geojson']);
  final val = RecordMappingGeometryOneOf1(
    geojson: $checkedConvert('geojson', (v) => v as String),
    fallback: $checkedConvert(
      'fallback',
      (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$RecordMappingGeometryOneOf1ToJson(
  RecordMappingGeometryOneOf1 instance,
) => <String, dynamic>{
  'geojson': instance.geojson,
  'fallback': ?instance.fallback,
};
