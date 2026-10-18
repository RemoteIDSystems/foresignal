//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/evidence_group.dart';
import 'package:foresignal_api/src/model/observation_link.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'canonical_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CanonicalEvent {
  /// Returns a new [CanonicalEvent] instance.
  CanonicalEvent({

    required  this.id,

    required  this.ref,

    required  this.family,

    required  this.eventTime,

     this.geometry,

     this.regionCodes,

     this.firstObservedAt,

     this.firstObservationRef,

    required  this.observationCount,

    required  this.evidenceGroupCount,

    required  this.qualityFlags,

    required  this.status,

     this.mergedInto,

    required  this.resolverVersion,

    required  this.createdAt,

    required  this.updatedAt,

     this.observations,

     this.evidenceGroups,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// canonical-event:{id}
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



      /// Signal family plus subtype, e.g. seismic, fire, conflict, outage, disaster, weather-alert
  @JsonKey(
    
    name: r'family',
    required: true,
    includeIfNull: false,
  )


  final String family;



  @JsonKey(
    
    name: r'eventTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime eventTime;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



  @JsonKey(
    
    name: r'firstObservedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? firstObservedAt;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'firstObservationRef',
    required: false,
    includeIfNull: false,
  )


  final String? firstObservationRef;



  @JsonKey(
    
    name: r'observationCount',
    required: true,
    includeIfNull: false,
  )


  final int observationCount;



  @JsonKey(
    
    name: r'evidenceGroupCount',
    required: true,
    includeIfNull: false,
  )


  final int evidenceGroupCount;



  @JsonKey(
    
    name: r'qualityFlags',
    required: true,
    includeIfNull: false,
  )


  final List<CanonicalEventQualityFlagsEnum> qualityFlags;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final CanonicalEventStatusEnum status;



      /// ULID identifier
  @JsonKey(
    
    name: r'mergedInto',
    required: false,
    includeIfNull: false,
  )


  final String? mergedInto;



  @JsonKey(
    
    name: r'resolverVersion',
    required: true,
    includeIfNull: false,
  )


  final String resolverVersion;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;



      /// Detail only
  @JsonKey(
    
    name: r'observations',
    required: false,
    includeIfNull: false,
  )


  final List<ObservationLink>? observations;



      /// Detail only
  @JsonKey(
    
    name: r'evidenceGroups',
    required: false,
    includeIfNull: false,
  )


  final List<EvidenceGroup>? evidenceGroups;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CanonicalEvent &&
      other.id == id &&
      other.ref == ref &&
      other.family == family &&
      other.eventTime == eventTime &&
      other.geometry == geometry &&
      other.regionCodes == regionCodes &&
      other.firstObservedAt == firstObservedAt &&
      other.firstObservationRef == firstObservationRef &&
      other.observationCount == observationCount &&
      other.evidenceGroupCount == evidenceGroupCount &&
      other.qualityFlags == qualityFlags &&
      other.status == status &&
      other.mergedInto == mergedInto &&
      other.resolverVersion == resolverVersion &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.observations == observations &&
      other.evidenceGroups == evidenceGroups;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        family.hashCode +
        eventTime.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        regionCodes.hashCode +
        firstObservedAt.hashCode +
        firstObservationRef.hashCode +
        observationCount.hashCode +
        evidenceGroupCount.hashCode +
        qualityFlags.hashCode +
        status.hashCode +
        (mergedInto == null ? 0 : mergedInto.hashCode) +
        resolverVersion.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        observations.hashCode +
        evidenceGroups.hashCode;

  factory CanonicalEvent.fromJson(Map<String, dynamic> json) => _$CanonicalEventFromJson(json);

  Map<String, dynamic> toJson() => _$CanonicalEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CanonicalEventQualityFlagsEnum {
@JsonValue(r'ECHO')
ECHO(r'ECHO'),
@JsonValue(r'TIMESTAMP_CONFLICT')
TIMESTAMP_CONFLICT(r'TIMESTAMP_CONFLICT'),
@JsonValue(r'SINGLE_ORIGINATOR')
SINGLE_ORIGINATOR(r'SINGLE_ORIGINATOR'),
@JsonValue(r'LATE_REVISION')
LATE_REVISION(r'LATE_REVISION');

const CanonicalEventQualityFlagsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum CanonicalEventStatusEnum {
@JsonValue(r'open')
open(r'open'),
@JsonValue(r'merged')
merged(r'merged'),
@JsonValue(r'closed')
closed(r'closed');

const CanonicalEventStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


