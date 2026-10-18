// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_node.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraNodeCWProxy {
  InfraNode id(int id);

  InfraNode sourceId(String sourceId);

  InfraNode externalId(String externalId);

  InfraNode facilityType(InfraNodeFacilityTypeEnum facilityType);

  InfraNode name(String? name);

  InfraNode geometry(Geometry geometry);

  InfraNode point(Geometry? point);

  InfraNode attributes(Map<String, Object>? attributes);

  InfraNode regionTags(List<String>? regionTags);

  InfraNode licence(String licence);

  InfraNode builtAt(DateTime? builtAt);

  InfraNode edges(InfraNodeEdges? edges);

  InfraNode attribution(List<String>? attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraNode(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraNode(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraNode call({
    int id,
    String sourceId,
    String externalId,
    InfraNodeFacilityTypeEnum facilityType,
    String? name,
    Geometry geometry,
    Geometry? point,
    Map<String, Object>? attributes,
    List<String>? regionTags,
    String licence,
    DateTime? builtAt,
    InfraNodeEdges? edges,
    List<String>? attribution,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraNode.copyWith(...)` or call `instanceOfInfraNode.copyWith.fieldName(value)` for a single field.
class _$InfraNodeCWProxyImpl implements _$InfraNodeCWProxy {
  const _$InfraNodeCWProxyImpl(this._value);

  final InfraNode _value;

  @override
  InfraNode id(int id) => call(id: id);

  @override
  InfraNode sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  InfraNode externalId(String externalId) => call(externalId: externalId);

  @override
  InfraNode facilityType(InfraNodeFacilityTypeEnum facilityType) =>
      call(facilityType: facilityType);

  @override
  InfraNode name(String? name) => call(name: name);

  @override
  InfraNode geometry(Geometry geometry) => call(geometry: geometry);

  @override
  InfraNode point(Geometry? point) => call(point: point);

  @override
  InfraNode attributes(Map<String, Object>? attributes) =>
      call(attributes: attributes);

  @override
  InfraNode regionTags(List<String>? regionTags) =>
      call(regionTags: regionTags);

  @override
  InfraNode licence(String licence) => call(licence: licence);

  @override
  InfraNode builtAt(DateTime? builtAt) => call(builtAt: builtAt);

  @override
  InfraNode edges(InfraNodeEdges? edges) => call(edges: edges);

  @override
  InfraNode attribution(List<String>? attribution) =>
      call(attribution: attribution);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraNode(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraNode(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraNode call({
    Object? id = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? facilityType = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? point = const $CopyWithPlaceholder(),
    Object? attributes = const $CopyWithPlaceholder(),
    Object? regionTags = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? builtAt = const $CopyWithPlaceholder(),
    Object? edges = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
  }) {
    return InfraNode(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      facilityType:
          facilityType == const $CopyWithPlaceholder() || facilityType == null
          ? _value.facilityType
          // ignore: cast_nullable_to_non_nullable
          : facilityType as InfraNodeFacilityTypeEnum,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      geometry: geometry == const $CopyWithPlaceholder() || geometry == null
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry,
      point: point == const $CopyWithPlaceholder()
          ? _value.point
          // ignore: cast_nullable_to_non_nullable
          : point as Geometry?,
      attributes: attributes == const $CopyWithPlaceholder()
          ? _value.attributes
          // ignore: cast_nullable_to_non_nullable
          : attributes as Map<String, Object>?,
      regionTags: regionTags == const $CopyWithPlaceholder()
          ? _value.regionTags
          // ignore: cast_nullable_to_non_nullable
          : regionTags as List<String>?,
      licence: licence == const $CopyWithPlaceholder() || licence == null
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String,
      builtAt: builtAt == const $CopyWithPlaceholder()
          ? _value.builtAt
          // ignore: cast_nullable_to_non_nullable
          : builtAt as DateTime?,
      edges: edges == const $CopyWithPlaceholder()
          ? _value.edges
          // ignore: cast_nullable_to_non_nullable
          : edges as InfraNodeEdges?,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as List<String>?,
    );
  }
}

extension $InfraNodeCopyWith on InfraNode {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraNode.copyWith(...)` or `instanceOfInfraNode.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraNodeCWProxy get copyWith => _$InfraNodeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraNode _$InfraNodeFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('InfraNode', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'sourceId',
      'externalId',
      'facilityType',
      'geometry',
      'licence',
    ],
  );
  final val = InfraNode(
    id: $checkedConvert('id', (v) => (v as num).toInt()),
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    externalId: $checkedConvert('externalId', (v) => v as String),
    facilityType: $checkedConvert(
      'facilityType',
      (v) => $enumDecode(_$InfraNodeFacilityTypeEnumEnumMap, v),
    ),
    name: $checkedConvert('name', (v) => v as String?),
    geometry: $checkedConvert(
      'geometry',
      (v) => Geometry.fromJson(v as Map<String, dynamic>),
    ),
    point: $checkedConvert(
      'point',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    attributes: $checkedConvert(
      'attributes',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    regionTags: $checkedConvert(
      'regionTags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    licence: $checkedConvert('licence', (v) => v as String),
    builtAt: $checkedConvert(
      'builtAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    edges: $checkedConvert(
      'edges',
      (v) =>
          v == null ? null : InfraNodeEdges.fromJson(v as Map<String, dynamic>),
    ),
    attribution: $checkedConvert(
      'attribution',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$InfraNodeToJson(InfraNode instance) => <String, dynamic>{
  'id': instance.id,
  'sourceId': instance.sourceId,
  'externalId': instance.externalId,
  'facilityType': _$InfraNodeFacilityTypeEnumEnumMap[instance.facilityType]!,
  'name': ?instance.name,
  'geometry': instance.geometry.toJson(),
  'point': ?instance.point?.toJson(),
  'attributes': ?instance.attributes,
  'regionTags': ?instance.regionTags,
  'licence': instance.licence,
  'builtAt': ?instance.builtAt?.toIso8601String(),
  'edges': ?instance.edges?.toJson(),
  'attribution': ?instance.attribution,
};

const _$InfraNodeFacilityTypeEnumEnumMap = {
  InfraNodeFacilityTypeEnum.powerPeriodGeneration: 'power.generation',
  InfraNodeFacilityTypeEnum.powerPeriodNuclear: 'power.nuclear',
  InfraNodeFacilityTypeEnum.powerPeriodSubstation: 'power.substation',
  InfraNodeFacilityTypeEnum.powerPeriodLine: 'power.line',
  InfraNodeFacilityTypeEnum.telecomPeriodDatacentre: 'telecom.datacentre',
  InfraNodeFacilityTypeEnum.telecomPeriodIxp: 'telecom.ixp',
  InfraNodeFacilityTypeEnum.telecomPeriodExchange: 'telecom.exchange',
  InfraNodeFacilityTypeEnum.telecomPeriodCable: 'telecom.cable',
  InfraNodeFacilityTypeEnum.telecomPeriodLanding: 'telecom.landing',
  InfraNodeFacilityTypeEnum.transportPeriodAirport: 'transport.airport',
  InfraNodeFacilityTypeEnum.transportPeriodPort: 'transport.port',
  InfraNodeFacilityTypeEnum.healthPeriodHospital: 'health.hospital',
  InfraNodeFacilityTypeEnum.waterPeriodTreatment: 'water.treatment',
  InfraNodeFacilityTypeEnum.spacePeriodSpaceport: 'space.spaceport',
};
