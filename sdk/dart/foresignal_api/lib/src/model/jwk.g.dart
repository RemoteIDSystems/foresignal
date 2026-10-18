// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'jwk.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$JwkCWProxy {
  Jwk kty(JwkKtyEnum kty);

  Jwk crv(JwkCrvEnum crv);

  Jwk x(String x);

  Jwk kid(String kid);

  Jwk use(JwkUseEnum use);

  Jwk alg(JwkAlgEnum alg);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Jwk(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Jwk(...).copyWith(id: 12, name: "My name")
  /// ```
  Jwk call({
    JwkKtyEnum kty,
    JwkCrvEnum crv,
    String x,
    String kid,
    JwkUseEnum use,
    JwkAlgEnum alg,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfJwk.copyWith(...)` or call `instanceOfJwk.copyWith.fieldName(value)` for a single field.
class _$JwkCWProxyImpl implements _$JwkCWProxy {
  const _$JwkCWProxyImpl(this._value);

  final Jwk _value;

  @override
  Jwk kty(JwkKtyEnum kty) => call(kty: kty);

  @override
  Jwk crv(JwkCrvEnum crv) => call(crv: crv);

  @override
  Jwk x(String x) => call(x: x);

  @override
  Jwk kid(String kid) => call(kid: kid);

  @override
  Jwk use(JwkUseEnum use) => call(use: use);

  @override
  Jwk alg(JwkAlgEnum alg) => call(alg: alg);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Jwk(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Jwk(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Jwk call({
    Object? kty = const $CopyWithPlaceholder(),
    Object? crv = const $CopyWithPlaceholder(),
    Object? x = const $CopyWithPlaceholder(),
    Object? kid = const $CopyWithPlaceholder(),
    Object? use = const $CopyWithPlaceholder(),
    Object? alg = const $CopyWithPlaceholder(),
  }) {
    return Jwk(
      kty: kty == const $CopyWithPlaceholder() || kty == null
          ? _value.kty
          // ignore: cast_nullable_to_non_nullable
          : kty as JwkKtyEnum,
      crv: crv == const $CopyWithPlaceholder() || crv == null
          ? _value.crv
          // ignore: cast_nullable_to_non_nullable
          : crv as JwkCrvEnum,
      x: x == const $CopyWithPlaceholder() || x == null
          ? _value.x
          // ignore: cast_nullable_to_non_nullable
          : x as String,
      kid: kid == const $CopyWithPlaceholder() || kid == null
          ? _value.kid
          // ignore: cast_nullable_to_non_nullable
          : kid as String,
      use: use == const $CopyWithPlaceholder() || use == null
          ? _value.use
          // ignore: cast_nullable_to_non_nullable
          : use as JwkUseEnum,
      alg: alg == const $CopyWithPlaceholder() || alg == null
          ? _value.alg
          // ignore: cast_nullable_to_non_nullable
          : alg as JwkAlgEnum,
    );
  }
}

extension $JwkCopyWith on Jwk {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfJwk.copyWith(...)` or `instanceOfJwk.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$JwkCWProxy get copyWith => _$JwkCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Jwk _$JwkFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Jwk', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['kty', 'crv', 'x', 'kid', 'use', 'alg'],
      );
      final val = Jwk(
        kty: $checkedConvert('kty', (v) => $enumDecode(_$JwkKtyEnumEnumMap, v)),
        crv: $checkedConvert('crv', (v) => $enumDecode(_$JwkCrvEnumEnumMap, v)),
        x: $checkedConvert('x', (v) => v as String),
        kid: $checkedConvert('kid', (v) => v as String),
        use: $checkedConvert('use', (v) => $enumDecode(_$JwkUseEnumEnumMap, v)),
        alg: $checkedConvert('alg', (v) => $enumDecode(_$JwkAlgEnumEnumMap, v)),
      );
      return val;
    });

Map<String, dynamic> _$JwkToJson(Jwk instance) => <String, dynamic>{
  'kty': _$JwkKtyEnumEnumMap[instance.kty]!,
  'crv': _$JwkCrvEnumEnumMap[instance.crv]!,
  'x': instance.x,
  'kid': instance.kid,
  'use': _$JwkUseEnumEnumMap[instance.use]!,
  'alg': _$JwkAlgEnumEnumMap[instance.alg]!,
};

const _$JwkKtyEnumEnumMap = {JwkKtyEnum.OKP: 'OKP'};

const _$JwkCrvEnumEnumMap = {JwkCrvEnum.ed25519: 'Ed25519'};

const _$JwkUseEnumEnumMap = {JwkUseEnum.sig: 'sig'};

const _$JwkAlgEnumEnumMap = {
  JwkAlgEnum.ed25519: 'Ed25519',
  JwkAlgEnum.edDSA: 'EdDSA',
};
