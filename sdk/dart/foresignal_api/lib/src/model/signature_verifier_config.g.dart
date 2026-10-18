// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'signature_verifier_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SignatureVerifierConfigCWProxy {
  SignatureVerifierConfig scheme(SignatureVerifierConfigSchemeEnum scheme);

  SignatureVerifierConfig header(String? header);

  SignatureVerifierConfig secret(String? secret);

  SignatureVerifierConfig secretSet(bool? secretSet);

  SignatureVerifierConfig keyOptional(bool? keyOptional);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SignatureVerifierConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SignatureVerifierConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  SignatureVerifierConfig call({
    SignatureVerifierConfigSchemeEnum scheme,
    String? header,
    String? secret,
    bool? secretSet,
    bool? keyOptional,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSignatureVerifierConfig.copyWith(...)` or call `instanceOfSignatureVerifierConfig.copyWith.fieldName(value)` for a single field.
class _$SignatureVerifierConfigCWProxyImpl
    implements _$SignatureVerifierConfigCWProxy {
  const _$SignatureVerifierConfigCWProxyImpl(this._value);

  final SignatureVerifierConfig _value;

  @override
  SignatureVerifierConfig scheme(SignatureVerifierConfigSchemeEnum scheme) =>
      call(scheme: scheme);

  @override
  SignatureVerifierConfig header(String? header) => call(header: header);

  @override
  SignatureVerifierConfig secret(String? secret) => call(secret: secret);

  @override
  SignatureVerifierConfig secretSet(bool? secretSet) =>
      call(secretSet: secretSet);

  @override
  SignatureVerifierConfig keyOptional(bool? keyOptional) =>
      call(keyOptional: keyOptional);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SignatureVerifierConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SignatureVerifierConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SignatureVerifierConfig call({
    Object? scheme = const $CopyWithPlaceholder(),
    Object? header = const $CopyWithPlaceholder(),
    Object? secret = const $CopyWithPlaceholder(),
    Object? secretSet = const $CopyWithPlaceholder(),
    Object? keyOptional = const $CopyWithPlaceholder(),
  }) {
    return SignatureVerifierConfig(
      scheme: scheme == const $CopyWithPlaceholder() || scheme == null
          ? _value.scheme
          // ignore: cast_nullable_to_non_nullable
          : scheme as SignatureVerifierConfigSchemeEnum,
      header: header == const $CopyWithPlaceholder()
          ? _value.header
          // ignore: cast_nullable_to_non_nullable
          : header as String?,
      secret: secret == const $CopyWithPlaceholder()
          ? _value.secret
          // ignore: cast_nullable_to_non_nullable
          : secret as String?,
      secretSet: secretSet == const $CopyWithPlaceholder()
          ? _value.secretSet
          // ignore: cast_nullable_to_non_nullable
          : secretSet as bool?,
      keyOptional: keyOptional == const $CopyWithPlaceholder()
          ? _value.keyOptional
          // ignore: cast_nullable_to_non_nullable
          : keyOptional as bool?,
    );
  }
}

extension $SignatureVerifierConfigCopyWith on SignatureVerifierConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSignatureVerifierConfig.copyWith(...)` or `instanceOfSignatureVerifierConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SignatureVerifierConfigCWProxy get copyWith =>
      _$SignatureVerifierConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SignatureVerifierConfig _$SignatureVerifierConfigFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SignatureVerifierConfig', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['scheme']);
  final val = SignatureVerifierConfig(
    scheme: $checkedConvert(
      'scheme',
      (v) => $enumDecode(_$SignatureVerifierConfigSchemeEnumEnumMap, v),
    ),
    header: $checkedConvert('header', (v) => v as String?),
    secret: $checkedConvert('secret', (v) => v as String?),
    secretSet: $checkedConvert('secretSet', (v) => v as bool?),
    keyOptional: $checkedConvert('keyOptional', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$SignatureVerifierConfigToJson(
  SignatureVerifierConfig instance,
) => <String, dynamic>{
  'scheme': _$SignatureVerifierConfigSchemeEnumEnumMap[instance.scheme]!,
  'header': ?instance.header,
  'secret': ?instance.secret,
  'secretSet': ?instance.secretSet,
  'keyOptional': ?instance.keyOptional,
};

const _$SignatureVerifierConfigSchemeEnumEnumMap = {
  SignatureVerifierConfigSchemeEnum.none: 'none',
  SignatureVerifierConfigSchemeEnum.hmacSha256: 'hmac-sha256',
  SignatureVerifierConfigSchemeEnum.standardWebhooks: 'standard-webhooks',
  SignatureVerifierConfigSchemeEnum.foresignal: 'foresignal',
};
