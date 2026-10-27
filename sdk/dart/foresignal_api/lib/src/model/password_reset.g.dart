// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'password_reset.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PasswordResetCWProxy {
  PasswordReset token(String token);

  PasswordReset newPassword(String newPassword);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PasswordReset(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PasswordReset(...).copyWith(id: 12, name: "My name")
  /// ```
  PasswordReset call({String token, String newPassword});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPasswordReset.copyWith(...)` or call `instanceOfPasswordReset.copyWith.fieldName(value)` for a single field.
class _$PasswordResetCWProxyImpl implements _$PasswordResetCWProxy {
  const _$PasswordResetCWProxyImpl(this._value);

  final PasswordReset _value;

  @override
  PasswordReset token(String token) => call(token: token);

  @override
  PasswordReset newPassword(String newPassword) =>
      call(newPassword: newPassword);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PasswordReset(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PasswordReset(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PasswordReset call({
    Object? token = const $CopyWithPlaceholder(),
    Object? newPassword = const $CopyWithPlaceholder(),
  }) {
    return PasswordReset(
      token: token == const $CopyWithPlaceholder() || token == null
          ? _value.token
          // ignore: cast_nullable_to_non_nullable
          : token as String,
      newPassword:
          newPassword == const $CopyWithPlaceholder() || newPassword == null
          ? _value.newPassword
          // ignore: cast_nullable_to_non_nullable
          : newPassword as String,
    );
  }
}

extension $PasswordResetCopyWith on PasswordReset {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPasswordReset.copyWith(...)` or `instanceOfPasswordReset.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PasswordResetCWProxy get copyWith => _$PasswordResetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PasswordReset _$PasswordResetFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PasswordReset', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['token', 'newPassword']);
      final val = PasswordReset(
        token: $checkedConvert('token', (v) => v as String),
        newPassword: $checkedConvert('newPassword', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$PasswordResetToJson(PasswordReset instance) =>
    <String, dynamic>{
      'token': instance.token,
      'newPassword': instance.newPassword,
    };
