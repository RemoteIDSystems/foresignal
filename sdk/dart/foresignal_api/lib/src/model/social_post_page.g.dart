// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_post_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPostPageCWProxy {
  SocialPostPage items(List<SocialPost> items);

  SocialPostPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPostPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPostPage(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPostPage call({List<SocialPost> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPostPage.copyWith(...)` or call `instanceOfSocialPostPage.copyWith.fieldName(value)` for a single field.
class _$SocialPostPageCWProxyImpl implements _$SocialPostPageCWProxy {
  const _$SocialPostPageCWProxyImpl(this._value);

  final SocialPostPage _value;

  @override
  SocialPostPage items(List<SocialPost> items) => call(items: items);

  @override
  SocialPostPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPostPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPostPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPostPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SocialPostPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SocialPost>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SocialPostPageCopyWith on SocialPostPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPostPage.copyWith(...)` or `instanceOfSocialPostPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPostPageCWProxy get copyWith => _$SocialPostPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPostPage _$SocialPostPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialPostPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = SocialPostPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => SocialPost.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SocialPostPageToJson(SocialPostPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
