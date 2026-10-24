//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/provenance.dart';
import 'package:foresignal_api/src/model/history_tier.dart';
import 'package:foresignal_api/src/model/quality.dart';
import 'package:foresignal_api/src/model/persist_reason.dart';
import 'package:foresignal_api/src/model/region_tags.dart';
import 'package:foresignal_api/src/model/observer.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_version.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordVersion {
  /// Returns a new [RecordVersion] instance.
  RecordVersion({

    required  this.id,

    required  this.source_,

    required  this.externalId,

    required  this.kind,

    required  this.version,

    required  this.seq,

    required  this.observedAt,

    required  this.ingestedAt,

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

    required  this.persistReason,

     this.prevSeq,

     this.historyTier,

    required  this.receivedAt,

     this.batchId,

     this.downsampled = false,
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



  @JsonKey(
    
    name: r'ingestedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime ingestedAt;



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



      /// null when the source has no attribution line
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



      /// urn:foresignal:{instanceId}:{sourceId}:{externalId}
  @JsonKey(
    
    name: r'globalRef',
    required: false,
    includeIfNull: false,
  )


  final String? globalRef;



  @JsonKey(
    
    name: r'persistReason',
    required: true,
    includeIfNull: false,
  )


  final PersistReason persistReason;



      /// seq of the previous version of this record
  @JsonKey(
    
    name: r'prevSeq',
    required: false,
    includeIfNull: false,
  )


  final int? prevSeq;



  @JsonKey(
    
    name: r'historyTier',
    required: false,
    includeIfNull: false,
  )


  final HistoryTier? historyTier;



  @JsonKey(
    
    name: r'receivedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime receivedAt;



      /// fetch_log id of the batch that wrote this version
  @JsonKey(
    
    name: r'batchId',
    required: false,
    includeIfNull: false,
  )


  final int? batchId;



      /// Seq falls in a downsampled (thinned) TRACK leaf; a `prevSeq` that no longer exists was removed by downsampling
  @JsonKey(
    defaultValue: false,
    name: r'downsampled',
    required: false,
    includeIfNull: false,
  )


  final bool? downsampled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordVersion &&
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
      other.globalRef == globalRef &&
      other.persistReason == persistReason &&
      other.prevSeq == prevSeq &&
      other.historyTier == historyTier &&
      other.receivedAt == receivedAt &&
      other.batchId == batchId &&
      other.downsampled == downsampled;

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
        (attribution == null ? 0 : attribution.hashCode) +
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
        globalRef.hashCode +
        persistReason.hashCode +
        (prevSeq == null ? 0 : prevSeq.hashCode) +
        historyTier.hashCode +
        receivedAt.hashCode +
        (batchId == null ? 0 : batchId.hashCode) +
        downsampled.hashCode;

  factory RecordVersion.fromJson(Map<String, dynamic> json) => _$RecordVersionFromJson(json);

  Map<String, dynamic> toJson() => _$RecordVersionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

