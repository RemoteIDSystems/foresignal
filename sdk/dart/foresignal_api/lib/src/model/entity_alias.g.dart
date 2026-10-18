// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'entity_alias.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EntityAliasCWProxy {
  EntityAlias id(int id);

  EntityAlias alias(String alias);

  EntityAlias aliasType(EntityAliasAliasTypeEnum aliasType);

  EntityAlias validFrom(DateTime validFrom);

  EntityAlias validTo(DateTime? validTo);

  EntityAlias origin(EntityAliasOriginEnum origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityAlias(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityAlias(...).copyWith(id: 12, name: "My name")
  /// ```
  EntityAlias call({
    int id,
    String alias,
    EntityAliasAliasTypeEnum aliasType,
    DateTime validFrom,
    DateTime? validTo,
    EntityAliasOriginEnum origin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEntityAlias.copyWith(...)` or call `instanceOfEntityAlias.copyWith.fieldName(value)` for a single field.
class _$EntityAliasCWProxyImpl implements _$EntityAliasCWProxy {
  const _$EntityAliasCWProxyImpl(this._value);

  final EntityAlias _value;

  @override
  EntityAlias id(int id) => call(id: id);

  @override
  EntityAlias alias(String alias) => call(alias: alias);

  @override
  EntityAlias aliasType(EntityAliasAliasTypeEnum aliasType) =>
      call(aliasType: aliasType);

  @override
  EntityAlias validFrom(DateTime validFrom) => call(validFrom: validFrom);

  @override
  EntityAlias validTo(DateTime? validTo) => call(validTo: validTo);

  @override
  EntityAlias origin(EntityAliasOriginEnum origin) => call(origin: origin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EntityAlias(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EntityAlias(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EntityAlias call({
    Object? id = const $CopyWithPlaceholder(),
    Object? alias = const $CopyWithPlaceholder(),
    Object? aliasType = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? validTo = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
  }) {
    return EntityAlias(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      alias: alias == const $CopyWithPlaceholder() || alias == null
          ? _value.alias
          // ignore: cast_nullable_to_non_nullable
          : alias as String,
      aliasType: aliasType == const $CopyWithPlaceholder() || aliasType == null
          ? _value.aliasType
          // ignore: cast_nullable_to_non_nullable
          : aliasType as EntityAliasAliasTypeEnum,
      validFrom: validFrom == const $CopyWithPlaceholder() || validFrom == null
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime,
      validTo: validTo == const $CopyWithPlaceholder()
          ? _value.validTo
          // ignore: cast_nullable_to_non_nullable
          : validTo as DateTime?,
      origin: origin == const $CopyWithPlaceholder() || origin == null
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as EntityAliasOriginEnum,
    );
  }
}

extension $EntityAliasCopyWith on EntityAlias {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEntityAlias.copyWith(...)` or `instanceOfEntityAlias.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EntityAliasCWProxy get copyWith => _$EntityAliasCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EntityAlias _$EntityAliasFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EntityAlias', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'alias', 'aliasType', 'validFrom', 'origin'],
      );
      final val = EntityAlias(
        id: $checkedConvert('id', (v) => (v as num).toInt()),
        alias: $checkedConvert('alias', (v) => v as String),
        aliasType: $checkedConvert(
          'aliasType',
          (v) => $enumDecode(_$EntityAliasAliasTypeEnumEnumMap, v),
        ),
        validFrom: $checkedConvert(
          'validFrom',
          (v) => DateTime.parse(v as String),
        ),
        validTo: $checkedConvert(
          'validTo',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        origin: $checkedConvert(
          'origin',
          (v) => $enumDecode(_$EntityAliasOriginEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EntityAliasToJson(EntityAlias instance) =>
    <String, dynamic>{
      'id': instance.id,
      'alias': instance.alias,
      'aliasType': _$EntityAliasAliasTypeEnumEnumMap[instance.aliasType]!,
      'validFrom': instance.validFrom.toIso8601String(),
      'validTo': ?instance.validTo?.toIso8601String(),
      'origin': _$EntityAliasOriginEnumEnumMap[instance.origin]!,
    };

const _$EntityAliasAliasTypeEnumEnumMap = {
  EntityAliasAliasTypeEnum.NAME: 'NAME',
  EntityAliasAliasTypeEnum.FORMER_NAME: 'FORMER_NAME',
  EntityAliasAliasTypeEnum.OPERATOR_LABEL: 'OPERATOR_LABEL',
};

const _$EntityAliasOriginEnumEnumMap = {
  EntityAliasOriginEnum.REGISTRY: 'REGISTRY',
  EntityAliasOriginEnum.CO_ASSERTED: 'CO_ASSERTED',
  EntityAliasOriginEnum.OBSERVED: 'OBSERVED',
  EntityAliasOriginEnum.OPERATOR: 'OPERATOR',
};
