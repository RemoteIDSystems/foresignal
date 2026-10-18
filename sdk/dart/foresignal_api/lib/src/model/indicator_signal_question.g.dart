// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'indicator_signal_question.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndicatorSignalQuestionCWProxy {
  IndicatorSignalQuestion questionId(String questionId);

  IndicatorSignalQuestion outcome(String outcome);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorSignalQuestion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorSignalQuestion(...).copyWith(id: 12, name: "My name")
  /// ```
  IndicatorSignalQuestion call({String questionId, String outcome});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndicatorSignalQuestion.copyWith(...)` or call `instanceOfIndicatorSignalQuestion.copyWith.fieldName(value)` for a single field.
class _$IndicatorSignalQuestionCWProxyImpl
    implements _$IndicatorSignalQuestionCWProxy {
  const _$IndicatorSignalQuestionCWProxyImpl(this._value);

  final IndicatorSignalQuestion _value;

  @override
  IndicatorSignalQuestion questionId(String questionId) =>
      call(questionId: questionId);

  @override
  IndicatorSignalQuestion outcome(String outcome) => call(outcome: outcome);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorSignalQuestion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorSignalQuestion(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndicatorSignalQuestion call({
    Object? questionId = const $CopyWithPlaceholder(),
    Object? outcome = const $CopyWithPlaceholder(),
  }) {
    return IndicatorSignalQuestion(
      questionId:
          questionId == const $CopyWithPlaceholder() || questionId == null
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String,
      outcome: outcome == const $CopyWithPlaceholder() || outcome == null
          ? _value.outcome
          // ignore: cast_nullable_to_non_nullable
          : outcome as String,
    );
  }
}

extension $IndicatorSignalQuestionCopyWith on IndicatorSignalQuestion {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndicatorSignalQuestion.copyWith(...)` or `instanceOfIndicatorSignalQuestion.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndicatorSignalQuestionCWProxy get copyWith =>
      _$IndicatorSignalQuestionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndicatorSignalQuestion _$IndicatorSignalQuestionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IndicatorSignalQuestion', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['questionId', 'outcome']);
  final val = IndicatorSignalQuestion(
    questionId: $checkedConvert('questionId', (v) => v as String),
    outcome: $checkedConvert('outcome', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$IndicatorSignalQuestionToJson(
  IndicatorSignalQuestion instance,
) => <String, dynamic>{
  'questionId': instance.questionId,
  'outcome': instance.outcome,
};
