// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'share_link_create.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ShareLinkCreateCWProxy {
  ShareLinkCreate targetType(ShareLinkCreateTargetTypeEnum targetType);

  ShareLinkCreate targetRef(String targetRef);

  ShareLinkCreate fieldMask(List<String>? fieldMask);

  ShareLinkCreate precisionM(int? precisionM);

  ShareLinkCreate expiresAt(DateTime? expiresAt);

  ShareLinkCreate maxUses(int? maxUses);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ShareLinkCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ShareLinkCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  ShareLinkCreate call({
    ShareLinkCreateTargetTypeEnum targetType,
    String targetRef,
    List<String>? fieldMask,
    int? precisionM,
    DateTime? expiresAt,
    int? maxUses,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfShareLinkCreate.copyWith(...)` or call `instanceOfShareLinkCreate.copyWith.fieldName(value)` for a single field.
class _$ShareLinkCreateCWProxyImpl implements _$ShareLinkCreateCWProxy {
  const _$ShareLinkCreateCWProxyImpl(this._value);

  final ShareLinkCreate _value;

  @override
  ShareLinkCreate targetType(ShareLinkCreateTargetTypeEnum targetType) =>
      call(targetType: targetType);

  @override
  ShareLinkCreate targetRef(String targetRef) => call(targetRef: targetRef);

  @override
  ShareLinkCreate fieldMask(List<String>? fieldMask) =>
      call(fieldMask: fieldMask);

  @override
  ShareLinkCreate precisionM(int? precisionM) => call(precisionM: precisionM);

  @override
  ShareLinkCreate expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  @override
  ShareLinkCreate maxUses(int? maxUses) => call(maxUses: maxUses);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ShareLinkCreate(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ShareLinkCreate(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ShareLinkCreate call({
    Object? targetType = const $CopyWithPlaceholder(),
    Object? targetRef = const $CopyWithPlaceholder(),
    Object? fieldMask = const $CopyWithPlaceholder(),
    Object? precisionM = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? maxUses = const $CopyWithPlaceholder(),
  }) {
    return ShareLinkCreate(
      targetType:
          targetType == const $CopyWithPlaceholder() || targetType == null
          ? _value.targetType
          // ignore: cast_nullable_to_non_nullable
          : targetType as ShareLinkCreateTargetTypeEnum,
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
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      maxUses: maxUses == const $CopyWithPlaceholder()
          ? _value.maxUses
          // ignore: cast_nullable_to_non_nullable
          : maxUses as int?,
    );
  }
}

extension $ShareLinkCreateCopyWith on ShareLinkCreate {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfShareLinkCreate.copyWith(...)` or `instanceOfShareLinkCreate.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ShareLinkCreateCWProxy get copyWith => _$ShareLinkCreateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ShareLinkCreate _$ShareLinkCreateFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ShareLinkCreate', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['targetType', 'targetRef']);
      final val = ShareLinkCreate(
        targetType: $checkedConvert(
          'targetType',
          (v) => $enumDecode(_$ShareLinkCreateTargetTypeEnumEnumMap, v),
        ),
        targetRef: $checkedConvert('targetRef', (v) => v as String),
        fieldMask: $checkedConvert(
          'fieldMask',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        precisionM: $checkedConvert('precisionM', (v) => (v as num?)?.toInt()),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        maxUses: $checkedConvert('maxUses', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ShareLinkCreateToJson(
  ShareLinkCreate instance,
) => <String, dynamic>{
  'targetType': _$ShareLinkCreateTargetTypeEnumEnumMap[instance.targetType]!,
  'targetRef': instance.targetRef,
  'fieldMask': ?instance.fieldMask,
  'precisionM': ?instance.precisionM,
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
  'maxUses': ?instance.maxUses,
};

const _$ShareLinkCreateTargetTypeEnumEnumMap = {
  ShareLinkCreateTargetTypeEnum.record: 'record',
  ShareLinkCreateTargetTypeEnum.alert: 'alert',
  ShareLinkCreateTargetTypeEnum.view: 'view',
  ShareLinkCreateTargetTypeEnum.track: 'track',
  ShareLinkCreateTargetTypeEnum.briefingRun: 'briefing-run',
  ShareLinkCreateTargetTypeEnum.export_: 'export',
};
