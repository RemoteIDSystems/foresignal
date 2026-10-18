// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'overland_batch_locations_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OverlandBatchLocationsInnerCWProxy {
  OverlandBatchLocationsInner type(OverlandBatchLocationsInnerTypeEnum? type);

  OverlandBatchLocationsInner geometry(Geometry geometry);

  OverlandBatchLocationsInner properties(
    OverlandBatchLocationsInnerProperties properties,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OverlandBatchLocationsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OverlandBatchLocationsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  OverlandBatchLocationsInner call({
    OverlandBatchLocationsInnerTypeEnum? type,
    Geometry geometry,
    OverlandBatchLocationsInnerProperties properties,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOverlandBatchLocationsInner.copyWith(...)` or call `instanceOfOverlandBatchLocationsInner.copyWith.fieldName(value)` for a single field.
class _$OverlandBatchLocationsInnerCWProxyImpl
    implements _$OverlandBatchLocationsInnerCWProxy {
  const _$OverlandBatchLocationsInnerCWProxyImpl(this._value);

  final OverlandBatchLocationsInner _value;

  @override
  OverlandBatchLocationsInner type(OverlandBatchLocationsInnerTypeEnum? type) =>
      call(type: type);

  @override
  OverlandBatchLocationsInner geometry(Geometry geometry) =>
      call(geometry: geometry);

  @override
  OverlandBatchLocationsInner properties(
    OverlandBatchLocationsInnerProperties properties,
  ) => call(properties: properties);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OverlandBatchLocationsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OverlandBatchLocationsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OverlandBatchLocationsInner call({
    Object? type = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
  }) {
    return OverlandBatchLocationsInner(
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as OverlandBatchLocationsInnerTypeEnum?,
      geometry: geometry == const $CopyWithPlaceholder() || geometry == null
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry,
      properties:
          properties == const $CopyWithPlaceholder() || properties == null
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as OverlandBatchLocationsInnerProperties,
    );
  }
}

extension $OverlandBatchLocationsInnerCopyWith on OverlandBatchLocationsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOverlandBatchLocationsInner.copyWith(...)` or `instanceOfOverlandBatchLocationsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OverlandBatchLocationsInnerCWProxy get copyWith =>
      _$OverlandBatchLocationsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OverlandBatchLocationsInner _$OverlandBatchLocationsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OverlandBatchLocationsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['geometry', 'properties']);
  final val = OverlandBatchLocationsInner(
    type: $checkedConvert(
      'type',
      (v) =>
          $enumDecodeNullable(_$OverlandBatchLocationsInnerTypeEnumEnumMap, v),
    ),
    geometry: $checkedConvert(
      'geometry',
      (v) => Geometry.fromJson(v as Map<String, dynamic>),
    ),
    properties: $checkedConvert(
      'properties',
      (v) => OverlandBatchLocationsInnerProperties.fromJson(
        v as Map<String, dynamic>,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$OverlandBatchLocationsInnerToJson(
  OverlandBatchLocationsInner instance,
) => <String, dynamic>{
  'type': ?_$OverlandBatchLocationsInnerTypeEnumEnumMap[instance.type],
  'geometry': instance.geometry.toJson(),
  'properties': instance.properties.toJson(),
};

const _$OverlandBatchLocationsInnerTypeEnumEnumMap = {
  OverlandBatchLocationsInnerTypeEnum.feature: 'Feature',
};
