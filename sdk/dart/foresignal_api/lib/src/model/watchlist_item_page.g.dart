// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'watchlist_item_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WatchlistItemPageCWProxy {
  WatchlistItemPage items(List<WatchlistItem> items);

  WatchlistItemPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistItemPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistItemPage(...).copyWith(id: 12, name: "My name")
  /// ```
  WatchlistItemPage call({List<WatchlistItem> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWatchlistItemPage.copyWith(...)` or call `instanceOfWatchlistItemPage.copyWith.fieldName(value)` for a single field.
class _$WatchlistItemPageCWProxyImpl implements _$WatchlistItemPageCWProxy {
  const _$WatchlistItemPageCWProxyImpl(this._value);

  final WatchlistItemPage _value;

  @override
  WatchlistItemPage items(List<WatchlistItem> items) => call(items: items);

  @override
  WatchlistItemPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistItemPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistItemPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WatchlistItemPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return WatchlistItemPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<WatchlistItem>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $WatchlistItemPageCopyWith on WatchlistItemPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWatchlistItemPage.copyWith(...)` or `instanceOfWatchlistItemPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WatchlistItemPageCWProxy get copyWith =>
      _$WatchlistItemPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WatchlistItemPage _$WatchlistItemPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WatchlistItemPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = WatchlistItemPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => WatchlistItem.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$WatchlistItemPageToJson(WatchlistItemPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
