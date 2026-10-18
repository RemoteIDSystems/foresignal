// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_preference_input_channel.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationPreferenceInputChannelCWProxy {
  NotificationPreferenceInputChannel type(
    NotificationPreferenceInputChannelTypeEnum type,
  );

  NotificationPreferenceInputChannel config(Map<String, Object>? config);

  NotificationPreferenceInputChannel secrets(Map<String, String>? secrets);

  NotificationPreferenceInputChannel language(String? language);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationPreferenceInputChannel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationPreferenceInputChannel(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationPreferenceInputChannel call({
    NotificationPreferenceInputChannelTypeEnum type,
    Map<String, Object>? config,
    Map<String, String>? secrets,
    String? language,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationPreferenceInputChannel.copyWith(...)` or call `instanceOfNotificationPreferenceInputChannel.copyWith.fieldName(value)` for a single field.
class _$NotificationPreferenceInputChannelCWProxyImpl
    implements _$NotificationPreferenceInputChannelCWProxy {
  const _$NotificationPreferenceInputChannelCWProxyImpl(this._value);

  final NotificationPreferenceInputChannel _value;

  @override
  NotificationPreferenceInputChannel type(
    NotificationPreferenceInputChannelTypeEnum type,
  ) => call(type: type);

  @override
  NotificationPreferenceInputChannel config(Map<String, Object>? config) =>
      call(config: config);

  @override
  NotificationPreferenceInputChannel secrets(Map<String, String>? secrets) =>
      call(secrets: secrets);

  @override
  NotificationPreferenceInputChannel language(String? language) =>
      call(language: language);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationPreferenceInputChannel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationPreferenceInputChannel(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationPreferenceInputChannel call({
    Object? type = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? secrets = const $CopyWithPlaceholder(),
    Object? language = const $CopyWithPlaceholder(),
  }) {
    return NotificationPreferenceInputChannel(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as NotificationPreferenceInputChannelTypeEnum,
      config: config == const $CopyWithPlaceholder()
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>?,
      secrets: secrets == const $CopyWithPlaceholder()
          ? _value.secrets
          // ignore: cast_nullable_to_non_nullable
          : secrets as Map<String, String>?,
      language: language == const $CopyWithPlaceholder()
          ? _value.language
          // ignore: cast_nullable_to_non_nullable
          : language as String?,
    );
  }
}

extension $NotificationPreferenceInputChannelCopyWith
    on NotificationPreferenceInputChannel {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationPreferenceInputChannel.copyWith(...)` or `instanceOfNotificationPreferenceInputChannel.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationPreferenceInputChannelCWProxy get copyWith =>
      _$NotificationPreferenceInputChannelCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationPreferenceInputChannel _$NotificationPreferenceInputChannelFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationPreferenceInputChannel', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = NotificationPreferenceInputChannel(
    type: $checkedConvert(
      'type',
      (v) =>
          $enumDecode(_$NotificationPreferenceInputChannelTypeEnumEnumMap, v),
    ),
    config: $checkedConvert(
      'config',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    secrets: $checkedConvert(
      'secrets',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    language: $checkedConvert('language', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$NotificationPreferenceInputChannelToJson(
  NotificationPreferenceInputChannel instance,
) => <String, dynamic>{
  'type': _$NotificationPreferenceInputChannelTypeEnumEnumMap[instance.type]!,
  'config': ?instance.config,
  'secrets': ?instance.secrets,
  'language': ?instance.language,
};

const _$NotificationPreferenceInputChannelTypeEnumEnumMap = {
  NotificationPreferenceInputChannelTypeEnum.webhook: 'webhook',
  NotificationPreferenceInputChannelTypeEnum.slack: 'slack',
  NotificationPreferenceInputChannelTypeEnum.mattermost: 'mattermost',
  NotificationPreferenceInputChannelTypeEnum.discord: 'discord',
  NotificationPreferenceInputChannelTypeEnum.teams: 'teams',
  NotificationPreferenceInputChannelTypeEnum.googleChat: 'google-chat',
  NotificationPreferenceInputChannelTypeEnum.telegram: 'telegram',
  NotificationPreferenceInputChannelTypeEnum.matrix: 'matrix',
  NotificationPreferenceInputChannelTypeEnum.ntfy: 'ntfy',
  NotificationPreferenceInputChannelTypeEnum.gotify: 'gotify',
  NotificationPreferenceInputChannelTypeEnum.pushover: 'pushover',
  NotificationPreferenceInputChannelTypeEnum.email: 'email',
  NotificationPreferenceInputChannelTypeEnum.webpush: 'webpush',
};
