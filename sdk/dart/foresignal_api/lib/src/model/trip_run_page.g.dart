// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trip_run_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TripRunPageCWProxy {
  TripRunPage items(List<TripRun> items);

  TripRunPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripRunPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripRunPage(...).copyWith(id: 12, name: "My name")
  /// ```
  TripRunPage call({List<TripRun> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTripRunPage.copyWith(...)` or call `instanceOfTripRunPage.copyWith.fieldName(value)` for a single field.
class _$TripRunPageCWProxyImpl implements _$TripRunPageCWProxy {
  const _$TripRunPageCWProxyImpl(this._value);

  final TripRunPage _value;

  @override
  TripRunPage items(List<TripRun> items) => call(items: items);

  @override
  TripRunPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripRunPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripRunPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TripRunPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return TripRunPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<TripRun>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $TripRunPageCopyWith on TripRunPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTripRunPage.copyWith(...)` or `instanceOfTripRunPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TripRunPageCWProxy get copyWith => _$TripRunPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TripRunPage _$TripRunPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TripRunPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = TripRunPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => TripRun.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$TripRunPageToJson(TripRunPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
