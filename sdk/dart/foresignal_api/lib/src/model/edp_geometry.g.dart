// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_geometry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpGeometryCWProxy {
  EdpGeometry type(EdpGeometryTypeEnum type);

  EdpGeometry coordinates(List<Object> coordinates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpGeometry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpGeometry(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpGeometry call({EdpGeometryTypeEnum type, List<Object> coordinates});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpGeometry.copyWith(...)` or call `instanceOfEdpGeometry.copyWith.fieldName(value)` for a single field.
class _$EdpGeometryCWProxyImpl implements _$EdpGeometryCWProxy {
  const _$EdpGeometryCWProxyImpl(this._value);

  final EdpGeometry _value;

  @override
  EdpGeometry type(EdpGeometryTypeEnum type) => call(type: type);

  @override
  EdpGeometry coordinates(List<Object> coordinates) =>
      call(coordinates: coordinates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpGeometry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpGeometry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpGeometry call({
    Object? type = const $CopyWithPlaceholder(),
    Object? coordinates = const $CopyWithPlaceholder(),
  }) {
    return EdpGeometry(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as EdpGeometryTypeEnum,
      coordinates:
          coordinates == const $CopyWithPlaceholder() || coordinates == null
          ? _value.coordinates
          // ignore: cast_nullable_to_non_nullable
          : coordinates as List<Object>,
    );
  }
}

extension $EdpGeometryCopyWith on EdpGeometry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpGeometry.copyWith(...)` or `instanceOfEdpGeometry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpGeometryCWProxy get copyWith => _$EdpGeometryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpGeometry _$EdpGeometryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpGeometry', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'coordinates']);
      final val = EdpGeometry(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$EdpGeometryTypeEnumEnumMap, v),
        ),
        coordinates: $checkedConvert(
          'coordinates',
          (v) => (v as List<dynamic>).map((e) => e as Object).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EdpGeometryToJson(EdpGeometry instance) =>
    <String, dynamic>{
      'type': _$EdpGeometryTypeEnumEnumMap[instance.type]!,
      'coordinates': instance.coordinates,
    };

const _$EdpGeometryTypeEnumEnumMap = {
  EdpGeometryTypeEnum.point: 'Point',
  EdpGeometryTypeEnum.multiPoint: 'MultiPoint',
  EdpGeometryTypeEnum.lineString: 'LineString',
  EdpGeometryTypeEnum.multiLineString: 'MultiLineString',
  EdpGeometryTypeEnum.polygon: 'Polygon',
  EdpGeometryTypeEnum.multiPolygon: 'MultiPolygon',
};
