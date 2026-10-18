// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_notifications.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoNotificationsCWProxy {
  SystemInfoNotifications channels(int? channels);

  SystemInfoNotifications pendingDeliveries(int? pendingDeliveries);

  SystemInfoNotifications failedLast24h(int? failedLast24h);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoNotifications(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoNotifications(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoNotifications call({
    int? channels,
    int? pendingDeliveries,
    int? failedLast24h,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoNotifications.copyWith(...)` or call `instanceOfSystemInfoNotifications.copyWith.fieldName(value)` for a single field.
class _$SystemInfoNotificationsCWProxyImpl
    implements _$SystemInfoNotificationsCWProxy {
  const _$SystemInfoNotificationsCWProxyImpl(this._value);

  final SystemInfoNotifications _value;

  @override
  SystemInfoNotifications channels(int? channels) => call(channels: channels);

  @override
  SystemInfoNotifications pendingDeliveries(int? pendingDeliveries) =>
      call(pendingDeliveries: pendingDeliveries);

  @override
  SystemInfoNotifications failedLast24h(int? failedLast24h) =>
      call(failedLast24h: failedLast24h);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoNotifications(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoNotifications(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoNotifications call({
    Object? channels = const $CopyWithPlaceholder(),
    Object? pendingDeliveries = const $CopyWithPlaceholder(),
    Object? failedLast24h = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoNotifications(
      channels: channels == const $CopyWithPlaceholder()
          ? _value.channels
          // ignore: cast_nullable_to_non_nullable
          : channels as int?,
      pendingDeliveries: pendingDeliveries == const $CopyWithPlaceholder()
          ? _value.pendingDeliveries
          // ignore: cast_nullable_to_non_nullable
          : pendingDeliveries as int?,
      failedLast24h: failedLast24h == const $CopyWithPlaceholder()
          ? _value.failedLast24h
          // ignore: cast_nullable_to_non_nullable
          : failedLast24h as int?,
    );
  }
}

extension $SystemInfoNotificationsCopyWith on SystemInfoNotifications {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoNotifications.copyWith(...)` or `instanceOfSystemInfoNotifications.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoNotificationsCWProxy get copyWith =>
      _$SystemInfoNotificationsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoNotifications _$SystemInfoNotificationsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SystemInfoNotifications', json, ($checkedConvert) {
  final val = SystemInfoNotifications(
    channels: $checkedConvert('channels', (v) => (v as num?)?.toInt()),
    pendingDeliveries: $checkedConvert(
      'pendingDeliveries',
      (v) => (v as num?)?.toInt(),
    ),
    failedLast24h: $checkedConvert(
      'failedLast24h',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SystemInfoNotificationsToJson(
  SystemInfoNotifications instance,
) => <String, dynamic>{
  'channels': ?instance.channels,
  'pendingDeliveries': ?instance.pendingDeliveries,
  'failedLast24h': ?instance.failedLast24h,
};
