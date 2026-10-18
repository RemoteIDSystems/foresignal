// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_review_input_lr_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisReviewInputLrInnerCWProxy {
  HypothesisReviewInputLrInner indicatorId(String indicatorId);

  HypothesisReviewInputLrInner hypothesisId(String hypothesisId);

  HypothesisReviewInputLrInner likelihood(IndicatorLikelihood likelihood);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisReviewInputLrInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisReviewInputLrInner(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisReviewInputLrInner call({
    String indicatorId,
    String hypothesisId,
    IndicatorLikelihood likelihood,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisReviewInputLrInner.copyWith(...)` or call `instanceOfHypothesisReviewInputLrInner.copyWith.fieldName(value)` for a single field.
class _$HypothesisReviewInputLrInnerCWProxyImpl
    implements _$HypothesisReviewInputLrInnerCWProxy {
  const _$HypothesisReviewInputLrInnerCWProxyImpl(this._value);

  final HypothesisReviewInputLrInner _value;

  @override
  HypothesisReviewInputLrInner indicatorId(String indicatorId) =>
      call(indicatorId: indicatorId);

  @override
  HypothesisReviewInputLrInner hypothesisId(String hypothesisId) =>
      call(hypothesisId: hypothesisId);

  @override
  HypothesisReviewInputLrInner likelihood(IndicatorLikelihood likelihood) =>
      call(likelihood: likelihood);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisReviewInputLrInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisReviewInputLrInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisReviewInputLrInner call({
    Object? indicatorId = const $CopyWithPlaceholder(),
    Object? hypothesisId = const $CopyWithPlaceholder(),
    Object? likelihood = const $CopyWithPlaceholder(),
  }) {
    return HypothesisReviewInputLrInner(
      indicatorId:
          indicatorId == const $CopyWithPlaceholder() || indicatorId == null
          ? _value.indicatorId
          // ignore: cast_nullable_to_non_nullable
          : indicatorId as String,
      hypothesisId:
          hypothesisId == const $CopyWithPlaceholder() || hypothesisId == null
          ? _value.hypothesisId
          // ignore: cast_nullable_to_non_nullable
          : hypothesisId as String,
      likelihood:
          likelihood == const $CopyWithPlaceholder() || likelihood == null
          ? _value.likelihood
          // ignore: cast_nullable_to_non_nullable
          : likelihood as IndicatorLikelihood,
    );
  }
}

extension $HypothesisReviewInputLrInnerCopyWith
    on HypothesisReviewInputLrInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisReviewInputLrInner.copyWith(...)` or `instanceOfHypothesisReviewInputLrInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisReviewInputLrInnerCWProxy get copyWith =>
      _$HypothesisReviewInputLrInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisReviewInputLrInner _$HypothesisReviewInputLrInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisReviewInputLrInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['indicatorId', 'hypothesisId', 'likelihood'],
  );
  final val = HypothesisReviewInputLrInner(
    indicatorId: $checkedConvert('indicatorId', (v) => v as String),
    hypothesisId: $checkedConvert('hypothesisId', (v) => v as String),
    likelihood: $checkedConvert(
      'likelihood',
      (v) => IndicatorLikelihood.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$HypothesisReviewInputLrInnerToJson(
  HypothesisReviewInputLrInner instance,
) => <String, dynamic>{
  'indicatorId': instance.indicatorId,
  'hypothesisId': instance.hypothesisId,
  'likelihood': instance.likelihood.toJson(),
};
