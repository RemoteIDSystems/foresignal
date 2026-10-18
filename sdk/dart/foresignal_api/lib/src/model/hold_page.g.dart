// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hold_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HoldPageCWProxy {
  HoldPage items(List<Hold> items);

  HoldPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HoldPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HoldPage(...).copyWith(id: 12, name: "My name")
  /// ```
  HoldPage call({List<Hold> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHoldPage.copyWith(...)` or call `instanceOfHoldPage.copyWith.fieldName(value)` for a single field.
class _$HoldPageCWProxyImpl implements _$HoldPageCWProxy {
  const _$HoldPageCWProxyImpl(this._value);

  final HoldPage _value;

  @override
  HoldPage items(List<Hold> items) => call(items: items);

  @override
  HoldPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HoldPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HoldPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HoldPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return HoldPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Hold>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $HoldPageCopyWith on HoldPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHoldPage.copyWith(...)` or `instanceOfHoldPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HoldPageCWProxy get copyWith => _$HoldPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HoldPage _$HoldPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HoldPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = HoldPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Hold.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$HoldPageToJson(HoldPage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
