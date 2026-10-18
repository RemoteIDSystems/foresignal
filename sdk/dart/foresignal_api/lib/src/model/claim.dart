//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/claim_place.dart';
import 'package:foresignal_api/src/model/claim_event_time.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'claim.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Claim {
  /// Returns a new [Claim] instance.
  Claim({

    required  this.id,

    required  this.ref,

    required  this.narrativeId,

    required  this.type,

    required  this.signalFamily,

    required  this.polarity,

    required  this.hedged,

     this.place,

     this.entityRefs,

     this.eventTime,

     this.actor,

    required  this.supportCount,

    required  this.evidenceGroupCount,

    required  this.firstObservedAt,

    required  this.extraction,

    required  this.manifestId,

    required  this.confidence,

    required  this.status,

     this.comparisonId,

    required  this.sourceReliability,

    required  this.informationCredibility,

    required  this.grade,

     this.tlp,

     this.createdAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



      /// ULID identifier
  @JsonKey(
    
    name: r'narrativeId',
    required: true,
    includeIfNull: false,
  )


  final String narrativeId;



      /// Lexicon id (`intel/social/claim-lexicon.json`)
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



      /// 119 signal family the type maps to
  @JsonKey(
    
    name: r'signalFamily',
    required: true,
    includeIfNull: false,
  )


  final String signalFamily;



  @JsonKey(
    
    name: r'polarity',
    required: true,
    includeIfNull: false,
  )


  final ClaimPolarityEnum polarity;



      /// Hedge markers such as \"reportedly\", \"unconfirmed\"
  @JsonKey(
    
    name: r'hedged',
    required: true,
    includeIfNull: false,
  )


  final bool hedged;



  @JsonKey(
    
    name: r'place',
    required: false,
    includeIfNull: false,
  )


  final ClaimPlace? place;



  @JsonKey(
    
    name: r'entityRefs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? entityRefs;



  @JsonKey(
    
    name: r'eventTime',
    required: false,
    includeIfNull: false,
  )


  final ClaimEventTime? eventTime;



      /// Organisation name if stated
  @JsonKey(
    
    name: r'actor',
    required: false,
    includeIfNull: false,
  )


  final String? actor;



      /// Posts supporting the claim
          // minimum: 0
  @JsonKey(
    
    name: r'supportCount',
    required: true,
    includeIfNull: false,
  )


  final int supportCount;



          // minimum: 0
  @JsonKey(
    
    name: r'evidenceGroupCount',
    required: true,
    includeIfNull: false,
  )


  final int evidenceGroupCount;



  @JsonKey(
    
    name: r'firstObservedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime firstObservedAt;



  @JsonKey(
    
    name: r'extraction',
    required: true,
    includeIfNull: false,
  )


  final ClaimExtractionEnum extraction;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: true,
    includeIfNull: false,
  )


  final String manifestId;



      /// Extraction confidence only
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'confidence',
    required: true,
    includeIfNull: false,
  )


  final num confidence;



      /// Set by 137 comparisons
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ClaimStatusEnum status;



      /// 137 comparison
  @JsonKey(
    
    name: r'comparisonId',
    required: false,
    includeIfNull: false,
  )


  final String? comparisonId;



      /// Admiralty: F by default, E/D from the account's or channel's 137 track record, or an operator override
  @JsonKey(
    
    name: r'sourceReliability',
    required: true,
    includeIfNull: false,
  )


  final ClaimSourceReliabilityEnum sourceReliability;



      /// From 137 status: 6 unassessed, 2 corroborated, 5 unobserved, 4 contradicted
          // minimum: 1
          // maximum: 6
  @JsonKey(
    
    name: r'informationCredibility',
    required: true,
    includeIfNull: false,
  )


  final int informationCredibility;



  @JsonKey(
    
    name: r'grade',
    required: true,
    includeIfNull: false,
  )


  final String grade;



  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Claim &&
      other.id == id &&
      other.ref == ref &&
      other.narrativeId == narrativeId &&
      other.type == type &&
      other.signalFamily == signalFamily &&
      other.polarity == polarity &&
      other.hedged == hedged &&
      other.place == place &&
      other.entityRefs == entityRefs &&
      other.eventTime == eventTime &&
      other.actor == actor &&
      other.supportCount == supportCount &&
      other.evidenceGroupCount == evidenceGroupCount &&
      other.firstObservedAt == firstObservedAt &&
      other.extraction == extraction &&
      other.manifestId == manifestId &&
      other.confidence == confidence &&
      other.status == status &&
      other.comparisonId == comparisonId &&
      other.sourceReliability == sourceReliability &&
      other.informationCredibility == informationCredibility &&
      other.grade == grade &&
      other.tlp == tlp &&
      other.createdAt == createdAt;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        narrativeId.hashCode +
        type.hashCode +
        signalFamily.hashCode +
        polarity.hashCode +
        hedged.hashCode +
        (place == null ? 0 : place.hashCode) +
        entityRefs.hashCode +
        (eventTime == null ? 0 : eventTime.hashCode) +
        (actor == null ? 0 : actor.hashCode) +
        supportCount.hashCode +
        evidenceGroupCount.hashCode +
        firstObservedAt.hashCode +
        extraction.hashCode +
        manifestId.hashCode +
        confidence.hashCode +
        status.hashCode +
        (comparisonId == null ? 0 : comparisonId.hashCode) +
        sourceReliability.hashCode +
        informationCredibility.hashCode +
        grade.hashCode +
        tlp.hashCode +
        createdAt.hashCode;

  factory Claim.fromJson(Map<String, dynamic> json) => _$ClaimFromJson(json);

  Map<String, dynamic> toJson() => _$ClaimToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ClaimPolarityEnum {
@JsonValue(r'asserted')
asserted(r'asserted'),
@JsonValue(r'denied')
denied(r'denied'),
@JsonValue(r'questioned')
questioned(r'questioned');

const ClaimPolarityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ClaimExtractionEnum {
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const ClaimExtractionEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Set by 137 comparisons
enum ClaimStatusEnum {
@JsonValue(r'unassessed')
unassessed(r'unassessed'),
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'corroborated')
corroborated(r'corroborated'),
@JsonValue(r'unobserved')
unobserved(r'unobserved'),
@JsonValue(r'contradicted')
contradicted(r'contradicted'),
@JsonValue(r'insufficient-coverage')
insufficientCoverage(r'insufficient-coverage');

const ClaimStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Admiralty: F by default, E/D from the account's or channel's 137 track record, or an operator override
enum ClaimSourceReliabilityEnum {
@JsonValue(r'A')
A(r'A'),
@JsonValue(r'B')
B(r'B'),
@JsonValue(r'C')
C(r'C'),
@JsonValue(r'D')
D(r'D'),
@JsonValue(r'E')
E(r'E'),
@JsonValue(r'F')
F(r'F');

const ClaimSourceReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


