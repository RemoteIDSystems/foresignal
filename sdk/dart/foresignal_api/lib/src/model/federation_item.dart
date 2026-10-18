//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/provenance.dart';
import 'package:foresignal_api/src/model/quality.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'federation_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FederationItem {
  /// Returns a new [FederationItem] instance.
  FederationItem({

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


  final FederationItemPersistReasonEnum? persistReason;



      /// The version's history tier; a consumer stores TRACK items in its peer-{id}-trk source (113)
  @JsonKey(
    
    name: r'historyTier',
    required: false,
    includeIfNull: false,
  )


  final FederationItemHistoryTierEnum? historyTier;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is FederationItem &&
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
      other.provenance == provenance;

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
        provenance.hashCode;

  factory FederationItem.fromJson(Map<String, dynamic> json) => _$FederationItemFromJson(json);

  Map<String, dynamic> toJson() => _$FederationItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FederationItemPersistReasonEnum {
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

const FederationItemPersistReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


/// The version's history tier; a consumer stores TRACK items in its peer-{id}-trk source (113)
enum FederationItemHistoryTierEnum {
@JsonValue(r'STANDARD')
STANDARD(r'STANDARD'),
@JsonValue(r'TRACK')
TRACK(r'TRACK');

const FederationItemHistoryTierEnum(this.value);

final String value;

@override
String toString() => value;
}


