// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of1_all_of_judgments_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf1AllOfJudgmentsInnerCWProxy {
  ReportBlockOneOf1AllOfJudgmentsInner text(String text);

  ReportBlockOneOf1AllOfJudgmentsInner probability(num? probability);

  ReportBlockOneOf1AllOfJudgmentsInner icdTerm(IcdTerm? icdTerm);

  ReportBlockOneOf1AllOfJudgmentsInner confidence(ConfidenceLevel? confidence);

  ReportBlockOneOf1AllOfJudgmentsInner questionRef(String? questionRef);

  ReportBlockOneOf1AllOfJudgmentsInner hypothesisSetRef(
    String? hypothesisSetRef,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf1AllOfJudgmentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf1AllOfJudgmentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf1AllOfJudgmentsInner call({
    String text,
    num? probability,
    IcdTerm? icdTerm,
    ConfidenceLevel? confidence,
    String? questionRef,
    String? hypothesisSetRef,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf1AllOfJudgmentsInner.copyWith(...)` or call `instanceOfReportBlockOneOf1AllOfJudgmentsInner.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf1AllOfJudgmentsInnerCWProxyImpl
    implements _$ReportBlockOneOf1AllOfJudgmentsInnerCWProxy {
  const _$ReportBlockOneOf1AllOfJudgmentsInnerCWProxyImpl(this._value);

  final ReportBlockOneOf1AllOfJudgmentsInner _value;

  @override
  ReportBlockOneOf1AllOfJudgmentsInner text(String text) => call(text: text);

  @override
  ReportBlockOneOf1AllOfJudgmentsInner probability(num? probability) =>
      call(probability: probability);

  @override
  ReportBlockOneOf1AllOfJudgmentsInner icdTerm(IcdTerm? icdTerm) =>
      call(icdTerm: icdTerm);

  @override
  ReportBlockOneOf1AllOfJudgmentsInner confidence(
    ConfidenceLevel? confidence,
  ) => call(confidence: confidence);

  @override
  ReportBlockOneOf1AllOfJudgmentsInner questionRef(String? questionRef) =>
      call(questionRef: questionRef);

  @override
  ReportBlockOneOf1AllOfJudgmentsInner hypothesisSetRef(
    String? hypothesisSetRef,
  ) => call(hypothesisSetRef: hypothesisSetRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf1AllOfJudgmentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf1AllOfJudgmentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf1AllOfJudgmentsInner call({
    Object? text = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? icdTerm = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? questionRef = const $CopyWithPlaceholder(),
    Object? hypothesisSetRef = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf1AllOfJudgmentsInner(
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      icdTerm: icdTerm == const $CopyWithPlaceholder()
          ? _value.icdTerm
          // ignore: cast_nullable_to_non_nullable
          : icdTerm as IcdTerm?,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as ConfidenceLevel?,
      questionRef: questionRef == const $CopyWithPlaceholder()
          ? _value.questionRef
          // ignore: cast_nullable_to_non_nullable
          : questionRef as String?,
      hypothesisSetRef: hypothesisSetRef == const $CopyWithPlaceholder()
          ? _value.hypothesisSetRef
          // ignore: cast_nullable_to_non_nullable
          : hypothesisSetRef as String?,
    );
  }
}

extension $ReportBlockOneOf1AllOfJudgmentsInnerCopyWith
    on ReportBlockOneOf1AllOfJudgmentsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf1AllOfJudgmentsInner.copyWith(...)` or `instanceOfReportBlockOneOf1AllOfJudgmentsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf1AllOfJudgmentsInnerCWProxy get copyWith =>
      _$ReportBlockOneOf1AllOfJudgmentsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf1AllOfJudgmentsInner
_$ReportBlockOneOf1AllOfJudgmentsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockOneOf1AllOfJudgmentsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['text']);
      final val = ReportBlockOneOf1AllOfJudgmentsInner(
        text: $checkedConvert('text', (v) => v as String),
        probability: $checkedConvert('probability', (v) => v as num?),
        icdTerm: $checkedConvert(
          'icdTerm',
          (v) => $enumDecodeNullable(_$IcdTermEnumMap, v),
        ),
        confidence: $checkedConvert(
          'confidence',
          (v) => $enumDecodeNullable(_$ConfidenceLevelEnumMap, v),
        ),
        questionRef: $checkedConvert('questionRef', (v) => v as String?),
        hypothesisSetRef: $checkedConvert(
          'hypothesisSetRef',
          (v) => v as String?,
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockOneOf1AllOfJudgmentsInnerToJson(
  ReportBlockOneOf1AllOfJudgmentsInner instance,
) => <String, dynamic>{
  'text': instance.text,
  'probability': ?instance.probability,
  'icdTerm': ?_$IcdTermEnumMap[instance.icdTerm],
  'confidence': ?_$ConfidenceLevelEnumMap[instance.confidence],
  'questionRef': ?instance.questionRef,
  'hypothesisSetRef': ?instance.hypothesisSetRef,
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
