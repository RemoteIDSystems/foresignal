//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'osm_infrastructure_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OsmInfrastructurePayload {
  /// Returns a new [OsmInfrastructurePayload] instance.
  OsmInfrastructurePayload({

     this.name,

     this.operator_,

     this.voltageKv,

     this.capacityMw,

     this.beds,

     this.emergency,

     this.iata,

     this.icao,

     this.ref,

     this.osmType,

     this.osmVersion,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'operator',
    required: false,
    includeIfNull: false,
  )


  final String? operator_;



  @JsonKey(
    
    name: r'voltageKv',
    required: false,
    includeIfNull: false,
  )


  final num? voltageKv;



  @JsonKey(
    
    name: r'capacityMw',
    required: false,
    includeIfNull: false,
  )


  final num? capacityMw;



  @JsonKey(
    
    name: r'beds',
    required: false,
    includeIfNull: false,
  )


  final int? beds;



  @JsonKey(
    
    name: r'emergency',
    required: false,
    includeIfNull: false,
  )


  final String? emergency;



  @JsonKey(
    
    name: r'iata',
    required: false,
    includeIfNull: false,
  )


  final String? iata;



  @JsonKey(
    
    name: r'icao',
    required: false,
    includeIfNull: false,
  )


  final String? icao;



  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



  @JsonKey(
    
    name: r'osmType',
    required: false,
    includeIfNull: false,
  )


  final OsmInfrastructurePayloadOsmTypeEnum? osmType;



  @JsonKey(
    
    name: r'osmVersion',
    required: false,
    includeIfNull: false,
  )


  final int? osmVersion;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OsmInfrastructurePayload &&
      other.name == name &&
      other.operator_ == operator_ &&
      other.voltageKv == voltageKv &&
      other.capacityMw == capacityMw &&
      other.beds == beds &&
      other.emergency == emergency &&
      other.iata == iata &&
      other.icao == icao &&
      other.ref == ref &&
      other.osmType == osmType &&
      other.osmVersion == osmVersion;

    @override
    int get hashCode =>
        (name == null ? 0 : name.hashCode) +
        (operator_ == null ? 0 : operator_.hashCode) +
        (voltageKv == null ? 0 : voltageKv.hashCode) +
        (capacityMw == null ? 0 : capacityMw.hashCode) +
        (beds == null ? 0 : beds.hashCode) +
        (emergency == null ? 0 : emergency.hashCode) +
        (iata == null ? 0 : iata.hashCode) +
        (icao == null ? 0 : icao.hashCode) +
        (ref == null ? 0 : ref.hashCode) +
        (osmType == null ? 0 : osmType.hashCode) +
        (osmVersion == null ? 0 : osmVersion.hashCode);

  factory OsmInfrastructurePayload.fromJson(Map<String, dynamic> json) => _$OsmInfrastructurePayloadFromJson(json);

  Map<String, dynamic> toJson() => _$OsmInfrastructurePayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OsmInfrastructurePayloadOsmTypeEnum {
@JsonValue(r'node')
node(r'node'),
@JsonValue(r'way')
way(r'way'),
@JsonValue(r'relation')
relation(r'relation');

const OsmInfrastructurePayloadOsmTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


