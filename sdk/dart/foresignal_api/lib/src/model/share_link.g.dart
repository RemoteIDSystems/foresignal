// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'share_link.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ShareLinkCWProxy {
  ShareLink id(String id);

  ShareLink keyPrefix(String keyPrefix);

  ShareLink targetType(ShareLinkTargetTypeEnum targetType);

  ShareLink targetRef(String targetRef);

  ShareLink fieldMask(List<String>? fieldMask);

  ShareLink precisionM(int? precisionM);

  ShareLink masked(bool? masked);

  ShareLink expiresAt(DateTime expiresAt);

  ShareLink maxUses(int? maxUses);

  ShareLink uses(int uses);

  ShareLink status(ShareLinkStatusEnum status);

  ShareLink createdBy(String? createdBy);

  ShareLink createdAt(DateTime createdAt);

  ShareLink revokedAt(DateTime? revokedAt);

  ShareLink lastUsedAt(DateTime? lastUsedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ShareLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ShareLink(...).copyWith(id: 12, name: "My name")
  /// ```
  ShareLink call({
    String id,
    String keyPrefix,
    ShareLinkTargetTypeEnum targetType,
    String targetRef,
    List<String>? fieldMask,
    int? precisionM,
    bool? masked,
    DateTime expiresAt,
    int? maxUses,
    int uses,
    ShareLinkStatusEnum status,
    String? createdBy,
    DateTime createdAt,
    DateTime? revokedAt,
    DateTime? lastUsedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfShareLink.copyWith(...)` or call `instanceOfShareLink.copyWith.fieldName(value)` for a single field.
class _$ShareLinkCWProxyImpl implements _$ShareLinkCWProxy {
  const _$ShareLinkCWProxyImpl(this._value);

  final ShareLink _value;

  @override
  ShareLink id(String id) => call(id: id);

  @override
  ShareLink keyPrefix(String keyPrefix) => call(keyPrefix: keyPrefix);

  @override
  ShareLink targetType(ShareLinkTargetTypeEnum targetType) =>
      call(targetType: targetType);

  @override
  ShareLink targetRef(String targetRef) => call(targetRef: targetRef);

  @override
  ShareLink fieldMask(List<String>? fieldMask) => call(fieldMask: fieldMask);

  @override
  ShareLink precisionM(int? precisionM) => call(precisionM: precisionM);

  @override
  ShareLink masked(bool? masked) => call(masked: masked);

  @override
  ShareLink expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  @override
  ShareLink maxUses(int? maxUses) => call(maxUses: maxUses);

  @override
  ShareLink uses(int uses) => call(uses: uses);

  @override
  ShareLink status(ShareLinkStatusEnum status) => call(status: status);

  @override
  ShareLink createdBy(String? createdBy) => call(createdBy: createdBy);

  @override
  ShareLink createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  ShareLink revokedAt(DateTime? revokedAt) => call(revokedAt: revokedAt);

  @override
  ShareLink lastUsedAt(DateTime? lastUsedAt) => call(lastUsedAt: lastUsedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ShareLink(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ShareLink(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ShareLink call({
    Object? id = const $CopyWithPlaceholder(),
    Object? keyPrefix = const $CopyWithPlaceholder(),
    Object? targetType = const $CopyWithPlaceholder(),
    Object? targetRef = const $CopyWithPlaceholder(),
    Object? fieldMask = const $CopyWithPlaceholder(),
    Object? precisionM = const $CopyWithPlaceholder(),
    Object? masked = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? maxUses = const $CopyWithPlaceholder(),
    Object? uses = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? revokedAt = const $CopyWithPlaceholder(),
    Object? lastUsedAt = const $CopyWithPlaceholder(),
  }) {
    return ShareLink(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      keyPrefix: keyPrefix == const $CopyWithPlaceholder() || keyPrefix == null
          ? _value.keyPrefix
          // ignore: cast_nullable_to_non_nullable
          : keyPrefix as String,
      targetType:
          targetType == const $CopyWithPlaceholder() || targetType == null
          ? _value.targetType
          // ignore: cast_nullable_to_non_nullable
          : targetType as ShareLinkTargetTypeEnum,
      targetRef: targetRef == const $CopyWithPlaceholder() || targetRef == null
          ? _value.targetRef
          // ignore: cast_nullable_to_non_nullable
          : targetRef as String,
      fieldMask: fieldMask == const $CopyWithPlaceholder()
          ? _value.fieldMask
          // ignore: cast_nullable_to_non_nullable
          : fieldMask as List<String>?,
      precisionM: precisionM == const $CopyWithPlaceholder()
          ? _value.precisionM
          // ignore: cast_nullable_to_non_nullable
          : precisionM as int?,
      masked: masked == const $CopyWithPlaceholder()
          ? _value.masked
          // ignore: cast_nullable_to_non_nullable
          : masked as bool?,
      expiresAt: expiresAt == const $CopyWithPlaceholder() || expiresAt == null
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime,
      maxUses: maxUses == const $CopyWithPlaceholder()
          ? _value.maxUses
          // ignore: cast_nullable_to_non_nullable
          : maxUses as int?,
      uses: uses == const $CopyWithPlaceholder() || uses == null
          ? _value.uses
          // ignore: cast_nullable_to_non_nullable
          : uses as int,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ShareLinkStatusEnum,
      createdBy: createdBy == const $CopyWithPlaceholder()
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      revokedAt: revokedAt == const $CopyWithPlaceholder()
          ? _value.revokedAt
          // ignore: cast_nullable_to_non_nullable
          : revokedAt as DateTime?,
      lastUsedAt: lastUsedAt == const $CopyWithPlaceholder()
          ? _value.lastUsedAt
          // ignore: cast_nullable_to_non_nullable
          : lastUsedAt as DateTime?,
    );
  }
}

extension $ShareLinkCopyWith on ShareLink {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfShareLink.copyWith(...)` or `instanceOfShareLink.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ShareLinkCWProxy get copyWith => _$ShareLinkCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ShareLink _$ShareLinkFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ShareLink', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'keyPrefix',
      'targetType',
      'targetRef',
      'expiresAt',
      'uses',
      'status',
      'createdAt',
    ],
  );
  final val = ShareLink(
    id: $checkedConvert('id', (v) => v as String),
    keyPrefix: $checkedConvert('keyPrefix', (v) => v as String),
    targetType: $checkedConvert(
      'targetType',
      (v) => $enumDecode(_$ShareLinkTargetTypeEnumEnumMap, v),
    ),
    targetRef: $checkedConvert('targetRef', (v) => v as String),
    fieldMask: $checkedConvert(
      'fieldMask',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    precisionM: $checkedConvert('precisionM', (v) => (v as num?)?.toInt()),
    masked: $checkedConvert('masked', (v) => v as bool?),
    expiresAt: $checkedConvert('expiresAt', (v) => DateTime.parse(v as String)),
    maxUses: $checkedConvert('maxUses', (v) => (v as num?)?.toInt()),
    uses: $checkedConvert('uses', (v) => (v as num).toInt()),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ShareLinkStatusEnumEnumMap, v),
    ),
    createdBy: $checkedConvert('createdBy', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    revokedAt: $checkedConvert(
      'revokedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastUsedAt: $checkedConvert(
      'lastUsedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ShareLinkToJson(ShareLink instance) => <String, dynamic>{
  'id': instance.id,
  'keyPrefix': instance.keyPrefix,
  'targetType': _$ShareLinkTargetTypeEnumEnumMap[instance.targetType]!,
  'targetRef': instance.targetRef,
  'fieldMask': ?instance.fieldMask,
  'precisionM': ?instance.precisionM,
  'masked': ?instance.masked,
  'expiresAt': instance.expiresAt.toIso8601String(),
  'maxUses': ?instance.maxUses,
  'uses': instance.uses,
  'status': _$ShareLinkStatusEnumEnumMap[instance.status]!,
  'createdBy': ?instance.createdBy,
  'createdAt': instance.createdAt.toIso8601String(),
  'revokedAt': ?instance.revokedAt?.toIso8601String(),
  'lastUsedAt': ?instance.lastUsedAt?.toIso8601String(),
};

const _$ShareLinkTargetTypeEnumEnumMap = {
  ShareLinkTargetTypeEnum.record: 'record',
  ShareLinkTargetTypeEnum.alert: 'alert',
  ShareLinkTargetTypeEnum.view: 'view',
  ShareLinkTargetTypeEnum.track: 'track',
  ShareLinkTargetTypeEnum.briefingRun: 'briefing-run',
  ShareLinkTargetTypeEnum.export_: 'export',
  ShareLinkTargetTypeEnum.workspace: 'workspace',
  ShareLinkTargetTypeEnum.report: 'report',
  ShareLinkTargetTypeEnum.editionSite: 'edition-site',
};

const _$ShareLinkStatusEnumEnumMap = {
  ShareLinkStatusEnum.active: 'active',
  ShareLinkStatusEnum.expired: 'expired',
  ShareLinkStatusEnum.revoked: 'revoked',
  ShareLinkStatusEnum.exhausted: 'exhausted',
};
