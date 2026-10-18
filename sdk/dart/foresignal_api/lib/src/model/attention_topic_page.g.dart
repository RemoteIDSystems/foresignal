// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'attention_topic_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AttentionTopicPageCWProxy {
  AttentionTopicPage items(List<AttentionTopic> items);

  AttentionTopicPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopicPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopicPage(...).copyWith(id: 12, name: "My name")
  /// ```
  AttentionTopicPage call({List<AttentionTopic> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAttentionTopicPage.copyWith(...)` or call `instanceOfAttentionTopicPage.copyWith.fieldName(value)` for a single field.
class _$AttentionTopicPageCWProxyImpl implements _$AttentionTopicPageCWProxy {
  const _$AttentionTopicPageCWProxyImpl(this._value);

  final AttentionTopicPage _value;

  @override
  AttentionTopicPage items(List<AttentionTopic> items) => call(items: items);

  @override
  AttentionTopicPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AttentionTopicPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AttentionTopicPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AttentionTopicPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AttentionTopicPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<AttentionTopic>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AttentionTopicPageCopyWith on AttentionTopicPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAttentionTopicPage.copyWith(...)` or `instanceOfAttentionTopicPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AttentionTopicPageCWProxy get copyWith =>
      _$AttentionTopicPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AttentionTopicPage _$AttentionTopicPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AttentionTopicPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = AttentionTopicPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => AttentionTopic.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AttentionTopicPageToJson(AttentionTopicPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
