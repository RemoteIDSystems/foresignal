// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'key_judgment.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$KeyJudgmentCWProxy {
  KeyJudgment id(String? id);

  KeyJudgment text(String text);

  KeyJudgment probability(num? probability);

  KeyJudgment estimativeTerm(IcdTerm? estimativeTerm);

  KeyJudgment confidence(ConfidenceLevel? confidence);

  KeyJudgment confidenceStatement(String? confidenceStatement);

  KeyJudgment citations(List<String> citations);

  KeyJudgment hypothesisSetId(String? hypothesisSetId);

  KeyJudgment questionId(String? questionId);

  KeyJudgment textOrigin(KeyJudgmentTextOriginEnum textOrigin);

  KeyJudgment change(KeyJudgmentChange? change);

  KeyJudgment downgraded(bool? downgraded);

  KeyJudgment warnings(List<String>? warnings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KeyJudgment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KeyJudgment(...).copyWith(id: 12, name: "My name")
  /// ```
  KeyJudgment call({
    String? id,
    String text,
    num? probability,
    IcdTerm? estimativeTerm,
    ConfidenceLevel? confidence,
    String? confidenceStatement,
    List<String> citations,
    String? hypothesisSetId,
    String? questionId,
    KeyJudgmentTextOriginEnum textOrigin,
    KeyJudgmentChange? change,
    bool? downgraded,
    List<String>? warnings,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfKeyJudgment.copyWith(...)` or call `instanceOfKeyJudgment.copyWith.fieldName(value)` for a single field.
class _$KeyJudgmentCWProxyImpl implements _$KeyJudgmentCWProxy {
  const _$KeyJudgmentCWProxyImpl(this._value);

  final KeyJudgment _value;

  @override
  KeyJudgment id(String? id) => call(id: id);

  @override
  KeyJudgment text(String text) => call(text: text);

  @override
  KeyJudgment probability(num? probability) => call(probability: probability);

  @override
  KeyJudgment estimativeTerm(IcdTerm? estimativeTerm) =>
      call(estimativeTerm: estimativeTerm);

  @override
  KeyJudgment confidence(ConfidenceLevel? confidence) =>
      call(confidence: confidence);

  @override
  KeyJudgment confidenceStatement(String? confidenceStatement) =>
      call(confidenceStatement: confidenceStatement);

  @override
  KeyJudgment citations(List<String> citations) => call(citations: citations);

  @override
  KeyJudgment hypothesisSetId(String? hypothesisSetId) =>
      call(hypothesisSetId: hypothesisSetId);

  @override
  KeyJudgment questionId(String? questionId) => call(questionId: questionId);

  @override
  KeyJudgment textOrigin(KeyJudgmentTextOriginEnum textOrigin) =>
      call(textOrigin: textOrigin);

  @override
  KeyJudgment change(KeyJudgmentChange? change) => call(change: change);

  @override
  KeyJudgment downgraded(bool? downgraded) => call(downgraded: downgraded);

  @override
  KeyJudgment warnings(List<String>? warnings) => call(warnings: warnings);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `KeyJudgment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// KeyJudgment(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  KeyJudgment call({
    Object? id = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? estimativeTerm = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? confidenceStatement = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? hypothesisSetId = const $CopyWithPlaceholder(),
    Object? questionId = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
    Object? change = const $CopyWithPlaceholder(),
    Object? downgraded = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
  }) {
    return KeyJudgment(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      estimativeTerm: estimativeTerm == const $CopyWithPlaceholder()
          ? _value.estimativeTerm
          // ignore: cast_nullable_to_non_nullable
          : estimativeTerm as IcdTerm?,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as ConfidenceLevel?,
      confidenceStatement: confidenceStatement == const $CopyWithPlaceholder()
          ? _value.confidenceStatement
          // ignore: cast_nullable_to_non_nullable
          : confidenceStatement as String?,
      citations: citations == const $CopyWithPlaceholder() || citations == null
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<String>,
      hypothesisSetId: hypothesisSetId == const $CopyWithPlaceholder()
          ? _value.hypothesisSetId
          // ignore: cast_nullable_to_non_nullable
          : hypothesisSetId as String?,
      questionId: questionId == const $CopyWithPlaceholder()
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String?,
      textOrigin:
          textOrigin == const $CopyWithPlaceholder() || textOrigin == null
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as KeyJudgmentTextOriginEnum,
      change: change == const $CopyWithPlaceholder()
          ? _value.change
          // ignore: cast_nullable_to_non_nullable
          : change as KeyJudgmentChange?,
      downgraded: downgraded == const $CopyWithPlaceholder()
          ? _value.downgraded
          // ignore: cast_nullable_to_non_nullable
          : downgraded as bool?,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<String>?,
    );
  }
}

extension $KeyJudgmentCopyWith on KeyJudgment {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfKeyJudgment.copyWith(...)` or `instanceOfKeyJudgment.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$KeyJudgmentCWProxy get copyWith => _$KeyJudgmentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KeyJudgment _$KeyJudgmentFromJson(Map<String, dynamic> json) => $checkedCreate(
  'KeyJudgment',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['text', 'citations', 'textOrigin']);
    final val = KeyJudgment(
      id: $checkedConvert('id', (v) => v as String?),
      text: $checkedConvert('text', (v) => v as String),
      probability: $checkedConvert('probability', (v) => v as num?),
      estimativeTerm: $checkedConvert(
        'estimativeTerm',
        (v) => $enumDecodeNullable(_$IcdTermEnumMap, v),
      ),
      confidence: $checkedConvert(
        'confidence',
        (v) => $enumDecodeNullable(_$ConfidenceLevelEnumMap, v),
      ),
      confidenceStatement: $checkedConvert(
        'confidenceStatement',
        (v) => v as String?,
      ),
      citations: $checkedConvert(
        'citations',
        (v) => (v as List<dynamic>).map((e) => e as String).toList(),
      ),
      hypothesisSetId: $checkedConvert('hypothesisSetId', (v) => v as String?),
      questionId: $checkedConvert('questionId', (v) => v as String?),
      textOrigin: $checkedConvert(
        'textOrigin',
        (v) => $enumDecode(_$KeyJudgmentTextOriginEnumEnumMap, v),
      ),
      change: $checkedConvert(
        'change',
        (v) => v == null
            ? null
            : KeyJudgmentChange.fromJson(v as Map<String, dynamic>),
      ),
      downgraded: $checkedConvert('downgraded', (v) => v as bool?),
      warnings: $checkedConvert(
        'warnings',
        (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$KeyJudgmentToJson(KeyJudgment instance) =>
    <String, dynamic>{
      'id': ?instance.id,
      'text': instance.text,
      'probability': ?instance.probability,
      'estimativeTerm': ?_$IcdTermEnumMap[instance.estimativeTerm],
      'confidence': ?_$ConfidenceLevelEnumMap[instance.confidence],
      'confidenceStatement': ?instance.confidenceStatement,
      'citations': instance.citations,
      'hypothesisSetId': ?instance.hypothesisSetId,
      'questionId': ?instance.questionId,
      'textOrigin': _$KeyJudgmentTextOriginEnumEnumMap[instance.textOrigin]!,
      'change': ?instance.change?.toJson(),
      'downgraded': ?instance.downgraded,
      'warnings': ?instance.warnings,
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

const _$KeyJudgmentTextOriginEnumEnumMap = {
  KeyJudgmentTextOriginEnum.deterministic: 'deterministic',
  KeyJudgmentTextOriginEnum.llm: 'llm',
};
