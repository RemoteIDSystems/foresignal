// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_preferences_notifications.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UserPreferencesNotificationsCWProxy {
  UserPreferencesNotifications emailEnabled(bool? emailEnabled);

  UserPreferencesNotifications pushEnabled(bool? pushEnabled);

  UserPreferencesNotifications quietHours(String? quietHours);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserPreferencesNotifications(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserPreferencesNotifications(...).copyWith(id: 12, name: "My name")
  /// ```
  UserPreferencesNotifications call({
    bool? emailEnabled,
    bool? pushEnabled,
    String? quietHours,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUserPreferencesNotifications.copyWith(...)` or call `instanceOfUserPreferencesNotifications.copyWith.fieldName(value)` for a single field.
class _$UserPreferencesNotificationsCWProxyImpl
    implements _$UserPreferencesNotificationsCWProxy {
  const _$UserPreferencesNotificationsCWProxyImpl(this._value);

  final UserPreferencesNotifications _value;

  @override
  UserPreferencesNotifications emailEnabled(bool? emailEnabled) =>
      call(emailEnabled: emailEnabled);

  @override
  UserPreferencesNotifications pushEnabled(bool? pushEnabled) =>
      call(pushEnabled: pushEnabled);

  @override
  UserPreferencesNotifications quietHours(String? quietHours) =>
      call(quietHours: quietHours);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserPreferencesNotifications(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserPreferencesNotifications(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UserPreferencesNotifications call({
    Object? emailEnabled = const $CopyWithPlaceholder(),
    Object? pushEnabled = const $CopyWithPlaceholder(),
    Object? quietHours = const $CopyWithPlaceholder(),
  }) {
    return UserPreferencesNotifications(
      emailEnabled: emailEnabled == const $CopyWithPlaceholder()
          ? _value.emailEnabled
          // ignore: cast_nullable_to_non_nullable
          : emailEnabled as bool?,
      pushEnabled: pushEnabled == const $CopyWithPlaceholder()
          ? _value.pushEnabled
          // ignore: cast_nullable_to_non_nullable
          : pushEnabled as bool?,
      quietHours: quietHours == const $CopyWithPlaceholder()
          ? _value.quietHours
          // ignore: cast_nullable_to_non_nullable
          : quietHours as String?,
    );
  }
}

extension $UserPreferencesNotificationsCopyWith
    on UserPreferencesNotifications {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUserPreferencesNotifications.copyWith(...)` or `instanceOfUserPreferencesNotifications.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UserPreferencesNotificationsCWProxy get copyWith =>
      _$UserPreferencesNotificationsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserPreferencesNotifications _$UserPreferencesNotificationsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('UserPreferencesNotifications', json, ($checkedConvert) {
  final val = UserPreferencesNotifications(
    emailEnabled: $checkedConvert('emailEnabled', (v) => v as bool? ?? true),
    pushEnabled: $checkedConvert('pushEnabled', (v) => v as bool? ?? true),
    quietHours: $checkedConvert('quietHours', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$UserPreferencesNotificationsToJson(
  UserPreferencesNotifications instance,
) => <String, dynamic>{
  'emailEnabled': ?instance.emailEnabled,
  'pushEnabled': ?instance.pushEnabled,
  'quietHours': ?instance.quietHours,
};
