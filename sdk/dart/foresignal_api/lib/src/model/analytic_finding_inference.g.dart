// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analytic_finding_inference.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalyticFindingInferenceCWProxy {
  AnalyticFindingInference detector(DetectorRef detector);

  AnalyticFindingInference stats(Map<String, Object> stats);

  AnalyticFindingInference baseRate(FindingBaseRate baseRate);

  AnalyticFindingInference explanation(String explanation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalyticFindingInference(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalyticFindingInference(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalyticFindingInference call({
    DetectorRef detector,
    Map<String, Object> stats,
    FindingBaseRate baseRate,
    String explanation,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalyticFindingInference.copyWith(...)` or call `instanceOfAnalyticFindingInference.copyWith.fieldName(value)` for a single field.
class _$AnalyticFindingInferenceCWProxyImpl
    implements _$AnalyticFindingInferenceCWProxy {
  const _$AnalyticFindingInferenceCWProxyImpl(this._value);

  final AnalyticFindingInference _value;

  @override
  AnalyticFindingInference detector(DetectorRef detector) =>
      call(detector: detector);

  @override
  AnalyticFindingInference stats(Map<String, Object> stats) =>
      call(stats: stats);

  @override
  AnalyticFindingInference baseRate(FindingBaseRate baseRate) =>
      call(baseRate: baseRate);

  @override
  AnalyticFindingInference explanation(String explanation) =>
      call(explanation: explanation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalyticFindingInference(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalyticFindingInference(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalyticFindingInference call({
    Object? detector = const $CopyWithPlaceholder(),
    Object? stats = const $CopyWithPlaceholder(),
    Object? baseRate = const $CopyWithPlaceholder(),
    Object? explanation = const $CopyWithPlaceholder(),
  }) {
    return AnalyticFindingInference(
      detector: detector == const $CopyWithPlaceholder() || detector == null
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DetectorRef,
      stats: stats == const $CopyWithPlaceholder() || stats == null
          ? _value.stats
          // ignore: cast_nullable_to_non_nullable
          : stats as Map<String, Object>,
      baseRate: baseRate == const $CopyWithPlaceholder() || baseRate == null
          ? _value.baseRate
          // ignore: cast_nullable_to_non_nullable
          : baseRate as FindingBaseRate,
      explanation:
          explanation == const $CopyWithPlaceholder() || explanation == null
          ? _value.explanation
          // ignore: cast_nullable_to_non_nullable
          : explanation as String,
    );
  }
}

extension $AnalyticFindingInferenceCopyWith on AnalyticFindingInference {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalyticFindingInference.copyWith(...)` or `instanceOfAnalyticFindingInference.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalyticFindingInferenceCWProxy get copyWith =>
      _$AnalyticFindingInferenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalyticFindingInference _$AnalyticFindingInferenceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalyticFindingInference', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['detector', 'stats', 'baseRate', 'explanation'],
  );
  final val = AnalyticFindingInference(
    detector: $checkedConvert(
      'detector',
      (v) => DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
    stats: $checkedConvert(
      'stats',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    baseRate: $checkedConvert(
      'baseRate',
      (v) => FindingBaseRate.fromJson(v as Map<String, dynamic>),
    ),
    explanation: $checkedConvert('explanation', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$AnalyticFindingInferenceToJson(
  AnalyticFindingInference instance,
) => <String, dynamic>{
  'detector': instance.detector.toJson(),
  'stats': instance.stats,
  'baseRate': instance.baseRate.toJson(),
  'explanation': instance.explanation,
};
