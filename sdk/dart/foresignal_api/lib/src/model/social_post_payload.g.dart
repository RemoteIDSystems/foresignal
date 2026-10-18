// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_post_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPostPayloadCWProxy {
  SocialPostPayload platform(String platform);

  SocialPostPayload postId(String postId);

  SocialPostPayload url(String? url);

  SocialPostPayload author(SocialAuthor author);

  SocialPostPayload authorKey(String authorKey);

  SocialPostPayload text(String? text);

  SocialPostPayload language(String? language);

  SocialPostPayload createdAt(DateTime createdAt);

  SocialPostPayload observedAt(DateTime observedAt);

  SocialPostPayload replyTo(SocialPostLink? replyTo);

  SocialPostPayload quoteOf(SocialPostLink? quoteOf);

  SocialPostPayload repostOf(SocialPostLink? repostOf);

  SocialPostPayload links(List<String>? links);

  SocialPostPayload hashtags(List<String>? hashtags);

  SocialPostPayload mentions(List<String>? mentions);

  SocialPostPayload media(List<SocialPostMedia>? media);

  SocialPostPayload engagement(SocialEngagement? engagement);

  SocialPostPayload geo(SocialPostGeo? geo);

  SocialPostPayload channel(String? channel);

  SocialPostPayload collection(SocialPostPayloadCollectionEnum collection);

  SocialPostPayload topicIds(List<String>? topicIds);

  SocialPostPayload cueId(String? cueId);

  SocialPostPayload deleted(bool deleted);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPostPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPostPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPostPayload call({
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
    SocialPostPayloadCollectionEnum collection,
    List<String>? topicIds,
    String? cueId,
    bool deleted,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPostPayload.copyWith(...)` or call `instanceOfSocialPostPayload.copyWith.fieldName(value)` for a single field.
class _$SocialPostPayloadCWProxyImpl implements _$SocialPostPayloadCWProxy {
  const _$SocialPostPayloadCWProxyImpl(this._value);

  final SocialPostPayload _value;

  @override
  SocialPostPayload platform(String platform) => call(platform: platform);

  @override
  SocialPostPayload postId(String postId) => call(postId: postId);

  @override
  SocialPostPayload url(String? url) => call(url: url);

  @override
  SocialPostPayload author(SocialAuthor author) => call(author: author);

  @override
  SocialPostPayload authorKey(String authorKey) => call(authorKey: authorKey);

  @override
  SocialPostPayload text(String? text) => call(text: text);

  @override
  SocialPostPayload language(String? language) => call(language: language);

  @override
  SocialPostPayload createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  SocialPostPayload observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  @override
  SocialPostPayload replyTo(SocialPostLink? replyTo) => call(replyTo: replyTo);

  @override
  SocialPostPayload quoteOf(SocialPostLink? quoteOf) => call(quoteOf: quoteOf);

  @override
  SocialPostPayload repostOf(SocialPostLink? repostOf) =>
      call(repostOf: repostOf);

  @override
  SocialPostPayload links(List<String>? links) => call(links: links);

  @override
  SocialPostPayload hashtags(List<String>? hashtags) =>
      call(hashtags: hashtags);

  @override
  SocialPostPayload mentions(List<String>? mentions) =>
      call(mentions: mentions);

  @override
  SocialPostPayload media(List<SocialPostMedia>? media) => call(media: media);

  @override
  SocialPostPayload engagement(SocialEngagement? engagement) =>
      call(engagement: engagement);

  @override
  SocialPostPayload geo(SocialPostGeo? geo) => call(geo: geo);

  @override
  SocialPostPayload channel(String? channel) => call(channel: channel);

  @override
  SocialPostPayload collection(SocialPostPayloadCollectionEnum collection) =>
      call(collection: collection);

  @override
  SocialPostPayload topicIds(List<String>? topicIds) =>
      call(topicIds: topicIds);

  @override
  SocialPostPayload cueId(String? cueId) => call(cueId: cueId);

  @override
  SocialPostPayload deleted(bool deleted) => call(deleted: deleted);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPostPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPostPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPostPayload call({
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
  }) {
    return SocialPostPayload(
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
          : collection as SocialPostPayloadCollectionEnum,
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
    );
  }
}

extension $SocialPostPayloadCopyWith on SocialPostPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPostPayload.copyWith(...)` or `instanceOfSocialPostPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPostPayloadCWProxy get copyWith =>
      _$SocialPostPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPostPayload _$SocialPostPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialPostPayload', json, ($checkedConvert) {
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
    ],
  );
  final val = SocialPostPayload(
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
      (v) => $enumDecode(_$SocialPostPayloadCollectionEnumEnumMap, v),
    ),
    topicIds: $checkedConvert(
      'topicIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    cueId: $checkedConvert('cueId', (v) => v as String?),
    deleted: $checkedConvert('deleted', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$SocialPostPayloadToJson(
  SocialPostPayload instance,
) => <String, dynamic>{
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
  'collection': _$SocialPostPayloadCollectionEnumEnumMap[instance.collection]!,
  'topicIds': ?instance.topicIds,
  'cueId': ?instance.cueId,
  'deleted': instance.deleted,
};

const _$SocialPostPayloadCollectionEnumEnumMap = {
  SocialPostPayloadCollectionEnum.stream: 'stream',
  SocialPostPayloadCollectionEnum.search: 'search',
  SocialPostPayloadCollectionEnum.sample: 'sample',
  SocialPostPayloadCollectionEnum.cue: 'cue',
  SocialPostPayloadCollectionEnum.import_: 'import',
};
