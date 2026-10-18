//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'evidence_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EvidenceEvent {
  /// Returns a new [EvidenceEvent] instance.
  EvidenceEvent({

    required  this.canonicalEventId,

    required  this.ref,

    required  this.family,

    required  this.status,

    required  this.eventTime,

     this.regionCodes,

    required  this.observationCount,

    required  this.evidenceGroupCount,

    required  this.qualityFlags,

     this.flagsAdded,

     this.mergedInto,

     this.mergedFrom,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'canonicalEventId',
    required: true,
    includeIfNull: false,
  )


  final String canonicalEventId;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'family',
    required: true,
    includeIfNull: false,
  )


  final String family;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final EvidenceEventStatusEnum status;



  @JsonKey(
    
    name: r'eventTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime eventTime;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



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


  final List<EvidenceEventQualityFlagsEnum> qualityFlags;



      /// canonical.flagged
  @JsonKey(
    
    name: r'flagsAdded',
    required: false,
    includeIfNull: false,
  )


  final List<EvidenceEventFlagsAddedEnum>? flagsAdded;



      /// ULID identifier
  @JsonKey(
    
    name: r'mergedInto',
    required: false,
    includeIfNull: false,
  )


  final String? mergedInto;



      /// canonical.merged
  @JsonKey(
    
    name: r'mergedFrom',
    required: false,
    includeIfNull: false,
  )


  final List<String>? mergedFrom;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EvidenceEvent &&
      other.canonicalEventId == canonicalEventId &&
      other.ref == ref &&
      other.family == family &&
      other.status == status &&
      other.eventTime == eventTime &&
      other.regionCodes == regionCodes &&
      other.observationCount == observationCount &&
      other.evidenceGroupCount == evidenceGroupCount &&
      other.qualityFlags == qualityFlags &&
      other.flagsAdded == flagsAdded &&
      other.mergedInto == mergedInto &&
      other.mergedFrom == mergedFrom;

    @override
    int get hashCode =>
        canonicalEventId.hashCode +
        ref.hashCode +
        family.hashCode +
        status.hashCode +
        eventTime.hashCode +
        regionCodes.hashCode +
        observationCount.hashCode +
        evidenceGroupCount.hashCode +
        qualityFlags.hashCode +
        flagsAdded.hashCode +
        (mergedInto == null ? 0 : mergedInto.hashCode) +
        mergedFrom.hashCode;

  factory EvidenceEvent.fromJson(Map<String, dynamic> json) => _$EvidenceEventFromJson(json);

  Map<String, dynamic> toJson() => _$EvidenceEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EvidenceEventStatusEnum {
@JsonValue(r'open')
open(r'open'),
@JsonValue(r'merged')
merged(r'merged'),
@JsonValue(r'closed')
closed(r'closed');

const EvidenceEventStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EvidenceEventQualityFlagsEnum {
@JsonValue(r'ECHO')
ECHO(r'ECHO'),
@JsonValue(r'TIMESTAMP_CONFLICT')
TIMESTAMP_CONFLICT(r'TIMESTAMP_CONFLICT'),
@JsonValue(r'SINGLE_ORIGINATOR')
SINGLE_ORIGINATOR(r'SINGLE_ORIGINATOR'),
@JsonValue(r'LATE_REVISION')
LATE_REVISION(r'LATE_REVISION');

const EvidenceEventQualityFlagsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EvidenceEventFlagsAddedEnum {
@JsonValue(r'ECHO')
ECHO(r'ECHO'),
@JsonValue(r'TIMESTAMP_CONFLICT')
TIMESTAMP_CONFLICT(r'TIMESTAMP_CONFLICT'),
@JsonValue(r'SINGLE_ORIGINATOR')
SINGLE_ORIGINATOR(r'SINGLE_ORIGINATOR'),
@JsonValue(r'LATE_REVISION')
LATE_REVISION(r'LATE_REVISION');

const EvidenceEventFlagsAddedEnum(this.value);

final String value;

@override
String toString() => value;
}


