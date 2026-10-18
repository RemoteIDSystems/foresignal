// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'watchlist_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WatchlistPageCWProxy {
  WatchlistPage items(List<Watchlist> items);

  WatchlistPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistPage(...).copyWith(id: 12, name: "My name")
  /// ```
  WatchlistPage call({List<Watchlist> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWatchlistPage.copyWith(...)` or call `instanceOfWatchlistPage.copyWith.fieldName(value)` for a single field.
class _$WatchlistPageCWProxyImpl implements _$WatchlistPageCWProxy {
  const _$WatchlistPageCWProxyImpl(this._value);

  final WatchlistPage _value;

  @override
  WatchlistPage items(List<Watchlist> items) => call(items: items);

  @override
  WatchlistPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WatchlistPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return WatchlistPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Watchlist>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $WatchlistPageCopyWith on WatchlistPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWatchlistPage.copyWith(...)` or `instanceOfWatchlistPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WatchlistPageCWProxy get copyWith => _$WatchlistPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WatchlistPage _$WatchlistPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WatchlistPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = WatchlistPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Watchlist.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$WatchlistPageToJson(WatchlistPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
