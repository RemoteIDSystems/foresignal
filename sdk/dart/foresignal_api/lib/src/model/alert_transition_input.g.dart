// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alert_transition_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AlertTransitionInputCWProxy {
  AlertTransitionInput note(String? note);

  AlertTransitionInput feedback(AlertTransitionInputFeedbackEnum? feedback);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertTransitionInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertTransitionInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AlertTransitionInput call({
    String? note,
    AlertTransitionInputFeedbackEnum? feedback,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAlertTransitionInput.copyWith(...)` or call `instanceOfAlertTransitionInput.copyWith.fieldName(value)` for a single field.
class _$AlertTransitionInputCWProxyImpl
    implements _$AlertTransitionInputCWProxy {
  const _$AlertTransitionInputCWProxyImpl(this._value);

  final AlertTransitionInput _value;

  @override
  AlertTransitionInput note(String? note) => call(note: note);

  @override
  AlertTransitionInput feedback(AlertTransitionInputFeedbackEnum? feedback) =>
      call(feedback: feedback);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertTransitionInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertTransitionInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AlertTransitionInput call({
    Object? note = const $CopyWithPlaceholder(),
    Object? feedback = const $CopyWithPlaceholder(),
  }) {
    return AlertTransitionInput(
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
      feedback: feedback == const $CopyWithPlaceholder()
          ? _value.feedback
          // ignore: cast_nullable_to_non_nullable
          : feedback as AlertTransitionInputFeedbackEnum?,
    );
  }
}

extension $AlertTransitionInputCopyWith on AlertTransitionInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAlertTransitionInput.copyWith(...)` or `instanceOfAlertTransitionInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AlertTransitionInputCWProxy get copyWith =>
      _$AlertTransitionInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AlertTransitionInput _$AlertTransitionInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AlertTransitionInput', json, ($checkedConvert) {
  final val = AlertTransitionInput(
    note: $checkedConvert('note', (v) => v as String?),
    feedback: $checkedConvert(
      'feedback',
      (v) => $enumDecodeNullable(_$AlertTransitionInputFeedbackEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$AlertTransitionInputToJson(
  AlertTransitionInput instance,
) => <String, dynamic>{
  'note': ?instance.note,
  'feedback': ?_$AlertTransitionInputFeedbackEnumEnumMap[instance.feedback],
};

const _$AlertTransitionInputFeedbackEnumEnumMap = {
  AlertTransitionInputFeedbackEnum.useful: 'useful',
  AlertTransitionInputFeedbackEnum.noise: 'noise',
  AlertTransitionInputFeedbackEnum.late_: 'late',
  AlertTransitionInputFeedbackEnum.falsePositive: 'false_positive',
};
