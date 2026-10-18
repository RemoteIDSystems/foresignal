// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_patch_probability_override.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisPatchProbabilityOverrideCWProxy {
  HypothesisPatchProbabilityOverride probability(num probability);

  HypothesisPatchProbabilityOverride rationale(String rationale);

  HypothesisPatchProbabilityOverride exceedCap(bool? exceedCap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisPatchProbabilityOverride(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisPatchProbabilityOverride(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisPatchProbabilityOverride call({
    num probability,
    String rationale,
    bool? exceedCap,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisPatchProbabilityOverride.copyWith(...)` or call `instanceOfHypothesisPatchProbabilityOverride.copyWith.fieldName(value)` for a single field.
class _$HypothesisPatchProbabilityOverrideCWProxyImpl
    implements _$HypothesisPatchProbabilityOverrideCWProxy {
  const _$HypothesisPatchProbabilityOverrideCWProxyImpl(this._value);

  final HypothesisPatchProbabilityOverride _value;

  @override
  HypothesisPatchProbabilityOverride probability(num probability) =>
      call(probability: probability);

  @override
  HypothesisPatchProbabilityOverride rationale(String rationale) =>
      call(rationale: rationale);

  @override
  HypothesisPatchProbabilityOverride exceedCap(bool? exceedCap) =>
      call(exceedCap: exceedCap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisPatchProbabilityOverride(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisPatchProbabilityOverride(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisPatchProbabilityOverride call({
    Object? probability = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? exceedCap = const $CopyWithPlaceholder(),
  }) {
    return HypothesisPatchProbabilityOverride(
      probability:
          probability == const $CopyWithPlaceholder() || probability == null
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num,
      rationale: rationale == const $CopyWithPlaceholder() || rationale == null
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String,
      exceedCap: exceedCap == const $CopyWithPlaceholder()
          ? _value.exceedCap
          // ignore: cast_nullable_to_non_nullable
          : exceedCap as bool?,
    );
  }
}

extension $HypothesisPatchProbabilityOverrideCopyWith
    on HypothesisPatchProbabilityOverride {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisPatchProbabilityOverride.copyWith(...)` or `instanceOfHypothesisPatchProbabilityOverride.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisPatchProbabilityOverrideCWProxy get copyWith =>
      _$HypothesisPatchProbabilityOverrideCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisPatchProbabilityOverride _$HypothesisPatchProbabilityOverrideFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisPatchProbabilityOverride', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['probability', 'rationale']);
  final val = HypothesisPatchProbabilityOverride(
    probability: $checkedConvert('probability', (v) => v as num),
    rationale: $checkedConvert('rationale', (v) => v as String),
    exceedCap: $checkedConvert('exceedCap', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$HypothesisPatchProbabilityOverrideToJson(
  HypothesisPatchProbabilityOverride instance,
) => <String, dynamic>{
  'probability': instance.probability,
  'rationale': instance.rationale,
  'exceedCap': ?instance.exceedCap,
};
