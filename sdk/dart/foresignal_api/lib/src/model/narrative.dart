//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/narrative_platform_stats.dart';
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/media_fingerprint.dart';
import 'package:foresignal_api/src/model/coordination_indicators.dart';
import 'package:foresignal_api/src/model/narrative_log_entry.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:foresignal_api/src/model/claim.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'narrative.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Narrative {
  /// Returns a new [Narrative] instance.
  Narrative({

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

    required  this.envelope,

    required  this.claims,

    required  this.media,

     this.coordination,

     this.newsStories,

     this.situations,

     this.comparisonIds,

    required  this.history,

    required  this.clusterer,

    required  this.manifestId,

     this.caveats,
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


  final NarrativeStatusEnum status;



      /// By velocity and age
  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final NarrativeStateEnum state;



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



  @JsonKey(
    
    name: r'envelope',
    required: true,
    includeIfNull: false,
  )


  final ObjectEnvelope envelope;



  @JsonKey(
    
    name: r'claims',
    required: true,
    includeIfNull: false,
  )


  final List<Claim> claims;



  @JsonKey(
    
    name: r'media',
    required: true,
    includeIfNull: false,
  )


  final List<MediaFingerprint> media;



  @JsonKey(
    
    name: r'coordination',
    required: false,
    includeIfNull: false,
  )


  final CoordinationIndicators? coordination;



      /// Linked 133 stories
  @JsonKey(
    
    name: r'newsStories',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectEnvelope>? newsStories;



      /// Linked 118 situations
  @JsonKey(
    
    name: r'situations',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectEnvelope>? situations;



      /// 137 comparisons of the narrative's claims
  @JsonKey(
    
    name: r'comparisonIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? comparisonIds;



  @JsonKey(
    
    name: r'history',
    required: true,
    includeIfNull: false,
  )


  final List<NarrativeLogEntry> history;



  @JsonKey(
    
    name: r'clusterer',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef clusterer;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: true,
    includeIfNull: false,
  )


  final String manifestId;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Narrative &&
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
      other.tlp == tlp &&
      other.envelope == envelope &&
      other.claims == claims &&
      other.media == media &&
      other.coordination == coordination &&
      other.newsStories == newsStories &&
      other.situations == situations &&
      other.comparisonIds == comparisonIds &&
      other.history == history &&
      other.clusterer == clusterer &&
      other.manifestId == manifestId &&
      other.caveats == caveats;

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
        tlp.hashCode +
        envelope.hashCode +
        claims.hashCode +
        media.hashCode +
        (coordination == null ? 0 : coordination.hashCode) +
        newsStories.hashCode +
        situations.hashCode +
        comparisonIds.hashCode +
        history.hashCode +
        clusterer.hashCode +
        manifestId.hashCode +
        caveats.hashCode;

  factory Narrative.fromJson(Map<String, dynamic> json) => _$NarrativeFromJson(json);

  Map<String, dynamic> toJson() => _$NarrativeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Set from 137 comparisons; describes the relationship to independent evidence, never truth
enum NarrativeStatusEnum {
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

const NarrativeStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


/// By velocity and age
enum NarrativeStateEnum {
@JsonValue(r'emerging')
emerging(r'emerging'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'fading')
fading(r'fading'),
@JsonValue(r'dormant')
dormant(r'dormant');

const NarrativeStateEnum(this.value);

final String value;

@override
String toString() => value;
}


