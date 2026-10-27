// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mfa_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MfaStatusCWProxy {
  MfaStatus enabled(bool enabled);

  MfaStatus enrolledAt(DateTime? enrolledAt);

  MfaStatus required_(bool? required_);

  MfaStatus recoveryCodes(MfaRecoveryCodes? recoveryCodes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MfaStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MfaStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  MfaStatus call({
    bool enabled,
    DateTime? enrolledAt,
    bool? required_,
    MfaRecoveryCodes? recoveryCodes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMfaStatus.copyWith(...)` or call `instanceOfMfaStatus.copyWith.fieldName(value)` for a single field.
class _$MfaStatusCWProxyImpl implements _$MfaStatusCWProxy {
  const _$MfaStatusCWProxyImpl(this._value);

  final MfaStatus _value;

  @override
  MfaStatus enabled(bool enabled) => call(enabled: enabled);

  @override
  MfaStatus enrolledAt(DateTime? enrolledAt) => call(enrolledAt: enrolledAt);

  @override
  MfaStatus required_(bool? required_) => call(required_: required_);

  @override
  MfaStatus recoveryCodes(MfaRecoveryCodes? recoveryCodes) =>
      call(recoveryCodes: recoveryCodes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MfaStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MfaStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MfaStatus call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? enrolledAt = const $CopyWithPlaceholder(),
    Object? required_ = const $CopyWithPlaceholder(),
    Object? recoveryCodes = const $CopyWithPlaceholder(),
  }) {
    return MfaStatus(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      enrolledAt: enrolledAt == const $CopyWithPlaceholder()
          ? _value.enrolledAt
          // ignore: cast_nullable_to_non_nullable
          : enrolledAt as DateTime?,
      required_: required_ == const $CopyWithPlaceholder()
          ? _value.required_
          // ignore: cast_nullable_to_non_nullable
          : required_ as bool?,
      recoveryCodes: recoveryCodes == const $CopyWithPlaceholder()
          ? _value.recoveryCodes
          // ignore: cast_nullable_to_non_nullable
          : recoveryCodes as MfaRecoveryCodes?,
    );
  }
}

extension $MfaStatusCopyWith on MfaStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMfaStatus.copyWith(...)` or `instanceOfMfaStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MfaStatusCWProxy get copyWith => _$MfaStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MfaStatus _$MfaStatusFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MfaStatus', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['enabled']);
      final val = MfaStatus(
        enabled: $checkedConvert('enabled', (v) => v as bool),
        enrolledAt: $checkedConvert(
          'enrolledAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        required_: $checkedConvert('required', (v) => v as bool?),
        recoveryCodes: $checkedConvert(
          'recoveryCodes',
          (v) => v == null
              ? null
              : MfaRecoveryCodes.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    }, fieldKeyMap: const {'required_': 'required'});

Map<String, dynamic> _$MfaStatusToJson(MfaStatus instance) => <String, dynamic>{
  'enabled': instance.enabled,
  'enrolledAt': ?instance.enrolledAt?.toIso8601String(),
  'required': ?instance.required_,
  'recoveryCodes': ?instance.recoveryCodes?.toJson(),
};
