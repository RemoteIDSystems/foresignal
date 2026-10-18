// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_set.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisSetCWProxy {
  HypothesisSet id(String id);

  HypothesisSet question(String question);

  HypothesisSet description(String? description);

  HypothesisSet kind(HypothesisSetKindEnum kind);

  HypothesisSet cadence(HypothesisSetCadenceEnum cadence);

  HypothesisSet horizon(DateTime? horizon);

  HypothesisSet status(HypothesisSetStatusEnum status);

  HypothesisSet situationId(String? situationId);

  HypothesisSet questionId(String? questionId);

  HypothesisSet regionCodes(List<String>? regionCodes);

  HypothesisSet themes(List<String>? themes);

  HypothesisSet origin(HypothesisSetOriginEnum origin);

  HypothesisSet confidence(ConfidenceLevel confidence);

  HypothesisSet lastReviewedAt(DateTime? lastReviewedAt);

  HypothesisSet nextReviewAt(DateTime? nextReviewAt);

  HypothesisSet lastRevisionId(String? lastRevisionId);

  HypothesisSet lastRevisionAt(DateTime? lastRevisionAt);

  HypothesisSet private(bool private);

  HypothesisSet version(int version);

  HypothesisSet hypotheses(List<Hypothesis> hypotheses);

  HypothesisSet indicators(List<Indicator>? indicators);

  HypothesisSet assumptions(List<KeyAssumption>? assumptions);

  HypothesisSet linkedQuestion(LedgerQuestionBrief? linkedQuestion);

  HypothesisSet recordRef(String? recordRef);

  HypothesisSet createdAt(DateTime createdAt);

  HypothesisSet updatedAt(DateTime updatedAt);

  HypothesisSet reviewFlagged(bool? reviewFlagged);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSet(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSet(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisSet call({
    String id,
    String question,
    String? description,
    HypothesisSetKindEnum kind,
    HypothesisSetCadenceEnum cadence,
    DateTime? horizon,
    HypothesisSetStatusEnum status,
    String? situationId,
    String? questionId,
    List<String>? regionCodes,
    List<String>? themes,
    HypothesisSetOriginEnum origin,
    ConfidenceLevel confidence,
    DateTime? lastReviewedAt,
    DateTime? nextReviewAt,
    String? lastRevisionId,
    DateTime? lastRevisionAt,
    bool private,
    int version,
    List<Hypothesis> hypotheses,
    List<Indicator>? indicators,
    List<KeyAssumption>? assumptions,
    LedgerQuestionBrief? linkedQuestion,
    String? recordRef,
    DateTime createdAt,
    DateTime updatedAt,
    bool? reviewFlagged,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisSet.copyWith(...)` or call `instanceOfHypothesisSet.copyWith.fieldName(value)` for a single field.
class _$HypothesisSetCWProxyImpl implements _$HypothesisSetCWProxy {
  const _$HypothesisSetCWProxyImpl(this._value);

  final HypothesisSet _value;

  @override
  HypothesisSet id(String id) => call(id: id);

  @override
  HypothesisSet question(String question) => call(question: question);

  @override
  HypothesisSet description(String? description) =>
      call(description: description);

  @override
  HypothesisSet kind(HypothesisSetKindEnum kind) => call(kind: kind);

  @override
  HypothesisSet cadence(HypothesisSetCadenceEnum cadence) =>
      call(cadence: cadence);

  @override
  HypothesisSet horizon(DateTime? horizon) => call(horizon: horizon);

  @override
  HypothesisSet status(HypothesisSetStatusEnum status) => call(status: status);

  @override
  HypothesisSet situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  HypothesisSet questionId(String? questionId) => call(questionId: questionId);

  @override
  HypothesisSet regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  HypothesisSet themes(List<String>? themes) => call(themes: themes);

  @override
  HypothesisSet origin(HypothesisSetOriginEnum origin) => call(origin: origin);

  @override
  HypothesisSet confidence(ConfidenceLevel confidence) =>
      call(confidence: confidence);

  @override
  HypothesisSet lastReviewedAt(DateTime? lastReviewedAt) =>
      call(lastReviewedAt: lastReviewedAt);

  @override
  HypothesisSet nextReviewAt(DateTime? nextReviewAt) =>
      call(nextReviewAt: nextReviewAt);

  @override
  HypothesisSet lastRevisionId(String? lastRevisionId) =>
      call(lastRevisionId: lastRevisionId);

  @override
  HypothesisSet lastRevisionAt(DateTime? lastRevisionAt) =>
      call(lastRevisionAt: lastRevisionAt);

  @override
  HypothesisSet private(bool private) => call(private: private);

  @override
  HypothesisSet version(int version) => call(version: version);

  @override
  HypothesisSet hypotheses(List<Hypothesis> hypotheses) =>
      call(hypotheses: hypotheses);

  @override
  HypothesisSet indicators(List<Indicator>? indicators) =>
      call(indicators: indicators);

  @override
  HypothesisSet assumptions(List<KeyAssumption>? assumptions) =>
      call(assumptions: assumptions);

  @override
  HypothesisSet linkedQuestion(LedgerQuestionBrief? linkedQuestion) =>
      call(linkedQuestion: linkedQuestion);

  @override
  HypothesisSet recordRef(String? recordRef) => call(recordRef: recordRef);

  @override
  HypothesisSet createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  HypothesisSet updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  @override
  HypothesisSet reviewFlagged(bool? reviewFlagged) =>
      call(reviewFlagged: reviewFlagged);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSet(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSet(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisSet call({
    Object? id = const $CopyWithPlaceholder(),
    Object? question = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? cadence = const $CopyWithPlaceholder(),
    Object? horizon = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? questionId = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? themes = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? lastReviewedAt = const $CopyWithPlaceholder(),
    Object? nextReviewAt = const $CopyWithPlaceholder(),
    Object? lastRevisionId = const $CopyWithPlaceholder(),
    Object? lastRevisionAt = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? hypotheses = const $CopyWithPlaceholder(),
    Object? indicators = const $CopyWithPlaceholder(),
    Object? assumptions = const $CopyWithPlaceholder(),
    Object? linkedQuestion = const $CopyWithPlaceholder(),
    Object? recordRef = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? reviewFlagged = const $CopyWithPlaceholder(),
  }) {
    return HypothesisSet(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      question: question == const $CopyWithPlaceholder() || question == null
          ? _value.question
          // ignore: cast_nullable_to_non_nullable
          : question as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as HypothesisSetKindEnum,
      cadence: cadence == const $CopyWithPlaceholder() || cadence == null
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as HypothesisSetCadenceEnum,
      horizon: horizon == const $CopyWithPlaceholder()
          ? _value.horizon
          // ignore: cast_nullable_to_non_nullable
          : horizon as DateTime?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as HypothesisSetStatusEnum,
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      questionId: questionId == const $CopyWithPlaceholder()
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String?,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      themes: themes == const $CopyWithPlaceholder()
          ? _value.themes
          // ignore: cast_nullable_to_non_nullable
          : themes as List<String>?,
      origin: origin == const $CopyWithPlaceholder() || origin == null
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as HypothesisSetOriginEnum,
      confidence:
          confidence == const $CopyWithPlaceholder() || confidence == null
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as ConfidenceLevel,
      lastReviewedAt: lastReviewedAt == const $CopyWithPlaceholder()
          ? _value.lastReviewedAt
          // ignore: cast_nullable_to_non_nullable
          : lastReviewedAt as DateTime?,
      nextReviewAt: nextReviewAt == const $CopyWithPlaceholder()
          ? _value.nextReviewAt
          // ignore: cast_nullable_to_non_nullable
          : nextReviewAt as DateTime?,
      lastRevisionId: lastRevisionId == const $CopyWithPlaceholder()
          ? _value.lastRevisionId
          // ignore: cast_nullable_to_non_nullable
          : lastRevisionId as String?,
      lastRevisionAt: lastRevisionAt == const $CopyWithPlaceholder()
          ? _value.lastRevisionAt
          // ignore: cast_nullable_to_non_nullable
          : lastRevisionAt as DateTime?,
      private: private == const $CopyWithPlaceholder() || private == null
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      hypotheses:
          hypotheses == const $CopyWithPlaceholder() || hypotheses == null
          ? _value.hypotheses
          // ignore: cast_nullable_to_non_nullable
          : hypotheses as List<Hypothesis>,
      indicators: indicators == const $CopyWithPlaceholder()
          ? _value.indicators
          // ignore: cast_nullable_to_non_nullable
          : indicators as List<Indicator>?,
      assumptions: assumptions == const $CopyWithPlaceholder()
          ? _value.assumptions
          // ignore: cast_nullable_to_non_nullable
          : assumptions as List<KeyAssumption>?,
      linkedQuestion: linkedQuestion == const $CopyWithPlaceholder()
          ? _value.linkedQuestion
          // ignore: cast_nullable_to_non_nullable
          : linkedQuestion as LedgerQuestionBrief?,
      recordRef: recordRef == const $CopyWithPlaceholder()
          ? _value.recordRef
          // ignore: cast_nullable_to_non_nullable
          : recordRef as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
      reviewFlagged: reviewFlagged == const $CopyWithPlaceholder()
          ? _value.reviewFlagged
          // ignore: cast_nullable_to_non_nullable
          : reviewFlagged as bool?,
    );
  }
}

extension $HypothesisSetCopyWith on HypothesisSet {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisSet.copyWith(...)` or `instanceOfHypothesisSet.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisSetCWProxy get copyWith => _$HypothesisSetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisSet _$HypothesisSetFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisSet', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'question',
      'kind',
      'cadence',
      'status',
      'origin',
      'confidence',
      'private',
      'version',
      'hypotheses',
      'createdAt',
      'updatedAt',
    ],
  );
  final val = HypothesisSet(
    id: $checkedConvert('id', (v) => v as String),
    question: $checkedConvert('question', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String?),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$HypothesisSetKindEnumEnumMap, v),
    ),
    cadence: $checkedConvert(
      'cadence',
      (v) => $enumDecode(_$HypothesisSetCadenceEnumEnumMap, v),
    ),
    horizon: $checkedConvert(
      'horizon',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$HypothesisSetStatusEnumEnumMap, v),
    ),
    situationId: $checkedConvert('situationId', (v) => v as String?),
    questionId: $checkedConvert('questionId', (v) => v as String?),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    themes: $checkedConvert(
      'themes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    origin: $checkedConvert(
      'origin',
      (v) => $enumDecode(_$HypothesisSetOriginEnumEnumMap, v),
    ),
    confidence: $checkedConvert(
      'confidence',
      (v) => $enumDecode(_$ConfidenceLevelEnumMap, v),
    ),
    lastReviewedAt: $checkedConvert(
      'lastReviewedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    nextReviewAt: $checkedConvert(
      'nextReviewAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastRevisionId: $checkedConvert('lastRevisionId', (v) => v as String?),
    lastRevisionAt: $checkedConvert(
      'lastRevisionAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    private: $checkedConvert('private', (v) => v as bool),
    version: $checkedConvert('version', (v) => (v as num).toInt()),
    hypotheses: $checkedConvert(
      'hypotheses',
      (v) => (v as List<dynamic>)
          .map((e) => Hypothesis.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    indicators: $checkedConvert(
      'indicators',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Indicator.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    assumptions: $checkedConvert(
      'assumptions',
      (v) => (v as List<dynamic>?)
          ?.map((e) => KeyAssumption.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    linkedQuestion: $checkedConvert(
      'linkedQuestion',
      (v) => v == null
          ? null
          : LedgerQuestionBrief.fromJson(v as Map<String, dynamic>),
    ),
    recordRef: $checkedConvert('recordRef', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
    reviewFlagged: $checkedConvert('reviewFlagged', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$HypothesisSetToJson(HypothesisSet instance) =>
    <String, dynamic>{
      'id': instance.id,
      'question': instance.question,
      'description': ?instance.description,
      'kind': _$HypothesisSetKindEnumEnumMap[instance.kind]!,
      'cadence': _$HypothesisSetCadenceEnumEnumMap[instance.cadence]!,
      'horizon': ?instance.horizon?.toIso8601String(),
      'status': _$HypothesisSetStatusEnumEnumMap[instance.status]!,
      'situationId': ?instance.situationId,
      'questionId': ?instance.questionId,
      'regionCodes': ?instance.regionCodes,
      'themes': ?instance.themes,
      'origin': _$HypothesisSetOriginEnumEnumMap[instance.origin]!,
      'confidence': _$ConfidenceLevelEnumMap[instance.confidence]!,
      'lastReviewedAt': ?instance.lastReviewedAt?.toIso8601String(),
      'nextReviewAt': ?instance.nextReviewAt?.toIso8601String(),
      'lastRevisionId': ?instance.lastRevisionId,
      'lastRevisionAt': ?instance.lastRevisionAt?.toIso8601String(),
      'private': instance.private,
      'version': instance.version,
      'hypotheses': instance.hypotheses.map((e) => e.toJson()).toList(),
      'indicators': ?instance.indicators?.map((e) => e.toJson()).toList(),
      'assumptions': ?instance.assumptions?.map((e) => e.toJson()).toList(),
      'linkedQuestion': ?instance.linkedQuestion?.toJson(),
      'recordRef': ?instance.recordRef,
      'createdAt': instance.createdAt.toIso8601String(),
      'updatedAt': instance.updatedAt.toIso8601String(),
      'reviewFlagged': ?instance.reviewFlagged,
    };

const _$HypothesisSetKindEnumEnumMap = {
  HypothesisSetKindEnum.exclusive: 'exclusive',
  HypothesisSetKindEnum.independent: 'independent',
};

const _$HypothesisSetCadenceEnumEnumMap = {
  HypothesisSetCadenceEnum.daily: 'daily',
  HypothesisSetCadenceEnum.weekly: 'weekly',
  HypothesisSetCadenceEnum.monthly: 'monthly',
};

const _$HypothesisSetStatusEnumEnumMap = {
  HypothesisSetStatusEnum.active: 'active',
  HypothesisSetStatusEnum.resolved: 'resolved',
  HypothesisSetStatusEnum.archived: 'archived',
};

const _$HypothesisSetOriginEnumEnumMap = {
  HypothesisSetOriginEnum.operator_: 'operator',
  HypothesisSetOriginEnum.agent: 'agent',
  HypothesisSetOriginEnum.analyst: 'analyst',
};

const _$ConfidenceLevelEnumMap = {
  ConfidenceLevel.low: 'low',
  ConfidenceLevel.moderate: 'moderate',
  ConfidenceLevel.high: 'high',
};
