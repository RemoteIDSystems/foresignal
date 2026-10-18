// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'allowed_chat.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AllowedChatCWProxy {
  AllowedChat chatId(String chatId);

  AllowedChat name(String? name);

  AllowedChat scope(AllowedChatScopeEnum scope);

  AllowedChat userIds(List<String>? userIds);

  AllowedChat privateAccess(bool? privateAccess);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AllowedChat(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AllowedChat(...).copyWith(id: 12, name: "My name")
  /// ```
  AllowedChat call({
    String chatId,
    String? name,
    AllowedChatScopeEnum scope,
    List<String>? userIds,
    bool? privateAccess,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAllowedChat.copyWith(...)` or call `instanceOfAllowedChat.copyWith.fieldName(value)` for a single field.
class _$AllowedChatCWProxyImpl implements _$AllowedChatCWProxy {
  const _$AllowedChatCWProxyImpl(this._value);

  final AllowedChat _value;

  @override
  AllowedChat chatId(String chatId) => call(chatId: chatId);

  @override
  AllowedChat name(String? name) => call(name: name);

  @override
  AllowedChat scope(AllowedChatScopeEnum scope) => call(scope: scope);

  @override
  AllowedChat userIds(List<String>? userIds) => call(userIds: userIds);

  @override
  AllowedChat privateAccess(bool? privateAccess) =>
      call(privateAccess: privateAccess);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AllowedChat(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AllowedChat(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AllowedChat call({
    Object? chatId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? userIds = const $CopyWithPlaceholder(),
    Object? privateAccess = const $CopyWithPlaceholder(),
  }) {
    return AllowedChat(
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
          : scope as AllowedChatScopeEnum,
      userIds: userIds == const $CopyWithPlaceholder()
          ? _value.userIds
          // ignore: cast_nullable_to_non_nullable
          : userIds as List<String>?,
      privateAccess: privateAccess == const $CopyWithPlaceholder()
          ? _value.privateAccess
          // ignore: cast_nullable_to_non_nullable
          : privateAccess as bool?,
    );
  }
}

extension $AllowedChatCopyWith on AllowedChat {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAllowedChat.copyWith(...)` or `instanceOfAllowedChat.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AllowedChatCWProxy get copyWith => _$AllowedChatCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AllowedChat _$AllowedChatFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AllowedChat', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['chatId', 'scope']);
      final val = AllowedChat(
        chatId: $checkedConvert('chatId', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String?),
        scope: $checkedConvert(
          'scope',
          (v) => $enumDecode(_$AllowedChatScopeEnumEnumMap, v),
        ),
        userIds: $checkedConvert(
          'userIds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        privateAccess: $checkedConvert(
          'privateAccess',
          (v) => v as bool? ?? false,
        ),
      );
      return val;
    });

Map<String, dynamic> _$AllowedChatToJson(AllowedChat instance) =>
    <String, dynamic>{
      'chatId': instance.chatId,
      'name': ?instance.name,
      'scope': _$AllowedChatScopeEnumEnumMap[instance.scope]!,
      'userIds': ?instance.userIds,
      'privateAccess': ?instance.privateAccess,
    };

const _$AllowedChatScopeEnumEnumMap = {
  AllowedChatScopeEnum.read: 'read',
  AllowedChatScopeEnum.agent: 'agent',
};
