// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_create.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UserCreateCWProxy {
  UserCreate email(String email);

  UserCreate fullName(String fullName);

  UserCreate roles(List<String> roles);

  UserCreate sendInvite(bool? sendInvite);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  UserCreate call({
    String email,
    String fullName,
    List<String> roles,
    bool? sendInvite,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUserCreate.copyWith(...)` or call `instanceOfUserCreate.copyWith.fieldName(value)` for a single field.
class _$UserCreateCWProxyImpl implements _$UserCreateCWProxy {
  const _$UserCreateCWProxyImpl(this._value);

  final UserCreate _value;

  @override
  UserCreate email(String email) => call(email: email);

  @override
  UserCreate fullName(String fullName) => call(fullName: fullName);

  @override
  UserCreate roles(List<String> roles) => call(roles: roles);

  @override
  UserCreate sendInvite(bool? sendInvite) => call(sendInvite: sendInvite);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UserCreate call({
    Object? email = const $CopyWithPlaceholder(),
    Object? fullName = const $CopyWithPlaceholder(),
    Object? roles = const $CopyWithPlaceholder(),
    Object? sendInvite = const $CopyWithPlaceholder(),
  }) {
    return UserCreate(
      email: email == const $CopyWithPlaceholder() || email == null
          ? _value.email
          // ignore: cast_nullable_to_non_nullable
          : email as String,
      fullName: fullName == const $CopyWithPlaceholder() || fullName == null
          ? _value.fullName
          // ignore: cast_nullable_to_non_nullable
          : fullName as String,
      roles: roles == const $CopyWithPlaceholder() || roles == null
          ? _value.roles
          // ignore: cast_nullable_to_non_nullable
          : roles as List<String>,
      sendInvite: sendInvite == const $CopyWithPlaceholder()
          ? _value.sendInvite
          // ignore: cast_nullable_to_non_nullable
          : sendInvite as bool?,
    );
  }
}

extension $UserCreateCopyWith on UserCreate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUserCreate.copyWith(...)` or `instanceOfUserCreate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UserCreateCWProxy get copyWith => _$UserCreateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserCreate _$UserCreateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('UserCreate', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['email', 'fullName', 'roles']);
      final val = UserCreate(
        email: $checkedConvert('email', (v) => v as String),
        fullName: $checkedConvert('fullName', (v) => v as String),
        roles: $checkedConvert(
          'roles',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        sendInvite: $checkedConvert('sendInvite', (v) => v as bool? ?? true),
      );
      return val;
    });

Map<String, dynamic> _$UserCreateToJson(UserCreate instance) =>
    <String, dynamic>{
      'email': instance.email,
      'fullName': instance.fullName,
      'roles': instance.roles,
      'sendInvite': ?instance.sendInvite,
    };
