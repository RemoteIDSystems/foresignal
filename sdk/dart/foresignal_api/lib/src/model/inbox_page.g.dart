// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'inbox_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InboxPageCWProxy {
  InboxPage items(List<InboxItem> items);

  InboxPage nextCursor(String? nextCursor);

  InboxPage unread(int? unread);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InboxPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InboxPage(...).copyWith(id: 12, name: "My name")
  /// ```
  InboxPage call({List<InboxItem> items, String? nextCursor, int? unread});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInboxPage.copyWith(...)` or call `instanceOfInboxPage.copyWith.fieldName(value)` for a single field.
class _$InboxPageCWProxyImpl implements _$InboxPageCWProxy {
  const _$InboxPageCWProxyImpl(this._value);

  final InboxPage _value;

  @override
  InboxPage items(List<InboxItem> items) => call(items: items);

  @override
  InboxPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  @override
  InboxPage unread(int? unread) => call(unread: unread);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InboxPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InboxPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InboxPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
    Object? unread = const $CopyWithPlaceholder(),
  }) {
    return InboxPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<InboxItem>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
      unread: unread == const $CopyWithPlaceholder()
          ? _value.unread
          // ignore: cast_nullable_to_non_nullable
          : unread as int?,
    );
  }
}

extension $InboxPageCopyWith on InboxPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInboxPage.copyWith(...)` or `instanceOfInboxPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InboxPageCWProxy get copyWith => _$InboxPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InboxPage _$InboxPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InboxPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = InboxPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => InboxItem.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
        unread: $checkedConvert('unread', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$InboxPageToJson(InboxPage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
  'unread': ?instance.unread,
};
