// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationPageCWProxy {
  NotificationPage items(List<Notification> items);

  NotificationPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationPage(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationPage call({List<Notification> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationPage.copyWith(...)` or call `instanceOfNotificationPage.copyWith.fieldName(value)` for a single field.
class _$NotificationPageCWProxyImpl implements _$NotificationPageCWProxy {
  const _$NotificationPageCWProxyImpl(this._value);

  final NotificationPage _value;

  @override
  NotificationPage items(List<Notification> items) => call(items: items);

  @override
  NotificationPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return NotificationPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Notification>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $NotificationPageCopyWith on NotificationPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationPage.copyWith(...)` or `instanceOfNotificationPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationPageCWProxy get copyWith => _$NotificationPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationPage _$NotificationPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NotificationPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = NotificationPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Notification.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$NotificationPageToJson(NotificationPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
