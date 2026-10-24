// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'entity_key.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EntityKeyCWProxy {
  EntityKey id(int id);

  EntityKey entityId(String entityId);

  EntityKey keyType(EntityKeyKeyTypeEnum keyType);

  EntityKey keyValue(String keyValue);

  EntityKey role(EntityKeyRoleEnum role);

  EntityKey confidence(num confidence);

  EntityKey origin(EntityKeyOriginEnum origin);

  EntityKey originRef(String? originRef);

  EntityKey validFrom(DateTime validFrom);

  EntityKey validTo(DateTime? validTo);

  EntityKey retracted(bool retracted);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityKey(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityKey(...).copyWith(id: 12, name: "My name")
  /// ```
  EntityKey call({
    int id,
    String entityId,
    EntityKeyKeyTypeEnum keyType,
    String keyValue,
    EntityKeyRoleEnum role,
    num confidence,
    EntityKeyOriginEnum origin,
    String? originRef,
    DateTime validFrom,
    DateTime? validTo,
    bool retracted,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEntityKey.copyWith(...)` or call `instanceOfEntityKey.copyWith.fieldName(value)` for a single field.
class _$EntityKeyCWProxyImpl implements _$EntityKeyCWProxy {
  const _$EntityKeyCWProxyImpl(this._value);

  final EntityKey _value;

  @override
  EntityKey id(int id) => call(id: id);

  @override
  EntityKey entityId(String entityId) => call(entityId: entityId);

  @override
  EntityKey keyType(EntityKeyKeyTypeEnum keyType) => call(keyType: keyType);

  @override
  EntityKey keyValue(String keyValue) => call(keyValue: keyValue);

  @override
  EntityKey role(EntityKeyRoleEnum role) => call(role: role);

  @override
  EntityKey confidence(num confidence) => call(confidence: confidence);

  @override
  EntityKey origin(EntityKeyOriginEnum origin) => call(origin: origin);

  @override
  EntityKey originRef(String? originRef) => call(originRef: originRef);

  @override
  EntityKey validFrom(DateTime validFrom) => call(validFrom: validFrom);

  @override
  EntityKey validTo(DateTime? validTo) => call(validTo: validTo);

  @override
  EntityKey retracted(bool retracted) => call(retracted: retracted);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityKey(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityKey(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EntityKey call({
    Object? id = const $CopyWithPlaceholder(),
    Object? entityId = const $CopyWithPlaceholder(),
    Object? keyType = const $CopyWithPlaceholder(),
    Object? keyValue = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? originRef = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? validTo = const $CopyWithPlaceholder(),
    Object? retracted = const $CopyWithPlaceholder(),
  }) {
    return EntityKey(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      entityId: entityId == const $CopyWithPlaceholder() || entityId == null
          ? _value.entityId
          // ignore: cast_nullable_to_non_nullable
          : entityId as String,
      keyType: keyType == const $CopyWithPlaceholder() || keyType == null
          ? _value.keyType
          // ignore: cast_nullable_to_non_nullable
          : keyType as EntityKeyKeyTypeEnum,
      keyValue: keyValue == const $CopyWithPlaceholder() || keyValue == null
          ? _value.keyValue
          // ignore: cast_nullable_to_non_nullable
          : keyValue as String,
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as EntityKeyRoleEnum,
      confidence:
          confidence == const $CopyWithPlaceholder() || confidence == null
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num,
      origin: origin == const $CopyWithPlaceholder() || origin == null
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as EntityKeyOriginEnum,
      originRef: originRef == const $CopyWithPlaceholder()
          ? _value.originRef
          // ignore: cast_nullable_to_non_nullable
          : originRef as String?,
      validFrom: validFrom == const $CopyWithPlaceholder() || validFrom == null
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime,
      validTo: validTo == const $CopyWithPlaceholder()
          ? _value.validTo
          // ignore: cast_nullable_to_non_nullable
          : validTo as DateTime?,
      retracted: retracted == const $CopyWithPlaceholder() || retracted == null
          ? _value.retracted
          // ignore: cast_nullable_to_non_nullable
          : retracted as bool,
    );
  }
}

extension $EntityKeyCopyWith on EntityKey {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEntityKey.copyWith(...)` or `instanceOfEntityKey.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EntityKeyCWProxy get copyWith => _$EntityKeyCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EntityKey _$EntityKeyFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EntityKey', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'entityId',
          'keyType',
          'keyValue',
          'role',
          'confidence',
          'origin',
          'validFrom',
          'retracted',
        ],
      );
      final val = EntityKey(
        id: $checkedConvert('id', (v) => (v as num).toInt()),
        entityId: $checkedConvert('entityId', (v) => v as String),
        keyType: $checkedConvert(
          'keyType',
          (v) => $enumDecode(_$EntityKeyKeyTypeEnumEnumMap, v),
        ),
        keyValue: $checkedConvert('keyValue', (v) => v as String),
        role: $checkedConvert(
          'role',
          (v) => $enumDecode(_$EntityKeyRoleEnumEnumMap, v),
        ),
        confidence: $checkedConvert('confidence', (v) => v as num),
        origin: $checkedConvert(
          'origin',
          (v) => $enumDecode(_$EntityKeyOriginEnumEnumMap, v),
        ),
        originRef: $checkedConvert('originRef', (v) => v as String?),
        validFrom: $checkedConvert(
          'validFrom',
          (v) => DateTime.parse(v as String),
        ),
        validTo: $checkedConvert(
          'validTo',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        retracted: $checkedConvert('retracted', (v) => v as bool),
      );
      return val;
    });

Map<String, dynamic> _$EntityKeyToJson(EntityKey instance) => <String, dynamic>{
  'id': instance.id,
  'entityId': instance.entityId,
  'keyType': _$EntityKeyKeyTypeEnumEnumMap[instance.keyType]!,
  'keyValue': instance.keyValue,
  'role': _$EntityKeyRoleEnumEnumMap[instance.role]!,
  'confidence': instance.confidence,
  'origin': _$EntityKeyOriginEnumEnumMap[instance.origin]!,
  'originRef': ?instance.originRef,
  'validFrom': instance.validFrom.toIso8601String(),
  'validTo': ?instance.validTo?.toIso8601String(),
  'retracted': instance.retracted,
};

const _$EntityKeyKeyTypeEnumEnumMap = {
  EntityKeyKeyTypeEnum.airframe: 'airframe',
  EntityKeyKeyTypeEnum.icao24: 'icao24',
  EntityKeyKeyTypeEnum.registration: 'registration',
  EntityKeyKeyTypeEnum.imo: 'imo',
  EntityKeyKeyTypeEnum.mmsi: 'mmsi',
  EntityKeyKeyTypeEnum.norad: 'norad',
  EntityKeyKeyTypeEnum.cospar: 'cospar',
  EntityKeyKeyTypeEnum.callsign: 'callsign',
  EntityKeyKeyTypeEnum.lei: 'lei',
  EntityKeyKeyTypeEnum.companyNumber: 'company-number',
  EntityKeyKeyTypeEnum.taxId: 'tax-id',
  EntityKeyKeyTypeEnum.officerId: 'officer-id',
  EntityKeyKeyTypeEnum.personKey: 'person-key',
};

const _$EntityKeyRoleEnumEnumMap = {
  EntityKeyRoleEnum.ANCHOR: 'ANCHOR',
  EntityKeyRoleEnum.WEAK: 'WEAK',
};

const _$EntityKeyOriginEnumEnumMap = {
  EntityKeyOriginEnum.REGISTRY: 'REGISTRY',
  EntityKeyOriginEnum.CO_ASSERTED: 'CO_ASSERTED',
  EntityKeyOriginEnum.OBSERVED: 'OBSERVED',
  EntityKeyOriginEnum.OPERATOR: 'OPERATOR',
};
