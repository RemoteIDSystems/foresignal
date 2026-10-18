//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/history_tier.dart';
import 'package:foresignal_api/src/model/persist_reason.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'change.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Change {
  /// Returns a new [Change] instance.
  Change({

    required  this.seq,

    required  this.source_,

    required  this.externalId,

    required  this.kind,

    required  this.version,

    required  this.observedAt,

    required  this.tombstone,

     this.geometry,

     this.payload,

     this.prevSeq,

     this.historyTier,

     this.persistReason,

     this.eventTime,

     this.validUntil,

     this.qaFlags,

     this.bbox,

     this.prevPersistedBbox,

     this.prevLiveBbox,

     this.leftFilter = false,

     this.purged,

     this.validFrom,

     this.globalRef,
  });

  @JsonKey(
    
    name: r'seq',
    required: true,
    includeIfNull: false,
  )


  final int seq;



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
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



  @JsonKey(
    
    name: r'tombstone',
    required: true,
    includeIfNull: false,
  )


  final bool tombstone;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



      /// Present with payload=true, and always for kinds in `inlinePayloadKinds` (default raster-frame)
  @JsonKey(
    
    name: r'payload',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? payload;



      /// seq of the previous version; null for FIRST
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
    
    name: r'persistReason',
    required: false,
    includeIfNull: false,
  )


  final PersistReason? persistReason;



  @JsonKey(
    
    name: r'eventTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? eventTime;



      /// Effective expiry; offline mirrors drop the record after it
  @JsonKey(
    
    name: r'validUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validUntil;



      /// Quality bitmask: 1 FUTURE_CLAMPED, 2 GEOMETRY_FIXED, 4 NULL_ISLAND, 8 TIME_IMPUTED, 16 POSITION_STALE
          // minimum: 0
  @JsonKey(
    
    name: r'qaFlags',
    required: false,
    includeIfNull: false,
  )


  final int? qaFlags;



      /// Bounding box of this version
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



      /// SSE only: bbox of the previous persisted version
  @JsonKey(
    
    name: r'prevPersistedBbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? prevPersistedBbox;



      /// SSE only: bbox of the live position before this version
  @JsonKey(
    
    name: r'prevLiveBbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? prevLiveBbox;



      /// SSE only: only a previous box matched the spatial filter; remove the feature
  @JsonKey(
    defaultValue: false,
    name: r'leftFilter',
    required: false,
    includeIfNull: false,
  )


  final bool? leftFilter;



      /// Tombstone written by an entity purge: clients and caches delete the entity (039)
  @JsonKey(
    
    name: r'purged',
    required: false,
    includeIfNull: false,
  )


  final bool? purged;



      /// Start of validity (see Record.validFrom)
  @JsonKey(
    
    name: r'validFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validFrom;



      /// urn:foresignal:{instanceId}:{sourceId}:{externalId}
  @JsonKey(
    
    name: r'globalRef',
    required: false,
    includeIfNull: false,
  )


  final String? globalRef;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Change &&
      other.seq == seq &&
      other.source_ == source_ &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.version == version &&
      other.observedAt == observedAt &&
      other.tombstone == tombstone &&
      other.geometry == geometry &&
      other.payload == payload &&
      other.prevSeq == prevSeq &&
      other.historyTier == historyTier &&
      other.persistReason == persistReason &&
      other.eventTime == eventTime &&
      other.validUntil == validUntil &&
      other.qaFlags == qaFlags &&
      other.bbox == bbox &&
      other.prevPersistedBbox == prevPersistedBbox &&
      other.prevLiveBbox == prevLiveBbox &&
      other.leftFilter == leftFilter &&
      other.purged == purged &&
      other.validFrom == validFrom &&
      other.globalRef == globalRef;

    @override
    int get hashCode =>
        seq.hashCode +
        source_.hashCode +
        externalId.hashCode +
        kind.hashCode +
        version.hashCode +
        observedAt.hashCode +
        tombstone.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        (payload == null ? 0 : payload.hashCode) +
        (prevSeq == null ? 0 : prevSeq.hashCode) +
        historyTier.hashCode +
        persistReason.hashCode +
        (eventTime == null ? 0 : eventTime.hashCode) +
        (validUntil == null ? 0 : validUntil.hashCode) +
        qaFlags.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        (prevPersistedBbox == null ? 0 : prevPersistedBbox.hashCode) +
        (prevLiveBbox == null ? 0 : prevLiveBbox.hashCode) +
        leftFilter.hashCode +
        purged.hashCode +
        (validFrom == null ? 0 : validFrom.hashCode) +
        globalRef.hashCode;

  factory Change.fromJson(Map<String, dynamic> json) => _$ChangeFromJson(json);

  Map<String, dynamic> toJson() => _$ChangeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

