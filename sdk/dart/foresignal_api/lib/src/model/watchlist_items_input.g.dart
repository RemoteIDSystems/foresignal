// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'watchlist_items_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WatchlistItemsInputCWProxy {
  WatchlistItemsInput items(List<WatchlistItemInput> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistItemsInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistItemsInput(...).copyWith(id: 12, name: "My name")
  /// ```
  WatchlistItemsInput call({List<WatchlistItemInput> items});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWatchlistItemsInput.copyWith(...)` or call `instanceOfWatchlistItemsInput.copyWith.fieldName(value)` for a single field.
class _$WatchlistItemsInputCWProxyImpl implements _$WatchlistItemsInputCWProxy {
  const _$WatchlistItemsInputCWProxyImpl(this._value);

  final WatchlistItemsInput _value;

  @override
  WatchlistItemsInput items(List<WatchlistItemInput> items) =>
      call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WatchlistItemsInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WatchlistItemsInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WatchlistItemsInput call({Object? items = const $CopyWithPlaceholder()}) {
    return WatchlistItemsInput(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<WatchlistItemInput>,
    );
  }
}

extension $WatchlistItemsInputCopyWith on WatchlistItemsInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWatchlistItemsInput.copyWith(...)` or `instanceOfWatchlistItemsInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WatchlistItemsInputCWProxy get copyWith =>
      _$WatchlistItemsInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WatchlistItemsInput _$WatchlistItemsInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WatchlistItemsInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = WatchlistItemsInput(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map(
                (e) => WatchlistItemInput.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$WatchlistItemsInputToJson(
  WatchlistItemsInput instance,
) => <String, dynamic>{'items': instance.items.map((e) => e.toJson()).toList()};
