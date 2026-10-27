// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'smtp_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SmtpSettingsCWProxy {
  SmtpSettings enabled(bool enabled);

  SmtpSettings host(String? host);

  SmtpSettings port(int? port);

  SmtpSettings security(String? security);

  SmtpSettings username(String? username);

  SmtpSettings fromAddress(String? fromAddress);

  SmtpSettings fromName(String? fromName);

  SmtpSettings secretsSet(List<String>? secretsSet);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SmtpSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SmtpSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  SmtpSettings call({
    bool enabled,
    String? host,
    int? port,
    String? security,
    String? username,
    String? fromAddress,
    String? fromName,
    List<String>? secretsSet,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSmtpSettings.copyWith(...)` or call `instanceOfSmtpSettings.copyWith.fieldName(value)` for a single field.
class _$SmtpSettingsCWProxyImpl implements _$SmtpSettingsCWProxy {
  const _$SmtpSettingsCWProxyImpl(this._value);

  final SmtpSettings _value;

  @override
  SmtpSettings enabled(bool enabled) => call(enabled: enabled);

  @override
  SmtpSettings host(String? host) => call(host: host);

  @override
  SmtpSettings port(int? port) => call(port: port);

  @override
  SmtpSettings security(String? security) => call(security: security);

  @override
  SmtpSettings username(String? username) => call(username: username);

  @override
  SmtpSettings fromAddress(String? fromAddress) =>
      call(fromAddress: fromAddress);

  @override
  SmtpSettings fromName(String? fromName) => call(fromName: fromName);

  @override
  SmtpSettings secretsSet(List<String>? secretsSet) =>
      call(secretsSet: secretsSet);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SmtpSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SmtpSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SmtpSettings call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? host = const $CopyWithPlaceholder(),
    Object? port = const $CopyWithPlaceholder(),
    Object? security = const $CopyWithPlaceholder(),
    Object? username = const $CopyWithPlaceholder(),
    Object? fromAddress = const $CopyWithPlaceholder(),
    Object? fromName = const $CopyWithPlaceholder(),
    Object? secretsSet = const $CopyWithPlaceholder(),
  }) {
    return SmtpSettings(
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
      fromAddress: fromAddress == const $CopyWithPlaceholder()
          ? _value.fromAddress
          // ignore: cast_nullable_to_non_nullable
          : fromAddress as String?,
      fromName: fromName == const $CopyWithPlaceholder()
          ? _value.fromName
          // ignore: cast_nullable_to_non_nullable
          : fromName as String?,
      secretsSet: secretsSet == const $CopyWithPlaceholder()
          ? _value.secretsSet
          // ignore: cast_nullable_to_non_nullable
          : secretsSet as List<String>?,
    );
  }
}

extension $SmtpSettingsCopyWith on SmtpSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSmtpSettings.copyWith(...)` or `instanceOfSmtpSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SmtpSettingsCWProxy get copyWith => _$SmtpSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SmtpSettings _$SmtpSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SmtpSettings', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['enabled']);
      final val = SmtpSettings(
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? false),
        host: $checkedConvert('host', (v) => v as String?),
        port: $checkedConvert('port', (v) => (v as num?)?.toInt()),
        security: $checkedConvert(
          'security',
          (v) => v as String? ?? 'starttls',
        ),
        username: $checkedConvert('username', (v) => v as String?),
        fromAddress: $checkedConvert('fromAddress', (v) => v as String?),
        fromName: $checkedConvert('fromName', (v) => v as String?),
        secretsSet: $checkedConvert(
          'secretsSet',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SmtpSettingsToJson(SmtpSettings instance) =>
    <String, dynamic>{
      'enabled': instance.enabled,
      'host': ?instance.host,
      'port': ?instance.port,
      'security': ?instance.security,
      'username': ?instance.username,
      'fromAddress': ?instance.fromAddress,
      'fromName': ?instance.fromName,
      'secretsSet': ?instance.secretsSet,
    };
