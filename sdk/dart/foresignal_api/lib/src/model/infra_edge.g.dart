// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_edge.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraEdgeCWProxy {
  InfraEdge id(int id);

  InfraEdge fromNode(int fromNode);

  InfraEdge toNode(int toNode);

  InfraEdge relation(InfraEdgeRelationEnum relation);

  InfraEdge method(InfraEdgeMethodEnum method);

  InfraEdge label(InfraEdgeLabelEnum label);

  InfraEdge score(num score);

  InfraEdge ruleId(String ruleId);

  InfraEdge ruleVersion(String ruleVersion);

  InfraEdge params(Map<String, Object>? params);

  InfraEdge evidence(InfraEdgeEvidence? evidence);

  InfraEdge licence(String licence);

  InfraEdge builtAt(DateTime? builtAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraEdge(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraEdge(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraEdge call({
    int id,
    int fromNode,
    int toNode,
    InfraEdgeRelationEnum relation,
    InfraEdgeMethodEnum method,
    InfraEdgeLabelEnum label,
    num score,
    String ruleId,
    String ruleVersion,
    Map<String, Object>? params,
    InfraEdgeEvidence? evidence,
    String licence,
    DateTime? builtAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraEdge.copyWith(...)` or call `instanceOfInfraEdge.copyWith.fieldName(value)` for a single field.
class _$InfraEdgeCWProxyImpl implements _$InfraEdgeCWProxy {
  const _$InfraEdgeCWProxyImpl(this._value);

  final InfraEdge _value;

  @override
  InfraEdge id(int id) => call(id: id);

  @override
  InfraEdge fromNode(int fromNode) => call(fromNode: fromNode);

  @override
  InfraEdge toNode(int toNode) => call(toNode: toNode);

  @override
  InfraEdge relation(InfraEdgeRelationEnum relation) =>
      call(relation: relation);

  @override
  InfraEdge method(InfraEdgeMethodEnum method) => call(method: method);

  @override
  InfraEdge label(InfraEdgeLabelEnum label) => call(label: label);

  @override
  InfraEdge score(num score) => call(score: score);

  @override
  InfraEdge ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  InfraEdge ruleVersion(String ruleVersion) => call(ruleVersion: ruleVersion);

  @override
  InfraEdge params(Map<String, Object>? params) => call(params: params);

  @override
  InfraEdge evidence(InfraEdgeEvidence? evidence) => call(evidence: evidence);

  @override
  InfraEdge licence(String licence) => call(licence: licence);

  @override
  InfraEdge builtAt(DateTime? builtAt) => call(builtAt: builtAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraEdge(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraEdge(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraEdge call({
    Object? id = const $CopyWithPlaceholder(),
    Object? fromNode = const $CopyWithPlaceholder(),
    Object? toNode = const $CopyWithPlaceholder(),
    Object? relation = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? score = const $CopyWithPlaceholder(),
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? ruleVersion = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? builtAt = const $CopyWithPlaceholder(),
  }) {
    return InfraEdge(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      fromNode: fromNode == const $CopyWithPlaceholder() || fromNode == null
          ? _value.fromNode
          // ignore: cast_nullable_to_non_nullable
          : fromNode as int,
      toNode: toNode == const $CopyWithPlaceholder() || toNode == null
          ? _value.toNode
          // ignore: cast_nullable_to_non_nullable
          : toNode as int,
      relation: relation == const $CopyWithPlaceholder() || relation == null
          ? _value.relation
          // ignore: cast_nullable_to_non_nullable
          : relation as InfraEdgeRelationEnum,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as InfraEdgeMethodEnum,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as InfraEdgeLabelEnum,
      score: score == const $CopyWithPlaceholder() || score == null
          ? _value.score
          // ignore: cast_nullable_to_non_nullable
          : score as num,
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      ruleVersion:
          ruleVersion == const $CopyWithPlaceholder() || ruleVersion == null
          ? _value.ruleVersion
          // ignore: cast_nullable_to_non_nullable
          : ruleVersion as String,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as Map<String, Object>?,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as InfraEdgeEvidence?,
      licence: licence == const $CopyWithPlaceholder() || licence == null
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String,
      builtAt: builtAt == const $CopyWithPlaceholder()
          ? _value.builtAt
          // ignore: cast_nullable_to_non_nullable
          : builtAt as DateTime?,
    );
  }
}

extension $InfraEdgeCopyWith on InfraEdge {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraEdge.copyWith(...)` or `instanceOfInfraEdge.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraEdgeCWProxy get copyWith => _$InfraEdgeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraEdge _$InfraEdgeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InfraEdge', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'id',
          'fromNode',
          'toNode',
          'relation',
          'method',
          'label',
          'score',
          'ruleId',
          'ruleVersion',
          'licence',
        ],
      );
      final val = InfraEdge(
        id: $checkedConvert('id', (v) => (v as num).toInt()),
        fromNode: $checkedConvert('fromNode', (v) => (v as num).toInt()),
        toNode: $checkedConvert('toNode', (v) => (v as num).toInt()),
        relation: $checkedConvert(
          'relation',
          (v) => $enumDecode(_$InfraEdgeRelationEnumEnumMap, v),
        ),
        method: $checkedConvert(
          'method',
          (v) => $enumDecode(_$InfraEdgeMethodEnumEnumMap, v),
        ),
        label: $checkedConvert(
          'label',
          (v) => $enumDecode(_$InfraEdgeLabelEnumEnumMap, v),
        ),
        score: $checkedConvert('score', (v) => v as num),
        ruleId: $checkedConvert('ruleId', (v) => v as String),
        ruleVersion: $checkedConvert('ruleVersion', (v) => v as String),
        params: $checkedConvert(
          'params',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        evidence: $checkedConvert(
          'evidence',
          (v) => v == null
              ? null
              : InfraEdgeEvidence.fromJson(v as Map<String, dynamic>),
        ),
        licence: $checkedConvert('licence', (v) => v as String),
        builtAt: $checkedConvert(
          'builtAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$InfraEdgeToJson(InfraEdge instance) => <String, dynamic>{
  'id': instance.id,
  'fromNode': instance.fromNode,
  'toNode': instance.toNode,
  'relation': _$InfraEdgeRelationEnumEnumMap[instance.relation]!,
  'method': _$InfraEdgeMethodEnumEnumMap[instance.method]!,
  'label': _$InfraEdgeLabelEnumEnumMap[instance.label]!,
  'score': instance.score,
  'ruleId': instance.ruleId,
  'ruleVersion': instance.ruleVersion,
  'params': ?instance.params,
  'evidence': ?instance.evidence?.toJson(),
  'licence': instance.licence,
  'builtAt': ?instance.builtAt?.toIso8601String(),
};

const _$InfraEdgeRelationEnumEnumMap = {
  InfraEdgeRelationEnum.suppliesPower: 'supplies_power',
  InfraEdgeRelationEnum.transmitsPower: 'transmits_power',
  InfraEdgeRelationEnum.landsAt: 'lands_at',
  InfraEdgeRelationEnum.interconnects: 'interconnects',
  InfraEdgeRelationEnum.hosts: 'hosts',
  InfraEdgeRelationEnum.serves: 'serves',
};

const _$InfraEdgeMethodEnumEnumMap = {
  InfraEdgeMethodEnum.declared: 'declared',
  InfraEdgeMethodEnum.topological: 'topological',
  InfraEdgeMethodEnum.proximity: 'proximity',
  InfraEdgeMethodEnum.inferred: 'inferred',
};

const _$InfraEdgeLabelEnumEnumMap = {
  InfraEdgeLabelEnum.confirmed: 'confirmed',
  InfraEdgeLabelEnum.likely: 'likely',
  InfraEdgeLabelEnum.possible: 'possible',
  InfraEdgeLabelEnum.speculative: 'speculative',
};
