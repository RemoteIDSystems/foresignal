// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'password_reset_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PasswordResetRequestCWProxy {
  PasswordResetRequest email(String email);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PasswordResetRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PasswordResetRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  PasswordResetRequest call({String email});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPasswordResetRequest.copyWith(...)` or call `instanceOfPasswordResetRequest.copyWith.fieldName(value)` for a single field.
class _$PasswordResetRequestCWProxyImpl
    implements _$PasswordResetRequestCWProxy {
  const _$PasswordResetRequestCWProxyImpl(this._value);

  final PasswordResetRequest _value;

  @override
  PasswordResetRequest email(String email) => call(email: email);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PasswordResetRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PasswordResetRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PasswordResetRequest call({Object? email = const $CopyWithPlaceholder()}) {
    return PasswordResetRequest(
      email: email == const $CopyWithPlaceholder() || email == null
          ? _value.email
          // ignore: cast_nullable_to_non_nullable
          : email as String,
    );
  }
}

extension $PasswordResetRequestCopyWith on PasswordResetRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPasswordResetRequest.copyWith(...)` or `instanceOfPasswordResetRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PasswordResetRequestCWProxy get copyWith =>
      _$PasswordResetRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PasswordResetRequest _$PasswordResetRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PasswordResetRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['email']);
  final val = PasswordResetRequest(
    email: $checkedConvert('email', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$PasswordResetRequestToJson(
  PasswordResetRequest instance,
) => <String, dynamic>{'email': instance.email};
