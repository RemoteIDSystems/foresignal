// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'news_article_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NewsArticlePageCWProxy {
  NewsArticlePage items(List<NewsArticle> items);

  NewsArticlePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsArticlePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsArticlePage(...).copyWith(id: 12, name: "My name")
  /// ```
  NewsArticlePage call({List<NewsArticle> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNewsArticlePage.copyWith(...)` or call `instanceOfNewsArticlePage.copyWith.fieldName(value)` for a single field.
class _$NewsArticlePageCWProxyImpl implements _$NewsArticlePageCWProxy {
  const _$NewsArticlePageCWProxyImpl(this._value);

  final NewsArticlePage _value;

  @override
  NewsArticlePage items(List<NewsArticle> items) => call(items: items);

  @override
  NewsArticlePage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsArticlePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsArticlePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NewsArticlePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return NewsArticlePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<NewsArticle>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $NewsArticlePageCopyWith on NewsArticlePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNewsArticlePage.copyWith(...)` or `instanceOfNewsArticlePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NewsArticlePageCWProxy get copyWith => _$NewsArticlePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NewsArticlePage _$NewsArticlePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NewsArticlePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = NewsArticlePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => NewsArticle.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$NewsArticlePageToJson(NewsArticlePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
