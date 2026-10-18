// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'archive_settings_secrets.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ArchiveSettingsSecretsCWProxy {
  ArchiveSettingsSecrets accessKey(String? accessKey);

  ArchiveSettingsSecrets secretKey(String? secretKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchiveSettingsSecrets(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchiveSettingsSecrets(...).copyWith(id: 12, name: "My name")
  /// ```
  ArchiveSettingsSecrets call({String? accessKey, String? secretKey});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfArchiveSettingsSecrets.copyWith(...)` or call `instanceOfArchiveSettingsSecrets.copyWith.fieldName(value)` for a single field.
class _$ArchiveSettingsSecretsCWProxyImpl
    implements _$ArchiveSettingsSecretsCWProxy {
  const _$ArchiveSettingsSecretsCWProxyImpl(this._value);

  final ArchiveSettingsSecrets _value;

  @override
  ArchiveSettingsSecrets accessKey(String? accessKey) =>
      call(accessKey: accessKey);

  @override
  ArchiveSettingsSecrets secretKey(String? secretKey) =>
      call(secretKey: secretKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchiveSettingsSecrets(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchiveSettingsSecrets(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ArchiveSettingsSecrets call({
    Object? accessKey = const $CopyWithPlaceholder(),
    Object? secretKey = const $CopyWithPlaceholder(),
  }) {
    return ArchiveSettingsSecrets(
      accessKey: accessKey == const $CopyWithPlaceholder()
          ? _value.accessKey
          // ignore: cast_nullable_to_non_nullable
          : accessKey as String?,
      secretKey: secretKey == const $CopyWithPlaceholder()
          ? _value.secretKey
          // ignore: cast_nullable_to_non_nullable
          : secretKey as String?,
    );
  }
}

extension $ArchiveSettingsSecretsCopyWith on ArchiveSettingsSecrets {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfArchiveSettingsSecrets.copyWith(...)` or `instanceOfArchiveSettingsSecrets.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ArchiveSettingsSecretsCWProxy get copyWith =>
      _$ArchiveSettingsSecretsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ArchiveSettingsSecrets _$ArchiveSettingsSecretsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ArchiveSettingsSecrets', json, ($checkedConvert) {
  final val = ArchiveSettingsSecrets(
    accessKey: $checkedConvert('accessKey', (v) => v as String?),
    secretKey: $checkedConvert('secretKey', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ArchiveSettingsSecretsToJson(
  ArchiveSettingsSecrets instance,
) => <String, dynamic>{
  'accessKey': ?instance.accessKey,
  'secretKey': ?instance.secretKey,
};
