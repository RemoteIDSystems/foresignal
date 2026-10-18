// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'interactive_chat_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InteractiveChatConfigCWProxy {
  InteractiveChatConfig interactive(bool? interactive);

  InteractiveChatConfig allowedChats(List<AllowedChat>? allowedChats);

  InteractiveChatConfig askRateLimitPerHour(int? askRateLimitPerHour);

  InteractiveChatConfig commandRateLimit(int? commandRateLimit);

  InteractiveChatConfig takeOverWebhook(bool? takeOverWebhook);

  InteractiveChatConfig discoveryMode(bool? discoveryMode);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InteractiveChatConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InteractiveChatConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  InteractiveChatConfig call({
    bool? interactive,
    List<AllowedChat>? allowedChats,
    int? askRateLimitPerHour,
    int? commandRateLimit,
    bool? takeOverWebhook,
    bool? discoveryMode,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInteractiveChatConfig.copyWith(...)` or call `instanceOfInteractiveChatConfig.copyWith.fieldName(value)` for a single field.
class _$InteractiveChatConfigCWProxyImpl
    implements _$InteractiveChatConfigCWProxy {
  const _$InteractiveChatConfigCWProxyImpl(this._value);

  final InteractiveChatConfig _value;

  @override
  InteractiveChatConfig interactive(bool? interactive) =>
      call(interactive: interactive);

  @override
  InteractiveChatConfig allowedChats(List<AllowedChat>? allowedChats) =>
      call(allowedChats: allowedChats);

  @override
  InteractiveChatConfig askRateLimitPerHour(int? askRateLimitPerHour) =>
      call(askRateLimitPerHour: askRateLimitPerHour);

  @override
  InteractiveChatConfig commandRateLimit(int? commandRateLimit) =>
      call(commandRateLimit: commandRateLimit);

  @override
  InteractiveChatConfig takeOverWebhook(bool? takeOverWebhook) =>
      call(takeOverWebhook: takeOverWebhook);

  @override
  InteractiveChatConfig discoveryMode(bool? discoveryMode) =>
      call(discoveryMode: discoveryMode);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InteractiveChatConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InteractiveChatConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InteractiveChatConfig call({
    Object? interactive = const $CopyWithPlaceholder(),
    Object? allowedChats = const $CopyWithPlaceholder(),
    Object? askRateLimitPerHour = const $CopyWithPlaceholder(),
    Object? commandRateLimit = const $CopyWithPlaceholder(),
    Object? takeOverWebhook = const $CopyWithPlaceholder(),
    Object? discoveryMode = const $CopyWithPlaceholder(),
  }) {
    return InteractiveChatConfig(
      interactive: interactive == const $CopyWithPlaceholder()
          ? _value.interactive
          // ignore: cast_nullable_to_non_nullable
          : interactive as bool?,
      allowedChats: allowedChats == const $CopyWithPlaceholder()
          ? _value.allowedChats
          // ignore: cast_nullable_to_non_nullable
          : allowedChats as List<AllowedChat>?,
      askRateLimitPerHour: askRateLimitPerHour == const $CopyWithPlaceholder()
          ? _value.askRateLimitPerHour
          // ignore: cast_nullable_to_non_nullable
          : askRateLimitPerHour as int?,
      commandRateLimit: commandRateLimit == const $CopyWithPlaceholder()
          ? _value.commandRateLimit
          // ignore: cast_nullable_to_non_nullable
          : commandRateLimit as int?,
      takeOverWebhook: takeOverWebhook == const $CopyWithPlaceholder()
          ? _value.takeOverWebhook
          // ignore: cast_nullable_to_non_nullable
          : takeOverWebhook as bool?,
      discoveryMode: discoveryMode == const $CopyWithPlaceholder()
          ? _value.discoveryMode
          // ignore: cast_nullable_to_non_nullable
          : discoveryMode as bool?,
    );
  }
}

extension $InteractiveChatConfigCopyWith on InteractiveChatConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInteractiveChatConfig.copyWith(...)` or `instanceOfInteractiveChatConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InteractiveChatConfigCWProxy get copyWith =>
      _$InteractiveChatConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InteractiveChatConfig _$InteractiveChatConfigFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('InteractiveChatConfig', json, ($checkedConvert) {
  final val = InteractiveChatConfig(
    interactive: $checkedConvert('interactive', (v) => v as bool? ?? false),
    allowedChats: $checkedConvert(
      'allowedChats',
      (v) => (v as List<dynamic>?)
          ?.map((e) => AllowedChat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    askRateLimitPerHour: $checkedConvert(
      'askRateLimitPerHour',
      (v) => (v as num?)?.toInt() ?? 10,
    ),
    commandRateLimit: $checkedConvert(
      'commandRateLimit',
      (v) => (v as num?)?.toInt() ?? 30,
    ),
    takeOverWebhook: $checkedConvert(
      'takeOverWebhook',
      (v) => v as bool? ?? false,
    ),
    discoveryMode: $checkedConvert('discoveryMode', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$InteractiveChatConfigToJson(
  InteractiveChatConfig instance,
) => <String, dynamic>{
  'interactive': ?instance.interactive,
  'allowedChats': ?instance.allowedChats?.map((e) => e.toJson()).toList(),
  'askRateLimitPerHour': ?instance.askRateLimitPerHour,
  'commandRateLimit': ?instance.commandRateLimit,
  'takeOverWebhook': ?instance.takeOverWebhook,
  'discoveryMode': ?instance.discoveryMode,
};
