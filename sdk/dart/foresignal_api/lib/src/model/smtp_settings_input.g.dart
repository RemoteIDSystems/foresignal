// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'smtp_settings_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SmtpSettingsInputCWProxy {
  SmtpSettingsInput enabled(bool enabled);

  SmtpSettingsInput host(String? host);

  SmtpSettingsInput port(int? port);

  SmtpSettingsInput security(String? security);

  SmtpSettingsInput username(String? username);

  SmtpSettingsInput password(String? password);

  SmtpSettingsInput fromAddress(String? fromAddress);

  SmtpSettingsInput fromName(String? fromName);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SmtpSettingsInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SmtpSettingsInput(...).copyWith(id: 12, name: "My name")
  /// ```
  SmtpSettingsInput call({
    bool enabled,
    String? host,
    int? port,
    String? security,
    String? username,
    String? password,
    String? fromAddress,
    String? fromName,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSmtpSettingsInput.copyWith(...)` or call `instanceOfSmtpSettingsInput.copyWith.fieldName(value)` for a single field.
class _$SmtpSettingsInputCWProxyImpl implements _$SmtpSettingsInputCWProxy {
  const _$SmtpSettingsInputCWProxyImpl(this._value);

  final SmtpSettingsInput _value;

  @override
  SmtpSettingsInput enabled(bool enabled) => call(enabled: enabled);

  @override
  SmtpSettingsInput host(String? host) => call(host: host);

  @override
  SmtpSettingsInput port(int? port) => call(port: port);

  @override
  SmtpSettingsInput security(String? security) => call(security: security);

  @override
  SmtpSettingsInput username(String? username) => call(username: username);

  @override
  SmtpSettingsInput password(String? password) => call(password: password);

  @override
  SmtpSettingsInput fromAddress(String? fromAddress) =>
      call(fromAddress: fromAddress);

  @override
  SmtpSettingsInput fromName(String? fromName) => call(fromName: fromName);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SmtpSettingsInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SmtpSettingsInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SmtpSettingsInput call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? host = const $CopyWithPlaceholder(),
    Object? port = const $CopyWithPlaceholder(),
    Object? security = const $CopyWithPlaceholder(),
    Object? username = const $CopyWithPlaceholder(),
    Object? password = const $CopyWithPlaceholder(),
    Object? fromAddress = const $CopyWithPlaceholder(),
    Object? fromName = const $CopyWithPlaceholder(),
  }) {
    return SmtpSettingsInput(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      host: host == const $CopyWithPlaceholder()
          ? _value.host
          // ignore: cast_nullable_to_non_nullable
          : host as String?,
      port: port == const $CopyWithPlaceholder()
          ? _value.port
          // ignore: cast_nullable_to_non_nullable
          : port as int?,
      security: security == const $CopyWithPlaceholder()
          ? _value.security
          // ignore: cast_nullable_to_non_nullable
          : security as String?,
      username: username == const $CopyWithPlaceholder()
          ? _value.username
          // ignore: cast_nullable_to_non_nullable
          : username as String?,
      password: password == const $CopyWithPlaceholder()
          ? _value.password
          // ignore: cast_nullable_to_non_nullable
          : password as String?,
      fromAddress: fromAddress == const $CopyWithPlaceholder()
          ? _value.fromAddress
          // ignore: cast_nullable_to_non_nullable
          : fromAddress as String?,
      fromName: fromName == const $CopyWithPlaceholder()
          ? _value.fromName
          // ignore: cast_nullable_to_non_nullable
          : fromName as String?,
    );
  }
}

extension $SmtpSettingsInputCopyWith on SmtpSettingsInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSmtpSettingsInput.copyWith(...)` or `instanceOfSmtpSettingsInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SmtpSettingsInputCWProxy get copyWith =>
      _$SmtpSettingsInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SmtpSettingsInput _$SmtpSettingsInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SmtpSettingsInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['enabled']);
      final val = SmtpSettingsInput(
        enabled: $checkedConvert('enabled', (v) => v as bool),
        host: $checkedConvert('host', (v) => v as String?),
        port: $checkedConvert('port', (v) => (v as num?)?.toInt()),
        security: $checkedConvert('security', (v) => v as String?),
        username: $checkedConvert('username', (v) => v as String?),
        password: $checkedConvert('password', (v) => v as String?),
        fromAddress: $checkedConvert('fromAddress', (v) => v as String?),
        fromName: $checkedConvert('fromName', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SmtpSettingsInputToJson(SmtpSettingsInput instance) =>
    <String, dynamic>{
      'enabled': instance.enabled,
      'host': ?instance.host,
      'port': ?instance.port,
      'security': ?instance.security,
      'username': ?instance.username,
      'password': ?instance.password,
      'fromAddress': ?instance.fromAddress,
      'fromName': ?instance.fromName,
    };
