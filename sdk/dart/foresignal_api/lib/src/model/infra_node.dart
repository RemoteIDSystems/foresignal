//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/infra_node_edges.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_node.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraNode {
  /// Returns a new [InfraNode] instance.
  InfraNode({

    required  this.id,

    required  this.sourceId,

    required  this.externalId,

    required  this.facilityType,

     this.name,

    required  this.geometry,

     this.point,

     this.attributes,

     this.regionTags,

    required  this.licence,

     this.builtAt,

     this.edges,

     this.attribution,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'facilityType',
    required: true,
    includeIfNull: false,
  )


  final InfraNodeFacilityTypeEnum facilityType;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'geometry',
    required: true,
    includeIfNull: false,
  )


  final Geometry geometry;



  @JsonKey(
    
    name: r'point',
    required: false,
    includeIfNull: false,
  )


  final Geometry? point;



      /// capacityMw, voltageKv, iata, beds, …
  @JsonKey(
    
    name: r'attributes',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? attributes;



  @JsonKey(
    
    name: r'regionTags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionTags;



  @JsonKey(
    
    name: r'licence',
    required: true,
    includeIfNull: false,
  )


  final String licence;



  @JsonKey(
    
    name: r'builtAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? builtAt;



  @JsonKey(
    
    name: r'edges',
    required: false,
    includeIfNull: false,
  )


  final InfraNodeEdges? edges;



      /// e.g. © OpenStreetMap contributors
  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final List<String>? attribution;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraNode &&
      other.id == id &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.facilityType == facilityType &&
      other.name == name &&
      other.geometry == geometry &&
      other.point == point &&
      other.attributes == attributes &&
      other.regionTags == regionTags &&
      other.licence == licence &&
      other.builtAt == builtAt &&
      other.edges == edges &&
      other.attribution == attribution;

    @override
    int get hashCode =>
        id.hashCode +
        sourceId.hashCode +
        externalId.hashCode +
        facilityType.hashCode +
        (name == null ? 0 : name.hashCode) +
        geometry.hashCode +
        point.hashCode +
        attributes.hashCode +
        regionTags.hashCode +
        licence.hashCode +
        builtAt.hashCode +
        (edges == null ? 0 : edges.hashCode) +
        attribution.hashCode;

  factory InfraNode.fromJson(Map<String, dynamic> json) => _$InfraNodeFromJson(json);

  Map<String, dynamic> toJson() => _$InfraNodeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InfraNodeFacilityTypeEnum {
@JsonValue(r'power.generation')
powerPeriodGeneration(r'power.generation'),
@JsonValue(r'power.nuclear')
powerPeriodNuclear(r'power.nuclear'),
@JsonValue(r'power.substation')
powerPeriodSubstation(r'power.substation'),
@JsonValue(r'power.line')
powerPeriodLine(r'power.line'),
@JsonValue(r'telecom.datacentre')
telecomPeriodDatacentre(r'telecom.datacentre'),
@JsonValue(r'telecom.ixp')
telecomPeriodIxp(r'telecom.ixp'),
@JsonValue(r'telecom.exchange')
telecomPeriodExchange(r'telecom.exchange'),
@JsonValue(r'telecom.cable')
telecomPeriodCable(r'telecom.cable'),
@JsonValue(r'telecom.landing')
telecomPeriodLanding(r'telecom.landing'),
@JsonValue(r'transport.airport')
transportPeriodAirport(r'transport.airport'),
@JsonValue(r'transport.port')
transportPeriodPort(r'transport.port'),
@JsonValue(r'health.hospital')
healthPeriodHospital(r'health.hospital'),
@JsonValue(r'water.treatment')
waterPeriodTreatment(r'water.treatment'),
@JsonValue(r'space.spaceport')
spacePeriodSpaceport(r'space.spaceport');

const InfraNodeFacilityTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


