// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'news_story_outlets_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NewsStoryOutletsInnerCWProxy {
  NewsStoryOutletsInner outlet(MediaOutlet outlet);

  NewsStoryOutletsInner articles(List<NewsArticle> articles);

  NewsStoryOutletsInner evidenceGroupId(String? evidenceGroupId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsStoryOutletsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsStoryOutletsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  NewsStoryOutletsInner call({
    MediaOutlet outlet,
    List<NewsArticle> articles,
    String? evidenceGroupId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNewsStoryOutletsInner.copyWith(...)` or call `instanceOfNewsStoryOutletsInner.copyWith.fieldName(value)` for a single field.
class _$NewsStoryOutletsInnerCWProxyImpl
    implements _$NewsStoryOutletsInnerCWProxy {
  const _$NewsStoryOutletsInnerCWProxyImpl(this._value);

  final NewsStoryOutletsInner _value;

  @override
  NewsStoryOutletsInner outlet(MediaOutlet outlet) => call(outlet: outlet);

  @override
  NewsStoryOutletsInner articles(List<NewsArticle> articles) =>
      call(articles: articles);

  @override
  NewsStoryOutletsInner evidenceGroupId(String? evidenceGroupId) =>
      call(evidenceGroupId: evidenceGroupId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsStoryOutletsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsStoryOutletsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NewsStoryOutletsInner call({
    Object? outlet = const $CopyWithPlaceholder(),
    Object? articles = const $CopyWithPlaceholder(),
    Object? evidenceGroupId = const $CopyWithPlaceholder(),
  }) {
    return NewsStoryOutletsInner(
      outlet: outlet == const $CopyWithPlaceholder() || outlet == null
          ? _value.outlet
          // ignore: cast_nullable_to_non_nullable
          : outlet as MediaOutlet,
      articles: articles == const $CopyWithPlaceholder() || articles == null
          ? _value.articles
          // ignore: cast_nullable_to_non_nullable
          : articles as List<NewsArticle>,
      evidenceGroupId: evidenceGroupId == const $CopyWithPlaceholder()
          ? _value.evidenceGroupId
          // ignore: cast_nullable_to_non_nullable
          : evidenceGroupId as String?,
    );
  }
}

extension $NewsStoryOutletsInnerCopyWith on NewsStoryOutletsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNewsStoryOutletsInner.copyWith(...)` or `instanceOfNewsStoryOutletsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NewsStoryOutletsInnerCWProxy get copyWith =>
      _$NewsStoryOutletsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NewsStoryOutletsInner _$NewsStoryOutletsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NewsStoryOutletsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['outlet', 'articles']);
  final val = NewsStoryOutletsInner(
    outlet: $checkedConvert(
      'outlet',
      (v) => MediaOutlet.fromJson(v as Map<String, dynamic>),
    ),
    articles: $checkedConvert(
      'articles',
      (v) => (v as List<dynamic>)
          .map((e) => NewsArticle.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    evidenceGroupId: $checkedConvert('evidenceGroupId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$NewsStoryOutletsInnerToJson(
  NewsStoryOutletsInner instance,
) => <String, dynamic>{
  'outlet': instance.outlet.toJson(),
  'articles': instance.articles.map((e) => e.toJson()).toList(),
  'evidenceGroupId': ?instance.evidenceGroupId,
};
