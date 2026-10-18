// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exercise_stats_skipped_rules_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExerciseStatsSkippedRulesInnerCWProxy {
  ExerciseStatsSkippedRulesInner ruleId(String ruleId);

  ExerciseStatsSkippedRulesInner reason(String reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExerciseStatsSkippedRulesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExerciseStatsSkippedRulesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ExerciseStatsSkippedRulesInner call({String ruleId, String reason});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExerciseStatsSkippedRulesInner.copyWith(...)` or call `instanceOfExerciseStatsSkippedRulesInner.copyWith.fieldName(value)` for a single field.
class _$ExerciseStatsSkippedRulesInnerCWProxyImpl
    implements _$ExerciseStatsSkippedRulesInnerCWProxy {
  const _$ExerciseStatsSkippedRulesInnerCWProxyImpl(this._value);

  final ExerciseStatsSkippedRulesInner _value;

  @override
  ExerciseStatsSkippedRulesInner ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  ExerciseStatsSkippedRulesInner reason(String reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExerciseStatsSkippedRulesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExerciseStatsSkippedRulesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExerciseStatsSkippedRulesInner call({
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return ExerciseStatsSkippedRulesInner(
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
    );
  }
}

extension $ExerciseStatsSkippedRulesInnerCopyWith
    on ExerciseStatsSkippedRulesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExerciseStatsSkippedRulesInner.copyWith(...)` or `instanceOfExerciseStatsSkippedRulesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExerciseStatsSkippedRulesInnerCWProxy get copyWith =>
      _$ExerciseStatsSkippedRulesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExerciseStatsSkippedRulesInner _$ExerciseStatsSkippedRulesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ExerciseStatsSkippedRulesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ruleId', 'reason']);
  final val = ExerciseStatsSkippedRulesInner(
    ruleId: $checkedConvert('ruleId', (v) => v as String),
    reason: $checkedConvert('reason', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$ExerciseStatsSkippedRulesInnerToJson(
  ExerciseStatsSkippedRulesInner instance,
) => <String, dynamic>{'ruleId': instance.ruleId, 'reason': instance.reason};
