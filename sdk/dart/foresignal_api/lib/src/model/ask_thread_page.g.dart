// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ask_thread_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AskThreadPageCWProxy {
  AskThreadPage items(List<AskThreadPageItemsInner> items);

  AskThreadPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskThreadPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskThreadPage(...).copyWith(id: 12, name: "My name")
  /// ```
  AskThreadPage call({List<AskThreadPageItemsInner> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAskThreadPage.copyWith(...)` or call `instanceOfAskThreadPage.copyWith.fieldName(value)` for a single field.
class _$AskThreadPageCWProxyImpl implements _$AskThreadPageCWProxy {
  const _$AskThreadPageCWProxyImpl(this._value);

  final AskThreadPage _value;

  @override
  AskThreadPage items(List<AskThreadPageItemsInner> items) =>
      call(items: items);

  @override
  AskThreadPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AskThreadPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AskThreadPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AskThreadPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AskThreadPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<AskThreadPageItemsInner>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AskThreadPageCopyWith on AskThreadPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAskThreadPage.copyWith(...)` or `instanceOfAskThreadPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AskThreadPageCWProxy get copyWith => _$AskThreadPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AskThreadPage _$AskThreadPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AskThreadPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = AskThreadPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map(
                (e) =>
                    AskThreadPageItemsInner.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AskThreadPageToJson(AskThreadPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
