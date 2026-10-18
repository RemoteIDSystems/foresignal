// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_impact_result_dependents_inner_path_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraImpactResultDependentsInnerPathInnerCWProxy {
  InfraImpactResultDependentsInnerPathInner edgeId(int edgeId);

  InfraImpactResultDependentsInnerPathInner relation(
    InfraImpactResultDependentsInnerPathInnerRelationEnum relation,
  );

  InfraImpactResultDependentsInnerPathInner method(
    InfraImpactResultDependentsInnerPathInnerMethodEnum method,
  );

  InfraImpactResultDependentsInnerPathInner label(
    InfraImpactResultDependentsInnerPathInnerLabelEnum label,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraImpactResultDependentsInnerPathInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraImpactResultDependentsInnerPathInner(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraImpactResultDependentsInnerPathInner call({
    int edgeId,
    InfraImpactResultDependentsInnerPathInnerRelationEnum relation,
    InfraImpactResultDependentsInnerPathInnerMethodEnum method,
    InfraImpactResultDependentsInnerPathInnerLabelEnum label,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraImpactResultDependentsInnerPathInner.copyWith(...)` or call `instanceOfInfraImpactResultDependentsInnerPathInner.copyWith.fieldName(value)` for a single field.
class _$InfraImpactResultDependentsInnerPathInnerCWProxyImpl
    implements _$InfraImpactResultDependentsInnerPathInnerCWProxy {
  const _$InfraImpactResultDependentsInnerPathInnerCWProxyImpl(this._value);

  final InfraImpactResultDependentsInnerPathInner _value;

  @override
  InfraImpactResultDependentsInnerPathInner edgeId(int edgeId) =>
      call(edgeId: edgeId);

  @override
  InfraImpactResultDependentsInnerPathInner relation(
    InfraImpactResultDependentsInnerPathInnerRelationEnum relation,
  ) => call(relation: relation);

  @override
  InfraImpactResultDependentsInnerPathInner method(
    InfraImpactResultDependentsInnerPathInnerMethodEnum method,
  ) => call(method: method);

  @override
  InfraImpactResultDependentsInnerPathInner label(
    InfraImpactResultDependentsInnerPathInnerLabelEnum label,
  ) => call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraImpactResultDependentsInnerPathInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraImpactResultDependentsInnerPathInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraImpactResultDependentsInnerPathInner call({
    Object? edgeId = const $CopyWithPlaceholder(),
    Object? relation = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
  }) {
    return InfraImpactResultDependentsInnerPathInner(
      edgeId: edgeId == const $CopyWithPlaceholder() || edgeId == null
          ? _value.edgeId
          // ignore: cast_nullable_to_non_nullable
          : edgeId as int,
      relation: relation == const $CopyWithPlaceholder() || relation == null
          ? _value.relation
          // ignore: cast_nullable_to_non_nullable
          : relation as InfraImpactResultDependentsInnerPathInnerRelationEnum,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as InfraImpactResultDependentsInnerPathInnerMethodEnum,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as InfraImpactResultDependentsInnerPathInnerLabelEnum,
    );
  }
}

extension $InfraImpactResultDependentsInnerPathInnerCopyWith
    on InfraImpactResultDependentsInnerPathInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraImpactResultDependentsInnerPathInner.copyWith(...)` or `instanceOfInfraImpactResultDependentsInnerPathInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraImpactResultDependentsInnerPathInnerCWProxy get copyWith =>
      _$InfraImpactResultDependentsInnerPathInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraImpactResultDependentsInnerPathInner
_$InfraImpactResultDependentsInnerPathInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('InfraImpactResultDependentsInnerPathInner', json, (
  $checkedConvert,
) {
  $checkKeys(
    json,
    requiredKeys: const ['edgeId', 'relation', 'method', 'label'],
  );
  final val = InfraImpactResultDependentsInnerPathInner(
    edgeId: $checkedConvert('edgeId', (v) => (v as num).toInt()),
    relation: $checkedConvert(
      'relation',
      (v) => $enumDecode(
        _$InfraImpactResultDependentsInnerPathInnerRelationEnumEnumMap,
        v,
      ),
    ),
    method: $checkedConvert(
      'method',
      (v) => $enumDecode(
        _$InfraImpactResultDependentsInnerPathInnerMethodEnumEnumMap,
        v,
      ),
    ),
    label: $checkedConvert(
      'label',
      (v) => $enumDecode(
        _$InfraImpactResultDependentsInnerPathInnerLabelEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$InfraImpactResultDependentsInnerPathInnerToJson(
  InfraImpactResultDependentsInnerPathInner instance,
) => <String, dynamic>{
  'edgeId': instance.edgeId,
  'relation':
      _$InfraImpactResultDependentsInnerPathInnerRelationEnumEnumMap[instance
          .relation]!,
  'method':
      _$InfraImpactResultDependentsInnerPathInnerMethodEnumEnumMap[instance
          .method]!,
  'label':
      _$InfraImpactResultDependentsInnerPathInnerLabelEnumEnumMap[instance
          .label]!,
};

const _$InfraImpactResultDependentsInnerPathInnerRelationEnumEnumMap = {
  InfraImpactResultDependentsInnerPathInnerRelationEnum.suppliesPower:
      'supplies_power',
  InfraImpactResultDependentsInnerPathInnerRelationEnum.transmitsPower:
      'transmits_power',
  InfraImpactResultDependentsInnerPathInnerRelationEnum.landsAt: 'lands_at',
  InfraImpactResultDependentsInnerPathInnerRelationEnum.interconnects:
      'interconnects',
  InfraImpactResultDependentsInnerPathInnerRelationEnum.hosts: 'hosts',
  InfraImpactResultDependentsInnerPathInnerRelationEnum.serves: 'serves',
};

const _$InfraImpactResultDependentsInnerPathInnerMethodEnumEnumMap = {
  InfraImpactResultDependentsInnerPathInnerMethodEnum.declared: 'declared',
  InfraImpactResultDependentsInnerPathInnerMethodEnum.topological:
      'topological',
  InfraImpactResultDependentsInnerPathInnerMethodEnum.proximity: 'proximity',
  InfraImpactResultDependentsInnerPathInnerMethodEnum.inferred: 'inferred',
};

const _$InfraImpactResultDependentsInnerPathInnerLabelEnumEnumMap = {
  InfraImpactResultDependentsInnerPathInnerLabelEnum.confirmed: 'confirmed',
  InfraImpactResultDependentsInnerPathInnerLabelEnum.likely: 'likely',
  InfraImpactResultDependentsInnerPathInnerLabelEnum.possible: 'possible',
  InfraImpactResultDependentsInnerPathInnerLabelEnum.speculative: 'speculative',
};
