// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_set_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisSetInputCWProxy {
  HypothesisSetInput question(String question);

  HypothesisSetInput description(String? description);

  HypothesisSetInput kind(HypothesisSetInputKindEnum kind);

  HypothesisSetInput cadence(HypothesisSetInputCadenceEnum cadence);

  HypothesisSetInput horizon(DateTime? horizon);

  HypothesisSetInput situationId(String? situationId);

  HypothesisSetInput questionId(String? questionId);

  HypothesisSetInput regionCodes(List<String>? regionCodes);

  HypothesisSetInput themes(List<String>? themes);

  HypothesisSetInput private(bool? private);

  HypothesisSetInput hypotheses(List<HypothesisInput> hypotheses);

  HypothesisSetInput indicators(List<IndicatorInput>? indicators);

  HypothesisSetInput assumptions(List<KeyAssumptionInput>? assumptions);

  HypothesisSetInput ledgerQuestion(
    HypothesisSetInputLedgerQuestion? ledgerQuestion,
  );

  HypothesisSetInput proposalId(String? proposalId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetInput(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisSetInput call({
    String question,
    String? description,
    HypothesisSetInputKindEnum kind,
    HypothesisSetInputCadenceEnum cadence,
    DateTime? horizon,
    String? situationId,
    String? questionId,
    List<String>? regionCodes,
    List<String>? themes,
    bool? private,
    List<HypothesisInput> hypotheses,
    List<IndicatorInput>? indicators,
    List<KeyAssumptionInput>? assumptions,
    HypothesisSetInputLedgerQuestion? ledgerQuestion,
    String? proposalId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisSetInput.copyWith(...)` or call `instanceOfHypothesisSetInput.copyWith.fieldName(value)` for a single field.
class _$HypothesisSetInputCWProxyImpl implements _$HypothesisSetInputCWProxy {
  const _$HypothesisSetInputCWProxyImpl(this._value);

  final HypothesisSetInput _value;

  @override
  HypothesisSetInput question(String question) => call(question: question);

  @override
  HypothesisSetInput description(String? description) =>
      call(description: description);

  @override
  HypothesisSetInput kind(HypothesisSetInputKindEnum kind) => call(kind: kind);

  @override
  HypothesisSetInput cadence(HypothesisSetInputCadenceEnum cadence) =>
      call(cadence: cadence);

  @override
  HypothesisSetInput horizon(DateTime? horizon) => call(horizon: horizon);

  @override
  HypothesisSetInput situationId(String? situationId) =>
      call(situationId: situationId);

  @override
  HypothesisSetInput questionId(String? questionId) =>
      call(questionId: questionId);

  @override
  HypothesisSetInput regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  HypothesisSetInput themes(List<String>? themes) => call(themes: themes);

  @override
  HypothesisSetInput private(bool? private) => call(private: private);

  @override
  HypothesisSetInput hypotheses(List<HypothesisInput> hypotheses) =>
      call(hypotheses: hypotheses);

  @override
  HypothesisSetInput indicators(List<IndicatorInput>? indicators) =>
      call(indicators: indicators);

  @override
  HypothesisSetInput assumptions(List<KeyAssumptionInput>? assumptions) =>
      call(assumptions: assumptions);

  @override
  HypothesisSetInput ledgerQuestion(
    HypothesisSetInputLedgerQuestion? ledgerQuestion,
  ) => call(ledgerQuestion: ledgerQuestion);

  @override
  HypothesisSetInput proposalId(String? proposalId) =>
      call(proposalId: proposalId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisSetInput call({
    Object? question = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? cadence = const $CopyWithPlaceholder(),
    Object? horizon = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? questionId = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? themes = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? hypotheses = const $CopyWithPlaceholder(),
    Object? indicators = const $CopyWithPlaceholder(),
    Object? assumptions = const $CopyWithPlaceholder(),
    Object? ledgerQuestion = const $CopyWithPlaceholder(),
    Object? proposalId = const $CopyWithPlaceholder(),
  }) {
    return HypothesisSetInput(
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
          : kind as HypothesisSetInputKindEnum,
      cadence: cadence == const $CopyWithPlaceholder() || cadence == null
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as HypothesisSetInputCadenceEnum,
      horizon: horizon == const $CopyWithPlaceholder()
          ? _value.horizon
          // ignore: cast_nullable_to_non_nullable
          : horizon as DateTime?,
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
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool?,
      hypotheses:
          hypotheses == const $CopyWithPlaceholder() || hypotheses == null
          ? _value.hypotheses
          // ignore: cast_nullable_to_non_nullable
          : hypotheses as List<HypothesisInput>,
      indicators: indicators == const $CopyWithPlaceholder()
          ? _value.indicators
          // ignore: cast_nullable_to_non_nullable
          : indicators as List<IndicatorInput>?,
      assumptions: assumptions == const $CopyWithPlaceholder()
          ? _value.assumptions
          // ignore: cast_nullable_to_non_nullable
          : assumptions as List<KeyAssumptionInput>?,
      ledgerQuestion: ledgerQuestion == const $CopyWithPlaceholder()
          ? _value.ledgerQuestion
          // ignore: cast_nullable_to_non_nullable
          : ledgerQuestion as HypothesisSetInputLedgerQuestion?,
      proposalId: proposalId == const $CopyWithPlaceholder()
          ? _value.proposalId
          // ignore: cast_nullable_to_non_nullable
          : proposalId as String?,
    );
  }
}

extension $HypothesisSetInputCopyWith on HypothesisSetInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisSetInput.copyWith(...)` or `instanceOfHypothesisSetInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisSetInputCWProxy get copyWith =>
      _$HypothesisSetInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisSetInput _$HypothesisSetInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HypothesisSetInput', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['question', 'kind', 'cadence', 'hypotheses'],
      );
      final val = HypothesisSetInput(
        question: $checkedConvert('question', (v) => v as String),
        description: $checkedConvert('description', (v) => v as String?),
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$HypothesisSetInputKindEnumEnumMap, v),
        ),
        cadence: $checkedConvert(
          'cadence',
          (v) => $enumDecode(_$HypothesisSetInputCadenceEnumEnumMap, v),
        ),
        horizon: $checkedConvert(
          'horizon',
          (v) => v == null ? null : DateTime.parse(v as String),
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
        private: $checkedConvert('private', (v) => v as bool? ?? false),
        hypotheses: $checkedConvert(
          'hypotheses',
          (v) => (v as List<dynamic>)
              .map((e) => HypothesisInput.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        indicators: $checkedConvert(
          'indicators',
          (v) => (v as List<dynamic>?)
              ?.map((e) => IndicatorInput.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        assumptions: $checkedConvert(
          'assumptions',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => KeyAssumptionInput.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
        ledgerQuestion: $checkedConvert(
          'ledgerQuestion',
          (v) => v == null
              ? null
              : HypothesisSetInputLedgerQuestion.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
        proposalId: $checkedConvert('proposalId', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$HypothesisSetInputToJson(HypothesisSetInput instance) =>
    <String, dynamic>{
      'question': instance.question,
      'description': ?instance.description,
      'kind': _$HypothesisSetInputKindEnumEnumMap[instance.kind]!,
      'cadence': _$HypothesisSetInputCadenceEnumEnumMap[instance.cadence]!,
      'horizon': ?instance.horizon?.toIso8601String(),
      'situationId': ?instance.situationId,
      'questionId': ?instance.questionId,
      'regionCodes': ?instance.regionCodes,
      'themes': ?instance.themes,
      'private': ?instance.private,
      'hypotheses': instance.hypotheses.map((e) => e.toJson()).toList(),
      'indicators': ?instance.indicators?.map((e) => e.toJson()).toList(),
      'assumptions': ?instance.assumptions?.map((e) => e.toJson()).toList(),
      'ledgerQuestion': ?instance.ledgerQuestion?.toJson(),
      'proposalId': ?instance.proposalId,
    };

const _$HypothesisSetInputKindEnumEnumMap = {
  HypothesisSetInputKindEnum.exclusive: 'exclusive',
  HypothesisSetInputKindEnum.independent: 'independent',
};

const _$HypothesisSetInputCadenceEnumEnumMap = {
  HypothesisSetInputCadenceEnum.daily: 'daily',
  HypothesisSetInputCadenceEnum.weekly: 'weekly',
  HypothesisSetInputCadenceEnum.monthly: 'monthly',
};
