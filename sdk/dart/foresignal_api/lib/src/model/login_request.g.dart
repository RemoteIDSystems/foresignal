// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'login_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LoginRequestCWProxy {
  LoginRequest email(String email);

  LoginRequest password(String password);

  LoginRequest mfaCode(String? mfaCode);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LoginRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LoginRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  LoginRequest call({String email, String password, String? mfaCode});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLoginRequest.copyWith(...)` or call `instanceOfLoginRequest.copyWith.fieldName(value)` for a single field.
class _$LoginRequestCWProxyImpl implements _$LoginRequestCWProxy {
  const _$LoginRequestCWProxyImpl(this._value);

  final LoginRequest _value;

  @override
  LoginRequest email(String email) => call(email: email);

  @override
  LoginRequest password(String password) => call(password: password);

  @override
  LoginRequest mfaCode(String? mfaCode) => call(mfaCode: mfaCode);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LoginRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LoginRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LoginRequest call({
    Object? email = const $CopyWithPlaceholder(),
    Object? password = const $CopyWithPlaceholder(),
    Object? mfaCode = const $CopyWithPlaceholder(),
  }) {
    return LoginRequest(
      email: email == const $CopyWithPlaceholder() || email == null
          ? _value.email
          // ignore: cast_nullable_to_non_nullable
          : email as String,
      password: password == const $CopyWithPlaceholder() || password == null
          ? _value.password
          // ignore: cast_nullable_to_non_nullable
          : password as String,
      mfaCode: mfaCode == const $CopyWithPlaceholder()
          ? _value.mfaCode
          // ignore: cast_nullable_to_non_nullable
          : mfaCode as String?,
    );
  }
}

extension $LoginRequestCopyWith on LoginRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLoginRequest.copyWith(...)` or `instanceOfLoginRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LoginRequestCWProxy get copyWith => _$LoginRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LoginRequest _$LoginRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LoginRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['email', 'password']);
      final val = LoginRequest(
        email: $checkedConvert('email', (v) => v as String),
        password: $checkedConvert('password', (v) => v as String),
        mfaCode: $checkedConvert('mfaCode', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$LoginRequestToJson(LoginRequest instance) =>
    <String, dynamic>{
      'email': instance.email,
      'password': instance.password,
      'mfaCode': ?instance.mfaCode,
    };
