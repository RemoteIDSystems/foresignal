// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_preferences.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UserPreferencesCWProxy {
  UserPreferences timezone(String? timezone);

  UserPreferences locale(String? locale);

  UserPreferences theme(String? theme);

  UserPreferences notifications(UserPreferencesNotifications? notifications);

  UserPreferences ui(Map<String, Object>? ui);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserPreferences(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserPreferences(...).copyWith(id: 12, name: "My name")
  /// ```
  UserPreferences call({
    String? timezone,
    String? locale,
    String? theme,
    UserPreferencesNotifications? notifications,
    Map<String, Object>? ui,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUserPreferences.copyWith(...)` or call `instanceOfUserPreferences.copyWith.fieldName(value)` for a single field.
class _$UserPreferencesCWProxyImpl implements _$UserPreferencesCWProxy {
  const _$UserPreferencesCWProxyImpl(this._value);

  final UserPreferences _value;

  @override
  UserPreferences timezone(String? timezone) => call(timezone: timezone);

  @override
  UserPreferences locale(String? locale) => call(locale: locale);

  @override
  UserPreferences theme(String? theme) => call(theme: theme);

  @override
  UserPreferences notifications(UserPreferencesNotifications? notifications) =>
      call(notifications: notifications);

  @override
  UserPreferences ui(Map<String, Object>? ui) => call(ui: ui);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserPreferences(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserPreferences(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UserPreferences call({
    Object? timezone = const $CopyWithPlaceholder(),
    Object? locale = const $CopyWithPlaceholder(),
    Object? theme = const $CopyWithPlaceholder(),
    Object? notifications = const $CopyWithPlaceholder(),
    Object? ui = const $CopyWithPlaceholder(),
  }) {
    return UserPreferences(
      timezone: timezone == const $CopyWithPlaceholder()
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String?,
      locale: locale == const $CopyWithPlaceholder()
          ? _value.locale
          // ignore: cast_nullable_to_non_nullable
          : locale as String?,
      theme: theme == const $CopyWithPlaceholder()
          ? _value.theme
          // ignore: cast_nullable_to_non_nullable
          : theme as String?,
      notifications: notifications == const $CopyWithPlaceholder()
          ? _value.notifications
          // ignore: cast_nullable_to_non_nullable
          : notifications as UserPreferencesNotifications?,
      ui: ui == const $CopyWithPlaceholder()
          ? _value.ui
          // ignore: cast_nullable_to_non_nullable
          : ui as Map<String, Object>?,
    );
  }
}

extension $UserPreferencesCopyWith on UserPreferences {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUserPreferences.copyWith(...)` or `instanceOfUserPreferences.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UserPreferencesCWProxy get copyWith => _$UserPreferencesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserPreferences _$UserPreferencesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('UserPreferences', json, ($checkedConvert) {
  final val = UserPreferences(
    timezone: $checkedConvert('timezone', (v) => v as String?),
    locale: $checkedConvert('locale', (v) => v as String?),
    theme: $checkedConvert('theme', (v) => v as String?),
    notifications: $checkedConvert(
      'notifications',
      (v) => v == null
          ? null
          : UserPreferencesNotifications.fromJson(v as Map<String, dynamic>),
    ),
    ui: $checkedConvert(
      'ui',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$UserPreferencesToJson(UserPreferences instance) =>
    <String, dynamic>{
      'timezone': ?instance.timezone,
      'locale': ?instance.locale,
      'theme': ?instance.theme,
      'notifications': ?instance.notifications?.toJson(),
      'ui': ?instance.ui,
    };
