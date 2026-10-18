// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'news_story_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NewsStoryPageCWProxy {
  NewsStoryPage items(List<NewsStory> items);

  NewsStoryPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsStoryPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsStoryPage(...).copyWith(id: 12, name: "My name")
  /// ```
  NewsStoryPage call({List<NewsStory> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNewsStoryPage.copyWith(...)` or call `instanceOfNewsStoryPage.copyWith.fieldName(value)` for a single field.
class _$NewsStoryPageCWProxyImpl implements _$NewsStoryPageCWProxy {
  const _$NewsStoryPageCWProxyImpl(this._value);

  final NewsStoryPage _value;

  @override
  NewsStoryPage items(List<NewsStory> items) => call(items: items);

  @override
  NewsStoryPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NewsStoryPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NewsStoryPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NewsStoryPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return NewsStoryPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<NewsStory>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $NewsStoryPageCopyWith on NewsStoryPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNewsStoryPage.copyWith(...)` or `instanceOfNewsStoryPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NewsStoryPageCWProxy get copyWith => _$NewsStoryPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NewsStoryPage _$NewsStoryPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NewsStoryPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = NewsStoryPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => NewsStory.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$NewsStoryPageToJson(NewsStoryPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
