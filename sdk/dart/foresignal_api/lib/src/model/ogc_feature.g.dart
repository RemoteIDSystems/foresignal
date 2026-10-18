// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ogc_feature.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OgcFeatureCWProxy {
  OgcFeature type(OgcFeatureTypeEnum type);

  OgcFeature id(String id);

  OgcFeature geometry(Geometry? geometry);

  OgcFeature properties(OgcFeatureProperties properties);

  OgcFeature links(List<OgcLink>? links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcFeature(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcFeature(...).copyWith(id: 12, name: "My name")
  /// ```
  OgcFeature call({
    OgcFeatureTypeEnum type,
    String id,
    Geometry? geometry,
    OgcFeatureProperties properties,
    List<OgcLink>? links,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOgcFeature.copyWith(...)` or call `instanceOfOgcFeature.copyWith.fieldName(value)` for a single field.
class _$OgcFeatureCWProxyImpl implements _$OgcFeatureCWProxy {
  const _$OgcFeatureCWProxyImpl(this._value);

  final OgcFeature _value;

  @override
  OgcFeature type(OgcFeatureTypeEnum type) => call(type: type);

  @override
  OgcFeature id(String id) => call(id: id);

  @override
  OgcFeature geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  OgcFeature properties(OgcFeatureProperties properties) =>
      call(properties: properties);

  @override
  OgcFeature links(List<OgcLink>? links) => call(links: links);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OgcFeature(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OgcFeature(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OgcFeature call({
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? properties = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
  }) {
    return OgcFeature(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as OgcFeatureTypeEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      properties:
          properties == const $CopyWithPlaceholder() || properties == null
          ? _value.properties
          // ignore: cast_nullable_to_non_nullable
          : properties as OgcFeatureProperties,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<OgcLink>?,
    );
  }
}

extension $OgcFeatureCopyWith on OgcFeature {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOgcFeature.copyWith(...)` or `instanceOfOgcFeature.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OgcFeatureCWProxy get copyWith => _$OgcFeatureCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OgcFeature _$OgcFeatureFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OgcFeature', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['type', 'id', 'geometry', 'properties'],
      );
      final val = OgcFeature(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$OgcFeatureTypeEnumEnumMap, v),
        ),
        id: $checkedConvert('id', (v) => v as String),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        properties: $checkedConvert(
          'properties',
          (v) => OgcFeatureProperties.fromJson(v as Map<String, dynamic>),
        ),
        links: $checkedConvert(
          'links',
          (v) => (v as List<dynamic>?)
              ?.map((e) => OgcLink.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OgcFeatureToJson(OgcFeature instance) =>
    <String, dynamic>{
      'type': _$OgcFeatureTypeEnumEnumMap[instance.type]!,
      'id': instance.id,
      'geometry': instance.geometry?.toJson(),
      'properties': instance.properties.toJson(),
      'links': ?instance.links?.map((e) => e.toJson()).toList(),
    };

const _$OgcFeatureTypeEnumEnumMap = {OgcFeatureTypeEnum.feature: 'Feature'};
