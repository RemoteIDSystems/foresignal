// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'attention_topic_social_accounts_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AttentionTopicSocialAccountsInnerCWProxy {
  AttentionTopicSocialAccountsInner platform(String platform);

  AttentionTopicSocialAccountsInner handle(String handle);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopicSocialAccountsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopicSocialAccountsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AttentionTopicSocialAccountsInner call({String platform, String handle});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAttentionTopicSocialAccountsInner.copyWith(...)` or call `instanceOfAttentionTopicSocialAccountsInner.copyWith.fieldName(value)` for a single field.
class _$AttentionTopicSocialAccountsInnerCWProxyImpl
    implements _$AttentionTopicSocialAccountsInnerCWProxy {
  const _$AttentionTopicSocialAccountsInnerCWProxyImpl(this._value);

  final AttentionTopicSocialAccountsInner _value;

  @override
  AttentionTopicSocialAccountsInner platform(String platform) =>
      call(platform: platform);

  @override
  AttentionTopicSocialAccountsInner handle(String handle) =>
      call(handle: handle);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopicSocialAccountsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopicSocialAccountsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AttentionTopicSocialAccountsInner call({
    Object? platform = const $CopyWithPlaceholder(),
    Object? handle = const $CopyWithPlaceholder(),
  }) {
    return AttentionTopicSocialAccountsInner(
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as String,
      handle: handle == const $CopyWithPlaceholder() || handle == null
          ? _value.handle
          // ignore: cast_nullable_to_non_nullable
          : handle as String,
    );
  }
}

extension $AttentionTopicSocialAccountsInnerCopyWith
    on AttentionTopicSocialAccountsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAttentionTopicSocialAccountsInner.copyWith(...)` or `instanceOfAttentionTopicSocialAccountsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AttentionTopicSocialAccountsInnerCWProxy get copyWith =>
      _$AttentionTopicSocialAccountsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AttentionTopicSocialAccountsInner _$AttentionTopicSocialAccountsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AttentionTopicSocialAccountsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['platform', 'handle']);
  final val = AttentionTopicSocialAccountsInner(
    platform: $checkedConvert('platform', (v) => v as String),
    handle: $checkedConvert('handle', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$AttentionTopicSocialAccountsInnerToJson(
  AttentionTopicSocialAccountsInner instance,
) => <String, dynamic>{
  'platform': instance.platform,
  'handle': instance.handle,
};
