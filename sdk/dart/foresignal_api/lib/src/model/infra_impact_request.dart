//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/infra_impact_request_record.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_impact_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraImpactRequest {
  /// Returns a new [InfraImpactRequest] instance.
  InfraImpactRequest({

     this.geometry,

     this.eventId,

     this.record,

     this.depth = 2,

     this.minLabel,

     this.types,
  });

  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



      /// ULID identifier
  @JsonKey(
    
    name: r'eventId',
    required: false,
    includeIfNull: false,
  )


  final String? eventId;



  @JsonKey(
    
    name: r'record',
    required: false,
    includeIfNull: false,
  )


  final InfraImpactRequestRecord? record;



          // minimum: 1
          // maximum: 3
  @JsonKey(
    defaultValue: 2,
    name: r'depth',
    required: false,
    includeIfNull: false,
  )


  final int? depth;



  @JsonKey(
    
    name: r'minLabel',
    required: false,
    includeIfNull: false,
  )


  final InfraImpactRequestMinLabelEnum? minLabel;



  @JsonKey(
    
    name: r'types',
    required: false,
    includeIfNull: false,
  )


  final List<InfraImpactRequestTypesEnum>? types;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraImpactRequest &&
      other.geometry == geometry &&
      other.eventId == eventId &&
      other.record == record &&
      other.depth == depth &&
      other.minLabel == minLabel &&
      other.types == types;

    @override
    int get hashCode =>
        geometry.hashCode +
        eventId.hashCode +
        record.hashCode +
        depth.hashCode +
        minLabel.hashCode +
        types.hashCode;

  factory InfraImpactRequest.fromJson(Map<String, dynamic> json) => _$InfraImpactRequestFromJson(json);

  Map<String, dynamic> toJson() => _$InfraImpactRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InfraImpactRequestMinLabelEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'likely')
likely(r'likely'),
@JsonValue(r'possible')
possible(r'possible'),
@JsonValue(r'speculative')
speculative(r'speculative');

const InfraImpactRequestMinLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


enum InfraImpactRequestTypesEnum {
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

const InfraImpactRequestTypesEnum(this.value);

final String value;

@override
String toString() => value;
}


