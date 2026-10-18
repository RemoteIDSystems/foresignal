// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alert_feedback_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AlertFeedbackInputCWProxy {
  AlertFeedbackInput feedback(AlertFeedbackInputFeedbackEnum feedback);

  AlertFeedbackInput note(String? note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertFeedbackInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertFeedbackInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AlertFeedbackInput call({
    AlertFeedbackInputFeedbackEnum feedback,
    String? note,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAlertFeedbackInput.copyWith(...)` or call `instanceOfAlertFeedbackInput.copyWith.fieldName(value)` for a single field.
class _$AlertFeedbackInputCWProxyImpl implements _$AlertFeedbackInputCWProxy {
  const _$AlertFeedbackInputCWProxyImpl(this._value);

  final AlertFeedbackInput _value;

  @override
  AlertFeedbackInput feedback(AlertFeedbackInputFeedbackEnum feedback) =>
      call(feedback: feedback);

  @override
  AlertFeedbackInput note(String? note) => call(note: note);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertFeedbackInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertFeedbackInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AlertFeedbackInput call({
    Object? feedback = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
  }) {
    return AlertFeedbackInput(
      feedback: feedback == const $CopyWithPlaceholder() || feedback == null
          ? _value.feedback
          // ignore: cast_nullable_to_non_nullable
          : feedback as AlertFeedbackInputFeedbackEnum,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $AlertFeedbackInputCopyWith on AlertFeedbackInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAlertFeedbackInput.copyWith(...)` or `instanceOfAlertFeedbackInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AlertFeedbackInputCWProxy get copyWith =>
      _$AlertFeedbackInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AlertFeedbackInput _$AlertFeedbackInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AlertFeedbackInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['feedback']);
      final val = AlertFeedbackInput(
        feedback: $checkedConvert(
          'feedback',
          (v) => $enumDecode(_$AlertFeedbackInputFeedbackEnumEnumMap, v),
        ),
        note: $checkedConvert('note', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AlertFeedbackInputToJson(AlertFeedbackInput instance) =>
    <String, dynamic>{
      'feedback': _$AlertFeedbackInputFeedbackEnumEnumMap[instance.feedback]!,
      'note': ?instance.note,
    };

const _$AlertFeedbackInputFeedbackEnumEnumMap = {
  AlertFeedbackInputFeedbackEnum.useful: 'useful',
  AlertFeedbackInputFeedbackEnum.noise: 'noise',
  AlertFeedbackInputFeedbackEnum.late_: 'late',
  AlertFeedbackInputFeedbackEnum.falsePositive: 'false_positive',
};
