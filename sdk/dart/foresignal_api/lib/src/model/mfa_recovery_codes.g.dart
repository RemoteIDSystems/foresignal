// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mfa_recovery_codes.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MfaRecoveryCodesCWProxy {
  MfaRecoveryCodes codes(List<String> codes);

  MfaRecoveryCodes generatedAt(DateTime generatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MfaRecoveryCodes(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MfaRecoveryCodes(...).copyWith(id: 12, name: "My name")
  /// ```
  MfaRecoveryCodes call({List<String> codes, DateTime generatedAt});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMfaRecoveryCodes.copyWith(...)` or call `instanceOfMfaRecoveryCodes.copyWith.fieldName(value)` for a single field.
class _$MfaRecoveryCodesCWProxyImpl implements _$MfaRecoveryCodesCWProxy {
  const _$MfaRecoveryCodesCWProxyImpl(this._value);

  final MfaRecoveryCodes _value;

  @override
  MfaRecoveryCodes codes(List<String> codes) => call(codes: codes);

  @override
  MfaRecoveryCodes generatedAt(DateTime generatedAt) =>
      call(generatedAt: generatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MfaRecoveryCodes(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MfaRecoveryCodes(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MfaRecoveryCodes call({
    Object? codes = const $CopyWithPlaceholder(),
    Object? generatedAt = const $CopyWithPlaceholder(),
  }) {
    return MfaRecoveryCodes(
      codes: codes == const $CopyWithPlaceholder() || codes == null
          ? _value.codes
          // ignore: cast_nullable_to_non_nullable
          : codes as List<String>,
      generatedAt:
          generatedAt == const $CopyWithPlaceholder() || generatedAt == null
          ? _value.generatedAt
          // ignore: cast_nullable_to_non_nullable
          : generatedAt as DateTime,
    );
  }
}

extension $MfaRecoveryCodesCopyWith on MfaRecoveryCodes {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMfaRecoveryCodes.copyWith(...)` or `instanceOfMfaRecoveryCodes.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MfaRecoveryCodesCWProxy get copyWith => _$MfaRecoveryCodesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MfaRecoveryCodes _$MfaRecoveryCodesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MfaRecoveryCodes', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['codes', 'generatedAt']);
      final val = MfaRecoveryCodes(
        codes: $checkedConvert(
          'codes',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        generatedAt: $checkedConvert(
          'generatedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MfaRecoveryCodesToJson(MfaRecoveryCodes instance) =>
    <String, dynamic>{
      'codes': instance.codes,
      'generatedAt': instance.generatedAt.toIso8601String(),
    };
