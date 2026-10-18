// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_source_registration.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalSourceRegistrationCWProxy {
  ExternalSourceRegistration manifest(EdpManifest? manifest);

  ExternalSourceRegistration manifestUrl(String? manifestUrl);

  ExternalSourceRegistration approve(bool? approve);

  ExternalSourceRegistration allowPrivateNetwork(bool? allowPrivateNetwork);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalSourceRegistration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalSourceRegistration(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalSourceRegistration call({
    EdpManifest? manifest,
    String? manifestUrl,
    bool? approve,
    bool? allowPrivateNetwork,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalSourceRegistration.copyWith(...)` or call `instanceOfExternalSourceRegistration.copyWith.fieldName(value)` for a single field.
class _$ExternalSourceRegistrationCWProxyImpl
    implements _$ExternalSourceRegistrationCWProxy {
  const _$ExternalSourceRegistrationCWProxyImpl(this._value);

  final ExternalSourceRegistration _value;

  @override
  ExternalSourceRegistration manifest(EdpManifest? manifest) =>
      call(manifest: manifest);

  @override
  ExternalSourceRegistration manifestUrl(String? manifestUrl) =>
      call(manifestUrl: manifestUrl);

  @override
  ExternalSourceRegistration approve(bool? approve) => call(approve: approve);

  @override
  ExternalSourceRegistration allowPrivateNetwork(bool? allowPrivateNetwork) =>
      call(allowPrivateNetwork: allowPrivateNetwork);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalSourceRegistration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalSourceRegistration(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalSourceRegistration call({
    Object? manifest = const $CopyWithPlaceholder(),
    Object? manifestUrl = const $CopyWithPlaceholder(),
    Object? approve = const $CopyWithPlaceholder(),
    Object? allowPrivateNetwork = const $CopyWithPlaceholder(),
  }) {
    return ExternalSourceRegistration(
      manifest: manifest == const $CopyWithPlaceholder()
          ? _value.manifest
          // ignore: cast_nullable_to_non_nullable
          : manifest as EdpManifest?,
      manifestUrl: manifestUrl == const $CopyWithPlaceholder()
          ? _value.manifestUrl
          // ignore: cast_nullable_to_non_nullable
          : manifestUrl as String?,
      approve: approve == const $CopyWithPlaceholder()
          ? _value.approve
          // ignore: cast_nullable_to_non_nullable
          : approve as bool?,
      allowPrivateNetwork: allowPrivateNetwork == const $CopyWithPlaceholder()
          ? _value.allowPrivateNetwork
          // ignore: cast_nullable_to_non_nullable
          : allowPrivateNetwork as bool?,
    );
  }
}

extension $ExternalSourceRegistrationCopyWith on ExternalSourceRegistration {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalSourceRegistration.copyWith(...)` or `instanceOfExternalSourceRegistration.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalSourceRegistrationCWProxy get copyWith =>
      _$ExternalSourceRegistrationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalSourceRegistration _$ExternalSourceRegistrationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExternalSourceRegistration', json, ($checkedConvert) {
  final val = ExternalSourceRegistration(
    manifest: $checkedConvert(
      'manifest',
      (v) => v == null ? null : EdpManifest.fromJson(v as Map<String, dynamic>),
    ),
    manifestUrl: $checkedConvert('manifestUrl', (v) => v as String?),
    approve: $checkedConvert('approve', (v) => v as bool? ?? false),
    allowPrivateNetwork: $checkedConvert(
      'allowPrivateNetwork',
      (v) => v as bool? ?? false,
    ),
  );
  return val;
});

Map<String, dynamic> _$ExternalSourceRegistrationToJson(
  ExternalSourceRegistration instance,
) => <String, dynamic>{
  'manifest': ?instance.manifest?.toJson(),
  'manifestUrl': ?instance.manifestUrl,
  'approve': ?instance.approve,
  'allowPrivateNetwork': ?instance.allowPrivateNetwork,
};
