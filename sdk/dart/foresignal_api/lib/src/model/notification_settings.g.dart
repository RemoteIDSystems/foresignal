// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationSettingsCWProxy {
  NotificationSettings pausedUntil(DateTime? pausedUntil);

  NotificationSettings pauseBypassSeverity(
    NotificationSettingsPauseBypassSeverityEnum? pauseBypassSeverity,
  );

  NotificationSettings chatops(NotificationSettingsChatops? chatops);

  NotificationSettings imageLinks(bool? imageLinks);

  NotificationSettings defaultLanguage(String? defaultLanguage);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationSettings call({
    DateTime? pausedUntil,
    NotificationSettingsPauseBypassSeverityEnum? pauseBypassSeverity,
    NotificationSettingsChatops? chatops,
    bool? imageLinks,
    String? defaultLanguage,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationSettings.copyWith(...)` or call `instanceOfNotificationSettings.copyWith.fieldName(value)` for a single field.
class _$NotificationSettingsCWProxyImpl
    implements _$NotificationSettingsCWProxy {
  const _$NotificationSettingsCWProxyImpl(this._value);

  final NotificationSettings _value;

  @override
  NotificationSettings pausedUntil(DateTime? pausedUntil) =>
      call(pausedUntil: pausedUntil);

  @override
  NotificationSettings pauseBypassSeverity(
    NotificationSettingsPauseBypassSeverityEnum? pauseBypassSeverity,
  ) => call(pauseBypassSeverity: pauseBypassSeverity);

  @override
  NotificationSettings chatops(NotificationSettingsChatops? chatops) =>
      call(chatops: chatops);

  @override
  NotificationSettings imageLinks(bool? imageLinks) =>
      call(imageLinks: imageLinks);

  @override
  NotificationSettings defaultLanguage(String? defaultLanguage) =>
      call(defaultLanguage: defaultLanguage);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationSettings call({
    Object? pausedUntil = const $CopyWithPlaceholder(),
    Object? pauseBypassSeverity = const $CopyWithPlaceholder(),
    Object? chatops = const $CopyWithPlaceholder(),
    Object? imageLinks = const $CopyWithPlaceholder(),
    Object? defaultLanguage = const $CopyWithPlaceholder(),
  }) {
    return NotificationSettings(
      pausedUntil: pausedUntil == const $CopyWithPlaceholder()
          ? _value.pausedUntil
          // ignore: cast_nullable_to_non_nullable
          : pausedUntil as DateTime?,
      pauseBypassSeverity: pauseBypassSeverity == const $CopyWithPlaceholder()
          ? _value.pauseBypassSeverity
          // ignore: cast_nullable_to_non_nullable
          : pauseBypassSeverity as NotificationSettingsPauseBypassSeverityEnum?,
      chatops: chatops == const $CopyWithPlaceholder()
          ? _value.chatops
          // ignore: cast_nullable_to_non_nullable
          : chatops as NotificationSettingsChatops?,
      imageLinks: imageLinks == const $CopyWithPlaceholder()
          ? _value.imageLinks
          // ignore: cast_nullable_to_non_nullable
          : imageLinks as bool?,
      defaultLanguage: defaultLanguage == const $CopyWithPlaceholder()
          ? _value.defaultLanguage
          // ignore: cast_nullable_to_non_nullable
          : defaultLanguage as String?,
    );
  }
}

extension $NotificationSettingsCopyWith on NotificationSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationSettings.copyWith(...)` or `instanceOfNotificationSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationSettingsCWProxy get copyWith =>
      _$NotificationSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationSettings _$NotificationSettingsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationSettings', json, ($checkedConvert) {
  final val = NotificationSettings(
    pausedUntil: $checkedConvert(
      'pausedUntil',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    pauseBypassSeverity: $checkedConvert(
      'pauseBypassSeverity',
      (v) => $enumDecodeNullable(
        _$NotificationSettingsPauseBypassSeverityEnumEnumMap,
        v,
      ),
    ),
    chatops: $checkedConvert(
      'chatops',
      (v) => v == null
          ? null
          : NotificationSettingsChatops.fromJson(v as Map<String, dynamic>),
    ),
    imageLinks: $checkedConvert('imageLinks', (v) => v as bool? ?? false),
    defaultLanguage: $checkedConvert(
      'defaultLanguage',
      (v) => v as String? ?? 'en',
    ),
  );
  return val;
});

Map<String, dynamic> _$NotificationSettingsToJson(
  NotificationSettings instance,
) => <String, dynamic>{
  'pausedUntil': ?instance.pausedUntil?.toIso8601String(),
  'pauseBypassSeverity':
      ?_$NotificationSettingsPauseBypassSeverityEnumEnumMap[instance
          .pauseBypassSeverity],
  'chatops': ?instance.chatops?.toJson(),
  'imageLinks': ?instance.imageLinks,
  'defaultLanguage': ?instance.defaultLanguage,
};

const _$NotificationSettingsPauseBypassSeverityEnumEnumMap = {
  NotificationSettingsPauseBypassSeverityEnum.info: 'info',
  NotificationSettingsPauseBypassSeverityEnum.warning: 'warning',
  NotificationSettingsPauseBypassSeverityEnum.critical: 'critical',
};
