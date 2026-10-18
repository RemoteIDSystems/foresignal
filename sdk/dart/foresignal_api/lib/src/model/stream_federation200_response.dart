//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/federation_item.dart';
import 'package:foresignal_api/src/model/live_event.dart';
import 'package:foresignal_api/src/model/provenance.dart';
import 'package:foresignal_api/src/model/quality.dart';
import 'package:foresignal_api/src/model/resync_event.dart';
import 'package:foresignal_api/src/model/reset_event.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stream_federation200_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StreamFederation200Response {
  /// Returns a new [StreamFederation200Response] instance.
  StreamFederation200Response({

    required  this.seq,

    required  this.ref,

    required  this.sourceId,

    required  this.externalId,

    required  this.kind,

    required  this.version,

     this.persistReason,

     this.historyTier,

    required  this.observedAt,

     this.eventTime,

     this.validUntil,

    required  this.tombstone,

     this.geometry,

    required  this.payload,

     this.quality,

    required  this.licence,

    required  this.provenance,

     this.bbox,

     this.prevBbox,

     this.properties,

     this.leftFilter = false,

    required  this.reason,

    required  this.watermarkSeq,

     this.resumeAfter,
  });

  @JsonKey(
    
    name: r'seq',
    required: true,
    includeIfNull: false,
  )


  final int seq;



      /// urn:foresignal:{instanceId}:{sourceId}:{externalId} (externalId percent-encoded)
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



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
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



      /// Persisted version this live state builds on
  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'persistReason',
    required: false,
    includeIfNull: false,
  )


  final StreamFederation200ResponsePersistReasonEnum? persistReason;



      /// The version's history tier; a consumer stores TRACK items in its peer-{id}-trk source (113)
  @JsonKey(
    
    name: r'historyTier',
    required: false,
    includeIfNull: false,
  )


  final StreamFederation200ResponseHistoryTierEnum? historyTier;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



  @JsonKey(
    
    name: r'eventTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? eventTime;



  @JsonKey(
    
    name: r'validUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validUntil;



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



  @JsonKey(
    
    name: r'payload',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> payload;



  @JsonKey(
    
    name: r'quality',
    required: false,
    includeIfNull: false,
  )


  final Quality? quality;



      /// LicenceTerms id in force for this version
  @JsonKey(
    
    name: r'licence',
    required: true,
    includeIfNull: false,
  )


  final String licence;



  @JsonKey(
    
    name: r'provenance',
    required: true,
    includeIfNull: false,
  )


  final Provenance provenance;



      /// Current bounding box
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



      /// Bounding box before this update (used by the spatial filter)
  @JsonKey(
    
    name: r'prevBbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? prevBbox;



      /// Kinematic (non-significant) properties after this update, over the persisted payload; a JSON null value means the key was removed
  @JsonKey(
    
    name: r'properties',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? properties;



      /// True when only `prevBbox` matched the spatial filter: remove the feature
  @JsonKey(
    defaultValue: false,
    name: r'leftFilter',
    required: false,
    includeIfNull: false,
  )


  final bool? leftFilter;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final StreamFederation200ResponseReasonEnum reason;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: true,
    includeIfNull: true,
  )


  final int? watermarkSeq;



      /// Last delivered seq to resume from (null for resync_required)
  @JsonKey(
    
    name: r'resumeAfter',
    required: false,
    includeIfNull: false,
  )


  final int? resumeAfter;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StreamFederation200Response &&
      other.seq == seq &&
      other.ref == ref &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.version == version &&
      other.persistReason == persistReason &&
      other.historyTier == historyTier &&
      other.observedAt == observedAt &&
      other.eventTime == eventTime &&
      other.validUntil == validUntil &&
      other.tombstone == tombstone &&
      other.geometry == geometry &&
      other.payload == payload &&
      other.quality == quality &&
      other.licence == licence &&
      other.provenance == provenance &&
      other.bbox == bbox &&
      other.prevBbox == prevBbox &&
      other.properties == properties &&
      other.leftFilter == leftFilter &&
      other.reason == reason &&
      other.watermarkSeq == watermarkSeq &&
      other.resumeAfter == resumeAfter;

    @override
    int get hashCode =>
        seq.hashCode +
        ref.hashCode +
        sourceId.hashCode +
        externalId.hashCode +
        kind.hashCode +
        version.hashCode +
        persistReason.hashCode +
        historyTier.hashCode +
        observedAt.hashCode +
        (eventTime == null ? 0 : eventTime.hashCode) +
        (validUntil == null ? 0 : validUntil.hashCode) +
        tombstone.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        payload.hashCode +
        (quality == null ? 0 : quality.hashCode) +
        licence.hashCode +
        provenance.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        (prevBbox == null ? 0 : prevBbox.hashCode) +
        properties.hashCode +
        leftFilter.hashCode +
        reason.hashCode +
        (watermarkSeq == null ? 0 : watermarkSeq.hashCode) +
        (resumeAfter == null ? 0 : resumeAfter.hashCode);

  factory StreamFederation200Response.fromJson(Map<String, dynamic> json) => _$StreamFederation200ResponseFromJson(json);

  Map<String, dynamic> toJson() => _$StreamFederation200ResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StreamFederation200ResponsePersistReasonEnum {
@JsonValue(r'FIRST')
FIRST(r'FIRST'),
@JsonValue(r'REAPPEAR')
REAPPEAR(r'REAPPEAR'),
@JsonValue(r'SIGNIFICANT')
SIGNIFICANT(r'SIGNIFICANT'),
@JsonValue(r'TOMBSTONE')
TOMBSTONE(r'TOMBSTONE'),
@JsonValue(r'CHANGE')
CHANGE(r'CHANGE'),
@JsonValue(r'SAMPLE')
SAMPLE(r'SAMPLE'),
@JsonValue(r'KEYFRAME')
KEYFRAME(r'KEYFRAME'),
@JsonValue(r'HEARTBEAT')
HEARTBEAT(r'HEARTBEAT'),
@JsonValue(r'TRIGGER_EDGE')
TRIGGER_EDGE(r'TRIGGER_EDGE'),
@JsonValue(r'SNAPSHOT')
SNAPSHOT(r'SNAPSHOT');

const StreamFederation200ResponsePersistReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


/// The version's history tier; a consumer stores TRACK items in its peer-{id}-trk source (113)
enum StreamFederation200ResponseHistoryTierEnum {
@JsonValue(r'STANDARD')
STANDARD(r'STANDARD'),
@JsonValue(r'TRACK')
TRACK(r'TRACK');

const StreamFederation200ResponseHistoryTierEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StreamFederation200ResponseReasonEnum {
@JsonValue(r'buffer_overflow')
bufferOverflow(r'buffer_overflow'),
@JsonValue(r'replay_cap')
replayCap(r'replay_cap'),
@JsonValue(r'resync_required')
resyncRequired(r'resync_required');

const StreamFederation200ResponseReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


