// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'signing_key_info.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SigningKeyInfoCWProxy {
  SigningKeyInfo kid(String kid);

  SigningKeyInfo alg(SigningKeyInfoAlgEnum alg);

  SigningKeyInfo status(SigningKeyInfoStatusEnum status);

  SigningKeyInfo createdAt(DateTime createdAt);

  SigningKeyInfo retiredAt(DateTime? retiredAt);

  SigningKeyInfo removeAfter(DateTime? removeAfter);

  SigningKeyInfo jwk(Jwk jwk);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SigningKeyInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SigningKeyInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  SigningKeyInfo call({
    String kid,
    SigningKeyInfoAlgEnum alg,
    SigningKeyInfoStatusEnum status,
    DateTime createdAt,
    DateTime? retiredAt,
    DateTime? removeAfter,
    Jwk jwk,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSigningKeyInfo.copyWith(...)` or call `instanceOfSigningKeyInfo.copyWith.fieldName(value)` for a single field.
class _$SigningKeyInfoCWProxyImpl implements _$SigningKeyInfoCWProxy {
  const _$SigningKeyInfoCWProxyImpl(this._value);

  final SigningKeyInfo _value;

  @override
  SigningKeyInfo kid(String kid) => call(kid: kid);

  @override
  SigningKeyInfo alg(SigningKeyInfoAlgEnum alg) => call(alg: alg);

  @override
  SigningKeyInfo status(SigningKeyInfoStatusEnum status) =>
      call(status: status);

  @override
  SigningKeyInfo createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  SigningKeyInfo retiredAt(DateTime? retiredAt) => call(retiredAt: retiredAt);

  @override
  SigningKeyInfo removeAfter(DateTime? removeAfter) =>
      call(removeAfter: removeAfter);

  @override
  SigningKeyInfo jwk(Jwk jwk) => call(jwk: jwk);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SigningKeyInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SigningKeyInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SigningKeyInfo call({
    Object? kid = const $CopyWithPlaceholder(),
    Object? alg = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? retiredAt = const $CopyWithPlaceholder(),
    Object? removeAfter = const $CopyWithPlaceholder(),
    Object? jwk = const $CopyWithPlaceholder(),
  }) {
    return SigningKeyInfo(
      kid: kid == const $CopyWithPlaceholder() || kid == null
          ? _value.kid
          // ignore: cast_nullable_to_non_nullable
          : kid as String,
      alg: alg == const $CopyWithPlaceholder() || alg == null
          ? _value.alg
          // ignore: cast_nullable_to_non_nullable
          : alg as SigningKeyInfoAlgEnum,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SigningKeyInfoStatusEnum,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      retiredAt: retiredAt == const $CopyWithPlaceholder()
          ? _value.retiredAt
          // ignore: cast_nullable_to_non_nullable
          : retiredAt as DateTime?,
      removeAfter: removeAfter == const $CopyWithPlaceholder()
          ? _value.removeAfter
          // ignore: cast_nullable_to_non_nullable
          : removeAfter as DateTime?,
      jwk: jwk == const $CopyWithPlaceholder() || jwk == null
          ? _value.jwk
          // ignore: cast_nullable_to_non_nullable
          : jwk as Jwk,
    );
  }
}

extension $SigningKeyInfoCopyWith on SigningKeyInfo {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSigningKeyInfo.copyWith(...)` or `instanceOfSigningKeyInfo.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SigningKeyInfoCWProxy get copyWith => _$SigningKeyInfoCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SigningKeyInfo _$SigningKeyInfoFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SigningKeyInfo', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['kid', 'alg', 'status', 'createdAt', 'jwk'],
  );
  final val = SigningKeyInfo(
    kid: $checkedConvert('kid', (v) => v as String),
    alg: $checkedConvert(
      'alg',
      (v) => $enumDecode(_$SigningKeyInfoAlgEnumEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$SigningKeyInfoStatusEnumEnumMap, v),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    retiredAt: $checkedConvert(
      'retiredAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    removeAfter: $checkedConvert(
      'removeAfter',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    jwk: $checkedConvert('jwk', (v) => Jwk.fromJson(v as Map<String, dynamic>)),
  );
  return val;
});

Map<String, dynamic> _$SigningKeyInfoToJson(SigningKeyInfo instance) =>
    <String, dynamic>{
      'kid': instance.kid,
      'alg': _$SigningKeyInfoAlgEnumEnumMap[instance.alg]!,
      'status': _$SigningKeyInfoStatusEnumEnumMap[instance.status]!,
      'createdAt': instance.createdAt.toIso8601String(),
      'retiredAt': ?instance.retiredAt?.toIso8601String(),
      'removeAfter': ?instance.removeAfter?.toIso8601String(),
      'jwk': instance.jwk.toJson(),
    };

const _$SigningKeyInfoAlgEnumEnumMap = {
  SigningKeyInfoAlgEnum.ed25519: 'Ed25519',
  SigningKeyInfoAlgEnum.edDSA: 'EdDSA',
};

const _$SigningKeyInfoStatusEnumEnumMap = {
  SigningKeyInfoStatusEnum.active: 'active',
  SigningKeyInfoStatusEnum.retired: 'retired',
};
