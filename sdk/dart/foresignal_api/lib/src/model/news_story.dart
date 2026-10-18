//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/news_story_outlets_inner.dart';
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/coverage_stats.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'news_story.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NewsStory {
  /// Returns a new [NewsStory] instance.
  NewsStory({

    required  this.id,

    required  this.ref,

    required  this.canonicalEventId,

    required  this.title,

    required  this.firstSeenAt,

    required  this.lastSeenAt,

    required  this.articleCount,

     this.outletCount,

    required  this.evidenceGroupCount,

     this.topicIds,

     this.regionCodes,

    required  this.coverage,

     this.blindspot,

    required  this.corroborated,

     this.situationIds,

     this.outlets,

     this.situations,

     this.canonicalEvents,
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
    
    name: r'canonicalEventId',
    required: true,
    includeIfNull: false,
  )


  final String canonicalEventId;



      /// Deterministic: the most central article's title
  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'firstSeenAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime firstSeenAt;



  @JsonKey(
    
    name: r'lastSeenAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime lastSeenAt;



  @JsonKey(
    
    name: r'articleCount',
    required: true,
    includeIfNull: false,
  )


  final int articleCount;



  @JsonKey(
    
    name: r'outletCount',
    required: false,
    includeIfNull: false,
  )


  final int? outletCount;



  @JsonKey(
    
    name: r'evidenceGroupCount',
    required: true,
    includeIfNull: false,
  )


  final int evidenceGroupCount;



  @JsonKey(
    
    name: r'topicIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? topicIds;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



  @JsonKey(
    
    name: r'coverage',
    required: true,
    includeIfNull: false,
  )


  final CoverageStats coverage;



  @JsonKey(
    
    name: r'blindspot',
    required: false,
    includeIfNull: false,
  )


  final NewsStoryBlindspotEnum? blindspot;



  @JsonKey(
    
    name: r'corroborated',
    required: true,
    includeIfNull: false,
  )


  final bool corroborated;



  @JsonKey(
    
    name: r'situationIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? situationIds;



  @JsonKey(
    
    name: r'outlets',
    required: false,
    includeIfNull: false,
  )


  final List<NewsStoryOutletsInner>? outlets;



      /// Detail view
  @JsonKey(
    
    name: r'situations',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectEnvelope>? situations;



      /// Detail view
  @JsonKey(
    
    name: r'canonicalEvents',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectEnvelope>? canonicalEvents;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NewsStory &&
      other.id == id &&
      other.ref == ref &&
      other.canonicalEventId == canonicalEventId &&
      other.title == title &&
      other.firstSeenAt == firstSeenAt &&
      other.lastSeenAt == lastSeenAt &&
      other.articleCount == articleCount &&
      other.outletCount == outletCount &&
      other.evidenceGroupCount == evidenceGroupCount &&
      other.topicIds == topicIds &&
      other.regionCodes == regionCodes &&
      other.coverage == coverage &&
      other.blindspot == blindspot &&
      other.corroborated == corroborated &&
      other.situationIds == situationIds &&
      other.outlets == outlets &&
      other.situations == situations &&
      other.canonicalEvents == canonicalEvents;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        canonicalEventId.hashCode +
        title.hashCode +
        firstSeenAt.hashCode +
        lastSeenAt.hashCode +
        articleCount.hashCode +
        outletCount.hashCode +
        evidenceGroupCount.hashCode +
        topicIds.hashCode +
        regionCodes.hashCode +
        coverage.hashCode +
        (blindspot == null ? 0 : blindspot.hashCode) +
        corroborated.hashCode +
        situationIds.hashCode +
        outlets.hashCode +
        situations.hashCode +
        canonicalEvents.hashCode;

  factory NewsStory.fromJson(Map<String, dynamic> json) => _$NewsStoryFromJson(json);

  Map<String, dynamic> toJson() => _$NewsStoryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NewsStoryBlindspotEnum {
@JsonValue(r'left')
left(r'left'),
@JsonValue(r'right')
right(r'right');

const NewsStoryBlindspotEnum(this.value);

final String value;

@override
String toString() => value;
}


