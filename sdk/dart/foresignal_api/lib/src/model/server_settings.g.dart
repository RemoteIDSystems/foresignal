// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'server_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ServerSettingsCWProxy {
  ServerSettings publicBaseUrl(String? publicBaseUrl);

  ServerSettings timezone(String? timezone);

  ServerSettings clientLinks(ClientLinks? clientLinks);

  ServerSettings warnings(List<SettingsWarning>? warnings);

  ServerSettings push(PushSettings? push);

  ServerSettings contactEmail(String? contactEmail);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ServerSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ServerSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  ServerSettings call({
    String? publicBaseUrl,
    String? timezone,
    ClientLinks? clientLinks,
    List<SettingsWarning>? warnings,
    PushSettings? push,
    String? contactEmail,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfServerSettings.copyWith(...)` or call `instanceOfServerSettings.copyWith.fieldName(value)` for a single field.
class _$ServerSettingsCWProxyImpl implements _$ServerSettingsCWProxy {
  const _$ServerSettingsCWProxyImpl(this._value);

  final ServerSettings _value;

  @override
  ServerSettings publicBaseUrl(String? publicBaseUrl) =>
      call(publicBaseUrl: publicBaseUrl);

  @override
  ServerSettings timezone(String? timezone) => call(timezone: timezone);

  @override
  ServerSettings clientLinks(ClientLinks? clientLinks) =>
      call(clientLinks: clientLinks);

  @override
  ServerSettings warnings(List<SettingsWarning>? warnings) =>
      call(warnings: warnings);

  @override
  ServerSettings push(PushSettings? push) => call(push: push);

  @override
  ServerSettings contactEmail(String? contactEmail) =>
      call(contactEmail: contactEmail);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ServerSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ServerSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ServerSettings call({
    Object? publicBaseUrl = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? clientLinks = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? push = const $CopyWithPlaceholder(),
    Object? contactEmail = const $CopyWithPlaceholder(),
  }) {
    return ServerSettings(
      publicBaseUrl: publicBaseUrl == const $CopyWithPlaceholder()
          ? _value.publicBaseUrl
          // ignore: cast_nullable_to_non_nullable
          : publicBaseUrl as String?,
      timezone: timezone == const $CopyWithPlaceholder()
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String?,
      clientLinks: clientLinks == const $CopyWithPlaceholder()
          ? _value.clientLinks
          // ignore: cast_nullable_to_non_nullable
          : clientLinks as ClientLinks?,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<SettingsWarning>?,
      push: push == const $CopyWithPlaceholder()
          ? _value.push
          // ignore: cast_nullable_to_non_nullable
          : push as PushSettings?,
      contactEmail: contactEmail == const $CopyWithPlaceholder()
          ? _value.contactEmail
          // ignore: cast_nullable_to_non_nullable
          : contactEmail as String?,
    );
  }
}

extension $ServerSettingsCopyWith on ServerSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfServerSettings.copyWith(...)` or `instanceOfServerSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ServerSettingsCWProxy get copyWith => _$ServerSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ServerSettings _$ServerSettingsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ServerSettings', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['publicBaseUrl']);
  final val = ServerSettings(
    publicBaseUrl: $checkedConvert('publicBaseUrl', (v) => v as String?),
    timezone: $checkedConvert('timezone', (v) => v as String? ?? 'UTC'),
    clientLinks: $checkedConvert(
      'clientLinks',
      (v) => v == null ? null : ClientLinks.fromJson(v as Map<String, dynamic>),
    ),
    warnings: $checkedConvert(
      'warnings',
      (v) => (v as List<dynamic>?)
          ?.map((e) => SettingsWarning.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    push: $checkedConvert(
      'push',
      (v) =>
          v == null ? null : PushSettings.fromJson(v as Map<String, dynamic>),
    ),
    contactEmail: $checkedConvert('contactEmail', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ServerSettingsToJson(ServerSettings instance) =>
    <String, dynamic>{
      'publicBaseUrl': instance.publicBaseUrl,
      'timezone': ?instance.timezone,
      'clientLinks': ?instance.clientLinks?.toJson(),
      'warnings': ?instance.warnings?.map((e) => e.toJson()).toList(),
      'push': ?instance.push?.toJson(),
      'contactEmail': ?instance.contactEmail,
    };
