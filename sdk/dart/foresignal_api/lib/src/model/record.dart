//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/provenance.dart';
import 'package:foresignal_api/src/model/quality.dart';
import 'package:foresignal_api/src/model/region_tags.dart';
import 'package:foresignal_api/src/model/observer.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Record {
  /// Returns a new [Record] instance.
  Record({

    required  this.id,

    required  this.source_,

    required  this.externalId,

    required  this.kind,

    required  this.version,

    required  this.seq,

    required  this.observedAt,

     this.ingestedAt,

     this.validUntil,

     this.tombstone,

     this.geometry,

    required  this.payload,

     this.attribution,

     this.title,

     this.persistedObservedAt,

     this.eventTime,

     this.validFrom,

     this.quality,

     this.live,

     this.qaFlags,

     this.distanceKm,

     this.observer,

     this.region,

     this.entityId,

     this.provenance,

     this.globalRef,
  });

      /// <sourceId>:<externalId>
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final String source_;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'seq',
    required: true,
    includeIfNull: false,
  )


  final int seq;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



      /// Versions, history and asOf responses only
  @JsonKey(
    
    name: r'ingestedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? ingestedAt;



  @JsonKey(
    
    name: r'validUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validUntil;



  @JsonKey(
    
    name: r'tombstone',
    required: false,
    includeIfNull: false,
  )


  final bool? tombstone;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



      /// Current payload `jsonb_strip_nulls(h.payload || l.kinematic)`; projected by `fields`
  @JsonKey(
    
    name: r'payload',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> payload;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;



      /// record_head.title (titleField, else title > name > callsign > place > r > hex > mmsi)
  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



      /// observedAt of the persisted version (observedAt may be newer from live updates)
  @JsonKey(
    
    name: r'persistedObservedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? persistedObservedAt;



      /// Discrete event time or measurement period start
  @JsonKey(
    
    name: r'eventTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? eventTime;



      /// Start of validity (`onset ?? effective` for alerts, scheduled start for road events); nullable, hashed like validUntil
  @JsonKey(
    
    name: r'validFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validFrom;



  @JsonKey(
    
    name: r'quality',
    required: false,
    includeIfNull: false,
  )


  final Quality? quality;



      /// True when live state differs from the persisted version (record_live.dirty)
  @JsonKey(
    
    name: r'live',
    required: false,
    includeIfNull: false,
  )


  final bool? live;



      /// Quality bitmask: 1 FUTURE_CLAMPED, 2 GEOMETRY_FIXED, 4 NULL_ISLAND, 8 TIME_IMPUTED, 16 POSITION_STALE
          // minimum: 0
  @JsonKey(
    
    name: r'qaFlags',
    required: false,
    includeIfNull: false,
  )


  final int? qaFlags;



      /// Distance from `near` (sort=distance only)
          // minimum: 0
  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;



      /// Only with include=observer for agent/admin tokens or when security.exposeObserver is on; otherwise omitted
  @JsonKey(
    
    name: r'observer',
    required: false,
    includeIfNull: false,
  )


  final List<Observer>? observer;



      /// Present with include=region (040)
  @JsonKey(
    
    name: r'region',
    required: false,
    includeIfNull: false,
  )


  final RegionTags? region;



      /// Canonical entity from the record's open link; present with `include=entity`
  @JsonKey(
    
    name: r'entityId',
    required: false,
    includeIfNull: false,
  )


  final String? entityId;



      /// Present with include=provenance
  @JsonKey(
    
    name: r'provenance',
    required: false,
    includeIfNull: false,
  )


  final Provenance? provenance;



      /// Global record ref urn:foresignal:{instanceId}:{sourceId}:{externalId} (externalId percent-encoded)
  @JsonKey(
    
    name: r'globalRef',
    required: false,
    includeIfNull: false,
  )


  final String? globalRef;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Record &&
      other.id == id &&
      other.source_ == source_ &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.version == version &&
      other.seq == seq &&
      other.observedAt == observedAt &&
      other.ingestedAt == ingestedAt &&
      other.validUntil == validUntil &&
      other.tombstone == tombstone &&
      other.geometry == geometry &&
      other.payload == payload &&
      other.attribution == attribution &&
      other.title == title &&
      other.persistedObservedAt == persistedObservedAt &&
      other.eventTime == eventTime &&
      other.validFrom == validFrom &&
      other.quality == quality &&
      other.live == live &&
      other.qaFlags == qaFlags &&
      other.distanceKm == distanceKm &&
      other.observer == observer &&
      other.region == region &&
      other.entityId == entityId &&
      other.provenance == provenance &&
      other.globalRef == globalRef;

    @override
    int get hashCode =>
        id.hashCode +
        source_.hashCode +
        externalId.hashCode +
        kind.hashCode +
        version.hashCode +
        seq.hashCode +
        observedAt.hashCode +
        ingestedAt.hashCode +
        (validUntil == null ? 0 : validUntil.hashCode) +
        tombstone.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        payload.hashCode +
        attribution.hashCode +
        (title == null ? 0 : title.hashCode) +
        persistedObservedAt.hashCode +
        (eventTime == null ? 0 : eventTime.hashCode) +
        (validFrom == null ? 0 : validFrom.hashCode) +
        (quality == null ? 0 : quality.hashCode) +
        live.hashCode +
        qaFlags.hashCode +
        distanceKm.hashCode +
        observer.hashCode +
        (region == null ? 0 : region.hashCode) +
        (entityId == null ? 0 : entityId.hashCode) +
        provenance.hashCode +
        globalRef.hashCode;

  factory Record.fromJson(Map<String, dynamic> json) => _$RecordFromJson(json);

  Map<String, dynamic> toJson() => _$RecordToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

