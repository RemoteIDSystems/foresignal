// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'access_request_grant.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AccessRequestGrantCWProxy {
  AccessRequestGrant deliveredAs(AccessRequestGrantDeliveredAsEnum deliveredAs);

  AccessRequestGrant role(AccessRequestGrantRoleEnum? role);

  AccessRequestGrant packageId(String? packageId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccessRequestGrant(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccessRequestGrant(...).copyWith(id: 12, name: "My name")
  /// ```
  AccessRequestGrant call({
    AccessRequestGrantDeliveredAsEnum deliveredAs,
    AccessRequestGrantRoleEnum? role,
    String? packageId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAccessRequestGrant.copyWith(...)` or call `instanceOfAccessRequestGrant.copyWith.fieldName(value)` for a single field.
class _$AccessRequestGrantCWProxyImpl implements _$AccessRequestGrantCWProxy {
  const _$AccessRequestGrantCWProxyImpl(this._value);

  final AccessRequestGrant _value;

  @override
  AccessRequestGrant deliveredAs(
    AccessRequestGrantDeliveredAsEnum deliveredAs,
  ) => call(deliveredAs: deliveredAs);

  @override
  AccessRequestGrant role(AccessRequestGrantRoleEnum? role) => call(role: role);

  @override
  AccessRequestGrant packageId(String? packageId) => call(packageId: packageId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AccessRequestGrant(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AccessRequestGrant(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AccessRequestGrant call({
    Object? deliveredAs = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? packageId = const $CopyWithPlaceholder(),
  }) {
    return AccessRequestGrant(
      deliveredAs:
          deliveredAs == const $CopyWithPlaceholder() || deliveredAs == null
          ? _value.deliveredAs
          // ignore: cast_nullable_to_non_nullable
          : deliveredAs as AccessRequestGrantDeliveredAsEnum,
      role: role == const $CopyWithPlaceholder()
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as AccessRequestGrantRoleEnum?,
      packageId: packageId == const $CopyWithPlaceholder()
          ? _value.packageId
          // ignore: cast_nullable_to_non_nullable
          : packageId as String?,
    );
  }
}

extension $AccessRequestGrantCopyWith on AccessRequestGrant {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAccessRequestGrant.copyWith(...)` or `instanceOfAccessRequestGrant.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AccessRequestGrantCWProxy get copyWith =>
      _$AccessRequestGrantCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AccessRequestGrant _$AccessRequestGrantFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AccessRequestGrant', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['deliveredAs']);
      final val = AccessRequestGrant(
        deliveredAs: $checkedConvert(
          'deliveredAs',
          (v) => $enumDecode(_$AccessRequestGrantDeliveredAsEnumEnumMap, v),
        ),
        role: $checkedConvert(
          'role',
          (v) => $enumDecodeNullable(_$AccessRequestGrantRoleEnumEnumMap, v),
        ),
        packageId: $checkedConvert('packageId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AccessRequestGrantToJson(AccessRequestGrant instance) =>
    <String, dynamic>{
      'deliveredAs':
          _$AccessRequestGrantDeliveredAsEnumEnumMap[instance.deliveredAs]!,
      'role': ?_$AccessRequestGrantRoleEnumEnumMap[instance.role],
      'packageId': ?instance.packageId,
    };

const _$AccessRequestGrantDeliveredAsEnumEnumMap = {
  AccessRequestGrantDeliveredAsEnum.acl: 'acl',
  AccessRequestGrantDeliveredAsEnum.report: 'report',
  AccessRequestGrantDeliveredAsEnum.chronicleExtract: 'chronicle-extract',
};

const _$AccessRequestGrantRoleEnumEnumMap = {
  AccessRequestGrantRoleEnum.viewer: 'viewer',
  AccessRequestGrantRoleEnum.commenter: 'commenter',
};
