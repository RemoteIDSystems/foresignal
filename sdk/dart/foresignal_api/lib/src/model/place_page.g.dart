// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlacePageCWProxy {
  PlacePage items(List<Place> items);

  PlacePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlacePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlacePage(...).copyWith(id: 12, name: "My name")
  /// ```
  PlacePage call({List<Place> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlacePage.copyWith(...)` or call `instanceOfPlacePage.copyWith.fieldName(value)` for a single field.
class _$PlacePageCWProxyImpl implements _$PlacePageCWProxy {
  const _$PlacePageCWProxyImpl(this._value);

  final PlacePage _value;

  @override
  PlacePage items(List<Place> items) => call(items: items);

  @override
  PlacePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlacePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlacePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlacePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return PlacePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Place>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $PlacePageCopyWith on PlacePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlacePage.copyWith(...)` or `instanceOfPlacePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlacePageCWProxy get copyWith => _$PlacePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlacePage _$PlacePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlacePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = PlacePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Place.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PlacePageToJson(PlacePage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
