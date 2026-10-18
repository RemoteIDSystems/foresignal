//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_author.dart';
import 'package:foresignal_api/src/model/social_post_link.dart';
import 'package:foresignal_api/src/model/social_engagement.dart';
import 'package:foresignal_api/src/model/social_collection_type.dart';
import 'package:foresignal_api/src/model/social_post_geo.dart';
import 'package:foresignal_api/src/model/social_post_media.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_post.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPost {
  /// Returns a new [SocialPost] instance.
  SocialPost({

    required  this.platform,

    required  this.postId,

     this.url,

    required  this.author,

    required  this.authorKey,

     this.text,

     this.language,

    required  this.createdAt,

    required  this.observedAt,

     this.replyTo,

     this.quoteOf,

     this.repostOf,

     this.links,

     this.hashtags,

     this.mentions,

     this.media,

     this.engagement,

     this.geo,

     this.channel,

    required  this.collection,

     this.topicIds,

     this.cueId,

    required  this.deleted,

    required  this.ref,

    required  this.sourceId,

    required  this.externalId,

    required  this.seq,

     this.version,

     this.collectionType,

     this.narrativeIds,
  });

      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final String platform;



      /// Platform post id (Bluesky AT-URI rkey path, Mastodon `{instance}/{id}`, …)
  @JsonKey(
    
    name: r'postId',
    required: true,
    includeIfNull: false,
  )


  final String postId;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'author',
    required: true,
    includeIfNull: false,
  )


  final SocialAuthor author;



      /// HMAC-SHA256 of `{platform}:{accountId}` with the instance social salt; used for distinct-author counts and coordination statistics
  @JsonKey(
    
    name: r'authorKey',
    required: true,
    includeIfNull: false,
  )


  final String authorKey;



      /// Null once deleted (tombstoned)
  @JsonKey(
    
    name: r'text',
    required: false,
    includeIfNull: false,
  )


  final String? text;



      /// BCP 47; platform-supplied, else detected (Lingua, restricted to `social.languages`)
  @JsonKey(
    
    name: r'language',
    required: false,
    includeIfNull: false,
  )


  final String? language;



      /// Platform creation time
  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



      /// When Foresignal saw the post
  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



  @JsonKey(
    
    name: r'replyTo',
    required: false,
    includeIfNull: false,
  )


  final SocialPostLink? replyTo;



  @JsonKey(
    
    name: r'quoteOf',
    required: false,
    includeIfNull: false,
  )


  final SocialPostLink? quoteOf;



  @JsonKey(
    
    name: r'repostOf',
    required: false,
    includeIfNull: false,
  )


  final SocialPostLink? repostOf;



      /// Expanded URLs
  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final List<String>? links;



  @JsonKey(
    
    name: r'hashtags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? hashtags;



      /// Mentioned handles
  @JsonKey(
    
    name: r'mentions',
    required: false,
    includeIfNull: false,
  )


  final List<String>? mentions;



  @JsonKey(
    
    name: r'media',
    required: false,
    includeIfNull: false,
  )


  final List<SocialPostMedia>? media;



  @JsonKey(
    
    name: r'engagement',
    required: false,
    includeIfNull: false,
  )


  final SocialEngagement? engagement;



  @JsonKey(
    
    name: r'geo',
    required: false,
    includeIfNull: false,
  )


  final SocialPostGeo? geo;



      /// Subreddit, Mastodon instance or YouTube channel
  @JsonKey(
    
    name: r'channel',
    required: false,
    includeIfNull: false,
  )


  final String? channel;



      /// How the post was collected; `cue` posts (137) are a targeted sample and never feed baselines
  @JsonKey(
    
    name: r'collection',
    required: true,
    includeIfNull: false,
  )


  final SocialPostCollectionEnum collection;



      /// 117 attention topics the post matched
  @JsonKey(
    
    name: r'topicIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? topicIds;



      /// 137 cue that found the post (`collection = cue`)
  @JsonKey(
    
    name: r'cueId',
    required: false,
    includeIfNull: false,
  )


  final String? cueId;



      /// Tombstoned by deletion sync: text and media links removed, aggregates kept
  @JsonKey(
    
    name: r'deleted',
    required: true,
    includeIfNull: false,
  )


  final bool deleted;



      /// Record ref `record:{sourceId}/{platform}%3A{postId}`
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



      /// Collecting source (adapter id or 024 custom source)
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
    
    name: r'seq',
    required: true,
    includeIfNull: false,
  )


  final int seq;



  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;



  @JsonKey(
    
    name: r'collectionType',
    required: false,
    includeIfNull: false,
  )


  final SocialCollectionType? collectionType;



      /// 136 narratives the post belongs to
  @JsonKey(
    
    name: r'narrativeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? narrativeIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPost &&
      other.platform == platform &&
      other.postId == postId &&
      other.url == url &&
      other.author == author &&
      other.authorKey == authorKey &&
      other.text == text &&
      other.language == language &&
      other.createdAt == createdAt &&
      other.observedAt == observedAt &&
      other.replyTo == replyTo &&
      other.quoteOf == quoteOf &&
      other.repostOf == repostOf &&
      other.links == links &&
      other.hashtags == hashtags &&
      other.mentions == mentions &&
      other.media == media &&
      other.engagement == engagement &&
      other.geo == geo &&
      other.channel == channel &&
      other.collection == collection &&
      other.topicIds == topicIds &&
      other.cueId == cueId &&
      other.deleted == deleted &&
      other.ref == ref &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.seq == seq &&
      other.version == version &&
      other.collectionType == collectionType &&
      other.narrativeIds == narrativeIds;

    @override
    int get hashCode =>
        platform.hashCode +
        postId.hashCode +
        (url == null ? 0 : url.hashCode) +
        author.hashCode +
        authorKey.hashCode +
        (text == null ? 0 : text.hashCode) +
        (language == null ? 0 : language.hashCode) +
        createdAt.hashCode +
        observedAt.hashCode +
        (replyTo == null ? 0 : replyTo.hashCode) +
        (quoteOf == null ? 0 : quoteOf.hashCode) +
        (repostOf == null ? 0 : repostOf.hashCode) +
        links.hashCode +
        hashtags.hashCode +
        mentions.hashCode +
        media.hashCode +
        (engagement == null ? 0 : engagement.hashCode) +
        (geo == null ? 0 : geo.hashCode) +
        (channel == null ? 0 : channel.hashCode) +
        collection.hashCode +
        topicIds.hashCode +
        (cueId == null ? 0 : cueId.hashCode) +
        deleted.hashCode +
        ref.hashCode +
        sourceId.hashCode +
        externalId.hashCode +
        seq.hashCode +
        version.hashCode +
        collectionType.hashCode +
        narrativeIds.hashCode;

  factory SocialPost.fromJson(Map<String, dynamic> json) => _$SocialPostFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPostToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// How the post was collected; `cue` posts (137) are a targeted sample and never feed baselines
enum SocialPostCollectionEnum {
@JsonValue(r'stream')
stream(r'stream'),
@JsonValue(r'search')
search(r'search'),
@JsonValue(r'sample')
sample(r'sample'),
@JsonValue(r'cue')
cue(r'cue'),
@JsonValue(r'import')
import_(r'import');

const SocialPostCollectionEnum(this.value);

final String value;

@override
String toString() => value;
}


