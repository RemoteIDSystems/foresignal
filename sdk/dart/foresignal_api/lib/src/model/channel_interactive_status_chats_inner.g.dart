// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'channel_interactive_status_chats_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChannelInteractiveStatusChatsInnerCWProxy {
  ChannelInteractiveStatusChatsInner chatId(String chatId);

  ChannelInteractiveStatusChatsInner name(String? name);

  ChannelInteractiveStatusChatsInner scope(
    ChannelInteractiveStatusChatsInnerScopeEnum scope,
  );

  ChannelInteractiveStatusChatsInner userIds(List<String>? userIds);

  ChannelInteractiveStatusChatsInner privateAccess(bool? privateAccess);

  ChannelInteractiveStatusChatsInner active(bool active);

  ChannelInteractiveStatusChatsInner reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelInteractiveStatusChatsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelInteractiveStatusChatsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChannelInteractiveStatusChatsInner call({
    String chatId,
    String? name,
    ChannelInteractiveStatusChatsInnerScopeEnum scope,
    List<String>? userIds,
    bool? privateAccess,
    bool active,
    String? reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChannelInteractiveStatusChatsInner.copyWith(...)` or call `instanceOfChannelInteractiveStatusChatsInner.copyWith.fieldName(value)` for a single field.
class _$ChannelInteractiveStatusChatsInnerCWProxyImpl
    implements _$ChannelInteractiveStatusChatsInnerCWProxy {
  const _$ChannelInteractiveStatusChatsInnerCWProxyImpl(this._value);

  final ChannelInteractiveStatusChatsInner _value;

  @override
  ChannelInteractiveStatusChatsInner chatId(String chatId) =>
      call(chatId: chatId);

  @override
  ChannelInteractiveStatusChatsInner name(String? name) => call(name: name);

  @override
  ChannelInteractiveStatusChatsInner scope(
    ChannelInteractiveStatusChatsInnerScopeEnum scope,
  ) => call(scope: scope);

  @override
  ChannelInteractiveStatusChatsInner userIds(List<String>? userIds) =>
      call(userIds: userIds);

  @override
  ChannelInteractiveStatusChatsInner privateAccess(bool? privateAccess) =>
      call(privateAccess: privateAccess);

  @override
  ChannelInteractiveStatusChatsInner active(bool active) =>
      call(active: active);

  @override
  ChannelInteractiveStatusChatsInner reason(String? reason) =>
      call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelInteractiveStatusChatsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelInteractiveStatusChatsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChannelInteractiveStatusChatsInner call({
    Object? chatId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? userIds = const $CopyWithPlaceholder(),
    Object? privateAccess = const $CopyWithPlaceholder(),
    Object? active = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return ChannelInteractiveStatusChatsInner(
      chatId: chatId == const $CopyWithPlaceholder() || chatId == null
          ? _value.chatId
          // ignore: cast_nullable_to_non_nullable
          : chatId as String,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      scope: scope == const $CopyWithPlaceholder() || scope == null
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as ChannelInteractiveStatusChatsInnerScopeEnum,
      userIds: userIds == const $CopyWithPlaceholder()
          ? _value.userIds
          // ignore: cast_nullable_to_non_nullable
          : userIds as List<String>?,
      privateAccess: privateAccess == const $CopyWithPlaceholder()
          ? _value.privateAccess
          // ignore: cast_nullable_to_non_nullable
          : privateAccess as bool?,
      active: active == const $CopyWithPlaceholder() || active == null
          ? _value.active
          // ignore: cast_nullable_to_non_nullable
          : active as bool,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $ChannelInteractiveStatusChatsInnerCopyWith
    on ChannelInteractiveStatusChatsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChannelInteractiveStatusChatsInner.copyWith(...)` or `instanceOfChannelInteractiveStatusChatsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChannelInteractiveStatusChatsInnerCWProxy get copyWith =>
      _$ChannelInteractiveStatusChatsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChannelInteractiveStatusChatsInner _$ChannelInteractiveStatusChatsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChannelInteractiveStatusChatsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['chatId', 'scope', 'active']);
  final val = ChannelInteractiveStatusChatsInner(
    chatId: $checkedConvert('chatId', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String?),
    scope: $checkedConvert(
      'scope',
      (v) =>
          $enumDecode(_$ChannelInteractiveStatusChatsInnerScopeEnumEnumMap, v),
    ),
    userIds: $checkedConvert(
      'userIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    privateAccess: $checkedConvert('privateAccess', (v) => v as bool?),
    active: $checkedConvert('active', (v) => v as bool),
    reason: $checkedConvert('reason', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ChannelInteractiveStatusChatsInnerToJson(
  ChannelInteractiveStatusChatsInner instance,
) => <String, dynamic>{
  'chatId': instance.chatId,
  'name': ?instance.name,
  'scope':
      _$ChannelInteractiveStatusChatsInnerScopeEnumEnumMap[instance.scope]!,
  'userIds': ?instance.userIds,
  'privateAccess': ?instance.privateAccess,
  'active': instance.active,
  'reason': ?instance.reason,
};

const _$ChannelInteractiveStatusChatsInnerScopeEnumEnumMap = {
  ChannelInteractiveStatusChatsInnerScopeEnum.read: 'read',
  ChannelInteractiveStatusChatsInnerScopeEnum.agent: 'agent',
};
