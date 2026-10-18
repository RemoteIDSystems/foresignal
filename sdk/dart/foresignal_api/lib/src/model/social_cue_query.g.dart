// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_cue_query.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialCueQueryCWProxy {
  SocialCueQuery platform(SocialPlatformId platform);

  SocialCueQuery query(String query);

  SocialCueQuery language(String? language);

  SocialCueQuery estimatedCostUsd(num? estimatedCostUsd);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCueQuery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCueQuery(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialCueQuery call({
    SocialPlatformId platform,
    String query,
    String? language,
    num? estimatedCostUsd,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialCueQuery.copyWith(...)` or call `instanceOfSocialCueQuery.copyWith.fieldName(value)` for a single field.
class _$SocialCueQueryCWProxyImpl implements _$SocialCueQueryCWProxy {
  const _$SocialCueQueryCWProxyImpl(this._value);

  final SocialCueQuery _value;

  @override
  SocialCueQuery platform(SocialPlatformId platform) =>
      call(platform: platform);

  @override
  SocialCueQuery query(String query) => call(query: query);

  @override
  SocialCueQuery language(String? language) => call(language: language);

  @override
  SocialCueQuery estimatedCostUsd(num? estimatedCostUsd) =>
      call(estimatedCostUsd: estimatedCostUsd);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialCueQuery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialCueQuery(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialCueQuery call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? query = const $CopyWithPlaceholder(),
    Object? language = const $CopyWithPlaceholder(),
    Object? estimatedCostUsd = const $CopyWithPlaceholder(),
  }) {
    return SocialCueQuery(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as SocialPlatformId,
      query: query == const $CopyWithPlaceholder() || query == null
          ? _value.query
          // ignore: cast_nullable_to_non_nullable
          : query as String,
      language: language == const $CopyWithPlaceholder()
          ? _value.language
          // ignore: cast_nullable_to_non_nullable
          : language as String?,
      estimatedCostUsd: estimatedCostUsd == const $CopyWithPlaceholder()
          ? _value.estimatedCostUsd
          // ignore: cast_nullable_to_non_nullable
          : estimatedCostUsd as num?,
    );
  }
}

extension $SocialCueQueryCopyWith on SocialCueQuery {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialCueQuery.copyWith(...)` or `instanceOfSocialCueQuery.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialCueQueryCWProxy get copyWith => _$SocialCueQueryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialCueQuery _$SocialCueQueryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialCueQuery', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['platform', 'query']);
      final val = SocialCueQuery(
        platform: $checkedConvert(
          'platform',
          (v) => $enumDecode(_$SocialPlatformIdEnumMap, v),
        ),
        query: $checkedConvert('query', (v) => v as String),
        language: $checkedConvert('language', (v) => v as String?),
        estimatedCostUsd: $checkedConvert('estimatedCostUsd', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$SocialCueQueryToJson(SocialCueQuery instance) =>
    <String, dynamic>{
      'platform': _$SocialPlatformIdEnumMap[instance.platform]!,
      'query': instance.query,
      'language': ?instance.language,
      'estimatedCostUsd': ?instance.estimatedCostUsd,
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
