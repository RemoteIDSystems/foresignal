// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'feature.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FeatureCWProxy {
  Feature type(FeatureTypeEnum type);

  Feature id(String? id);

  Feature geometry(Geometry? geometry);

  Feature properties(FeatureProperties properties);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Feature(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Feature(...).copyWith(id: 12, name: "My name")
  /// ```
  Feature call({
    FeatureTypeEnum type,
    String? id,
    Geometry? geometry,
    FeatureProperties properties,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFeature.copyWith(...)` or call `instanceOfFeature.copyWith.fieldName(value)` for a single field.
class _$FeatureCWProxyImpl implements _$FeatureCWProxy {
  const _$FeatureCWProxyImpl(this._value);

  final Feature _value;

  @override
  Feature type(FeatureTypeEnum type) => call(type: type);

  @override
  Feature id(String? id) => call(id: id);

  @override
  Feature geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  Feature properties(FeatureProperties properties) =>
      call(properties: properties);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Feature(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Feature(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Feature call({
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
  }) {
    return Feature(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as FeatureTypeEnum,
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      properties:
          properties == const $CopyWithPlaceholder() || properties == null
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as FeatureProperties,
    );
  }
}

extension $FeatureCopyWith on Feature {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFeature.copyWith(...)` or `instanceOfFeature.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FeatureCWProxy get copyWith => _$FeatureCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Feature _$FeatureFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Feature', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'geometry', 'properties']);
      final val = Feature(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$FeatureTypeEnumEnumMap, v),
        ),
        id: $checkedConvert('id', (v) => v as String?),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        properties: $checkedConvert(
          'properties',
          (v) => FeatureProperties.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$FeatureToJson(Feature instance) => <String, dynamic>{
  'type': _$FeatureTypeEnumEnumMap[instance.type]!,
  'id': ?instance.id,
  'geometry': instance.geometry?.toJson(),
  'properties': instance.properties.toJson(),
};

const _$FeatureTypeEnumEnumMap = {FeatureTypeEnum.feature: 'Feature'};
