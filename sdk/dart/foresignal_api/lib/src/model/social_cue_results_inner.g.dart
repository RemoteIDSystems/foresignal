// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_cue_results_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialCueResultsInnerCWProxy {
  SocialCueResultsInner platform(SocialPlatformId platform);

  SocialCueResultsInner posts(int posts);

  SocialCueResultsInner newPosts(int? newPosts);

  SocialCueResultsInner costUsd(num? costUsd);

  SocialCueResultsInner error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCueResultsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCueResultsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialCueResultsInner call({
    SocialPlatformId platform,
    int posts,
    int? newPosts,
    num? costUsd,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialCueResultsInner.copyWith(...)` or call `instanceOfSocialCueResultsInner.copyWith.fieldName(value)` for a single field.
class _$SocialCueResultsInnerCWProxyImpl
    implements _$SocialCueResultsInnerCWProxy {
  const _$SocialCueResultsInnerCWProxyImpl(this._value);

  final SocialCueResultsInner _value;

  @override
  SocialCueResultsInner platform(SocialPlatformId platform) =>
      call(platform: platform);

  @override
  SocialCueResultsInner posts(int posts) => call(posts: posts);

  @override
  SocialCueResultsInner newPosts(int? newPosts) => call(newPosts: newPosts);

  @override
  SocialCueResultsInner costUsd(num? costUsd) => call(costUsd: costUsd);

  @override
  SocialCueResultsInner error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCueResultsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCueResultsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialCueResultsInner call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? posts = const $CopyWithPlaceholder(),
    Object? newPosts = const $CopyWithPlaceholder(),
    Object? costUsd = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return SocialCueResultsInner(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as SocialPlatformId,
      posts: posts == const $CopyWithPlaceholder() || posts == null
          ? _value.posts
          // ignore: cast_nullable_to_non_nullable
          : posts as int,
      newPosts: newPosts == const $CopyWithPlaceholder()
          ? _value.newPosts
          // ignore: cast_nullable_to_non_nullable
          : newPosts as int?,
      costUsd: costUsd == const $CopyWithPlaceholder()
          ? _value.costUsd
          // ignore: cast_nullable_to_non_nullable
          : costUsd as num?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $SocialCueResultsInnerCopyWith on SocialCueResultsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialCueResultsInner.copyWith(...)` or `instanceOfSocialCueResultsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialCueResultsInnerCWProxy get copyWith =>
      _$SocialCueResultsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialCueResultsInner _$SocialCueResultsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialCueResultsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['platform', 'posts']);
  final val = SocialCueResultsInner(
    platform: $checkedConvert(
      'platform',
      (v) => $enumDecode(_$SocialPlatformIdEnumMap, v),
    ),
    posts: $checkedConvert('posts', (v) => (v as num).toInt()),
    newPosts: $checkedConvert('newPosts', (v) => (v as num?)?.toInt()),
    costUsd: $checkedConvert('costUsd', (v) => v as num?),
    error: $checkedConvert('error', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$SocialCueResultsInnerToJson(
  SocialCueResultsInner instance,
) => <String, dynamic>{
  'platform': _$SocialPlatformIdEnumMap[instance.platform]!,
  'posts': instance.posts,
  'newPosts': ?instance.newPosts,
  'costUsd': ?instance.costUsd,
  'error': ?instance.error,
};

const _$SocialPlatformIdEnumMap = {
  SocialPlatformId.blueskyJetstream: 'bluesky-jetstream',
  SocialPlatformId.blueskySearch: 'bluesky-search',
  SocialPlatformId.mastodon: 'mastodon',
  SocialPlatformId.reddit: 'reddit',
  SocialPlatformId.youtube: 'youtube',
  SocialPlatformId.x: 'x',
  SocialPlatformId.threads: 'threads',
  SocialPlatformId.instagramHashtags: 'instagram-hashtags',
};
