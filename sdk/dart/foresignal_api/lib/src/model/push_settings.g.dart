// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushSettingsCWProxy {
  PushSettings contact(String? contact);

  PushSettings allowedPrivateHosts(List<String>? allowedPrivateHosts);

  PushSettings defaultLang(String? defaultLang);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  PushSettings call({
    String? contact,
    List<String>? allowedPrivateHosts,
    String? defaultLang,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushSettings.copyWith(...)` or call `instanceOfPushSettings.copyWith.fieldName(value)` for a single field.
class _$PushSettingsCWProxyImpl implements _$PushSettingsCWProxy {
  const _$PushSettingsCWProxyImpl(this._value);

  final PushSettings _value;

  @override
  PushSettings contact(String? contact) => call(contact: contact);

  @override
  PushSettings allowedPrivateHosts(List<String>? allowedPrivateHosts) =>
      call(allowedPrivateHosts: allowedPrivateHosts);

  @override
  PushSettings defaultLang(String? defaultLang) =>
      call(defaultLang: defaultLang);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushSettings call({
    Object? contact = const $CopyWithPlaceholder(),
    Object? allowedPrivateHosts = const $CopyWithPlaceholder(),
    Object? defaultLang = const $CopyWithPlaceholder(),
  }) {
    return PushSettings(
      contact: contact == const $CopyWithPlaceholder()
          ? _value.contact
          // ignore: cast_nullable_to_non_nullable
          : contact as String?,
      allowedPrivateHosts: allowedPrivateHosts == const $CopyWithPlaceholder()
          ? _value.allowedPrivateHosts
          // ignore: cast_nullable_to_non_nullable
          : allowedPrivateHosts as List<String>?,
      defaultLang: defaultLang == const $CopyWithPlaceholder()
          ? _value.defaultLang
          // ignore: cast_nullable_to_non_nullable
          : defaultLang as String?,
    );
  }
}

extension $PushSettingsCopyWith on PushSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushSettings.copyWith(...)` or `instanceOfPushSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushSettingsCWProxy get copyWith => _$PushSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushSettings _$PushSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PushSettings', json, ($checkedConvert) {
      final val = PushSettings(
        contact: $checkedConvert(
          'contact',
          (v) => v as String? ?? 'mailto:admin@localhost',
        ),
        allowedPrivateHosts: $checkedConvert(
          'allowedPrivateHosts',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        defaultLang: $checkedConvert(
          'defaultLang',
          (v) => v as String? ?? 'en',
        ),
      );
      return val;
    });

Map<String, dynamic> _$PushSettingsToJson(PushSettings instance) =>
    <String, dynamic>{
      'contact': ?instance.contact,
      'allowedPrivateHosts': ?instance.allowedPrivateHosts,
      'defaultLang': ?instance.defaultLang,
    };
