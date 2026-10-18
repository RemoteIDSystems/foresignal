// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assessment_feedback_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AssessmentFeedbackInputCWProxy {
  AssessmentFeedbackInput rating(AssessmentFeedbackInputRatingEnum rating);

  AssessmentFeedbackInput comment(String? comment);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentFeedbackInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentFeedbackInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AssessmentFeedbackInput call({
    AssessmentFeedbackInputRatingEnum rating,
    String? comment,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAssessmentFeedbackInput.copyWith(...)` or call `instanceOfAssessmentFeedbackInput.copyWith.fieldName(value)` for a single field.
class _$AssessmentFeedbackInputCWProxyImpl
    implements _$AssessmentFeedbackInputCWProxy {
  const _$AssessmentFeedbackInputCWProxyImpl(this._value);

  final AssessmentFeedbackInput _value;

  @override
  AssessmentFeedbackInput rating(AssessmentFeedbackInputRatingEnum rating) =>
      call(rating: rating);

  @override
  AssessmentFeedbackInput comment(String? comment) => call(comment: comment);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentFeedbackInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentFeedbackInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AssessmentFeedbackInput call({
    Object? rating = const $CopyWithPlaceholder(),
    Object? comment = const $CopyWithPlaceholder(),
  }) {
    return AssessmentFeedbackInput(
      rating: rating == const $CopyWithPlaceholder() || rating == null
          ? _value.rating
          // ignore: cast_nullable_to_non_nullable
          : rating as AssessmentFeedbackInputRatingEnum,
      comment: comment == const $CopyWithPlaceholder()
          ? _value.comment
          // ignore: cast_nullable_to_non_nullable
          : comment as String?,
    );
  }
}

extension $AssessmentFeedbackInputCopyWith on AssessmentFeedbackInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAssessmentFeedbackInput.copyWith(...)` or `instanceOfAssessmentFeedbackInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AssessmentFeedbackInputCWProxy get copyWith =>
      _$AssessmentFeedbackInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AssessmentFeedbackInput _$AssessmentFeedbackInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AssessmentFeedbackInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['rating']);
  final val = AssessmentFeedbackInput(
    rating: $checkedConvert(
      'rating',
      (v) => $enumDecode(_$AssessmentFeedbackInputRatingEnumEnumMap, v),
    ),
    comment: $checkedConvert('comment', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$AssessmentFeedbackInputToJson(
  AssessmentFeedbackInput instance,
) => <String, dynamic>{
  'rating': _$AssessmentFeedbackInputRatingEnumEnumMap[instance.rating]!,
  'comment': ?instance.comment,
};

const _$AssessmentFeedbackInputRatingEnumEnumMap = {
  AssessmentFeedbackInputRatingEnum.useful: 'useful',
  AssessmentFeedbackInputRatingEnum.notUseful: 'not_useful',
  AssessmentFeedbackInputRatingEnum.wrong: 'wrong',
};
