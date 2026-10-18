// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_post.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPostCWProxy {
  SocialPost platform(String platform);

  SocialPost postId(String postId);

  SocialPost url(String? url);

  SocialPost author(SocialAuthor author);

  SocialPost authorKey(String authorKey);

  SocialPost text(String? text);

  SocialPost language(String? language);

  SocialPost createdAt(DateTime createdAt);

  SocialPost observedAt(DateTime observedAt);

  SocialPost replyTo(SocialPostLink? replyTo);

  SocialPost quoteOf(SocialPostLink? quoteOf);

  SocialPost repostOf(SocialPostLink? repostOf);

  SocialPost links(List<String>? links);

  SocialPost hashtags(List<String>? hashtags);

  SocialPost mentions(List<String>? mentions);

  SocialPost media(List<SocialPostMedia>? media);

  SocialPost engagement(SocialEngagement? engagement);

  SocialPost geo(SocialPostGeo? geo);

  SocialPost channel(String? channel);

  SocialPost collection(SocialPostCollectionEnum collection);

  SocialPost topicIds(List<String>? topicIds);

  SocialPost cueId(String? cueId);

  SocialPost deleted(bool deleted);

  SocialPost ref(String ref);

  SocialPost sourceId(String sourceId);

  SocialPost externalId(String externalId);

  SocialPost seq(int seq);

  SocialPost version(int? version);

  SocialPost collectionType(SocialCollectionType? collectionType);

  SocialPost narrativeIds(List<String>? narrativeIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPost(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPost(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPost call({
    String platform,
    String postId,
    String? url,
    SocialAuthor author,
    String authorKey,
    String? text,
    String? language,
    DateTime createdAt,
    DateTime observedAt,
    SocialPostLink? replyTo,
    SocialPostLink? quoteOf,
    SocialPostLink? repostOf,
    List<String>? links,
    List<String>? hashtags,
    List<String>? mentions,
    List<SocialPostMedia>? media,
    SocialEngagement? engagement,
    SocialPostGeo? geo,
    String? channel,
    SocialPostCollectionEnum collection,
    List<String>? topicIds,
    String? cueId,
    bool deleted,
    String ref,
    String sourceId,
    String externalId,
    int seq,
    int? version,
    SocialCollectionType? collectionType,
    List<String>? narrativeIds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPost.copyWith(...)` or call `instanceOfSocialPost.copyWith.fieldName(value)` for a single field.
class _$SocialPostCWProxyImpl implements _$SocialPostCWProxy {
  const _$SocialPostCWProxyImpl(this._value);

  final SocialPost _value;

  @override
  SocialPost platform(String platform) => call(platform: platform);

  @override
  SocialPost postId(String postId) => call(postId: postId);

  @override
  SocialPost url(String? url) => call(url: url);

  @override
  SocialPost author(SocialAuthor author) => call(author: author);

  @override
  SocialPost authorKey(String authorKey) => call(authorKey: authorKey);

  @override
  SocialPost text(String? text) => call(text: text);

  @override
  SocialPost language(String? language) => call(language: language);

  @override
  SocialPost createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  SocialPost observedAt(DateTime observedAt) => call(observedAt: observedAt);

  @override
  SocialPost replyTo(SocialPostLink? replyTo) => call(replyTo: replyTo);

  @override
  SocialPost quoteOf(SocialPostLink? quoteOf) => call(quoteOf: quoteOf);

  @override
  SocialPost repostOf(SocialPostLink? repostOf) => call(repostOf: repostOf);

  @override
  SocialPost links(List<String>? links) => call(links: links);

  @override
  SocialPost hashtags(List<String>? hashtags) => call(hashtags: hashtags);

  @override
  SocialPost mentions(List<String>? mentions) => call(mentions: mentions);

  @override
  SocialPost media(List<SocialPostMedia>? media) => call(media: media);

  @override
  SocialPost engagement(SocialEngagement? engagement) =>
      call(engagement: engagement);

  @override
  SocialPost geo(SocialPostGeo? geo) => call(geo: geo);

  @override
  SocialPost channel(String? channel) => call(channel: channel);

  @override
  SocialPost collection(SocialPostCollectionEnum collection) =>
      call(collection: collection);

  @override
  SocialPost topicIds(List<String>? topicIds) => call(topicIds: topicIds);

  @override
  SocialPost cueId(String? cueId) => call(cueId: cueId);

  @override
  SocialPost deleted(bool deleted) => call(deleted: deleted);

  @override
  SocialPost ref(String ref) => call(ref: ref);

  @override
  SocialPost sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  SocialPost externalId(String externalId) => call(externalId: externalId);

  @override
  SocialPost seq(int seq) => call(seq: seq);

  @override
  SocialPost version(int? version) => call(version: version);

  @override
  SocialPost collectionType(SocialCollectionType? collectionType) =>
      call(collectionType: collectionType);

  @override
  SocialPost narrativeIds(List<String>? narrativeIds) =>
      call(narrativeIds: narrativeIds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPost(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPost(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPost call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? postId = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? author = const $CopyWithPlaceholder(),
    Object? authorKey = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? language = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? replyTo = const $CopyWithPlaceholder(),
    Object? quoteOf = const $CopyWithPlaceholder(),
    Object? repostOf = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? hashtags = const $CopyWithPlaceholder(),
    Object? mentions = const $CopyWithPlaceholder(),
    Object? media = const $CopyWithPlaceholder(),
    Object? engagement = const $CopyWithPlaceholder(),
    Object? geo = const $CopyWithPlaceholder(),
    Object? channel = const $CopyWithPlaceholder(),
    Object? collection = const $CopyWithPlaceholder(),
    Object? topicIds = const $CopyWithPlaceholder(),
    Object? cueId = const $CopyWithPlaceholder(),
    Object? deleted = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? collectionType = const $CopyWithPlaceholder(),
    Object? narrativeIds = const $CopyWithPlaceholder(),
  }) {
    return SocialPost(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      postId: postId == const $CopyWithPlaceholder() || postId == null
          ? _value.postId
          // ignore: cast_nullable_to_non_nullable
          : postId as String,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      author: author == const $CopyWithPlaceholder() || author == null
          ? _value.author
          // ignore: cast_nullable_to_non_nullable
          : author as SocialAuthor,
      authorKey: authorKey == const $CopyWithPlaceholder() || authorKey == null
          ? _value.authorKey
          // ignore: cast_nullable_to_non_nullable
          : authorKey as String,
      text: text == const $CopyWithPlaceholder()
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String?,
      language: language == const $CopyWithPlaceholder()
          ? _value.language
          // ignore: cast_nullable_to_non_nullable
          : language as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      replyTo: replyTo == const $CopyWithPlaceholder()
          ? _value.replyTo
          // ignore: cast_nullable_to_non_nullable
          : replyTo as SocialPostLink?,
      quoteOf: quoteOf == const $CopyWithPlaceholder()
          ? _value.quoteOf
          // ignore: cast_nullable_to_non_nullable
          : quoteOf as SocialPostLink?,
      repostOf: repostOf == const $CopyWithPlaceholder()
          ? _value.repostOf
          // ignore: cast_nullable_to_non_nullable
          : repostOf as SocialPostLink?,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<String>?,
      hashtags: hashtags == const $CopyWithPlaceholder()
          ? _value.hashtags
          // ignore: cast_nullable_to_non_nullable
          : hashtags as List<String>?,
      mentions: mentions == const $CopyWithPlaceholder()
          ? _value.mentions
          // ignore: cast_nullable_to_non_nullable
          : mentions as List<String>?,
      media: media == const $CopyWithPlaceholder()
          ? _value.media
          // ignore: cast_nullable_to_non_nullable
          : media as List<SocialPostMedia>?,
      engagement: engagement == const $CopyWithPlaceholder()
          ? _value.engagement
          // ignore: cast_nullable_to_non_nullable
          : engagement as SocialEngagement?,
      geo: geo == const $CopyWithPlaceholder()
          ? _value.geo
          // ignore: cast_nullable_to_non_nullable
          : geo as SocialPostGeo?,
      channel: channel == const $CopyWithPlaceholder()
          ? _value.channel
          // ignore: cast_nullable_to_non_nullable
          : channel as String?,
      collection:
          collection == const $CopyWithPlaceholder() || collection == null
          ? _value.collection
          // ignore: cast_nullable_to_non_nullable
          : collection as SocialPostCollectionEnum,
      topicIds: topicIds == const $CopyWithPlaceholder()
          ? _value.topicIds
          // ignore: cast_nullable_to_non_nullable
          : topicIds as List<String>?,
      cueId: cueId == const $CopyWithPlaceholder()
          ? _value.cueId
          // ignore: cast_nullable_to_non_nullable
          : cueId as String?,
      deleted: deleted == const $CopyWithPlaceholder() || deleted == null
          ? _value.deleted
          // ignore: cast_nullable_to_non_nullable
          : deleted as bool,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
      collectionType: collectionType == const $CopyWithPlaceholder()
          ? _value.collectionType
          // ignore: cast_nullable_to_non_nullable
          : collectionType as SocialCollectionType?,
      narrativeIds: narrativeIds == const $CopyWithPlaceholder()
          ? _value.narrativeIds
          // ignore: cast_nullable_to_non_nullable
          : narrativeIds as List<String>?,
    );
  }
}

extension $SocialPostCopyWith on SocialPost {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPost.copyWith(...)` or `instanceOfSocialPost.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPostCWProxy get copyWith => _$SocialPostCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPost _$SocialPostFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialPost', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'platform',
      'postId',
      'author',
      'authorKey',
      'createdAt',
      'observedAt',
      'collection',
      'deleted',
      'ref',
      'sourceId',
      'externalId',
      'seq',
    ],
  );
  final val = SocialPost(
    platform: $checkedConvert('platform', (v) => v as String),
    postId: $checkedConvert('postId', (v) => v as String),
    url: $checkedConvert('url', (v) => v as String?),
    author: $checkedConvert(
      'author',
      (v) => SocialAuthor.fromJson(v as Map<String, dynamic>),
    ),
    authorKey: $checkedConvert('authorKey', (v) => v as String),
    text: $checkedConvert('text', (v) => v as String?),
    language: $checkedConvert('language', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => DateTime.parse(v as String),
    ),
    replyTo: $checkedConvert(
      'replyTo',
      (v) =>
          v == null ? null : SocialPostLink.fromJson(v as Map<String, dynamic>),
    ),
    quoteOf: $checkedConvert(
      'quoteOf',
      (v) =>
          v == null ? null : SocialPostLink.fromJson(v as Map<String, dynamic>),
    ),
    repostOf: $checkedConvert(
      'repostOf',
      (v) =>
          v == null ? null : SocialPostLink.fromJson(v as Map<String, dynamic>),
    ),
    links: $checkedConvert(
      'links',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    hashtags: $checkedConvert(
      'hashtags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    mentions: $checkedConvert(
      'mentions',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    media: $checkedConvert(
      'media',
      (v) => (v as List<dynamic>?)
          ?.map((e) => SocialPostMedia.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    engagement: $checkedConvert(
      'engagement',
      (v) => v == null
          ? null
          : SocialEngagement.fromJson(v as Map<String, dynamic>),
    ),
    geo: $checkedConvert(
      'geo',
      (v) =>
          v == null ? null : SocialPostGeo.fromJson(v as Map<String, dynamic>),
    ),
    channel: $checkedConvert('channel', (v) => v as String?),
    collection: $checkedConvert(
      'collection',
      (v) => $enumDecode(_$SocialPostCollectionEnumEnumMap, v),
    ),
    topicIds: $checkedConvert(
      'topicIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    cueId: $checkedConvert('cueId', (v) => v as String?),
    deleted: $checkedConvert('deleted', (v) => v as bool),
    ref: $checkedConvert('ref', (v) => v as String),
    sourceId: $checkedConvert('sourceId', (v) => v as String),
    externalId: $checkedConvert('externalId', (v) => v as String),
    seq: $checkedConvert('seq', (v) => (v as num).toInt()),
    version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
    collectionType: $checkedConvert(
      'collectionType',
      (v) => $enumDecodeNullable(_$SocialCollectionTypeEnumMap, v),
    ),
    narrativeIds: $checkedConvert(
      'narrativeIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SocialPostToJson(SocialPost instance) =>
    <String, dynamic>{
      'platform': instance.platform,
      'postId': instance.postId,
      'url': ?instance.url,
      'author': instance.author.toJson(),
      'authorKey': instance.authorKey,
      'text': ?instance.text,
      'language': ?instance.language,
      'createdAt': instance.createdAt.toIso8601String(),
      'observedAt': instance.observedAt.toIso8601String(),
      'replyTo': ?instance.replyTo?.toJson(),
      'quoteOf': ?instance.quoteOf?.toJson(),
      'repostOf': ?instance.repostOf?.toJson(),
      'links': ?instance.links,
      'hashtags': ?instance.hashtags,
      'mentions': ?instance.mentions,
      'media': ?instance.media?.map((e) => e.toJson()).toList(),
      'engagement': ?instance.engagement?.toJson(),
      'geo': ?instance.geo?.toJson(),
      'channel': ?instance.channel,
      'collection': _$SocialPostCollectionEnumEnumMap[instance.collection]!,
      'topicIds': ?instance.topicIds,
      'cueId': ?instance.cueId,
      'deleted': instance.deleted,
      'ref': instance.ref,
      'sourceId': instance.sourceId,
      'externalId': instance.externalId,
      'seq': instance.seq,
      'version': ?instance.version,
      'collectionType': ?_$SocialCollectionTypeEnumMap[instance.collectionType],
      'narrativeIds': ?instance.narrativeIds,
    };

const _$SocialPostCollectionEnumEnumMap = {
  SocialPostCollectionEnum.stream: 'stream',
  SocialPostCollectionEnum.search: 'search',
  SocialPostCollectionEnum.sample: 'sample',
  SocialPostCollectionEnum.cue: 'cue',
  SocialPostCollectionEnum.import_: 'import',
};

const _$SocialCollectionTypeEnumMap = {
  SocialCollectionType.complete: 'complete',
  SocialCollectionType.paged: 'paged',
  SocialCollectionType.sampled: 'sampled',
};
