// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trip_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TripPageCWProxy {
  TripPage items(List<Trip> items);

  TripPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripPage(...).copyWith(id: 12, name: "My name")
  /// ```
  TripPage call({List<Trip> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTripPage.copyWith(...)` or call `instanceOfTripPage.copyWith.fieldName(value)` for a single field.
class _$TripPageCWProxyImpl implements _$TripPageCWProxy {
  const _$TripPageCWProxyImpl(this._value);

  final TripPage _value;

  @override
  TripPage items(List<Trip> items) => call(items: items);

  @override
  TripPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TripPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return TripPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Trip>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $TripPageCopyWith on TripPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTripPage.copyWith(...)` or `instanceOfTripPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TripPageCWProxy get copyWith => _$TripPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TripPage _$TripPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TripPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = TripPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Trip.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$TripPageToJson(TripPage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
