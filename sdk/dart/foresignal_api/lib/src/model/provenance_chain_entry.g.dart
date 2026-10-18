// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'provenance_chain_entry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProvenanceChainEntryCWProxy {
  ProvenanceChainEntry instanceId(String instanceId);

  ProvenanceChainEntry principal(String principal);

  ProvenanceChainEntry productRef(String productRef);

  ProvenanceChainEntry manifestId(String? manifestId);

  ProvenanceChainEntry role(ProvenanceChainEntryRoleEnum? role);

  ProvenanceChainEntry at(DateTime? at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProvenanceChainEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProvenanceChainEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  ProvenanceChainEntry call({
    String instanceId,
    String principal,
    String productRef,
    String? manifestId,
    ProvenanceChainEntryRoleEnum? role,
    DateTime? at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProvenanceChainEntry.copyWith(...)` or call `instanceOfProvenanceChainEntry.copyWith.fieldName(value)` for a single field.
class _$ProvenanceChainEntryCWProxyImpl
    implements _$ProvenanceChainEntryCWProxy {
  const _$ProvenanceChainEntryCWProxyImpl(this._value);

  final ProvenanceChainEntry _value;

  @override
  ProvenanceChainEntry instanceId(String instanceId) =>
      call(instanceId: instanceId);

  @override
  ProvenanceChainEntry principal(String principal) =>
      call(principal: principal);

  @override
  ProvenanceChainEntry productRef(String productRef) =>
      call(productRef: productRef);

  @override
  ProvenanceChainEntry manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  @override
  ProvenanceChainEntry role(ProvenanceChainEntryRoleEnum? role) =>
      call(role: role);

  @override
  ProvenanceChainEntry at(DateTime? at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProvenanceChainEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProvenanceChainEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProvenanceChainEntry call({
    Object? instanceId = const $CopyWithPlaceholder(),
    Object? principal = const $CopyWithPlaceholder(),
    Object? productRef = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return ProvenanceChainEntry(
      instanceId:
          instanceId == const $CopyWithPlaceholder() || instanceId == null
          ? _value.instanceId
          // ignore: cast_nullable_to_non_nullable
          : instanceId as String,
      principal: principal == const $CopyWithPlaceholder() || principal == null
          ? _value.principal
          // ignore: cast_nullable_to_non_nullable
          : principal as String,
      productRef:
          productRef == const $CopyWithPlaceholder() || productRef == null
          ? _value.productRef
          // ignore: cast_nullable_to_non_nullable
          : productRef as String,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      role: role == const $CopyWithPlaceholder()
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as ProvenanceChainEntryRoleEnum?,
      at: at == const $CopyWithPlaceholder()
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime?,
    );
  }
}

extension $ProvenanceChainEntryCopyWith on ProvenanceChainEntry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProvenanceChainEntry.copyWith(...)` or `instanceOfProvenanceChainEntry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProvenanceChainEntryCWProxy get copyWith =>
      _$ProvenanceChainEntryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProvenanceChainEntry _$ProvenanceChainEntryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProvenanceChainEntry', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['instanceId', 'principal', 'productRef'],
  );
  final val = ProvenanceChainEntry(
    instanceId: $checkedConvert('instanceId', (v) => v as String),
    principal: $checkedConvert('principal', (v) => v as String),
    productRef: $checkedConvert('productRef', (v) => v as String),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
    role: $checkedConvert(
      'role',
      (v) => $enumDecodeNullable(_$ProvenanceChainEntryRoleEnumEnumMap, v),
    ),
    at: $checkedConvert(
      'at',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ProvenanceChainEntryToJson(
  ProvenanceChainEntry instance,
) => <String, dynamic>{
  'instanceId': instance.instanceId,
  'principal': instance.principal,
  'productRef': instance.productRef,
  'manifestId': ?instance.manifestId,
  'role': ?_$ProvenanceChainEntryRoleEnumEnumMap[instance.role],
  'at': ?instance.at?.toIso8601String(),
};

const _$ProvenanceChainEntryRoleEnumEnumMap = {
  ProvenanceChainEntryRoleEnum.origin: 'origin',
  ProvenanceChainEntryRoleEnum.author: 'author',
  ProvenanceChainEntryRoleEnum.forwarder: 'forwarder',
  ProvenanceChainEntryRoleEnum.importer: 'importer',
};
