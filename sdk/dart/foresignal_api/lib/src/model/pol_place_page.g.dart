// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'pol_place_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PolPlacePageCWProxy {
  PolPlacePage items(List<PolPlace> items);

  PolPlacePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolPlacePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolPlacePage(...).copyWith(id: 12, name: "My name")
  /// ```
  PolPlacePage call({List<PolPlace> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPolPlacePage.copyWith(...)` or call `instanceOfPolPlacePage.copyWith.fieldName(value)` for a single field.
class _$PolPlacePageCWProxyImpl implements _$PolPlacePageCWProxy {
  const _$PolPlacePageCWProxyImpl(this._value);

  final PolPlacePage _value;

  @override
  PolPlacePage items(List<PolPlace> items) => call(items: items);

  @override
  PolPlacePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PolPlacePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PolPlacePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PolPlacePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return PolPlacePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<PolPlace>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $PolPlacePageCopyWith on PolPlacePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPolPlacePage.copyWith(...)` or `instanceOfPolPlacePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PolPlacePageCWProxy get copyWith => _$PolPlacePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PolPlacePage _$PolPlacePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PolPlacePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = PolPlacePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => PolPlace.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PolPlacePageToJson(PolPlacePage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
