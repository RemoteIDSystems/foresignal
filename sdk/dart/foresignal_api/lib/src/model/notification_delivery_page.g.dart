// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_delivery_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationDeliveryPageCWProxy {
  NotificationDeliveryPage items(List<NotificationDelivery> items);

  NotificationDeliveryPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationDeliveryPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationDeliveryPage(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationDeliveryPage call({
    List<NotificationDelivery> items,
    String? nextCursor,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationDeliveryPage.copyWith(...)` or call `instanceOfNotificationDeliveryPage.copyWith.fieldName(value)` for a single field.
class _$NotificationDeliveryPageCWProxyImpl
    implements _$NotificationDeliveryPageCWProxy {
  const _$NotificationDeliveryPageCWProxyImpl(this._value);

  final NotificationDeliveryPage _value;

  @override
  NotificationDeliveryPage items(List<NotificationDelivery> items) =>
      call(items: items);

  @override
  NotificationDeliveryPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationDeliveryPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationDeliveryPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationDeliveryPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return NotificationDeliveryPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<NotificationDelivery>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $NotificationDeliveryPageCopyWith on NotificationDeliveryPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationDeliveryPage.copyWith(...)` or `instanceOfNotificationDeliveryPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationDeliveryPageCWProxy get copyWith =>
      _$NotificationDeliveryPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationDeliveryPage _$NotificationDeliveryPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationDeliveryPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items']);
  final val = NotificationDeliveryPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => NotificationDelivery.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$NotificationDeliveryPageToJson(
  NotificationDeliveryPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
