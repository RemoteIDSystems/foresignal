// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_probability_state.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisProbabilityStateCWProxy {
  HypothesisProbabilityState hypothesisId(String hypothesisId);

  HypothesisProbabilityState label(String? label);

  HypothesisProbabilityState probability(num probability);

  HypothesisProbabilityState estimativeTerm(IcdTerm estimativeTerm);

  HypothesisProbabilityState confidence(ConfidenceLevel? confidence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisProbabilityState(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisProbabilityState(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisProbabilityState call({
    String hypothesisId,
    String? label,
    num probability,
    IcdTerm estimativeTerm,
    ConfidenceLevel? confidence,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisProbabilityState.copyWith(...)` or call `instanceOfHypothesisProbabilityState.copyWith.fieldName(value)` for a single field.
class _$HypothesisProbabilityStateCWProxyImpl
    implements _$HypothesisProbabilityStateCWProxy {
  const _$HypothesisProbabilityStateCWProxyImpl(this._value);

  final HypothesisProbabilityState _value;

  @override
  HypothesisProbabilityState hypothesisId(String hypothesisId) =>
      call(hypothesisId: hypothesisId);

  @override
  HypothesisProbabilityState label(String? label) => call(label: label);

  @override
  HypothesisProbabilityState probability(num probability) =>
      call(probability: probability);

  @override
  HypothesisProbabilityState estimativeTerm(IcdTerm estimativeTerm) =>
      call(estimativeTerm: estimativeTerm);

  @override
  HypothesisProbabilityState confidence(ConfidenceLevel? confidence) =>
      call(confidence: confidence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisProbabilityState(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisProbabilityState(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisProbabilityState call({
    Object? hypothesisId = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? estimativeTerm = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
  }) {
    return HypothesisProbabilityState(
      hypothesisId:
          hypothesisId == const $CopyWithPlaceholder() || hypothesisId == null
          ? _value.hypothesisId
          // ignore: cast_nullable_to_non_nullable
          : hypothesisId as String,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
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
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as ConfidenceLevel?,
    );
  }
}

extension $HypothesisProbabilityStateCopyWith on HypothesisProbabilityState {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisProbabilityState.copyWith(...)` or `instanceOfHypothesisProbabilityState.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisProbabilityStateCWProxy get copyWith =>
      _$HypothesisProbabilityStateCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisProbabilityState _$HypothesisProbabilityStateFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisProbabilityState', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['hypothesisId', 'probability', 'estimativeTerm'],
  );
  final val = HypothesisProbabilityState(
    hypothesisId: $checkedConvert('hypothesisId', (v) => v as String),
    label: $checkedConvert('label', (v) => v as String?),
    probability: $checkedConvert('probability', (v) => v as num),
    estimativeTerm: $checkedConvert(
      'estimativeTerm',
      (v) => $enumDecode(_$IcdTermEnumMap, v),
    ),
    confidence: $checkedConvert(
      'confidence',
      (v) => $enumDecodeNullable(_$ConfidenceLevelEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$HypothesisProbabilityStateToJson(
  HypothesisProbabilityState instance,
) => <String, dynamic>{
  'hypothesisId': instance.hypothesisId,
  'label': ?instance.label,
  'probability': instance.probability,
  'estimativeTerm': _$IcdTermEnumMap[instance.estimativeTerm]!,
  'confidence': ?_$ConfidenceLevelEnumMap[instance.confidence],
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
