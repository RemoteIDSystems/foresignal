// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisCWProxy {
  Hypothesis id(String id);

  Hypothesis setId(String setId);

  Hypothesis label(String label);

  Hypothesis statement(String statement);

  Hypothesis type(HypothesisTypeEnum type);

  Hypothesis prior(num prior);

  Hypothesis priorBasis(HypothesisPriorBasis priorBasis);

  Hypothesis probability(num probability);

  Hypothesis estimativeTerm(IcdTerm estimativeTerm);

  Hypothesis confidence(ConfidenceLevel confidence);

  Hypothesis confidenceBasis(HypothesisConfidenceBasis? confidenceBasis);

  Hypothesis ledgerOutcome(String? ledgerOutcome);

  Hypothesis status(HypothesisStatusEnum status);

  Hypothesis change(HypothesisChange? change);

  Hypothesis linkedFindingIds(List<String>? linkedFindingIds);

  Hypothesis coincidenceBaseline(
    HypothesisCoincidenceBaseline? coincidenceBaseline,
  );

  Hypothesis alternatives(List<String>? alternatives);

  Hypothesis probabilityCap(num? probabilityCap);

  Hypothesis capOverride(HypothesisCapOverride? capOverride);

  Hypothesis version(int? version);

  Hypothesis newsCoverage(CoverageStats? newsCoverage);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Hypothesis(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Hypothesis(...).copyWith(id: 12, name: "My name")
  /// ```
  Hypothesis call({
    String id,
    String setId,
    String label,
    String statement,
    HypothesisTypeEnum type,
    num prior,
    HypothesisPriorBasis priorBasis,
    num probability,
    IcdTerm estimativeTerm,
    ConfidenceLevel confidence,
    HypothesisConfidenceBasis? confidenceBasis,
    String? ledgerOutcome,
    HypothesisStatusEnum status,
    HypothesisChange? change,
    List<String>? linkedFindingIds,
    HypothesisCoincidenceBaseline? coincidenceBaseline,
    List<String>? alternatives,
    num? probabilityCap,
    HypothesisCapOverride? capOverride,
    int? version,
    CoverageStats? newsCoverage,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesis.copyWith(...)` or call `instanceOfHypothesis.copyWith.fieldName(value)` for a single field.
class _$HypothesisCWProxyImpl implements _$HypothesisCWProxy {
  const _$HypothesisCWProxyImpl(this._value);

  final Hypothesis _value;

  @override
  Hypothesis id(String id) => call(id: id);

  @override
  Hypothesis setId(String setId) => call(setId: setId);

  @override
  Hypothesis label(String label) => call(label: label);

  @override
  Hypothesis statement(String statement) => call(statement: statement);

  @override
  Hypothesis type(HypothesisTypeEnum type) => call(type: type);

  @override
  Hypothesis prior(num prior) => call(prior: prior);

  @override
  Hypothesis priorBasis(HypothesisPriorBasis priorBasis) =>
      call(priorBasis: priorBasis);

  @override
  Hypothesis probability(num probability) => call(probability: probability);

  @override
  Hypothesis estimativeTerm(IcdTerm estimativeTerm) =>
      call(estimativeTerm: estimativeTerm);

  @override
  Hypothesis confidence(ConfidenceLevel confidence) =>
      call(confidence: confidence);

  @override
  Hypothesis confidenceBasis(HypothesisConfidenceBasis? confidenceBasis) =>
      call(confidenceBasis: confidenceBasis);

  @override
  Hypothesis ledgerOutcome(String? ledgerOutcome) =>
      call(ledgerOutcome: ledgerOutcome);

  @override
  Hypothesis status(HypothesisStatusEnum status) => call(status: status);

  @override
  Hypothesis change(HypothesisChange? change) => call(change: change);

  @override
  Hypothesis linkedFindingIds(List<String>? linkedFindingIds) =>
      call(linkedFindingIds: linkedFindingIds);

  @override
  Hypothesis coincidenceBaseline(
    HypothesisCoincidenceBaseline? coincidenceBaseline,
  ) => call(coincidenceBaseline: coincidenceBaseline);

  @override
  Hypothesis alternatives(List<String>? alternatives) =>
      call(alternatives: alternatives);

  @override
  Hypothesis probabilityCap(num? probabilityCap) =>
      call(probabilityCap: probabilityCap);

  @override
  Hypothesis capOverride(HypothesisCapOverride? capOverride) =>
      call(capOverride: capOverride);

  @override
  Hypothesis version(int? version) => call(version: version);

  @override
  Hypothesis newsCoverage(CoverageStats? newsCoverage) =>
      call(newsCoverage: newsCoverage);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Hypothesis(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Hypothesis(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Hypothesis call({
    Object? id = const $CopyWithPlaceholder(),
    Object? setId = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? statement = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? prior = const $CopyWithPlaceholder(),
    Object? priorBasis = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? estimativeTerm = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? confidenceBasis = const $CopyWithPlaceholder(),
    Object? ledgerOutcome = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? change = const $CopyWithPlaceholder(),
    Object? linkedFindingIds = const $CopyWithPlaceholder(),
    Object? coincidenceBaseline = const $CopyWithPlaceholder(),
    Object? alternatives = const $CopyWithPlaceholder(),
    Object? probabilityCap = const $CopyWithPlaceholder(),
    Object? capOverride = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? newsCoverage = const $CopyWithPlaceholder(),
  }) {
    return Hypothesis(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String,
      statement: statement == const $CopyWithPlaceholder() || statement == null
          ? _value.statement
          // ignore: cast_nullable_to_non_nullable
          : statement as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as HypothesisTypeEnum,
      prior: prior == const $CopyWithPlaceholder() || prior == null
          ? _value.prior
          // ignore: cast_nullable_to_non_nullable
          : prior as num,
      priorBasis:
          priorBasis == const $CopyWithPlaceholder() || priorBasis == null
          ? _value.priorBasis
          // ignore: cast_nullable_to_non_nullable
          : priorBasis as HypothesisPriorBasis,
      probability:
          probability == const $CopyWithPlaceholder() || probability == null
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num,
      estimativeTerm:
          estimativeTerm == const $CopyWithPlaceholder() ||
              estimativeTerm == null
          ? _value.estimativeTerm
          // ignore: cast_nullable_to_non_nullable
          : estimativeTerm as IcdTerm,
      confidence:
          confidence == const $CopyWithPlaceholder() || confidence == null
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as ConfidenceLevel,
      confidenceBasis: confidenceBasis == const $CopyWithPlaceholder()
          ? _value.confidenceBasis
          // ignore: cast_nullable_to_non_nullable
          : confidenceBasis as HypothesisConfidenceBasis?,
      ledgerOutcome: ledgerOutcome == const $CopyWithPlaceholder()
          ? _value.ledgerOutcome
          // ignore: cast_nullable_to_non_nullable
          : ledgerOutcome as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as HypothesisStatusEnum,
      change: change == const $CopyWithPlaceholder()
          ? _value.change
          // ignore: cast_nullable_to_non_nullable
          : change as HypothesisChange?,
      linkedFindingIds: linkedFindingIds == const $CopyWithPlaceholder()
          ? _value.linkedFindingIds
          // ignore: cast_nullable_to_non_nullable
          : linkedFindingIds as List<String>?,
      coincidenceBaseline: coincidenceBaseline == const $CopyWithPlaceholder()
          ? _value.coincidenceBaseline
          // ignore: cast_nullable_to_non_nullable
          : coincidenceBaseline as HypothesisCoincidenceBaseline?,
      alternatives: alternatives == const $CopyWithPlaceholder()
          ? _value.alternatives
          // ignore: cast_nullable_to_non_nullable
          : alternatives as List<String>?,
      probabilityCap: probabilityCap == const $CopyWithPlaceholder()
          ? _value.probabilityCap
          // ignore: cast_nullable_to_non_nullable
          : probabilityCap as num?,
      capOverride: capOverride == const $CopyWithPlaceholder()
          ? _value.capOverride
          // ignore: cast_nullable_to_non_nullable
          : capOverride as HypothesisCapOverride?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
      newsCoverage: newsCoverage == const $CopyWithPlaceholder()
          ? _value.newsCoverage
          // ignore: cast_nullable_to_non_nullable
          : newsCoverage as CoverageStats?,
    );
  }
}

extension $HypothesisCopyWith on Hypothesis {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesis.copyWith(...)` or `instanceOfHypothesis.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisCWProxy get copyWith => _$HypothesisCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Hypothesis _$HypothesisFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Hypothesis', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'setId',
      'label',
      'statement',
      'type',
      'prior',
      'priorBasis',
      'probability',
      'estimativeTerm',
      'confidence',
      'status',
    ],
  );
  final val = Hypothesis(
    id: $checkedConvert('id', (v) => v as String),
    setId: $checkedConvert('setId', (v) => v as String),
    label: $checkedConvert('label', (v) => v as String),
    statement: $checkedConvert('statement', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$HypothesisTypeEnumEnumMap, v),
    ),
    prior: $checkedConvert('prior', (v) => v as num),
    priorBasis: $checkedConvert(
      'priorBasis',
      (v) => HypothesisPriorBasis.fromJson(v as Map<String, dynamic>),
    ),
    probability: $checkedConvert('probability', (v) => v as num),
    estimativeTerm: $checkedConvert(
      'estimativeTerm',
      (v) => $enumDecode(_$IcdTermEnumMap, v),
    ),
    confidence: $checkedConvert(
      'confidence',
      (v) => $enumDecode(_$ConfidenceLevelEnumMap, v),
    ),
    confidenceBasis: $checkedConvert(
      'confidenceBasis',
      (v) => v == null
          ? null
          : HypothesisConfidenceBasis.fromJson(v as Map<String, dynamic>),
    ),
    ledgerOutcome: $checkedConvert('ledgerOutcome', (v) => v as String?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$HypothesisStatusEnumEnumMap, v),
    ),
    change: $checkedConvert(
      'change',
      (v) => v == null
          ? null
          : HypothesisChange.fromJson(v as Map<String, dynamic>),
    ),
    linkedFindingIds: $checkedConvert(
      'linkedFindingIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    coincidenceBaseline: $checkedConvert(
      'coincidenceBaseline',
      (v) => v == null
          ? null
          : HypothesisCoincidenceBaseline.fromJson(v as Map<String, dynamic>),
    ),
    alternatives: $checkedConvert(
      'alternatives',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    probabilityCap: $checkedConvert('probabilityCap', (v) => v as num?),
    capOverride: $checkedConvert(
      'capOverride',
      (v) => v == null
          ? null
          : HypothesisCapOverride.fromJson(v as Map<String, dynamic>),
    ),
    version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
    newsCoverage: $checkedConvert(
      'newsCoverage',
      (v) =>
          v == null ? null : CoverageStats.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$HypothesisToJson(Hypothesis instance) =>
    <String, dynamic>{
      'id': instance.id,
      'setId': instance.setId,
      'label': instance.label,
      'statement': instance.statement,
      'type': _$HypothesisTypeEnumEnumMap[instance.type]!,
      'prior': instance.prior,
      'priorBasis': instance.priorBasis.toJson(),
      'probability': instance.probability,
      'estimativeTerm': _$IcdTermEnumMap[instance.estimativeTerm]!,
      'confidence': _$ConfidenceLevelEnumMap[instance.confidence]!,
      'confidenceBasis': ?instance.confidenceBasis?.toJson(),
      'ledgerOutcome': ?instance.ledgerOutcome,
      'status': _$HypothesisStatusEnumEnumMap[instance.status]!,
      'change': ?instance.change?.toJson(),
      'linkedFindingIds': ?instance.linkedFindingIds,
      'coincidenceBaseline': ?instance.coincidenceBaseline?.toJson(),
      'alternatives': ?instance.alternatives,
      'probabilityCap': ?instance.probabilityCap,
      'capOverride': ?instance.capOverride?.toJson(),
      'version': ?instance.version,
      'newsCoverage': ?instance.newsCoverage?.toJson(),
    };

const _$HypothesisTypeEnumEnumMap = {
  HypothesisTypeEnum.outcome: 'outcome',
  HypothesisTypeEnum.explanation: 'explanation',
  HypothesisTypeEnum.speculativeLink: 'speculative-link',
  HypothesisTypeEnum.other: 'other',
};

const _$IcdTermEnumMap = {
  IcdTerm.almostNoChance: 'almost no chance',
  IcdTerm.veryUnlikely: 'very unlikely',
  IcdTerm.unlikely: 'unlikely',
  IcdTerm.roughlyEvenChance: 'roughly even chance',
  IcdTerm.likely: 'likely',
  IcdTerm.veryLikely: 'very likely',
  IcdTerm.almostCertain: 'almost certain',
};

const _$ConfidenceLevelEnumMap = {
  ConfidenceLevel.low: 'low',
  ConfidenceLevel.moderate: 'moderate',
  ConfidenceLevel.high: 'high',
};

const _$HypothesisStatusEnumEnumMap = {
  HypothesisStatusEnum.open: 'open',
  HypothesisStatusEnum.confirmed: 'confirmed',
  HypothesisStatusEnum.refuted: 'refuted',
  HypothesisStatusEnum.expired: 'expired',
};
