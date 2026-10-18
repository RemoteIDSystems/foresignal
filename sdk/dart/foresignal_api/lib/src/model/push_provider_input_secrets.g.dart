// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_provider_input_secrets.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushProviderInputSecretsCWProxy {
  PushProviderInputSecrets p8(String? p8);

  PushProviderInputSecrets serviceAccountJson(String? serviceAccountJson);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushProviderInputSecrets(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushProviderInputSecrets(...).copyWith(id: 12, name: "My name")
  /// ```
  PushProviderInputSecrets call({String? p8, String? serviceAccountJson});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushProviderInputSecrets.copyWith(...)` or call `instanceOfPushProviderInputSecrets.copyWith.fieldName(value)` for a single field.
class _$PushProviderInputSecretsCWProxyImpl
    implements _$PushProviderInputSecretsCWProxy {
  const _$PushProviderInputSecretsCWProxyImpl(this._value);

  final PushProviderInputSecrets _value;

  @override
  PushProviderInputSecrets p8(String? p8) => call(p8: p8);

  @override
  PushProviderInputSecrets serviceAccountJson(String? serviceAccountJson) =>
      call(serviceAccountJson: serviceAccountJson);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushProviderInputSecrets(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushProviderInputSecrets(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushProviderInputSecrets call({
    Object? p8 = const $CopyWithPlaceholder(),
    Object? serviceAccountJson = const $CopyWithPlaceholder(),
  }) {
    return PushProviderInputSecrets(
      p8: p8 == const $CopyWithPlaceholder()
          ? _value.p8
          // ignore: cast_nullable_to_non_nullable
          : p8 as String?,
      serviceAccountJson: serviceAccountJson == const $CopyWithPlaceholder()
          ? _value.serviceAccountJson
          // ignore: cast_nullable_to_non_nullable
          : serviceAccountJson as String?,
    );
  }
}

extension $PushProviderInputSecretsCopyWith on PushProviderInputSecrets {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushProviderInputSecrets.copyWith(...)` or `instanceOfPushProviderInputSecrets.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushProviderInputSecretsCWProxy get copyWith =>
      _$PushProviderInputSecretsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushProviderInputSecrets _$PushProviderInputSecretsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PushProviderInputSecrets', json, ($checkedConvert) {
  final val = PushProviderInputSecrets(
    p8: $checkedConvert('p8', (v) => v as String?),
    serviceAccountJson: $checkedConvert(
      'serviceAccountJson',
      (v) => v as String?,
    ),
  );
  return val;
});

Map<String, dynamic> _$PushProviderInputSecretsToJson(
  PushProviderInputSecrets instance,
) => <String, dynamic>{
  'p8': ?instance.p8,
  'serviceAccountJson': ?instance.serviceAccountJson,
};
