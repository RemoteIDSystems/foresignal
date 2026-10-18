// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tag_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TagPageCWProxy {
  TagPage items(List<Tag> items);

  TagPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagPage(...).copyWith(id: 12, name: "My name")
  /// ```
  TagPage call({List<Tag> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTagPage.copyWith(...)` or call `instanceOfTagPage.copyWith.fieldName(value)` for a single field.
class _$TagPageCWProxyImpl implements _$TagPageCWProxy {
  const _$TagPageCWProxyImpl(this._value);

  final TagPage _value;

  @override
  TagPage items(List<Tag> items) => call(items: items);

  @override
  TagPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TagPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return TagPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Tag>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $TagPageCopyWith on TagPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTagPage.copyWith(...)` or `instanceOfTagPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TagPageCWProxy get copyWith => _$TagPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TagPage _$TagPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TagPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = TagPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Tag.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$TagPageToJson(TagPage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
};
