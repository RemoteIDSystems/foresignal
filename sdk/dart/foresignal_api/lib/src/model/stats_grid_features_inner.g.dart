// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_grid_features_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsGridFeaturesInnerCWProxy {
  StatsGridFeaturesInner type(StatsGridFeaturesInnerTypeEnum type);

  StatsGridFeaturesInner id(String? id);

  StatsGridFeaturesInner geometry(Geometry geometry);

  StatsGridFeaturesInner properties(
    StatsGridFeaturesInnerProperties properties,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsGridFeaturesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsGridFeaturesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsGridFeaturesInner call({
    StatsGridFeaturesInnerTypeEnum type,
    String? id,
    Geometry geometry,
    StatsGridFeaturesInnerProperties properties,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsGridFeaturesInner.copyWith(...)` or call `instanceOfStatsGridFeaturesInner.copyWith.fieldName(value)` for a single field.
class _$StatsGridFeaturesInnerCWProxyImpl
    implements _$StatsGridFeaturesInnerCWProxy {
  const _$StatsGridFeaturesInnerCWProxyImpl(this._value);

  final StatsGridFeaturesInner _value;

  @override
  StatsGridFeaturesInner type(StatsGridFeaturesInnerTypeEnum type) =>
      call(type: type);

  @override
  StatsGridFeaturesInner id(String? id) => call(id: id);

  @override
  StatsGridFeaturesInner geometry(Geometry geometry) =>
      call(geometry: geometry);

  @override
  StatsGridFeaturesInner properties(
    StatsGridFeaturesInnerProperties properties,
  ) => call(properties: properties);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsGridFeaturesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsGridFeaturesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsGridFeaturesInner call({
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
  }) {
    return StatsGridFeaturesInner(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as StatsGridFeaturesInnerTypeEnum,
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      geometry: geometry == const $CopyWithPlaceholder() || geometry == null
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry,
      properties:
          properties == const $CopyWithPlaceholder() || properties == null
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as StatsGridFeaturesInnerProperties,
    );
  }
}

extension $StatsGridFeaturesInnerCopyWith on StatsGridFeaturesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsGridFeaturesInner.copyWith(...)` or `instanceOfStatsGridFeaturesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsGridFeaturesInnerCWProxy get copyWith =>
      _$StatsGridFeaturesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsGridFeaturesInner _$StatsGridFeaturesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StatsGridFeaturesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type', 'geometry', 'properties']);
  final val = StatsGridFeaturesInner(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$StatsGridFeaturesInnerTypeEnumEnumMap, v),
    ),
    id: $checkedConvert('id', (v) => v as String?),
    geometry: $checkedConvert(
      'geometry',
      (v) => Geometry.fromJson(v as Map<String, dynamic>),
    ),
    properties: $checkedConvert(
      'properties',
      (v) =>
          StatsGridFeaturesInnerProperties.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$StatsGridFeaturesInnerToJson(
  StatsGridFeaturesInner instance,
) => <String, dynamic>{
  'type': _$StatsGridFeaturesInnerTypeEnumEnumMap[instance.type]!,
  'id': ?instance.id,
  'geometry': instance.geometry.toJson(),
  'properties': instance.properties.toJson(),
};

const _$StatsGridFeaturesInnerTypeEnumEnumMap = {
  StatsGridFeaturesInnerTypeEnum.feature: 'Feature',
};
