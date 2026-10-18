// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_thread_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleThreadPageCWProxy {
  ChronicleThreadPage items(List<ChronicleThreadItem> items);

  ChronicleThreadPage nextCursor(String? nextCursor);

  ChronicleThreadPage unreadCount(int? unreadCount);

  ChronicleThreadPage readUpTo(int? readUpTo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleThreadPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleThreadPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleThreadPage call({
    List<ChronicleThreadItem> items,
    String? nextCursor,
    int? unreadCount,
    int? readUpTo,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleThreadPage.copyWith(...)` or call `instanceOfChronicleThreadPage.copyWith.fieldName(value)` for a single field.
class _$ChronicleThreadPageCWProxyImpl implements _$ChronicleThreadPageCWProxy {
  const _$ChronicleThreadPageCWProxyImpl(this._value);

  final ChronicleThreadPage _value;

  @override
  ChronicleThreadPage items(List<ChronicleThreadItem> items) =>
      call(items: items);

  @override
  ChronicleThreadPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  @override
  ChronicleThreadPage unreadCount(int? unreadCount) =>
      call(unreadCount: unreadCount);

  @override
  ChronicleThreadPage readUpTo(int? readUpTo) => call(readUpTo: readUpTo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleThreadPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleThreadPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleThreadPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
    Object? unreadCount = const $CopyWithPlaceholder(),
    Object? readUpTo = const $CopyWithPlaceholder(),
  }) {
    return ChronicleThreadPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ChronicleThreadItem>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
      unreadCount: unreadCount == const $CopyWithPlaceholder()
          ? _value.unreadCount
          // ignore: cast_nullable_to_non_nullable
          : unreadCount as int?,
      readUpTo: readUpTo == const $CopyWithPlaceholder()
          ? _value.readUpTo
          // ignore: cast_nullable_to_non_nullable
          : readUpTo as int?,
    );
  }
}

extension $ChronicleThreadPageCopyWith on ChronicleThreadPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleThreadPage.copyWith(...)` or `instanceOfChronicleThreadPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleThreadPageCWProxy get copyWith =>
      _$ChronicleThreadPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleThreadPage _$ChronicleThreadPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleThreadPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
  final val = ChronicleThreadPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => ChronicleThreadItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
    unreadCount: $checkedConvert('unreadCount', (v) => (v as num?)?.toInt()),
    readUpTo: $checkedConvert('readUpTo', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$ChronicleThreadPageToJson(
  ChronicleThreadPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
  'unreadCount': ?instance.unreadCount,
  'readUpTo': ?instance.readUpTo,
};
