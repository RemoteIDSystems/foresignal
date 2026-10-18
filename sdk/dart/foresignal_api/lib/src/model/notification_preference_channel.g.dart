// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_preference_channel.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationPreferenceChannelCWProxy {
  NotificationPreferenceChannel channelId(String? channelId);

  NotificationPreferenceChannel type(
    NotificationPreferenceChannelTypeEnum type,
  );

  NotificationPreferenceChannel config(Map<String, Object>? config);

  NotificationPreferenceChannel secretFields(List<String>? secretFields);

  NotificationPreferenceChannel language(String? language);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationPreferenceChannel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationPreferenceChannel(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationPreferenceChannel call({
    String? channelId,
    NotificationPreferenceChannelTypeEnum type,
    Map<String, Object>? config,
    List<String>? secretFields,
    String? language,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationPreferenceChannel.copyWith(...)` or call `instanceOfNotificationPreferenceChannel.copyWith.fieldName(value)` for a single field.
class _$NotificationPreferenceChannelCWProxyImpl
    implements _$NotificationPreferenceChannelCWProxy {
  const _$NotificationPreferenceChannelCWProxyImpl(this._value);

  final NotificationPreferenceChannel _value;

  @override
  NotificationPreferenceChannel channelId(String? channelId) =>
      call(channelId: channelId);

  @override
  NotificationPreferenceChannel type(
    NotificationPreferenceChannelTypeEnum type,
  ) => call(type: type);

  @override
  NotificationPreferenceChannel config(Map<String, Object>? config) =>
      call(config: config);

  @override
  NotificationPreferenceChannel secretFields(List<String>? secretFields) =>
      call(secretFields: secretFields);

  @override
  NotificationPreferenceChannel language(String? language) =>
      call(language: language);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationPreferenceChannel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationPreferenceChannel(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationPreferenceChannel call({
    Object? channelId = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? secretFields = const $CopyWithPlaceholder(),
    Object? language = const $CopyWithPlaceholder(),
  }) {
    return NotificationPreferenceChannel(
      channelId: channelId == const $CopyWithPlaceholder()
          ? _value.channelId
          // ignore: cast_nullable_to_non_nullable
          : channelId as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as NotificationPreferenceChannelTypeEnum,
      config: config == const $CopyWithPlaceholder()
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>?,
      secretFields: secretFields == const $CopyWithPlaceholder()
          ? _value.secretFields
          // ignore: cast_nullable_to_non_nullable
          : secretFields as List<String>?,
      language: language == const $CopyWithPlaceholder()
          ? _value.language
          // ignore: cast_nullable_to_non_nullable
          : language as String?,
    );
  }
}

extension $NotificationPreferenceChannelCopyWith
    on NotificationPreferenceChannel {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationPreferenceChannel.copyWith(...)` or `instanceOfNotificationPreferenceChannel.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationPreferenceChannelCWProxy get copyWith =>
      _$NotificationPreferenceChannelCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationPreferenceChannel _$NotificationPreferenceChannelFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationPreferenceChannel', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = NotificationPreferenceChannel(
    channelId: $checkedConvert('channelId', (v) => v as String?),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$NotificationPreferenceChannelTypeEnumEnumMap, v),
    ),
    config: $checkedConvert(
      'config',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    secretFields: $checkedConvert(
      'secretFields',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    language: $checkedConvert('language', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$NotificationPreferenceChannelToJson(
  NotificationPreferenceChannel instance,
) => <String, dynamic>{
  'channelId': ?instance.channelId,
  'type': _$NotificationPreferenceChannelTypeEnumEnumMap[instance.type]!,
  'config': ?instance.config,
  'secretFields': ?instance.secretFields,
  'language': ?instance.language,
};

const _$NotificationPreferenceChannelTypeEnumEnumMap = {
  NotificationPreferenceChannelTypeEnum.webhook: 'webhook',
  NotificationPreferenceChannelTypeEnum.slack: 'slack',
  NotificationPreferenceChannelTypeEnum.mattermost: 'mattermost',
  NotificationPreferenceChannelTypeEnum.discord: 'discord',
  NotificationPreferenceChannelTypeEnum.teams: 'teams',
  NotificationPreferenceChannelTypeEnum.googleChat: 'google-chat',
  NotificationPreferenceChannelTypeEnum.telegram: 'telegram',
  NotificationPreferenceChannelTypeEnum.matrix: 'matrix',
  NotificationPreferenceChannelTypeEnum.ntfy: 'ntfy',
  NotificationPreferenceChannelTypeEnum.gotify: 'gotify',
  NotificationPreferenceChannelTypeEnum.pushover: 'pushover',
  NotificationPreferenceChannelTypeEnum.email: 'email',
  NotificationPreferenceChannelTypeEnum.webpush: 'webpush',
};
