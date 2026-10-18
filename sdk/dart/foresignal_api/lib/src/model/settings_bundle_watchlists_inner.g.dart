// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'settings_bundle_watchlists_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SettingsBundleWatchlistsInnerCWProxy {
  SettingsBundleWatchlistsInner watchlist(Watchlist? watchlist);

  SettingsBundleWatchlistsInner items(List<WatchlistItem>? items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SettingsBundleWatchlistsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SettingsBundleWatchlistsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SettingsBundleWatchlistsInner call({
    Watchlist? watchlist,
    List<WatchlistItem>? items,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSettingsBundleWatchlistsInner.copyWith(...)` or call `instanceOfSettingsBundleWatchlistsInner.copyWith.fieldName(value)` for a single field.
class _$SettingsBundleWatchlistsInnerCWProxyImpl
    implements _$SettingsBundleWatchlistsInnerCWProxy {
  const _$SettingsBundleWatchlistsInnerCWProxyImpl(this._value);

  final SettingsBundleWatchlistsInner _value;

  @override
  SettingsBundleWatchlistsInner watchlist(Watchlist? watchlist) =>
      call(watchlist: watchlist);

  @override
  SettingsBundleWatchlistsInner items(List<WatchlistItem>? items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SettingsBundleWatchlistsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SettingsBundleWatchlistsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SettingsBundleWatchlistsInner call({
    Object? watchlist = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
  }) {
    return SettingsBundleWatchlistsInner(
      watchlist: watchlist == const $CopyWithPlaceholder()
          ? _value.watchlist
          // ignore: cast_nullable_to_non_nullable
          : watchlist as Watchlist?,
      items: items == const $CopyWithPlaceholder()
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<WatchlistItem>?,
    );
  }
}

extension $SettingsBundleWatchlistsInnerCopyWith
    on SettingsBundleWatchlistsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSettingsBundleWatchlistsInner.copyWith(...)` or `instanceOfSettingsBundleWatchlistsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SettingsBundleWatchlistsInnerCWProxy get copyWith =>
      _$SettingsBundleWatchlistsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SettingsBundleWatchlistsInner _$SettingsBundleWatchlistsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SettingsBundleWatchlistsInner', json, ($checkedConvert) {
  final val = SettingsBundleWatchlistsInner(
    watchlist: $checkedConvert(
      'watchlist',
      (v) => v == null ? null : Watchlist.fromJson(v as Map<String, dynamic>),
    ),
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>?)
          ?.map((e) => WatchlistItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SettingsBundleWatchlistsInnerToJson(
  SettingsBundleWatchlistsInner instance,
) => <String, dynamic>{
  'watchlist': ?instance.watchlist?.toJson(),
  'items': ?instance.items?.map((e) => e.toJson()).toList(),
};
