// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analog_outcomes_hypothesis_outcome.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalogOutcomesHypothesisOutcomeCWProxy {
  AnalogOutcomesHypothesisOutcome setId(String setId);

  AnalogOutcomesHypothesisOutcome outcome(String outcome);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogOutcomesHypothesisOutcome(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogOutcomesHypothesisOutcome(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalogOutcomesHypothesisOutcome call({String setId, String outcome});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalogOutcomesHypothesisOutcome.copyWith(...)` or call `instanceOfAnalogOutcomesHypothesisOutcome.copyWith.fieldName(value)` for a single field.
class _$AnalogOutcomesHypothesisOutcomeCWProxyImpl
    implements _$AnalogOutcomesHypothesisOutcomeCWProxy {
  const _$AnalogOutcomesHypothesisOutcomeCWProxyImpl(this._value);

  final AnalogOutcomesHypothesisOutcome _value;

  @override
  AnalogOutcomesHypothesisOutcome setId(String setId) => call(setId: setId);

  @override
  AnalogOutcomesHypothesisOutcome outcome(String outcome) =>
      call(outcome: outcome);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogOutcomesHypothesisOutcome(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogOutcomesHypothesisOutcome(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalogOutcomesHypothesisOutcome call({
    Object? setId = const $CopyWithPlaceholder(),
    Object? outcome = const $CopyWithPlaceholder(),
  }) {
    return AnalogOutcomesHypothesisOutcome(
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      outcome: outcome == const $CopyWithPlaceholder() || outcome == null
          ? _value.outcome
          // ignore: cast_nullable_to_non_nullable
          : outcome as String,
    );
  }
}

extension $AnalogOutcomesHypothesisOutcomeCopyWith
    on AnalogOutcomesHypothesisOutcome {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalogOutcomesHypothesisOutcome.copyWith(...)` or `instanceOfAnalogOutcomesHypothesisOutcome.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalogOutcomesHypothesisOutcomeCWProxy get copyWith =>
      _$AnalogOutcomesHypothesisOutcomeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalogOutcomesHypothesisOutcome _$AnalogOutcomesHypothesisOutcomeFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalogOutcomesHypothesisOutcome', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['setId', 'outcome']);
  final val = AnalogOutcomesHypothesisOutcome(
    setId: $checkedConvert('setId', (v) => v as String),
    outcome: $checkedConvert('outcome', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$AnalogOutcomesHypothesisOutcomeToJson(
  AnalogOutcomesHypothesisOutcome instance,
) => <String, dynamic>{'setId': instance.setId, 'outcome': instance.outcome};
