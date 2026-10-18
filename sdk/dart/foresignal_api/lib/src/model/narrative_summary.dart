//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/narrative_platform_stats.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'narrative_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NarrativeSummary {
  /// Returns a new [NarrativeSummary] instance.
  NarrativeSummary({

    required  this.id,

    required  this.ref,

    required  this.title,

    required  this.canonicalEventId,

     this.topics,

     this.placeRefs,

     this.entityRefs,

     this.regionCodes,

     this.languages,

    required  this.platforms,

    required  this.firstObservedAt,

    required  this.lastActivityAt,

    required  this.postCount,

    required  this.uniqueAuthors,

    required  this.evidenceGroupCount,

    required  this.originConcentration,

     this.velocity,

     this.mediaFingerprints,

     this.newsStoryIds,

     this.situationIds,

    required  this.status,

    required  this.state,

     this.coordinationIndicatorsPresent,

     this.mergedInto,

    required  this.tlp,
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



      /// Deterministic: the most central member's text truncated to 140 chars, then the claim summary once 2 or more claims exist
  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



      /// ULID identifier
  @JsonKey(
    
    name: r'canonicalEventId',
    required: true,
    includeIfNull: false,
  )


  final String canonicalEventId;



      /// 117 attention topic ids
  @JsonKey(
    
    name: r'topics',
    required: false,
    includeIfNull: false,
  )


  final List<String>? topics;



  @JsonKey(
    
    name: r'placeRefs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? placeRefs;



  @JsonKey(
    
    name: r'entityRefs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? entityRefs;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



  @JsonKey(
    
    name: r'languages',
    required: false,
    includeIfNull: false,
  )


  final List<String>? languages;



  @JsonKey(
    
    name: r'platforms',
    required: true,
    includeIfNull: false,
  )


  final List<NarrativePlatformStats> platforms;



  @JsonKey(
    
    name: r'firstObservedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime firstObservedAt;



  @JsonKey(
    
    name: r'lastActivityAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime lastActivityAt;



          // minimum: 0
  @JsonKey(
    
    name: r'postCount',
    required: true,
    includeIfNull: false,
  )


  final int postCount;



          // minimum: 0
  @JsonKey(
    
    name: r'uniqueAuthors',
    required: true,
    includeIfNull: false,
  )


  final int uniqueAuthors;



      /// Independent origins (reposts, quotes, shared links and media join their original's group)
          // minimum: 0
  @JsonKey(
    
    name: r'evidenceGroupCount',
    required: true,
    includeIfNull: false,
  )


  final int evidenceGroupCount;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'originConcentration',
    required: true,
    includeIfNull: false,
  )


  final num originConcentration;



      /// Current hourly velocity ratio from 135 baselines
  @JsonKey(
    
    name: r'velocity',
    required: false,
    includeIfNull: false,
  )


  final num? velocity;



      /// Media fingerprint ids
  @JsonKey(
    
    name: r'mediaFingerprints',
    required: false,
    includeIfNull: false,
  )


  final List<String>? mediaFingerprints;



      /// 133 stories linked by URL or text
  @JsonKey(
    
    name: r'newsStoryIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? newsStoryIds;



  @JsonKey(
    
    name: r'situationIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? situationIds;



      /// Set from 137 comparisons; describes the relationship to independent evidence, never truth
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final NarrativeSummaryStatusEnum status;



      /// By velocity and age
  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final NarrativeSummaryStateEnum state;



      /// A `social-coordination` finding exists for the narrative
  @JsonKey(
    
    name: r'coordinationIndicatorsPresent',
    required: false,
    includeIfNull: false,
  )


  final bool? coordinationIndicatorsPresent;



      /// ULID identifier
  @JsonKey(
    
    name: r'mergedInto',
    required: false,
    includeIfNull: false,
  )


  final String? mergedInto;



  @JsonKey(
    
    name: r'tlp',
    required: true,
    includeIfNull: false,
  )


  final Tlp tlp;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NarrativeSummary &&
      other.id == id &&
      other.ref == ref &&
      other.title == title &&
      other.canonicalEventId == canonicalEventId &&
      other.topics == topics &&
      other.placeRefs == placeRefs &&
      other.entityRefs == entityRefs &&
      other.regionCodes == regionCodes &&
      other.languages == languages &&
      other.platforms == platforms &&
      other.firstObservedAt == firstObservedAt &&
      other.lastActivityAt == lastActivityAt &&
      other.postCount == postCount &&
      other.uniqueAuthors == uniqueAuthors &&
      other.evidenceGroupCount == evidenceGroupCount &&
      other.originConcentration == originConcentration &&
      other.velocity == velocity &&
      other.mediaFingerprints == mediaFingerprints &&
      other.newsStoryIds == newsStoryIds &&
      other.situationIds == situationIds &&
      other.status == status &&
      other.state == state &&
      other.coordinationIndicatorsPresent == coordinationIndicatorsPresent &&
      other.mergedInto == mergedInto &&
      other.tlp == tlp;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        title.hashCode +
        canonicalEventId.hashCode +
        topics.hashCode +
        placeRefs.hashCode +
        entityRefs.hashCode +
        regionCodes.hashCode +
        languages.hashCode +
        platforms.hashCode +
        firstObservedAt.hashCode +
        lastActivityAt.hashCode +
        postCount.hashCode +
        uniqueAuthors.hashCode +
        evidenceGroupCount.hashCode +
        originConcentration.hashCode +
        (velocity == null ? 0 : velocity.hashCode) +
        mediaFingerprints.hashCode +
        newsStoryIds.hashCode +
        situationIds.hashCode +
        status.hashCode +
        state.hashCode +
        coordinationIndicatorsPresent.hashCode +
        (mergedInto == null ? 0 : mergedInto.hashCode) +
        tlp.hashCode;

  factory NarrativeSummary.fromJson(Map<String, dynamic> json) => _$NarrativeSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$NarrativeSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Set from 137 comparisons; describes the relationship to independent evidence, never truth
enum NarrativeSummaryStatusEnum {
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
@JsonValue(r'mixed')
mixed(r'mixed'),
@JsonValue(r'insufficient-coverage')
insufficientCoverage(r'insufficient-coverage');

const NarrativeSummaryStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


/// By velocity and age
enum NarrativeSummaryStateEnum {
@JsonValue(r'emerging')
emerging(r'emerging'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'fading')
fading(r'fading'),
@JsonValue(r'dormant')
dormant(r'dormant');

const NarrativeSummaryStateEnum(this.value);

final String value;

@override
String toString() => value;
}


