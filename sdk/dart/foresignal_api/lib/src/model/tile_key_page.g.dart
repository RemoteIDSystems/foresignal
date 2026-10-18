// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tile_key_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TileKeyPageCWProxy {
  TileKeyPage items(List<TileKey> items);

  TileKeyPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileKeyPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileKeyPage(...).copyWith(id: 12, name: "My name")
  /// ```
  TileKeyPage call({List<TileKey> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTileKeyPage.copyWith(...)` or call `instanceOfTileKeyPage.copyWith.fieldName(value)` for a single field.
class _$TileKeyPageCWProxyImpl implements _$TileKeyPageCWProxy {
  const _$TileKeyPageCWProxyImpl(this._value);

  final TileKeyPage _value;

  @override
  TileKeyPage items(List<TileKey> items) => call(items: items);

  @override
  TileKeyPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TileKeyPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TileKeyPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TileKeyPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return TileKeyPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<TileKey>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $TileKeyPageCopyWith on TileKeyPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTileKeyPage.copyWith(...)` or `instanceOfTileKeyPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TileKeyPageCWProxy get copyWith => _$TileKeyPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TileKeyPage _$TileKeyPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TileKeyPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = TileKeyPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => TileKey.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$TileKeyPageToJson(TileKeyPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
