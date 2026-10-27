// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mfa_verify_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MfaVerifyRequestCWProxy {
  MfaVerifyRequest code(String code);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MfaVerifyRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MfaVerifyRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  MfaVerifyRequest call({String code});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMfaVerifyRequest.copyWith(...)` or call `instanceOfMfaVerifyRequest.copyWith.fieldName(value)` for a single field.
class _$MfaVerifyRequestCWProxyImpl implements _$MfaVerifyRequestCWProxy {
  const _$MfaVerifyRequestCWProxyImpl(this._value);

  final MfaVerifyRequest _value;

  @override
  MfaVerifyRequest code(String code) => call(code: code);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MfaVerifyRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MfaVerifyRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MfaVerifyRequest call({Object? code = const $CopyWithPlaceholder()}) {
    return MfaVerifyRequest(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String,
    );
  }
}

extension $MfaVerifyRequestCopyWith on MfaVerifyRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMfaVerifyRequest.copyWith(...)` or `instanceOfMfaVerifyRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MfaVerifyRequestCWProxy get copyWith => _$MfaVerifyRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MfaVerifyRequest _$MfaVerifyRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MfaVerifyRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['code']);
      final val = MfaVerifyRequest(
        code: $checkedConvert('code', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$MfaVerifyRequestToJson(MfaVerifyRequest instance) =>
    <String, dynamic>{'code': instance.code};
