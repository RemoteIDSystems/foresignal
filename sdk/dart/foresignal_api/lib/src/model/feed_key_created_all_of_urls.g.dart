// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'feed_key_created_all_of_urls.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FeedKeyCreatedAllOfUrlsCWProxy {
  FeedKeyCreatedAllOfUrls prefix(String prefix);

  FeedKeyCreatedAllOfUrls url(String url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FeedKeyCreatedAllOfUrls(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FeedKeyCreatedAllOfUrls(...).copyWith(id: 12, name: "My name")
  /// ```
  FeedKeyCreatedAllOfUrls call({String prefix, String url});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFeedKeyCreatedAllOfUrls.copyWith(...)` or call `instanceOfFeedKeyCreatedAllOfUrls.copyWith.fieldName(value)` for a single field.
class _$FeedKeyCreatedAllOfUrlsCWProxyImpl
    implements _$FeedKeyCreatedAllOfUrlsCWProxy {
  const _$FeedKeyCreatedAllOfUrlsCWProxyImpl(this._value);

  final FeedKeyCreatedAllOfUrls _value;

  @override
  FeedKeyCreatedAllOfUrls prefix(String prefix) => call(prefix: prefix);

  @override
  FeedKeyCreatedAllOfUrls url(String url) => call(url: url);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FeedKeyCreatedAllOfUrls(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FeedKeyCreatedAllOfUrls(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FeedKeyCreatedAllOfUrls call({
    Object? prefix = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
  }) {
    return FeedKeyCreatedAllOfUrls(
      prefix: prefix == const $CopyWithPlaceholder() || prefix == null
          ? _value.prefix
          // ignore: cast_nullable_to_non_nullable
          : prefix as String,
      url: url == const $CopyWithPlaceholder() || url == null
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String,
    );
  }
}

extension $FeedKeyCreatedAllOfUrlsCopyWith on FeedKeyCreatedAllOfUrls {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFeedKeyCreatedAllOfUrls.copyWith(...)` or `instanceOfFeedKeyCreatedAllOfUrls.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FeedKeyCreatedAllOfUrlsCWProxy get copyWith =>
      _$FeedKeyCreatedAllOfUrlsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FeedKeyCreatedAllOfUrls _$FeedKeyCreatedAllOfUrlsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FeedKeyCreatedAllOfUrls', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['prefix', 'url']);
  final val = FeedKeyCreatedAllOfUrls(
    prefix: $checkedConvert('prefix', (v) => v as String),
    url: $checkedConvert('url', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$FeedKeyCreatedAllOfUrlsToJson(
  FeedKeyCreatedAllOfUrls instance,
) => <String, dynamic>{'prefix': instance.prefix, 'url': instance.url};
