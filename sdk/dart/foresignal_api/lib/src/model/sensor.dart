//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/object_envelope_links.dart';
import 'package:foresignal_api/src/model/provenance.dart';
import 'package:foresignal_api/src/model/object_action.dart';
import 'package:foresignal_api/src/model/object_envelope_time.dart';
import 'package:foresignal_api/src/model/sensor_all_of_capabilities.dart';
import 'package:foresignal_api/src/model/sensor_all_of_passes.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sensor.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Sensor {
  /// Returns a new [Sensor] instance.
  Sensor({

    required  this.ref,

    required  this.type,

    required  this.title,

     this.summary,

     this.textOrigin,

     this.status,

     this.severity,

     this.time,

     this.geometry,

     this.bbox,

     this.private,

     this.tlp,

     this.tags,

     this.provenance,

     this.links,

     this.actions,

    required  this.sensorKind,

     this.pDetect,

     this.method,

     this.families,

     this.rangeKm,

     this.passes,

     this.capabilities,

     this.footprint,

     this.acquiredAt,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;



  @JsonKey(
    
    name: r'textOrigin',
    required: false,
    includeIfNull: false,
  )


  final SensorTextOriginEnum? textOrigin;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final String? status;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final String? severity;



  @JsonKey(
    
    name: r'time',
    required: false,
    includeIfNull: false,
  )


  final ObjectEnvelopeTime? time;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



  @JsonKey(
    
    name: r'provenance',
    required: false,
    includeIfNull: false,
  )


  final Provenance? provenance;



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final ObjectEnvelopeLinks? links;



  @JsonKey(
    
    name: r'actions',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectAction>? actions;



  @JsonKey(
    
    name: r'sensorKind',
    required: true,
    includeIfNull: false,
  )


  final SensorSensorKindEnum sensorKind;



  @JsonKey(
    
    name: r'pDetect',
    required: false,
    includeIfNull: false,
  )


  final num? pDetect;



  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final String? method;



  @JsonKey(
    
    name: r'families',
    required: false,
    includeIfNull: false,
  )


  final List<String>? families;



  @JsonKey(
    
    name: r'rangeKm',
    required: false,
    includeIfNull: false,
  )


  final num? rangeKm;



  @JsonKey(
    
    name: r'passes',
    required: false,
    includeIfNull: false,
  )


  final List<SensorAllOfPasses>? passes;



  @JsonKey(
    
    name: r'capabilities',
    required: false,
    includeIfNull: false,
  )


  final SensorAllOfCapabilities? capabilities;



  @JsonKey(
    
    name: r'footprint',
    required: false,
    includeIfNull: false,
  )


  final Geometry? footprint;



  @JsonKey(
    
    name: r'acquiredAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? acquiredAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Sensor &&
      other.ref == ref &&
      other.type == type &&
      other.title == title &&
      other.summary == summary &&
      other.textOrigin == textOrigin &&
      other.status == status &&
      other.severity == severity &&
      other.time == time &&
      other.geometry == geometry &&
      other.bbox == bbox &&
      other.private == private &&
      other.tlp == tlp &&
      other.tags == tags &&
      other.provenance == provenance &&
      other.links == links &&
      other.actions == actions &&
      other.sensorKind == sensorKind &&
      other.pDetect == pDetect &&
      other.method == method &&
      other.families == families &&
      other.rangeKm == rangeKm &&
      other.passes == passes &&
      other.capabilities == capabilities &&
      other.footprint == footprint &&
      other.acquiredAt == acquiredAt;

    @override
    int get hashCode =>
        ref.hashCode +
        type.hashCode +
        title.hashCode +
        (summary == null ? 0 : summary.hashCode) +
        textOrigin.hashCode +
        (status == null ? 0 : status.hashCode) +
        (severity == null ? 0 : severity.hashCode) +
        time.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        (bbox == null ? 0 : bbox.hashCode) +
        private.hashCode +
        (tlp == null ? 0 : tlp.hashCode) +
        tags.hashCode +
        provenance.hashCode +
        links.hashCode +
        actions.hashCode +
        sensorKind.hashCode +
        (pDetect == null ? 0 : pDetect.hashCode) +
        (method == null ? 0 : method.hashCode) +
        families.hashCode +
        (rangeKm == null ? 0 : rangeKm.hashCode) +
        passes.hashCode +
        (capabilities == null ? 0 : capabilities.hashCode) +
        (footprint == null ? 0 : footprint.hashCode) +
        (acquiredAt == null ? 0 : acquiredAt.hashCode);

  factory Sensor.fromJson(Map<String, dynamic> json) => _$SensorFromJson(json);

  Map<String, dynamic> toJson() => _$SensorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SensorTextOriginEnum {
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const SensorTextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SensorSensorKindEnum {
@JsonValue(r'source')
source_(r'source'),
@JsonValue(r'receiver')
receiver(r'receiver'),
@JsonValue(r'station')
station(r'station'),
@JsonValue(r'satellite')
satellite(r'satellite'),
@JsonValue(r'scene')
scene(r'scene'),
@JsonValue(r'raster-product')
rasterProduct(r'raster-product');

const SensorSensorKindEnum(this.value);

final String value;

@override
String toString() => value;
}


