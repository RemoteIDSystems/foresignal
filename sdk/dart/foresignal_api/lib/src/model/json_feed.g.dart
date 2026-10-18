// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'json_feed.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$JsonFeedCWProxy {
  JsonFeed version(JsonFeedVersionEnum version);

  JsonFeed title(String title);

  JsonFeed homePageUrl(String? homePageUrl);

  JsonFeed feedUrl(String? feedUrl);

  JsonFeed description(String? description);

  JsonFeed language(String? language);

  JsonFeed authors(List<JsonFeedAuthorsInner>? authors);

  JsonFeed items(List<JsonFeedItem> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `JsonFeed(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// JsonFeed(...).copyWith(id: 12, name: "My name")
  /// ```
  JsonFeed call({
    JsonFeedVersionEnum version,
    String title,
    String? homePageUrl,
    String? feedUrl,
    String? description,
    String? language,
    List<JsonFeedAuthorsInner>? authors,
    List<JsonFeedItem> items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfJsonFeed.copyWith(...)` or call `instanceOfJsonFeed.copyWith.fieldName(value)` for a single field.
class _$JsonFeedCWProxyImpl implements _$JsonFeedCWProxy {
  const _$JsonFeedCWProxyImpl(this._value);

  final JsonFeed _value;

  @override
  JsonFeed version(JsonFeedVersionEnum version) => call(version: version);

  @override
  JsonFeed title(String title) => call(title: title);

  @override
  JsonFeed homePageUrl(String? homePageUrl) => call(homePageUrl: homePageUrl);

  @override
  JsonFeed feedUrl(String? feedUrl) => call(feedUrl: feedUrl);

  @override
  JsonFeed description(String? description) => call(description: description);

  @override
  JsonFeed language(String? language) => call(language: language);

  @override
  JsonFeed authors(List<JsonFeedAuthorsInner>? authors) =>
      call(authors: authors);

  @override
  JsonFeed items(List<JsonFeedItem> items) => call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `JsonFeed(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// JsonFeed(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  JsonFeed call({
    Object? version = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? homePageUrl = const $CopyWithPlaceholder(),
    Object? feedUrl = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? language = const $CopyWithPlaceholder(),
    Object? authors = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return JsonFeed(
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as JsonFeedVersionEnum,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      homePageUrl: homePageUrl == const $CopyWithPlaceholder()
          ? _value.homePageUrl
          // ignore: cast_nullable_to_non_nullable
          : homePageUrl as String?,
      feedUrl: feedUrl == const $CopyWithPlaceholder()
          ? _value.feedUrl
          // ignore: cast_nullable_to_non_nullable
          : feedUrl as String?,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      language: language == const $CopyWithPlaceholder()
          ? _value.language
          // ignore: cast_nullable_to_non_nullable
          : language as String?,
      authors: authors == const $CopyWithPlaceholder()
          ? _value.authors
          // ignore: cast_nullable_to_non_nullable
          : authors as List<JsonFeedAuthorsInner>?,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<JsonFeedItem>,
    );
  }
}

extension $JsonFeedCopyWith on JsonFeed {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfJsonFeed.copyWith(...)` or `instanceOfJsonFeed.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$JsonFeedCWProxy get copyWith => _$JsonFeedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

JsonFeed _$JsonFeedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('JsonFeed', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['version', 'title', 'items']);
  final val = JsonFeed(
    version: $checkedConvert(
      'version',
      (v) => $enumDecode(_$JsonFeedVersionEnumEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String),
    homePageUrl: $checkedConvert('home_page_url', (v) => v as String?),
    feedUrl: $checkedConvert('feed_url', (v) => v as String?),
    description: $checkedConvert('description', (v) => v as String?),
    language: $checkedConvert('language', (v) => v as String?),
    authors: $checkedConvert(
      'authors',
      (v) => (v as List<dynamic>?)
          ?.map((e) => JsonFeedAuthorsInner.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => JsonFeedItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'homePageUrl': 'home_page_url', 'feedUrl': 'feed_url'});

Map<String, dynamic> _$JsonFeedToJson(JsonFeed instance) => <String, dynamic>{
  'version': _$JsonFeedVersionEnumEnumMap[instance.version]!,
  'title': instance.title,
  'home_page_url': ?instance.homePageUrl,
  'feed_url': ?instance.feedUrl,
  'description': ?instance.description,
  'language': ?instance.language,
  'authors': ?instance.authors?.map((e) => e.toJson()).toList(),
  'items': instance.items.map((e) => e.toJson()).toList(),
};

const _$JsonFeedVersionEnumEnumMap = {
  JsonFeedVersionEnum
          .httpsColonSlashSlashJsonfeedPeriodOrgSlashVersionSlash1Period1:
      'https://jsonfeed.org/version/1.1',
};
