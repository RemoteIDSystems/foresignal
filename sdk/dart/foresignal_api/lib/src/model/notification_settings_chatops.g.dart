// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_settings_chatops.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationSettingsChatopsCWProxy {
  NotificationSettingsChatops enabled(bool? enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationSettingsChatops(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationSettingsChatops(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationSettingsChatops call({bool? enabled});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationSettingsChatops.copyWith(...)` or call `instanceOfNotificationSettingsChatops.copyWith.fieldName(value)` for a single field.
class _$NotificationSettingsChatopsCWProxyImpl
    implements _$NotificationSettingsChatopsCWProxy {
  const _$NotificationSettingsChatopsCWProxyImpl(this._value);

  final NotificationSettingsChatops _value;

  @override
  NotificationSettingsChatops enabled(bool? enabled) => call(enabled: enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationSettingsChatops(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationSettingsChatops(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationSettingsChatops call({
    Object? enabled = const $CopyWithPlaceholder(),
  }) {
    return NotificationSettingsChatops(
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
    );
  }
}

extension $NotificationSettingsChatopsCopyWith on NotificationSettingsChatops {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationSettingsChatops.copyWith(...)` or `instanceOfNotificationSettingsChatops.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationSettingsChatopsCWProxy get copyWith =>
      _$NotificationSettingsChatopsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationSettingsChatops _$NotificationSettingsChatopsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationSettingsChatops', json, ($checkedConvert) {
  final val = NotificationSettingsChatops(
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
  );
  return val;
});

Map<String, dynamic> _$NotificationSettingsChatopsToJson(
  NotificationSettingsChatops instance,
) => <String, dynamic>{'enabled': ?instance.enabled};
