// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'narrative_post.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NarrativePostCWProxy {
  NarrativePost post(SocialPost post);

  NarrativePost linkReason(NarrativePostLinkReasonEnum linkReason);

  NarrativePost evidenceGroupId(String evidenceGroupId);

  NarrativePost joinedAt(DateTime joinedAt);

  NarrativePost centrality(num? centrality);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativePost(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativePost(...).copyWith(id: 12, name: "My name")
  /// ```
  NarrativePost call({
    SocialPost post,
    NarrativePostLinkReasonEnum linkReason,
    String evidenceGroupId,
    DateTime joinedAt,
    num? centrality,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNarrativePost.copyWith(...)` or call `instanceOfNarrativePost.copyWith.fieldName(value)` for a single field.
class _$NarrativePostCWProxyImpl implements _$NarrativePostCWProxy {
  const _$NarrativePostCWProxyImpl(this._value);

  final NarrativePost _value;

  @override
  NarrativePost post(SocialPost post) => call(post: post);

  @override
  NarrativePost linkReason(NarrativePostLinkReasonEnum linkReason) =>
      call(linkReason: linkReason);

  @override
  NarrativePost evidenceGroupId(String evidenceGroupId) =>
      call(evidenceGroupId: evidenceGroupId);

  @override
  NarrativePost joinedAt(DateTime joinedAt) => call(joinedAt: joinedAt);

  @override
  NarrativePost centrality(num? centrality) => call(centrality: centrality);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NarrativePost(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NarrativePost(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NarrativePost call({
    Object? post = const $CopyWithPlaceholder(),
    Object? linkReason = const $CopyWithPlaceholder(),
    Object? evidenceGroupId = const $CopyWithPlaceholder(),
    Object? joinedAt = const $CopyWithPlaceholder(),
    Object? centrality = const $CopyWithPlaceholder(),
  }) {
    return NarrativePost(
      post: post == const $CopyWithPlaceholder() || post == null
          ? _value.post
          // ignore: cast_nullable_to_non_nullable
          : post as SocialPost,
      linkReason:
          linkReason == const $CopyWithPlaceholder() || linkReason == null
          ? _value.linkReason
          // ignore: cast_nullable_to_non_nullable
          : linkReason as NarrativePostLinkReasonEnum,
      evidenceGroupId:
          evidenceGroupId == const $CopyWithPlaceholder() ||
              evidenceGroupId == null
          ? _value.evidenceGroupId
          // ignore: cast_nullable_to_non_nullable
          : evidenceGroupId as String,
      joinedAt: joinedAt == const $CopyWithPlaceholder() || joinedAt == null
          ? _value.joinedAt
          // ignore: cast_nullable_to_non_nullable
          : joinedAt as DateTime,
      centrality: centrality == const $CopyWithPlaceholder()
          ? _value.centrality
          // ignore: cast_nullable_to_non_nullable
          : centrality as num?,
    );
  }
}

extension $NarrativePostCopyWith on NarrativePost {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNarrativePost.copyWith(...)` or `instanceOfNarrativePost.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NarrativePostCWProxy get copyWith => _$NarrativePostCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NarrativePost _$NarrativePostFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NarrativePost', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['post', 'linkReason', 'evidenceGroupId', 'joinedAt'],
  );
  final val = NarrativePost(
    post: $checkedConvert(
      'post',
      (v) => SocialPost.fromJson(v as Map<String, dynamic>),
    ),
    linkReason: $checkedConvert(
      'linkReason',
      (v) => $enumDecode(_$NarrativePostLinkReasonEnumEnumMap, v),
    ),
    evidenceGroupId: $checkedConvert('evidenceGroupId', (v) => v as String),
    joinedAt: $checkedConvert('joinedAt', (v) => DateTime.parse(v as String)),
    centrality: $checkedConvert('centrality', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$NarrativePostToJson(NarrativePost instance) =>
    <String, dynamic>{
      'post': instance.post.toJson(),
      'linkReason': _$NarrativePostLinkReasonEnumEnumMap[instance.linkReason]!,
      'evidenceGroupId': instance.evidenceGroupId,
      'joinedAt': instance.joinedAt.toIso8601String(),
      'centrality': ?instance.centrality,
    };

const _$NarrativePostLinkReasonEnumEnumMap = {
  NarrativePostLinkReasonEnum.seed: 'seed',
  NarrativePostLinkReasonEnum.repost: 'repost',
  NarrativePostLinkReasonEnum.quote: 'quote',
  NarrativePostLinkReasonEnum.text: 'text',
  NarrativePostLinkReasonEnum.link: 'link',
  NarrativePostLinkReasonEnum.media: 'media',
};
