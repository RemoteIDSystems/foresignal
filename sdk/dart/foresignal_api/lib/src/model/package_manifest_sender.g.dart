// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'package_manifest_sender.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PackageManifestSenderCWProxy {
  PackageManifestSender instanceId(String instanceId);

  PackageManifestSender instanceName(String? instanceName);

  PackageManifestSender keyId(String keyId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackageManifestSender(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackageManifestSender(...).copyWith(id: 12, name: "My name")
  /// ```
  PackageManifestSender call({
    String instanceId,
    String? instanceName,
    String keyId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPackageManifestSender.copyWith(...)` or call `instanceOfPackageManifestSender.copyWith.fieldName(value)` for a single field.
class _$PackageManifestSenderCWProxyImpl
    implements _$PackageManifestSenderCWProxy {
  const _$PackageManifestSenderCWProxyImpl(this._value);

  final PackageManifestSender _value;

  @override
  PackageManifestSender instanceId(String instanceId) =>
      call(instanceId: instanceId);

  @override
  PackageManifestSender instanceName(String? instanceName) =>
      call(instanceName: instanceName);

  @override
  PackageManifestSender keyId(String keyId) => call(keyId: keyId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackageManifestSender(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackageManifestSender(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PackageManifestSender call({
    Object? instanceId = const $CopyWithPlaceholder(),
    Object? instanceName = const $CopyWithPlaceholder(),
    Object? keyId = const $CopyWithPlaceholder(),
  }) {
    return PackageManifestSender(
      instanceId:
          instanceId == const $CopyWithPlaceholder() || instanceId == null
          ? _value.instanceId
          // ignore: cast_nullable_to_non_nullable
          : instanceId as String,
      instanceName: instanceName == const $CopyWithPlaceholder()
          ? _value.instanceName
          // ignore: cast_nullable_to_non_nullable
          : instanceName as String?,
      keyId: keyId == const $CopyWithPlaceholder() || keyId == null
          ? _value.keyId
          // ignore: cast_nullable_to_non_nullable
          : keyId as String,
    );
  }
}

extension $PackageManifestSenderCopyWith on PackageManifestSender {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPackageManifestSender.copyWith(...)` or `instanceOfPackageManifestSender.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PackageManifestSenderCWProxy get copyWith =>
      _$PackageManifestSenderCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PackageManifestSender _$PackageManifestSenderFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PackageManifestSender', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['instanceId', 'keyId']);
  final val = PackageManifestSender(
    instanceId: $checkedConvert('instanceId', (v) => v as String),
    instanceName: $checkedConvert('instanceName', (v) => v as String?),
    keyId: $checkedConvert('keyId', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$PackageManifestSenderToJson(
  PackageManifestSender instance,
) => <String, dynamic>{
  'instanceId': instance.instanceId,
  'instanceName': ?instance.instanceName,
  'keyId': instance.keyId,
};
