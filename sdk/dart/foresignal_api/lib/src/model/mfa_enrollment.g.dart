// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mfa_enrollment.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MfaEnrollmentCWProxy {
  MfaEnrollment secret(String secret);

  MfaEnrollment otpauthUri(String otpauthUri);

  MfaEnrollment qrSvg(String? qrSvg);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MfaEnrollment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MfaEnrollment(...).copyWith(id: 12, name: "My name")
  /// ```
  MfaEnrollment call({String secret, String otpauthUri, String? qrSvg});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMfaEnrollment.copyWith(...)` or call `instanceOfMfaEnrollment.copyWith.fieldName(value)` for a single field.
class _$MfaEnrollmentCWProxyImpl implements _$MfaEnrollmentCWProxy {
  const _$MfaEnrollmentCWProxyImpl(this._value);

  final MfaEnrollment _value;

  @override
  MfaEnrollment secret(String secret) => call(secret: secret);

  @override
  MfaEnrollment otpauthUri(String otpauthUri) => call(otpauthUri: otpauthUri);

  @override
  MfaEnrollment qrSvg(String? qrSvg) => call(qrSvg: qrSvg);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MfaEnrollment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MfaEnrollment(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MfaEnrollment call({
    Object? secret = const $CopyWithPlaceholder(),
    Object? otpauthUri = const $CopyWithPlaceholder(),
    Object? qrSvg = const $CopyWithPlaceholder(),
  }) {
    return MfaEnrollment(
      secret: secret == const $CopyWithPlaceholder() || secret == null
          ? _value.secret
          // ignore: cast_nullable_to_non_nullable
          : secret as String,
      otpauthUri:
          otpauthUri == const $CopyWithPlaceholder() || otpauthUri == null
          ? _value.otpauthUri
          // ignore: cast_nullable_to_non_nullable
          : otpauthUri as String,
      qrSvg: qrSvg == const $CopyWithPlaceholder()
          ? _value.qrSvg
          // ignore: cast_nullable_to_non_nullable
          : qrSvg as String?,
    );
  }
}

extension $MfaEnrollmentCopyWith on MfaEnrollment {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMfaEnrollment.copyWith(...)` or `instanceOfMfaEnrollment.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MfaEnrollmentCWProxy get copyWith => _$MfaEnrollmentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MfaEnrollment _$MfaEnrollmentFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MfaEnrollment', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['secret', 'otpauthUri']);
      final val = MfaEnrollment(
        secret: $checkedConvert('secret', (v) => v as String),
        otpauthUri: $checkedConvert('otpauthUri', (v) => v as String),
        qrSvg: $checkedConvert('qrSvg', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$MfaEnrollmentToJson(MfaEnrollment instance) =>
    <String, dynamic>{
      'secret': instance.secret,
      'otpauthUri': instance.otpauthUri,
      'qrSvg': ?instance.qrSvg,
    };
