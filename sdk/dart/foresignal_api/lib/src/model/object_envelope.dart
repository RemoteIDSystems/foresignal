//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/object_envelope_links.dart';
import 'package:foresignal_api/src/model/provenance.dart';
import 'package:foresignal_api/src/model/object_action.dart';
import 'package:foresignal_api/src/model/object_envelope_time.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'object_envelope.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObjectEnvelope {
  /// Returns a new [ObjectEnvelope] instance.
  ObjectEnvelope({

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


  final ObjectEnvelopeTextOriginEnum? textOrigin;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObjectEnvelope &&
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
      other.actions == actions;

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
        actions.hashCode;

  factory ObjectEnvelope.fromJson(Map<String, dynamic> json) => _$ObjectEnvelopeFromJson(json);

  Map<String, dynamic> toJson() => _$ObjectEnvelopeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ObjectEnvelopeTextOriginEnum {
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const ObjectEnvelopeTextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


