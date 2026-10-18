// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'package_manifest_authors_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PackageManifestAuthorsInnerCWProxy {
  PackageManifestAuthorsInner handle(String handle);

  PackageManifestAuthorsInner displayName(String? displayName);

  PackageManifestAuthorsInner instanceId(String instanceId);

  PackageManifestAuthorsInner role(PackageManifestAuthorsInnerRoleEnum role);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackageManifestAuthorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackageManifestAuthorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  PackageManifestAuthorsInner call({
    String handle,
    String? displayName,
    String instanceId,
    PackageManifestAuthorsInnerRoleEnum role,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPackageManifestAuthorsInner.copyWith(...)` or call `instanceOfPackageManifestAuthorsInner.copyWith.fieldName(value)` for a single field.
class _$PackageManifestAuthorsInnerCWProxyImpl
    implements _$PackageManifestAuthorsInnerCWProxy {
  const _$PackageManifestAuthorsInnerCWProxyImpl(this._value);

  final PackageManifestAuthorsInner _value;

  @override
  PackageManifestAuthorsInner handle(String handle) => call(handle: handle);

  @override
  PackageManifestAuthorsInner displayName(String? displayName) =>
      call(displayName: displayName);

  @override
  PackageManifestAuthorsInner instanceId(String instanceId) =>
      call(instanceId: instanceId);

  @override
  PackageManifestAuthorsInner role(PackageManifestAuthorsInnerRoleEnum role) =>
      call(role: role);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PackageManifestAuthorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PackageManifestAuthorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PackageManifestAuthorsInner call({
    Object? handle = const $CopyWithPlaceholder(),
    Object? displayName = const $CopyWithPlaceholder(),
    Object? instanceId = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
  }) {
    return PackageManifestAuthorsInner(
      handle: handle == const $CopyWithPlaceholder() || handle == null
          ? _value.handle
          // ignore: cast_nullable_to_non_nullable
          : handle as String,
      displayName: displayName == const $CopyWithPlaceholder()
          ? _value.displayName
          // ignore: cast_nullable_to_non_nullable
          : displayName as String?,
      instanceId:
          instanceId == const $CopyWithPlaceholder() || instanceId == null
          ? _value.instanceId
          // ignore: cast_nullable_to_non_nullable
          : instanceId as String,
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as PackageManifestAuthorsInnerRoleEnum,
    );
  }
}

extension $PackageManifestAuthorsInnerCopyWith on PackageManifestAuthorsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPackageManifestAuthorsInner.copyWith(...)` or `instanceOfPackageManifestAuthorsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PackageManifestAuthorsInnerCWProxy get copyWith =>
      _$PackageManifestAuthorsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PackageManifestAuthorsInner _$PackageManifestAuthorsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PackageManifestAuthorsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['handle', 'instanceId', 'role']);
  final val = PackageManifestAuthorsInner(
    handle: $checkedConvert('handle', (v) => v as String),
    displayName: $checkedConvert('displayName', (v) => v as String?),
    instanceId: $checkedConvert('instanceId', (v) => v as String),
    role: $checkedConvert(
      'role',
      (v) => $enumDecode(_$PackageManifestAuthorsInnerRoleEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$PackageManifestAuthorsInnerToJson(
  PackageManifestAuthorsInner instance,
) => <String, dynamic>{
  'handle': instance.handle,
  'displayName': ?instance.displayName,
  'instanceId': instance.instanceId,
  'role': _$PackageManifestAuthorsInnerRoleEnumEnumMap[instance.role]!,
};

const _$PackageManifestAuthorsInnerRoleEnumEnumMap = {
  PackageManifestAuthorsInnerRoleEnum.author: 'author',
  PackageManifestAuthorsInnerRoleEnum.forwarder: 'forwarder',
};
