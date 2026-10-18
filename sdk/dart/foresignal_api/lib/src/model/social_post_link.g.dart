// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_post_link.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPostLinkCWProxy {
  SocialPostLink platform(String platform);

  SocialPostLink postId(String postId);

  SocialPostLink ref(String? ref);

  SocialPostLink url(String? url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPostLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPostLink(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPostLink call({
    String platform,
    String postId,
    String? ref,
    String? url,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPostLink.copyWith(...)` or call `instanceOfSocialPostLink.copyWith.fieldName(value)` for a single field.
class _$SocialPostLinkCWProxyImpl implements _$SocialPostLinkCWProxy {
  const _$SocialPostLinkCWProxyImpl(this._value);

  final SocialPostLink _value;

  @override
  SocialPostLink platform(String platform) => call(platform: platform);

  @override
  SocialPostLink postId(String postId) => call(postId: postId);

  @override
  SocialPostLink ref(String? ref) => call(ref: ref);

  @override
  SocialPostLink url(String? url) => call(url: url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPostLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPostLink(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPostLink call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? postId = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
  }) {
    return SocialPostLink(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      postId: postId == const $CopyWithPlaceholder() || postId == null
          ? _value.postId
          // ignore: cast_nullable_to_non_nullable
          : postId as String,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
    );
  }
}

extension $SocialPostLinkCopyWith on SocialPostLink {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPostLink.copyWith(...)` or `instanceOfSocialPostLink.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPostLinkCWProxy get copyWith => _$SocialPostLinkCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPostLink _$SocialPostLinkFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialPostLink', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['platform', 'postId']);
      final val = SocialPostLink(
        platform: $checkedConvert('platform', (v) => v as String),
        postId: $checkedConvert('postId', (v) => v as String),
        ref: $checkedConvert('ref', (v) => v as String?),
        url: $checkedConvert('url', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialPostLinkToJson(SocialPostLink instance) =>
    <String, dynamic>{
      'platform': instance.platform,
      'postId': instance.postId,
      'ref': ?instance.ref,
      'url': ?instance.url,
    };
