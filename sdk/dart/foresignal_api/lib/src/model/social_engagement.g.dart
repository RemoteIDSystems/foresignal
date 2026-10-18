// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_engagement.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialEngagementCWProxy {
  SocialEngagement replies(int? replies);

  SocialEngagement reposts(int? reposts);

  SocialEngagement quotes(int? quotes);

  SocialEngagement likes(int? likes);

  SocialEngagement views(int? views);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialEngagement(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialEngagement(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialEngagement call({
    int? replies,
    int? reposts,
    int? quotes,
    int? likes,
    int? views,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialEngagement.copyWith(...)` or call `instanceOfSocialEngagement.copyWith.fieldName(value)` for a single field.
class _$SocialEngagementCWProxyImpl implements _$SocialEngagementCWProxy {
  const _$SocialEngagementCWProxyImpl(this._value);

  final SocialEngagement _value;

  @override
  SocialEngagement replies(int? replies) => call(replies: replies);

  @override
  SocialEngagement reposts(int? reposts) => call(reposts: reposts);

  @override
  SocialEngagement quotes(int? quotes) => call(quotes: quotes);

  @override
  SocialEngagement likes(int? likes) => call(likes: likes);

  @override
  SocialEngagement views(int? views) => call(views: views);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialEngagement(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialEngagement(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialEngagement call({
    Object? replies = const $CopyWithPlaceholder(),
    Object? reposts = const $CopyWithPlaceholder(),
    Object? quotes = const $CopyWithPlaceholder(),
    Object? likes = const $CopyWithPlaceholder(),
    Object? views = const $CopyWithPlaceholder(),
  }) {
    return SocialEngagement(
      replies: replies == const $CopyWithPlaceholder()
          ? _value.replies
          // ignore: cast_nullable_to_non_nullable
          : replies as int?,
      reposts: reposts == const $CopyWithPlaceholder()
          ? _value.reposts
          // ignore: cast_nullable_to_non_nullable
          : reposts as int?,
      quotes: quotes == const $CopyWithPlaceholder()
          ? _value.quotes
          // ignore: cast_nullable_to_non_nullable
          : quotes as int?,
      likes: likes == const $CopyWithPlaceholder()
          ? _value.likes
          // ignore: cast_nullable_to_non_nullable
          : likes as int?,
      views: views == const $CopyWithPlaceholder()
          ? _value.views
          // ignore: cast_nullable_to_non_nullable
          : views as int?,
    );
  }
}

extension $SocialEngagementCopyWith on SocialEngagement {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialEngagement.copyWith(...)` or `instanceOfSocialEngagement.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialEngagementCWProxy get copyWith => _$SocialEngagementCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialEngagement _$SocialEngagementFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialEngagement', json, ($checkedConvert) {
      final val = SocialEngagement(
        replies: $checkedConvert('replies', (v) => (v as num?)?.toInt()),
        reposts: $checkedConvert('reposts', (v) => (v as num?)?.toInt()),
        quotes: $checkedConvert('quotes', (v) => (v as num?)?.toInt()),
        likes: $checkedConvert('likes', (v) => (v as num?)?.toInt()),
        views: $checkedConvert('views', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$SocialEngagementToJson(SocialEngagement instance) =>
    <String, dynamic>{
      'replies': ?instance.replies,
      'reposts': ?instance.reposts,
      'quotes': ?instance.quotes,
      'likes': ?instance.likes,
      'views': ?instance.views,
    };
