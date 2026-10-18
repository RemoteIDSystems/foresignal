//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'news_article.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NewsArticle {
  /// Returns a new [NewsArticle] instance.
  NewsArticle({

    required  this.ref,

    required  this.externalId,

    required  this.title,

    required  this.url,

    required  this.domain,

     this.outletId,

     this.outletName,

     this.language,

     this.sourceCountry,

    required  this.seenAt,

     this.publishedAt,

     this.topicIds,

     this.summary,

     this.imageUrl,

     this.storyId,

     this.evidenceGroupId,

     this.syndicated,

    required  this.sourceId,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



      /// sha256 of the canonicalised URL
  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'url',
    required: true,
    includeIfNull: false,
  )


  final String url;



  @JsonKey(
    
    name: r'domain',
    required: true,
    includeIfNull: false,
  )


  final String domain;



  @JsonKey(
    
    name: r'outletId',
    required: false,
    includeIfNull: false,
  )


  final String? outletId;



  @JsonKey(
    
    name: r'outletName',
    required: false,
    includeIfNull: false,
  )


  final String? outletName;



  @JsonKey(
    
    name: r'language',
    required: false,
    includeIfNull: false,
  )


  final String? language;



  @JsonKey(
    
    name: r'sourceCountry',
    required: false,
    includeIfNull: false,
  )


  final String? sourceCountry;



  @JsonKey(
    
    name: r'seenAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime seenAt;



  @JsonKey(
    
    name: r'publishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? publishedAt;



  @JsonKey(
    
    name: r'topicIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? topicIds;



      /// Feed summary only
  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;



      /// Link only
  @JsonKey(
    
    name: r'imageUrl',
    required: false,
    includeIfNull: false,
  )


  final String? imageUrl;



      /// ULID identifier
  @JsonKey(
    
    name: r'storyId',
    required: false,
    includeIfNull: false,
  )


  final String? storyId;



  @JsonKey(
    
    name: r'evidenceGroupId',
    required: false,
    includeIfNull: false,
  )


  final String? evidenceGroupId;



      /// Reprint of a wire story
  @JsonKey(
    
    name: r'syndicated',
    required: false,
    includeIfNull: false,
  )


  final bool? syndicated;



      /// `gdelt-articles` or a `news-feed-*` source
  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NewsArticle &&
      other.ref == ref &&
      other.externalId == externalId &&
      other.title == title &&
      other.url == url &&
      other.domain == domain &&
      other.outletId == outletId &&
      other.outletName == outletName &&
      other.language == language &&
      other.sourceCountry == sourceCountry &&
      other.seenAt == seenAt &&
      other.publishedAt == publishedAt &&
      other.topicIds == topicIds &&
      other.summary == summary &&
      other.imageUrl == imageUrl &&
      other.storyId == storyId &&
      other.evidenceGroupId == evidenceGroupId &&
      other.syndicated == syndicated &&
      other.sourceId == sourceId;

    @override
    int get hashCode =>
        ref.hashCode +
        externalId.hashCode +
        title.hashCode +
        url.hashCode +
        domain.hashCode +
        (outletId == null ? 0 : outletId.hashCode) +
        (outletName == null ? 0 : outletName.hashCode) +
        (language == null ? 0 : language.hashCode) +
        (sourceCountry == null ? 0 : sourceCountry.hashCode) +
        seenAt.hashCode +
        (publishedAt == null ? 0 : publishedAt.hashCode) +
        topicIds.hashCode +
        (summary == null ? 0 : summary.hashCode) +
        (imageUrl == null ? 0 : imageUrl.hashCode) +
        (storyId == null ? 0 : storyId.hashCode) +
        (evidenceGroupId == null ? 0 : evidenceGroupId.hashCode) +
        syndicated.hashCode +
        sourceId.hashCode;

  factory NewsArticle.fromJson(Map<String, dynamic> json) => _$NewsArticleFromJson(json);

  Map<String, dynamic> toJson() => _$NewsArticleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

