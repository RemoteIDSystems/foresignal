// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'assessment_feedback.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AssessmentFeedbackCWProxy {
  AssessmentFeedback id(String id);

  AssessmentFeedback assessmentId(String assessmentId);

  AssessmentFeedback rating(AssessmentFeedbackRatingEnum rating);

  AssessmentFeedback comment(String? comment);

  AssessmentFeedback by(String by);

  AssessmentFeedback at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentFeedback(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentFeedback(...).copyWith(id: 12, name: "My name")
  /// ```
  AssessmentFeedback call({
    String id,
    String assessmentId,
    AssessmentFeedbackRatingEnum rating,
    String? comment,
    String by,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAssessmentFeedback.copyWith(...)` or call `instanceOfAssessmentFeedback.copyWith.fieldName(value)` for a single field.
class _$AssessmentFeedbackCWProxyImpl implements _$AssessmentFeedbackCWProxy {
  const _$AssessmentFeedbackCWProxyImpl(this._value);

  final AssessmentFeedback _value;

  @override
  AssessmentFeedback id(String id) => call(id: id);

  @override
  AssessmentFeedback assessmentId(String assessmentId) =>
      call(assessmentId: assessmentId);

  @override
  AssessmentFeedback rating(AssessmentFeedbackRatingEnum rating) =>
      call(rating: rating);

  @override
  AssessmentFeedback comment(String? comment) => call(comment: comment);

  @override
  AssessmentFeedback by(String by) => call(by: by);

  @override
  AssessmentFeedback at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AssessmentFeedback(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AssessmentFeedback(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AssessmentFeedback call({
    Object? id = const $CopyWithPlaceholder(),
    Object? assessmentId = const $CopyWithPlaceholder(),
    Object? rating = const $CopyWithPlaceholder(),
    Object? comment = const $CopyWithPlaceholder(),
    Object? by = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return AssessmentFeedback(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      assessmentId:
          assessmentId == const $CopyWithPlaceholder() || assessmentId == null
          ? _value.assessmentId
          // ignore: cast_nullable_to_non_nullable
          : assessmentId as String,
      rating: rating == const $CopyWithPlaceholder() || rating == null
          ? _value.rating
          // ignore: cast_nullable_to_non_nullable
          : rating as AssessmentFeedbackRatingEnum,
      comment: comment == const $CopyWithPlaceholder()
          ? _value.comment
          // ignore: cast_nullable_to_non_nullable
          : comment as String?,
      by: by == const $CopyWithPlaceholder() || by == null
          ? _value.by
          // ignore: cast_nullable_to_non_nullable
          : by as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $AssessmentFeedbackCopyWith on AssessmentFeedback {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAssessmentFeedback.copyWith(...)` or `instanceOfAssessmentFeedback.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AssessmentFeedbackCWProxy get copyWith =>
      _$AssessmentFeedbackCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AssessmentFeedback _$AssessmentFeedbackFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AssessmentFeedback', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'assessmentId', 'rating', 'by', 'at'],
      );
      final val = AssessmentFeedback(
        id: $checkedConvert('id', (v) => v as String),
        assessmentId: $checkedConvert('assessmentId', (v) => v as String),
        rating: $checkedConvert(
          'rating',
          (v) => $enumDecode(_$AssessmentFeedbackRatingEnumEnumMap, v),
        ),
        comment: $checkedConvert('comment', (v) => v as String?),
        by: $checkedConvert('by', (v) => v as String),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$AssessmentFeedbackToJson(AssessmentFeedback instance) =>
    <String, dynamic>{
      'id': instance.id,
      'assessmentId': instance.assessmentId,
      'rating': _$AssessmentFeedbackRatingEnumEnumMap[instance.rating]!,
      'comment': ?instance.comment,
      'by': instance.by,
      'at': instance.at.toIso8601String(),
    };

const _$AssessmentFeedbackRatingEnumEnumMap = {
  AssessmentFeedbackRatingEnum.useful: 'useful',
  AssessmentFeedbackRatingEnum.notUseful: 'not_useful',
  AssessmentFeedbackRatingEnum.wrong: 'wrong',
};
