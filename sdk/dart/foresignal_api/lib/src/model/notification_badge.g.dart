// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_badge.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationBadgeCWProxy {
  NotificationBadge unread(int unread);

  NotificationBadge unreadCritical(int unreadCritical);

  NotificationBadge latestId(int? latestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationBadge(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationBadge(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationBadge call({int unread, int unreadCritical, int? latestId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationBadge.copyWith(...)` or call `instanceOfNotificationBadge.copyWith.fieldName(value)` for a single field.
class _$NotificationBadgeCWProxyImpl implements _$NotificationBadgeCWProxy {
  const _$NotificationBadgeCWProxyImpl(this._value);

  final NotificationBadge _value;

  @override
  NotificationBadge unread(int unread) => call(unread: unread);

  @override
  NotificationBadge unreadCritical(int unreadCritical) =>
      call(unreadCritical: unreadCritical);

  @override
  NotificationBadge latestId(int? latestId) => call(latestId: latestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationBadge(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationBadge(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationBadge call({
    Object? unread = const $CopyWithPlaceholder(),
    Object? unreadCritical = const $CopyWithPlaceholder(),
    Object? latestId = const $CopyWithPlaceholder(),
  }) {
    return NotificationBadge(
      unread: unread == const $CopyWithPlaceholder() || unread == null
          ? _value.unread
          // ignore: cast_nullable_to_non_nullable
          : unread as int,
      unreadCritical:
          unreadCritical == const $CopyWithPlaceholder() ||
              unreadCritical == null
          ? _value.unreadCritical
          // ignore: cast_nullable_to_non_nullable
          : unreadCritical as int,
      latestId: latestId == const $CopyWithPlaceholder()
          ? _value.latestId
          // ignore: cast_nullable_to_non_nullable
          : latestId as int?,
    );
  }
}

extension $NotificationBadgeCopyWith on NotificationBadge {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationBadge.copyWith(...)` or `instanceOfNotificationBadge.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationBadgeCWProxy get copyWith =>
      _$NotificationBadgeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationBadge _$NotificationBadgeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NotificationBadge', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['unread', 'unreadCritical']);
      final val = NotificationBadge(
        unread: $checkedConvert('unread', (v) => (v as num).toInt()),
        unreadCritical: $checkedConvert(
          'unreadCritical',
          (v) => (v as num).toInt(),
        ),
        latestId: $checkedConvert('latestId', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$NotificationBadgeToJson(NotificationBadge instance) =>
    <String, dynamic>{
      'unread': instance.unread,
      'unreadCritical': instance.unreadCritical,
      'latestId': ?instance.latestId,
    };
