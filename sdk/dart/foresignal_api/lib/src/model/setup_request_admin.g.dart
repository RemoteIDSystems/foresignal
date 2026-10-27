// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'setup_request_admin.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SetupRequestAdminCWProxy {
  SetupRequestAdmin email(String email);

  SetupRequestAdmin fullName(String fullName);

  SetupRequestAdmin password(String password);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SetupRequestAdmin(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SetupRequestAdmin(...).copyWith(id: 12, name: "My name")
  /// ```
  SetupRequestAdmin call({String email, String fullName, String password});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSetupRequestAdmin.copyWith(...)` or call `instanceOfSetupRequestAdmin.copyWith.fieldName(value)` for a single field.
class _$SetupRequestAdminCWProxyImpl implements _$SetupRequestAdminCWProxy {
  const _$SetupRequestAdminCWProxyImpl(this._value);

  final SetupRequestAdmin _value;

  @override
  SetupRequestAdmin email(String email) => call(email: email);

  @override
  SetupRequestAdmin fullName(String fullName) => call(fullName: fullName);

  @override
  SetupRequestAdmin password(String password) => call(password: password);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SetupRequestAdmin(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SetupRequestAdmin(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SetupRequestAdmin call({
    Object? email = const $CopyWithPlaceholder(),
    Object? fullName = const $CopyWithPlaceholder(),
    Object? password = const $CopyWithPlaceholder(),
  }) {
    return SetupRequestAdmin(
      email: email == const $CopyWithPlaceholder() || email == null
          ? _value.email
          // ignore: cast_nullable_to_non_nullable
          : email as String,
      fullName: fullName == const $CopyWithPlaceholder() || fullName == null
          ? _value.fullName
          // ignore: cast_nullable_to_non_nullable
          : fullName as String,
      password: password == const $CopyWithPlaceholder() || password == null
          ? _value.password
          // ignore: cast_nullable_to_non_nullable
          : password as String,
    );
  }
}

extension $SetupRequestAdminCopyWith on SetupRequestAdmin {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSetupRequestAdmin.copyWith(...)` or `instanceOfSetupRequestAdmin.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SetupRequestAdminCWProxy get copyWith =>
      _$SetupRequestAdminCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SetupRequestAdmin _$SetupRequestAdminFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SetupRequestAdmin', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['email', 'fullName', 'password']);
      final val = SetupRequestAdmin(
        email: $checkedConvert('email', (v) => v as String),
        fullName: $checkedConvert('fullName', (v) => v as String),
        password: $checkedConvert('password', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$SetupRequestAdminToJson(SetupRequestAdmin instance) =>
    <String, dynamic>{
      'email': instance.email,
      'fullName': instance.fullName,
      'password': instance.password,
    };
