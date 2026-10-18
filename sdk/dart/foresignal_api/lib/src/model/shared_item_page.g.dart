// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'shared_item_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SharedItemPageCWProxy {
  SharedItemPage items(List<SharedItem> items);

  SharedItemPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SharedItemPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SharedItemPage(...).copyWith(id: 12, name: "My name")
  /// ```
  SharedItemPage call({List<SharedItem> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSharedItemPage.copyWith(...)` or call `instanceOfSharedItemPage.copyWith.fieldName(value)` for a single field.
class _$SharedItemPageCWProxyImpl implements _$SharedItemPageCWProxy {
  const _$SharedItemPageCWProxyImpl(this._value);

  final SharedItemPage _value;

  @override
  SharedItemPage items(List<SharedItem> items) => call(items: items);

  @override
  SharedItemPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SharedItemPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SharedItemPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SharedItemPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SharedItemPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SharedItem>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SharedItemPageCopyWith on SharedItemPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSharedItemPage.copyWith(...)` or `instanceOfSharedItemPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SharedItemPageCWProxy get copyWith => _$SharedItemPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SharedItemPage _$SharedItemPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SharedItemPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = SharedItemPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => SharedItem.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SharedItemPageToJson(SharedItemPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
