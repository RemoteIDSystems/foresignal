// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'me.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MeCWProxy {
  Me principal(Principal principal);

  Me token(MeToken token);

  Me scopes(List<MeScopesEnum> scopes);

  Me groups(List<MeGroupsInner> groups);

  Me notificationPreferences(
    List<NotificationPreference> notificationPreferences,
  );

  Me defaultTlp(Tlp? defaultTlp);

  Me user(User? user);

  Me permissions(List<String>? permissions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Me(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Me(...).copyWith(id: 12, name: "My name")
  /// ```
  Me call({
    Principal principal,
    MeToken token,
    List<MeScopesEnum> scopes,
    List<MeGroupsInner> groups,
    List<NotificationPreference> notificationPreferences,
    Tlp? defaultTlp,
    User? user,
    List<String>? permissions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMe.copyWith(...)` or call `instanceOfMe.copyWith.fieldName(value)` for a single field.
class _$MeCWProxyImpl implements _$MeCWProxy {
  const _$MeCWProxyImpl(this._value);

  final Me _value;

  @override
  Me principal(Principal principal) => call(principal: principal);

  @override
  Me token(MeToken token) => call(token: token);

  @override
  Me scopes(List<MeScopesEnum> scopes) => call(scopes: scopes);

  @override
  Me groups(List<MeGroupsInner> groups) => call(groups: groups);

  @override
  Me notificationPreferences(
    List<NotificationPreference> notificationPreferences,
  ) => call(notificationPreferences: notificationPreferences);

  @override
  Me defaultTlp(Tlp? defaultTlp) => call(defaultTlp: defaultTlp);

  @override
  Me user(User? user) => call(user: user);

  @override
  Me permissions(List<String>? permissions) => call(permissions: permissions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Me(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Me(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Me call({
    Object? principal = const $CopyWithPlaceholder(),
    Object? token = const $CopyWithPlaceholder(),
    Object? scopes = const $CopyWithPlaceholder(),
    Object? groups = const $CopyWithPlaceholder(),
    Object? notificationPreferences = const $CopyWithPlaceholder(),
    Object? defaultTlp = const $CopyWithPlaceholder(),
    Object? user = const $CopyWithPlaceholder(),
    Object? permissions = const $CopyWithPlaceholder(),
  }) {
    return Me(
      principal: principal == const $CopyWithPlaceholder() || principal == null
          ? _value.principal
          // ignore: cast_nullable_to_non_nullable
          : principal as Principal,
      token: token == const $CopyWithPlaceholder() || token == null
          ? _value.token
          // ignore: cast_nullable_to_non_nullable
          : token as MeToken,
      scopes: scopes == const $CopyWithPlaceholder() || scopes == null
          ? _value.scopes
          // ignore: cast_nullable_to_non_nullable
          : scopes as List<MeScopesEnum>,
      groups: groups == const $CopyWithPlaceholder() || groups == null
          ? _value.groups
          // ignore: cast_nullable_to_non_nullable
          : groups as List<MeGroupsInner>,
      notificationPreferences:
          notificationPreferences == const $CopyWithPlaceholder() ||
              notificationPreferences == null
          ? _value.notificationPreferences
          // ignore: cast_nullable_to_non_nullable
          : notificationPreferences as List<NotificationPreference>,
      defaultTlp: defaultTlp == const $CopyWithPlaceholder()
          ? _value.defaultTlp
          // ignore: cast_nullable_to_non_nullable
          : defaultTlp as Tlp?,
      user: user == const $CopyWithPlaceholder()
          ? _value.user
          // ignore: cast_nullable_to_non_nullable
          : user as User?,
      permissions: permissions == const $CopyWithPlaceholder()
          ? _value.permissions
          // ignore: cast_nullable_to_non_nullable
          : permissions as List<String>?,
    );
  }
}

extension $MeCopyWith on Me {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMe.copyWith(...)` or `instanceOfMe.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MeCWProxy get copyWith => _$MeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Me _$MeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Me', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'principal',
          'token',
          'scopes',
          'groups',
          'notificationPreferences',
        ],
      );
      final val = Me(
        principal: $checkedConvert(
          'principal',
          (v) => Principal.fromJson(v as Map<String, dynamic>),
        ),
        token: $checkedConvert(
          'token',
          (v) => MeToken.fromJson(v as Map<String, dynamic>),
        ),
        scopes: $checkedConvert(
          'scopes',
          (v) => (v as List<dynamic>)
              .map((e) => $enumDecode(_$MeScopesEnumEnumMap, e))
              .toList(),
        ),
        groups: $checkedConvert(
          'groups',
          (v) => (v as List<dynamic>)
              .map((e) => MeGroupsInner.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        notificationPreferences: $checkedConvert(
          'notificationPreferences',
          (v) => (v as List<dynamic>)
              .map(
                (e) =>
                    NotificationPreference.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
        defaultTlp: $checkedConvert(
          'defaultTlp',
          (v) => $enumDecodeNullable(_$TlpEnumMap, v),
        ),
        user: $checkedConvert(
          'user',
          (v) => v == null ? null : User.fromJson(v as Map<String, dynamic>),
        ),
        permissions: $checkedConvert(
          'permissions',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MeToJson(Me instance) => <String, dynamic>{
  'principal': instance.principal.toJson(),
  'token': instance.token.toJson(),
  'scopes': instance.scopes.map((e) => _$MeScopesEnumEnumMap[e]!).toList(),
  'groups': instance.groups.map((e) => e.toJson()).toList(),
  'notificationPreferences': instance.notificationPreferences
      .map((e) => e.toJson())
      .toList(),
  'defaultTlp': ?_$TlpEnumMap[instance.defaultTlp],
  'user': ?instance.user?.toJson(),
  'permissions': ?instance.permissions,
};

const _$MeScopesEnumEnumMap = {
  MeScopesEnum.read: 'read',
  MeScopesEnum.agent: 'agent',
  MeScopesEnum.admin: 'admin',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};
