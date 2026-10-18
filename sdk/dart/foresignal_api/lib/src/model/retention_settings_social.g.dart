// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'retention_settings_social.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RetentionSettingsSocialCWProxy {
  RetentionSettingsSocial posts(String? posts);

  RetentionSettingsSocial platforms(Map<String, String>? platforms);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionSettingsSocial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionSettingsSocial(...).copyWith(id: 12, name: "My name")
  /// ```
  RetentionSettingsSocial call({String? posts, Map<String, String>? platforms});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRetentionSettingsSocial.copyWith(...)` or call `instanceOfRetentionSettingsSocial.copyWith.fieldName(value)` for a single field.
class _$RetentionSettingsSocialCWProxyImpl
    implements _$RetentionSettingsSocialCWProxy {
  const _$RetentionSettingsSocialCWProxyImpl(this._value);

  final RetentionSettingsSocial _value;

  @override
  RetentionSettingsSocial posts(String? posts) => call(posts: posts);

  @override
  RetentionSettingsSocial platforms(Map<String, String>? platforms) =>
      call(platforms: platforms);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RetentionSettingsSocial(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RetentionSettingsSocial(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RetentionSettingsSocial call({
    Object? posts = const $CopyWithPlaceholder(),
    Object? platforms = const $CopyWithPlaceholder(),
  }) {
    return RetentionSettingsSocial(
      posts: posts == const $CopyWithPlaceholder()
          ? _value.posts
          // ignore: cast_nullable_to_non_nullable
          : posts as String?,
      platforms: platforms == const $CopyWithPlaceholder()
          ? _value.platforms
          // ignore: cast_nullable_to_non_nullable
          : platforms as Map<String, String>?,
    );
  }
}

extension $RetentionSettingsSocialCopyWith on RetentionSettingsSocial {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRetentionSettingsSocial.copyWith(...)` or `instanceOfRetentionSettingsSocial.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RetentionSettingsSocialCWProxy get copyWith =>
      _$RetentionSettingsSocialCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RetentionSettingsSocial _$RetentionSettingsSocialFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RetentionSettingsSocial', json, ($checkedConvert) {
  final val = RetentionSettingsSocial(
    posts: $checkedConvert('posts', (v) => v as String?),
    platforms: $checkedConvert(
      'platforms',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
  );
  return val;
});

Map<String, dynamic> _$RetentionSettingsSocialToJson(
  RetentionSettingsSocial instance,
) => <String, dynamic>{
  'posts': ?instance.posts,
  'platforms': ?instance.platforms,
};
