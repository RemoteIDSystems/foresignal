// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'json_feed_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$JsonFeedItemCWProxy {
  JsonFeedItem id(String id);

  JsonFeedItem url(String? url);

  JsonFeedItem title(String title);

  JsonFeedItem contentHtml(String? contentHtml);

  JsonFeedItem contentText(String? contentText);

  JsonFeedItem datePublished(DateTime? datePublished);

  JsonFeedItem dateModified(DateTime? dateModified);

  JsonFeedItem tags(List<String>? tags);

  JsonFeedItem authors(List<JsonFeedItemAuthorsInner>? authors);

  JsonFeedItem foresignal(JsonFeedItemForesignal? foresignal);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `JsonFeedItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// JsonFeedItem(...).copyWith(id: 12, name: "My name")
  /// ```
  JsonFeedItem call({
    String id,
    String? url,
    String title,
    String? contentHtml,
    String? contentText,
    DateTime? datePublished,
    DateTime? dateModified,
    List<String>? tags,
    List<JsonFeedItemAuthorsInner>? authors,
    JsonFeedItemForesignal? foresignal,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfJsonFeedItem.copyWith(...)` or call `instanceOfJsonFeedItem.copyWith.fieldName(value)` for a single field.
class _$JsonFeedItemCWProxyImpl implements _$JsonFeedItemCWProxy {
  const _$JsonFeedItemCWProxyImpl(this._value);

  final JsonFeedItem _value;

  @override
  JsonFeedItem id(String id) => call(id: id);

  @override
  JsonFeedItem url(String? url) => call(url: url);

  @override
  JsonFeedItem title(String title) => call(title: title);

  @override
  JsonFeedItem contentHtml(String? contentHtml) =>
      call(contentHtml: contentHtml);

  @override
  JsonFeedItem contentText(String? contentText) =>
      call(contentText: contentText);

  @override
  JsonFeedItem datePublished(DateTime? datePublished) =>
      call(datePublished: datePublished);

  @override
  JsonFeedItem dateModified(DateTime? dateModified) =>
      call(dateModified: dateModified);

  @override
  JsonFeedItem tags(List<String>? tags) => call(tags: tags);

  @override
  JsonFeedItem authors(List<JsonFeedItemAuthorsInner>? authors) =>
      call(authors: authors);

  @override
  JsonFeedItem foresignal(JsonFeedItemForesignal? foresignal) =>
      call(foresignal: foresignal);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `JsonFeedItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// JsonFeedItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  JsonFeedItem call({
    Object? id = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? contentHtml = const $CopyWithPlaceholder(),
    Object? contentText = const $CopyWithPlaceholder(),
    Object? datePublished = const $CopyWithPlaceholder(),
    Object? dateModified = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? authors = const $CopyWithPlaceholder(),
    Object? foresignal = const $CopyWithPlaceholder(),
  }) {
    return JsonFeedItem(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      contentHtml: contentHtml == const $CopyWithPlaceholder()
          ? _value.contentHtml
          // ignore: cast_nullable_to_non_nullable
          : contentHtml as String?,
      contentText: contentText == const $CopyWithPlaceholder()
          ? _value.contentText
          // ignore: cast_nullable_to_non_nullable
          : contentText as String?,
      datePublished: datePublished == const $CopyWithPlaceholder()
          ? _value.datePublished
          // ignore: cast_nullable_to_non_nullable
          : datePublished as DateTime?,
      dateModified: dateModified == const $CopyWithPlaceholder()
          ? _value.dateModified
          // ignore: cast_nullable_to_non_nullable
          : dateModified as DateTime?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      authors: authors == const $CopyWithPlaceholder()
          ? _value.authors
          // ignore: cast_nullable_to_non_nullable
          : authors as List<JsonFeedItemAuthorsInner>?,
      foresignal: foresignal == const $CopyWithPlaceholder()
          ? _value.foresignal
          // ignore: cast_nullable_to_non_nullable
          : foresignal as JsonFeedItemForesignal?,
    );
  }
}

extension $JsonFeedItemCopyWith on JsonFeedItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfJsonFeedItem.copyWith(...)` or `instanceOfJsonFeedItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$JsonFeedItemCWProxy get copyWith => _$JsonFeedItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

JsonFeedItem _$JsonFeedItemFromJson(Map<String, dynamic> json) =>
    $checkedCreate(
      'JsonFeedItem',
      json,
      ($checkedConvert) {
        $checkKeys(json, requiredKeys: const ['id', 'title']);
        final val = JsonFeedItem(
          id: $checkedConvert('id', (v) => v as String),
          url: $checkedConvert('url', (v) => v as String?),
          title: $checkedConvert('title', (v) => v as String),
          contentHtml: $checkedConvert('content_html', (v) => v as String?),
          contentText: $checkedConvert('content_text', (v) => v as String?),
          datePublished: $checkedConvert(
            'date_published',
            (v) => v == null ? null : DateTime.parse(v as String),
          ),
          dateModified: $checkedConvert(
            'date_modified',
            (v) => v == null ? null : DateTime.parse(v as String),
          ),
          tags: $checkedConvert(
            'tags',
            (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
          ),
          authors: $checkedConvert(
            'authors',
            (v) => (v as List<dynamic>?)
                ?.map(
                  (e) => JsonFeedItemAuthorsInner.fromJson(
                    e as Map<String, dynamic>,
                  ),
                )
                .toList(),
          ),
          foresignal: $checkedConvert(
            '_foresignal',
            (v) => v == null
                ? null
                : JsonFeedItemForesignal.fromJson(v as Map<String, dynamic>),
          ),
        );
        return val;
      },
      fieldKeyMap: const {
        'contentHtml': 'content_html',
        'contentText': 'content_text',
        'datePublished': 'date_published',
        'dateModified': 'date_modified',
        'foresignal': '_foresignal',
      },
    );

Map<String, dynamic> _$JsonFeedItemToJson(JsonFeedItem instance) =>
    <String, dynamic>{
      'id': instance.id,
      'url': ?instance.url,
      'title': instance.title,
      'content_html': ?instance.contentHtml,
      'content_text': ?instance.contentText,
      'date_published': ?instance.datePublished?.toIso8601String(),
      'date_modified': ?instance.dateModified?.toIso8601String(),
      'tags': ?instance.tags,
      'authors': ?instance.authors?.map((e) => e.toJson()).toList(),
      '_foresignal': ?instance.foresignal?.toJson(),
    };
