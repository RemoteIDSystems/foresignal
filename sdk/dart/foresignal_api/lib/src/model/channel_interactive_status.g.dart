// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'channel_interactive_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChannelInteractiveStatusCWProxy {
  ChannelInteractiveStatus interactive(bool interactive);

  ChannelInteractiveStatus chatopsEnabled(bool? chatopsEnabled);

  ChannelInteractiveStatus connected(bool connected);

  ChannelInteractiveStatus status(HealthStatus status);

  ChannelInteractiveStatus statusReason(String? statusReason);

  ChannelInteractiveStatus lastPollAt(DateTime? lastPollAt);

  ChannelInteractiveStatus lastError(String? lastError);

  ChannelInteractiveStatus discoveryModeUntil(DateTime? discoveryModeUntil);

  ChannelInteractiveStatus chats(
    List<ChannelInteractiveStatusChatsInner> chats,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelInteractiveStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelInteractiveStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  ChannelInteractiveStatus call({
    bool interactive,
    bool? chatopsEnabled,
    bool connected,
    HealthStatus status,
    String? statusReason,
    DateTime? lastPollAt,
    String? lastError,
    DateTime? discoveryModeUntil,
    List<ChannelInteractiveStatusChatsInner> chats,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChannelInteractiveStatus.copyWith(...)` or call `instanceOfChannelInteractiveStatus.copyWith.fieldName(value)` for a single field.
class _$ChannelInteractiveStatusCWProxyImpl
    implements _$ChannelInteractiveStatusCWProxy {
  const _$ChannelInteractiveStatusCWProxyImpl(this._value);

  final ChannelInteractiveStatus _value;

  @override
  ChannelInteractiveStatus interactive(bool interactive) =>
      call(interactive: interactive);

  @override
  ChannelInteractiveStatus chatopsEnabled(bool? chatopsEnabled) =>
      call(chatopsEnabled: chatopsEnabled);

  @override
  ChannelInteractiveStatus connected(bool connected) =>
      call(connected: connected);

  @override
  ChannelInteractiveStatus status(HealthStatus status) => call(status: status);

  @override
  ChannelInteractiveStatus statusReason(String? statusReason) =>
      call(statusReason: statusReason);

  @override
  ChannelInteractiveStatus lastPollAt(DateTime? lastPollAt) =>
      call(lastPollAt: lastPollAt);

  @override
  ChannelInteractiveStatus lastError(String? lastError) =>
      call(lastError: lastError);

  @override
  ChannelInteractiveStatus discoveryModeUntil(DateTime? discoveryModeUntil) =>
      call(discoveryModeUntil: discoveryModeUntil);

  @override
  ChannelInteractiveStatus chats(
    List<ChannelInteractiveStatusChatsInner> chats,
  ) => call(chats: chats);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelInteractiveStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelInteractiveStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChannelInteractiveStatus call({
    Object? interactive = const $CopyWithPlaceholder(),
    Object? chatopsEnabled = const $CopyWithPlaceholder(),
    Object? connected = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? statusReason = const $CopyWithPlaceholder(),
    Object? lastPollAt = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
    Object? discoveryModeUntil = const $CopyWithPlaceholder(),
    Object? chats = const $CopyWithPlaceholder(),
  }) {
    return ChannelInteractiveStatus(
      interactive:
          interactive == const $CopyWithPlaceholder() || interactive == null
          ? _value.interactive
          // ignore: cast_nullable_to_non_nullable
          : interactive as bool,
      chatopsEnabled: chatopsEnabled == const $CopyWithPlaceholder()
          ? _value.chatopsEnabled
          // ignore: cast_nullable_to_non_nullable
          : chatopsEnabled as bool?,
      connected: connected == const $CopyWithPlaceholder() || connected == null
          ? _value.connected
          // ignore: cast_nullable_to_non_nullable
          : connected as bool,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as HealthStatus,
      statusReason: statusReason == const $CopyWithPlaceholder()
          ? _value.statusReason
          // ignore: cast_nullable_to_non_nullable
          : statusReason as String?,
      lastPollAt: lastPollAt == const $CopyWithPlaceholder()
          ? _value.lastPollAt
          // ignore: cast_nullable_to_non_nullable
          : lastPollAt as DateTime?,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
      discoveryModeUntil: discoveryModeUntil == const $CopyWithPlaceholder()
          ? _value.discoveryModeUntil
          // ignore: cast_nullable_to_non_nullable
          : discoveryModeUntil as DateTime?,
      chats: chats == const $CopyWithPlaceholder() || chats == null
          ? _value.chats
          // ignore: cast_nullable_to_non_nullable
          : chats as List<ChannelInteractiveStatusChatsInner>,
    );
  }
}

extension $ChannelInteractiveStatusCopyWith on ChannelInteractiveStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChannelInteractiveStatus.copyWith(...)` or `instanceOfChannelInteractiveStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChannelInteractiveStatusCWProxy get copyWith =>
      _$ChannelInteractiveStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChannelInteractiveStatus _$ChannelInteractiveStatusFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChannelInteractiveStatus', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['interactive', 'connected', 'status', 'chats'],
  );
  final val = ChannelInteractiveStatus(
    interactive: $checkedConvert('interactive', (v) => v as bool),
    chatopsEnabled: $checkedConvert('chatopsEnabled', (v) => v as bool?),
    connected: $checkedConvert('connected', (v) => v as bool),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$HealthStatusEnumMap, v),
    ),
    statusReason: $checkedConvert('statusReason', (v) => v as String?),
    lastPollAt: $checkedConvert(
      'lastPollAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastError: $checkedConvert('lastError', (v) => v as String?),
    discoveryModeUntil: $checkedConvert(
      'discoveryModeUntil',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    chats: $checkedConvert(
      'chats',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ChannelInteractiveStatusChatsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChannelInteractiveStatusToJson(
  ChannelInteractiveStatus instance,
) => <String, dynamic>{
  'interactive': instance.interactive,
  'chatopsEnabled': ?instance.chatopsEnabled,
  'connected': instance.connected,
  'status': _$HealthStatusEnumMap[instance.status]!,
  'statusReason': ?instance.statusReason,
  'lastPollAt': ?instance.lastPollAt?.toIso8601String(),
  'lastError': ?instance.lastError,
  'discoveryModeUntil': ?instance.discoveryModeUntil?.toIso8601String(),
  'chats': instance.chats.map((e) => e.toJson()).toList(),
};

const _$HealthStatusEnumMap = {
  HealthStatus.healthy: 'healthy',
  HealthStatus.degraded: 'degraded',
  HealthStatus.failing: 'failing',
  HealthStatus.idle: 'idle',
  HealthStatus.disabled: 'disabled',
};
