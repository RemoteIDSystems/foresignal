// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mfa_disable_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MfaDisableRequestCWProxy {
  MfaDisableRequest code(String? code);

  MfaDisableRequest password(String? password);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MfaDisableRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MfaDisableRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  MfaDisableRequest call({String? code, String? password});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMfaDisableRequest.copyWith(...)` or call `instanceOfMfaDisableRequest.copyWith.fieldName(value)` for a single field.
class _$MfaDisableRequestCWProxyImpl implements _$MfaDisableRequestCWProxy {
  const _$MfaDisableRequestCWProxyImpl(this._value);

  final MfaDisableRequest _value;

  @override
  MfaDisableRequest code(String? code) => call(code: code);

  @override
  MfaDisableRequest password(String? password) => call(password: password);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MfaDisableRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MfaDisableRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MfaDisableRequest call({
    Object? code = const $CopyWithPlaceholder(),
    Object? password = const $CopyWithPlaceholder(),
  }) {
    return MfaDisableRequest(
      code: code == const $CopyWithPlaceholder()
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String?,
      password: password == const $CopyWithPlaceholder()
          ? _value.password
          // ignore: cast_nullable_to_non_nullable
          : password as String?,
    );
  }
}

extension $MfaDisableRequestCopyWith on MfaDisableRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMfaDisableRequest.copyWith(...)` or `instanceOfMfaDisableRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MfaDisableRequestCWProxy get copyWith =>
      _$MfaDisableRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MfaDisableRequest _$MfaDisableRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MfaDisableRequest', json, ($checkedConvert) {
      final val = MfaDisableRequest(
        code: $checkedConvert('code', (v) => v as String?),
        password: $checkedConvert('password', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$MfaDisableRequestToJson(MfaDisableRequest instance) =>
    <String, dynamic>{'code': ?instance.code, 'password': ?instance.password};
