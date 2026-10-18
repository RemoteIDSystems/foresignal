// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'geometry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GeometryCWProxy {
  Geometry type(GeometryTypeEnum type);

  Geometry coordinates(List<Object>? coordinates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Geometry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Geometry(...).copyWith(id: 12, name: "My name")
  /// ```
  Geometry call({GeometryTypeEnum type, List<Object>? coordinates});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGeometry.copyWith(...)` or call `instanceOfGeometry.copyWith.fieldName(value)` for a single field.
class _$GeometryCWProxyImpl implements _$GeometryCWProxy {
  const _$GeometryCWProxyImpl(this._value);

  final Geometry _value;

  @override
  Geometry type(GeometryTypeEnum type) => call(type: type);

  @override
  Geometry coordinates(List<Object>? coordinates) =>
      call(coordinates: coordinates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Geometry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Geometry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Geometry call({
    Object? type = const $CopyWithPlaceholder(),
    Object? coordinates = const $CopyWithPlaceholder(),
  }) {
    return Geometry(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as GeometryTypeEnum,
      coordinates: coordinates == const $CopyWithPlaceholder()
          ? _value.coordinates
          // ignore: cast_nullable_to_non_nullable
          : coordinates as List<Object>?,
    );
  }
}

extension $GeometryCopyWith on Geometry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGeometry.copyWith(...)` or `instanceOfGeometry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GeometryCWProxy get copyWith => _$GeometryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Geometry _$GeometryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Geometry', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type']);
      final val = Geometry(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$GeometryTypeEnumEnumMap, v),
        ),
        coordinates: $checkedConvert(
          'coordinates',
          (v) => (v as List<dynamic>?)?.map((e) => e as Object).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$GeometryToJson(Geometry instance) => <String, dynamic>{
  'type': _$GeometryTypeEnumEnumMap[instance.type]!,
  'coordinates': ?instance.coordinates,
};

const _$GeometryTypeEnumEnumMap = {
  GeometryTypeEnum.point: 'Point',
  GeometryTypeEnum.multiPoint: 'MultiPoint',
  GeometryTypeEnum.lineString: 'LineString',
  GeometryTypeEnum.multiLineString: 'MultiLineString',
  GeometryTypeEnum.polygon: 'Polygon',
  GeometryTypeEnum.multiPolygon: 'MultiPolygon',
  GeometryTypeEnum.geometryCollection: 'GeometryCollection',
};
