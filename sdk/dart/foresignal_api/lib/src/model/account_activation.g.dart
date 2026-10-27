// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'account_activation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AccountActivationCWProxy {
  AccountActivation token(String token);

  AccountActivation password(String password);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccountActivation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccountActivation(...).copyWith(id: 12, name: "My name")
  /// ```
  AccountActivation call({String token, String password});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAccountActivation.copyWith(...)` or call `instanceOfAccountActivation.copyWith.fieldName(value)` for a single field.
class _$AccountActivationCWProxyImpl implements _$AccountActivationCWProxy {
  const _$AccountActivationCWProxyImpl(this._value);

  final AccountActivation _value;

  @override
  AccountActivation token(String token) => call(token: token);

  @override
  AccountActivation password(String password) => call(password: password);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccountActivation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccountActivation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AccountActivation call({
    Object? token = const $CopyWithPlaceholder(),
    Object? password = const $CopyWithPlaceholder(),
  }) {
    return AccountActivation(
      token: token == const $CopyWithPlaceholder() || token == null
          ? _value.token
          // ignore: cast_nullable_to_non_nullable
          : token as String,
      password: password == const $CopyWithPlaceholder() || password == null
          ? _value.password
          // ignore: cast_nullable_to_non_nullable
          : password as String,
    );
  }
}

extension $AccountActivationCopyWith on AccountActivation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAccountActivation.copyWith(...)` or `instanceOfAccountActivation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AccountActivationCWProxy get copyWith =>
      _$AccountActivationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AccountActivation _$AccountActivationFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AccountActivation', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['token', 'password']);
      final val = AccountActivation(
        token: $checkedConvert('token', (v) => v as String),
        password: $checkedConvert('password', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$AccountActivationToJson(AccountActivation instance) =>
    <String, dynamic>{'token': instance.token, 'password': instance.password};
