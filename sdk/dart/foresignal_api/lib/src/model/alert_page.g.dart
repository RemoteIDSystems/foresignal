// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alert_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AlertPageCWProxy {
  AlertPage items(List<Alert> items);

  AlertPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertPage(...).copyWith(id: 12, name: "My name")
  /// ```
  AlertPage call({List<Alert> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAlertPage.copyWith(...)` or call `instanceOfAlertPage.copyWith.fieldName(value)` for a single field.
class _$AlertPageCWProxyImpl implements _$AlertPageCWProxy {
  const _$AlertPageCWProxyImpl(this._value);

  final AlertPage _value;

  @override
  AlertPage items(List<Alert> items) => call(items: items);

  @override
  AlertPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AlertPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return AlertPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Alert>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $AlertPageCopyWith on AlertPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAlertPage.copyWith(...)` or `instanceOfAlertPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AlertPageCWProxy get copyWith => _$AlertPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AlertPage _$AlertPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AlertPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = AlertPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Alert.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AlertPageToJson(AlertPage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
