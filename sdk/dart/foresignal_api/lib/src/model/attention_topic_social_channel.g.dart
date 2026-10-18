// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'attention_topic_social_channel.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AttentionTopicSocialChannelCWProxy {
  AttentionTopicSocialChannel platform(String platform);

  AttentionTopicSocialChannel type(AttentionTopicSocialChannelTypeEnum type);

  AttentionTopicSocialChannel id(String id);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopicSocialChannel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopicSocialChannel(...).copyWith(id: 12, name: "My name")
  /// ```
  AttentionTopicSocialChannel call({
    String platform,
    AttentionTopicSocialChannelTypeEnum type,
    String id,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAttentionTopicSocialChannel.copyWith(...)` or call `instanceOfAttentionTopicSocialChannel.copyWith.fieldName(value)` for a single field.
class _$AttentionTopicSocialChannelCWProxyImpl
    implements _$AttentionTopicSocialChannelCWProxy {
  const _$AttentionTopicSocialChannelCWProxyImpl(this._value);

  final AttentionTopicSocialChannel _value;

  @override
  AttentionTopicSocialChannel platform(String platform) =>
      call(platform: platform);

  @override
  AttentionTopicSocialChannel type(AttentionTopicSocialChannelTypeEnum type) =>
      call(type: type);

  @override
  AttentionTopicSocialChannel id(String id) => call(id: id);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopicSocialChannel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopicSocialChannel(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AttentionTopicSocialChannel call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
  }) {
    return AttentionTopicSocialChannel(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as AttentionTopicSocialChannelTypeEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
    );
  }
}

extension $AttentionTopicSocialChannelCopyWith on AttentionTopicSocialChannel {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAttentionTopicSocialChannel.copyWith(...)` or `instanceOfAttentionTopicSocialChannel.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AttentionTopicSocialChannelCWProxy get copyWith =>
      _$AttentionTopicSocialChannelCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AttentionTopicSocialChannel _$AttentionTopicSocialChannelFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AttentionTopicSocialChannel', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['platform', 'type', 'id']);
  final val = AttentionTopicSocialChannel(
    platform: $checkedConvert('platform', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$AttentionTopicSocialChannelTypeEnumEnumMap, v),
    ),
    id: $checkedConvert('id', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$AttentionTopicSocialChannelToJson(
  AttentionTopicSocialChannel instance,
) => <String, dynamic>{
  'platform': instance.platform,
  'type': _$AttentionTopicSocialChannelTypeEnumEnumMap[instance.type]!,
  'id': instance.id,
};

const _$AttentionTopicSocialChannelTypeEnumEnumMap = {
  AttentionTopicSocialChannelTypeEnum.subreddit: 'subreddit',
  AttentionTopicSocialChannelTypeEnum.mastodonInstance: 'mastodon-instance',
  AttentionTopicSocialChannelTypeEnum.mastodonHashtag: 'mastodon-hashtag',
  AttentionTopicSocialChannelTypeEnum.youtubeChannel: 'youtube-channel',
};
