// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_rules_rules_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraRulesRulesInnerCWProxy {
  InfraRulesRulesInner id(InfraRulesRulesInnerIdEnum id);

  InfraRulesRulesInner enabled(bool enabled);

  InfraRulesRulesInner method(InfraRulesRulesInnerMethodEnum? method);

  InfraRulesRulesInner relation(InfraRulesRulesInnerRelationEnum? relation);

  InfraRulesRulesInner label(InfraRulesRulesInnerLabelEnum? label);

  InfraRulesRulesInner params(Map<String, Object>? params);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraRulesRulesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraRulesRulesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraRulesRulesInner call({
    InfraRulesRulesInnerIdEnum id,
    bool enabled,
    InfraRulesRulesInnerMethodEnum? method,
    InfraRulesRulesInnerRelationEnum? relation,
    InfraRulesRulesInnerLabelEnum? label,
    Map<String, Object>? params,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraRulesRulesInner.copyWith(...)` or call `instanceOfInfraRulesRulesInner.copyWith.fieldName(value)` for a single field.
class _$InfraRulesRulesInnerCWProxyImpl
    implements _$InfraRulesRulesInnerCWProxy {
  const _$InfraRulesRulesInnerCWProxyImpl(this._value);

  final InfraRulesRulesInner _value;

  @override
  InfraRulesRulesInner id(InfraRulesRulesInnerIdEnum id) => call(id: id);

  @override
  InfraRulesRulesInner enabled(bool enabled) => call(enabled: enabled);

  @override
  InfraRulesRulesInner method(InfraRulesRulesInnerMethodEnum? method) =>
      call(method: method);

  @override
  InfraRulesRulesInner relation(InfraRulesRulesInnerRelationEnum? relation) =>
      call(relation: relation);

  @override
  InfraRulesRulesInner label(InfraRulesRulesInnerLabelEnum? label) =>
      call(label: label);

  @override
  InfraRulesRulesInner params(Map<String, Object>? params) =>
      call(params: params);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraRulesRulesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraRulesRulesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraRulesRulesInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? relation = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
  }) {
    return InfraRulesRulesInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as InfraRulesRulesInnerIdEnum,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      method: method == const $CopyWithPlaceholder()
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as InfraRulesRulesInnerMethodEnum?,
      relation: relation == const $CopyWithPlaceholder()
          ? _value.relation
          // ignore: cast_nullable_to_non_nullable
          : relation as InfraRulesRulesInnerRelationEnum?,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as InfraRulesRulesInnerLabelEnum?,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as Map<String, Object>?,
    );
  }
}

extension $InfraRulesRulesInnerCopyWith on InfraRulesRulesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraRulesRulesInner.copyWith(...)` or `instanceOfInfraRulesRulesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraRulesRulesInnerCWProxy get copyWith =>
      _$InfraRulesRulesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraRulesRulesInner _$InfraRulesRulesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('InfraRulesRulesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['id', 'enabled']);
  final val = InfraRulesRulesInner(
    id: $checkedConvert(
      'id',
      (v) => $enumDecode(_$InfraRulesRulesInnerIdEnumEnumMap, v),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    method: $checkedConvert(
      'method',
      (v) => $enumDecodeNullable(_$InfraRulesRulesInnerMethodEnumEnumMap, v),
    ),
    relation: $checkedConvert(
      'relation',
      (v) => $enumDecodeNullable(_$InfraRulesRulesInnerRelationEnumEnumMap, v),
    ),
    label: $checkedConvert(
      'label',
      (v) => $enumDecodeNullable(_$InfraRulesRulesInnerLabelEnumEnumMap, v),
    ),
    params: $checkedConvert(
      'params',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$InfraRulesRulesInnerToJson(
  InfraRulesRulesInner instance,
) => <String, dynamic>{
  'id': _$InfraRulesRulesInnerIdEnumEnumMap[instance.id]!,
  'enabled': instance.enabled,
  'method': ?_$InfraRulesRulesInnerMethodEnumEnumMap[instance.method],
  'relation': ?_$InfraRulesRulesInnerRelationEnumEnumMap[instance.relation],
  'label': ?_$InfraRulesRulesInnerLabelEnumEnumMap[instance.label],
  'params': ?instance.params,
};

const _$InfraRulesRulesInnerIdEnumEnumMap = {
  InfraRulesRulesInnerIdEnum.cableLanding: 'cable-landing',
  InfraRulesRulesInnerIdEnum.ixpFacility: 'ixp-facility',
  InfraRulesRulesInnerIdEnum.lineSubstation: 'line-substation',
  InfraRulesRulesInnerIdEnum.plantSubstation: 'plant-substation',
  InfraRulesRulesInnerIdEnum.substationCriticalLoad: 'substation-critical-load',
  InfraRulesRulesInnerIdEnum.airportPortServes: 'airport-port-serves',
};

const _$InfraRulesRulesInnerMethodEnumEnumMap = {
  InfraRulesRulesInnerMethodEnum.declared: 'declared',
  InfraRulesRulesInnerMethodEnum.topological: 'topological',
  InfraRulesRulesInnerMethodEnum.proximity: 'proximity',
  InfraRulesRulesInnerMethodEnum.inferred: 'inferred',
};

const _$InfraRulesRulesInnerRelationEnumEnumMap = {
  InfraRulesRulesInnerRelationEnum.suppliesPower: 'supplies_power',
  InfraRulesRulesInnerRelationEnum.transmitsPower: 'transmits_power',
  InfraRulesRulesInnerRelationEnum.landsAt: 'lands_at',
  InfraRulesRulesInnerRelationEnum.interconnects: 'interconnects',
  InfraRulesRulesInnerRelationEnum.hosts: 'hosts',
  InfraRulesRulesInnerRelationEnum.serves: 'serves',
};

const _$InfraRulesRulesInnerLabelEnumEnumMap = {
  InfraRulesRulesInnerLabelEnum.confirmed: 'confirmed',
  InfraRulesRulesInnerLabelEnum.likely: 'likely',
  InfraRulesRulesInnerLabelEnum.possible: 'possible',
  InfraRulesRulesInnerLabelEnum.speculative: 'speculative',
};
