// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'egress_route_input_secrets.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EgressRouteInputSecretsCWProxy {
  EgressRouteInputSecrets username(String? username);

  EgressRouteInputSecrets password(String? password);

  EgressRouteInputSecrets wireguardConfig(String? wireguardConfig);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressRouteInputSecrets(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressRouteInputSecrets(...).copyWith(id: 12, name: "My name")
  /// ```
  EgressRouteInputSecrets call({
    String? username,
    String? password,
    String? wireguardConfig,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEgressRouteInputSecrets.copyWith(...)` or call `instanceOfEgressRouteInputSecrets.copyWith.fieldName(value)` for a single field.
class _$EgressRouteInputSecretsCWProxyImpl
    implements _$EgressRouteInputSecretsCWProxy {
  const _$EgressRouteInputSecretsCWProxyImpl(this._value);

  final EgressRouteInputSecrets _value;

  @override
  EgressRouteInputSecrets username(String? username) =>
      call(username: username);

  @override
  EgressRouteInputSecrets password(String? password) =>
      call(password: password);

  @override
  EgressRouteInputSecrets wireguardConfig(String? wireguardConfig) =>
      call(wireguardConfig: wireguardConfig);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EgressRouteInputSecrets(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EgressRouteInputSecrets(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EgressRouteInputSecrets call({
    Object? username = const $CopyWithPlaceholder(),
    Object? password = const $CopyWithPlaceholder(),
    Object? wireguardConfig = const $CopyWithPlaceholder(),
  }) {
    return EgressRouteInputSecrets(
      username: username == const $CopyWithPlaceholder()
          ? _value.username
          // ignore: cast_nullable_to_non_nullable
          : username as String?,
      password: password == const $CopyWithPlaceholder()
          ? _value.password
          // ignore: cast_nullable_to_non_nullable
          : password as String?,
      wireguardConfig: wireguardConfig == const $CopyWithPlaceholder()
          ? _value.wireguardConfig
          // ignore: cast_nullable_to_non_nullable
          : wireguardConfig as String?,
    );
  }
}

extension $EgressRouteInputSecretsCopyWith on EgressRouteInputSecrets {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEgressRouteInputSecrets.copyWith(...)` or `instanceOfEgressRouteInputSecrets.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EgressRouteInputSecretsCWProxy get copyWith =>
      _$EgressRouteInputSecretsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EgressRouteInputSecrets _$EgressRouteInputSecretsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EgressRouteInputSecrets', json, ($checkedConvert) {
  final val = EgressRouteInputSecrets(
    username: $checkedConvert('username', (v) => v as String?),
    password: $checkedConvert('password', (v) => v as String?),
    wireguardConfig: $checkedConvert('wireguardConfig', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$EgressRouteInputSecretsToJson(
  EgressRouteInputSecrets instance,
) => <String, dynamic>{
  'username': ?instance.username,
  'password': ?instance.password,
  'wireguardConfig': ?instance.wireguardConfig,
};
