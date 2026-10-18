// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_thread_page_items_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskThreadPageItemsInnerCWProxy {
  AskThreadPageItemsInner id(String? id);

  AskThreadPageItemsInner title(String? title);

  AskThreadPageItemsInner createdAt(DateTime? createdAt);

  AskThreadPageItemsInner updatedAt(DateTime? updatedAt);

  AskThreadPageItemsInner messageCount(int? messageCount);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskThreadPageItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskThreadPageItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AskThreadPageItemsInner call({
    String? id,
    String? title,
    DateTime? createdAt,
    DateTime? updatedAt,
    int? messageCount,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskThreadPageItemsInner.copyWith(...)` or call `instanceOfAskThreadPageItemsInner.copyWith.fieldName(value)` for a single field.
class _$AskThreadPageItemsInnerCWProxyImpl
    implements _$AskThreadPageItemsInnerCWProxy {
  const _$AskThreadPageItemsInnerCWProxyImpl(this._value);

  final AskThreadPageItemsInner _value;

  @override
  AskThreadPageItemsInner id(String? id) => call(id: id);

  @override
  AskThreadPageItemsInner title(String? title) => call(title: title);

  @override
  AskThreadPageItemsInner createdAt(DateTime? createdAt) =>
      call(createdAt: createdAt);

  @override
  AskThreadPageItemsInner updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  @override
  AskThreadPageItemsInner messageCount(int? messageCount) =>
      call(messageCount: messageCount);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskThreadPageItemsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskThreadPageItemsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskThreadPageItemsInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? messageCount = const $CopyWithPlaceholder(),
  }) {
    return AskThreadPageItemsInner(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      messageCount: messageCount == const $CopyWithPlaceholder()
          ? _value.messageCount
          // ignore: cast_nullable_to_non_nullable
          : messageCount as int?,
    );
  }
}

extension $AskThreadPageItemsInnerCopyWith on AskThreadPageItemsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskThreadPageItemsInner.copyWith(...)` or `instanceOfAskThreadPageItemsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskThreadPageItemsInnerCWProxy get copyWith =>
      _$AskThreadPageItemsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskThreadPageItemsInner _$AskThreadPageItemsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AskThreadPageItemsInner', json, ($checkedConvert) {
  final val = AskThreadPageItemsInner(
    id: $checkedConvert('id', (v) => v as String?),
    title: $checkedConvert('title', (v) => v as String?),
    createdAt: $checkedConvert(
      'createdAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    messageCount: $checkedConvert('messageCount', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$AskThreadPageItemsInnerToJson(
  AskThreadPageItemsInner instance,
) => <String, dynamic>{
  'id': ?instance.id,
  'title': ?instance.title,
  'createdAt': ?instance.createdAt?.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'messageCount': ?instance.messageCount,
};
