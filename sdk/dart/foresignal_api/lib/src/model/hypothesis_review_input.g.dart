// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_review_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisReviewInputCWProxy {
  HypothesisReviewInput cause(HypothesisReviewInputCauseEnum? cause);

  HypothesisReviewInput rationale(String rationale);

  HypothesisReviewInput proposalId(String? proposalId);

  HypothesisReviewInput probabilities(Object? probabilities);

  HypothesisReviewInput lr(List<HypothesisReviewInputLrInner>? lr);

  HypothesisReviewInput assumptions(
    List<HypothesisReviewInputAssumptionsInner>? assumptions,
  );

  HypothesisReviewInput exceedCap(bool? exceedCap);

  HypothesisReviewInput citations(List<String>? citations);

  HypothesisReviewInput version(int? version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisReviewInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisReviewInput(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisReviewInput call({
    HypothesisReviewInputCauseEnum? cause,
    String rationale,
    String? proposalId,
    Object? probabilities,
    List<HypothesisReviewInputLrInner>? lr,
    List<HypothesisReviewInputAssumptionsInner>? assumptions,
    bool? exceedCap,
    List<String>? citations,
    int? version,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisReviewInput.copyWith(...)` or call `instanceOfHypothesisReviewInput.copyWith.fieldName(value)` for a single field.
class _$HypothesisReviewInputCWProxyImpl
    implements _$HypothesisReviewInputCWProxy {
  const _$HypothesisReviewInputCWProxyImpl(this._value);

  final HypothesisReviewInput _value;

  @override
  HypothesisReviewInput cause(HypothesisReviewInputCauseEnum? cause) =>
      call(cause: cause);

  @override
  HypothesisReviewInput rationale(String rationale) =>
      call(rationale: rationale);

  @override
  HypothesisReviewInput proposalId(String? proposalId) =>
      call(proposalId: proposalId);

  @override
  HypothesisReviewInput probabilities(Object? probabilities) =>
      call(probabilities: probabilities);

  @override
  HypothesisReviewInput lr(List<HypothesisReviewInputLrInner>? lr) =>
      call(lr: lr);

  @override
  HypothesisReviewInput assumptions(
    List<HypothesisReviewInputAssumptionsInner>? assumptions,
  ) => call(assumptions: assumptions);

  @override
  HypothesisReviewInput exceedCap(bool? exceedCap) =>
      call(exceedCap: exceedCap);

  @override
  HypothesisReviewInput citations(List<String>? citations) =>
      call(citations: citations);

  @override
  HypothesisReviewInput version(int? version) => call(version: version);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisReviewInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisReviewInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisReviewInput call({
    Object? cause = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? proposalId = const $CopyWithPlaceholder(),
    Object? probabilities = const $CopyWithPlaceholder(),
    Object? lr = const $CopyWithPlaceholder(),
    Object? assumptions = const $CopyWithPlaceholder(),
    Object? exceedCap = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
  }) {
    return HypothesisReviewInput(
      cause: cause == const $CopyWithPlaceholder()
          ? _value.cause
          // ignore: cast_nullable_to_non_nullable
          : cause as HypothesisReviewInputCauseEnum?,
      rationale: rationale == const $CopyWithPlaceholder() || rationale == null
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String,
      proposalId: proposalId == const $CopyWithPlaceholder()
          ? _value.proposalId
          // ignore: cast_nullable_to_non_nullable
          : proposalId as String?,
      probabilities: probabilities == const $CopyWithPlaceholder()
          ? _value.probabilities
          // ignore: cast_nullable_to_non_nullable
          : probabilities as Object?,
      lr: lr == const $CopyWithPlaceholder()
          ? _value.lr
          // ignore: cast_nullable_to_non_nullable
          : lr as List<HypothesisReviewInputLrInner>?,
      assumptions: assumptions == const $CopyWithPlaceholder()
          ? _value.assumptions
          // ignore: cast_nullable_to_non_nullable
          : assumptions as List<HypothesisReviewInputAssumptionsInner>?,
      exceedCap: exceedCap == const $CopyWithPlaceholder()
          ? _value.exceedCap
          // ignore: cast_nullable_to_non_nullable
          : exceedCap as bool?,
      citations: citations == const $CopyWithPlaceholder()
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<String>?,
      version: version == const $CopyWithPlaceholder()
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int?,
    );
  }
}

extension $HypothesisReviewInputCopyWith on HypothesisReviewInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisReviewInput.copyWith(...)` or `instanceOfHypothesisReviewInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisReviewInputCWProxy get copyWith =>
      _$HypothesisReviewInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisReviewInput _$HypothesisReviewInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisReviewInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['rationale']);
  final val = HypothesisReviewInput(
    cause: $checkedConvert(
      'cause',
      (v) => $enumDecodeNullable(_$HypothesisReviewInputCauseEnumEnumMap, v),
    ),
    rationale: $checkedConvert('rationale', (v) => v as String),
    proposalId: $checkedConvert('proposalId', (v) => v as String?),
    probabilities: $checkedConvert('probabilities', (v) => v),
    lr: $checkedConvert(
      'lr',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => HypothesisReviewInputLrInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    assumptions: $checkedConvert(
      'assumptions',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => HypothesisReviewInputAssumptionsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    exceedCap: $checkedConvert('exceedCap', (v) => v as bool? ?? false),
    citations: $checkedConvert(
      'citations',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    version: $checkedConvert('version', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$HypothesisReviewInputToJson(
  HypothesisReviewInput instance,
) => <String, dynamic>{
  'cause': ?_$HypothesisReviewInputCauseEnumEnumMap[instance.cause],
  'rationale': instance.rationale,
  'proposalId': ?instance.proposalId,
  'probabilities': ?instance.probabilities,
  'lr': ?instance.lr?.map((e) => e.toJson()).toList(),
  'assumptions': ?instance.assumptions?.map((e) => e.toJson()).toList(),
  'exceedCap': ?instance.exceedCap,
  'citations': ?instance.citations,
  'version': ?instance.version,
};

const _$HypothesisReviewInputCauseEnumEnumMap = {
  HypothesisReviewInputCauseEnum.review: 'review',
  HypothesisReviewInputCauseEnum.operator_: 'operator',
};
