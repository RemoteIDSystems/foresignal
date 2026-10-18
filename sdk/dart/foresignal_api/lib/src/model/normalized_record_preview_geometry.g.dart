// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'normalized_record_preview_geometry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NormalizedRecordPreviewGeometryCWProxy {
  NormalizedRecordPreviewGeometry type(
    NormalizedRecordPreviewGeometryTypeEnum type,
  );

  NormalizedRecordPreviewGeometry coordinates(List<Object>? coordinates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NormalizedRecordPreviewGeometry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NormalizedRecordPreviewGeometry(...).copyWith(id: 12, name: "My name")
  /// ```
  NormalizedRecordPreviewGeometry call({
    NormalizedRecordPreviewGeometryTypeEnum type,
    List<Object>? coordinates,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNormalizedRecordPreviewGeometry.copyWith(...)` or call `instanceOfNormalizedRecordPreviewGeometry.copyWith.fieldName(value)` for a single field.
class _$NormalizedRecordPreviewGeometryCWProxyImpl
    implements _$NormalizedRecordPreviewGeometryCWProxy {
  const _$NormalizedRecordPreviewGeometryCWProxyImpl(this._value);

  final NormalizedRecordPreviewGeometry _value;

  @override
  NormalizedRecordPreviewGeometry type(
    NormalizedRecordPreviewGeometryTypeEnum type,
  ) => call(type: type);

  @override
  NormalizedRecordPreviewGeometry coordinates(List<Object>? coordinates) =>
      call(coordinates: coordinates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NormalizedRecordPreviewGeometry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NormalizedRecordPreviewGeometry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NormalizedRecordPreviewGeometry call({
    Object? type = const $CopyWithPlaceholder(),
    Object? coordinates = const $CopyWithPlaceholder(),
  }) {
    return NormalizedRecordPreviewGeometry(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as NormalizedRecordPreviewGeometryTypeEnum,
      coordinates: coordinates == const $CopyWithPlaceholder()
          ? _value.coordinates
          // ignore: cast_nullable_to_non_nullable
          : coordinates as List<Object>?,
    );
  }
}

extension $NormalizedRecordPreviewGeometryCopyWith
    on NormalizedRecordPreviewGeometry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNormalizedRecordPreviewGeometry.copyWith(...)` or `instanceOfNormalizedRecordPreviewGeometry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NormalizedRecordPreviewGeometryCWProxy get copyWith =>
      _$NormalizedRecordPreviewGeometryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NormalizedRecordPreviewGeometry _$NormalizedRecordPreviewGeometryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NormalizedRecordPreviewGeometry', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = NormalizedRecordPreviewGeometry(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$NormalizedRecordPreviewGeometryTypeEnumEnumMap, v),
    ),
    coordinates: $checkedConvert(
      'coordinates',
      (v) => (v as List<dynamic>?)?.map((e) => e as Object).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$NormalizedRecordPreviewGeometryToJson(
  NormalizedRecordPreviewGeometry instance,
) => <String, dynamic>{
  'type': _$NormalizedRecordPreviewGeometryTypeEnumEnumMap[instance.type]!,
  'coordinates': ?instance.coordinates,
};

const _$NormalizedRecordPreviewGeometryTypeEnumEnumMap = {
  NormalizedRecordPreviewGeometryTypeEnum.point: 'Point',
  NormalizedRecordPreviewGeometryTypeEnum.multiPoint: 'MultiPoint',
  NormalizedRecordPreviewGeometryTypeEnum.lineString: 'LineString',
  NormalizedRecordPreviewGeometryTypeEnum.multiLineString: 'MultiLineString',
  NormalizedRecordPreviewGeometryTypeEnum.polygon: 'Polygon',
  NormalizedRecordPreviewGeometryTypeEnum.multiPolygon: 'MultiPolygon',
  NormalizedRecordPreviewGeometryTypeEnum.geometryCollection:
      'GeometryCollection',
};
