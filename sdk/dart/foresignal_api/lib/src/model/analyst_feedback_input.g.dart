// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_feedback_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystFeedbackInputCWProxy {
  AnalystFeedbackInput rating(AnalystFeedbackInputRatingEnum rating);

  AnalystFeedbackInput comment(String? comment);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystFeedbackInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystFeedbackInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystFeedbackInput call({
    AnalystFeedbackInputRatingEnum rating,
    String? comment,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystFeedbackInput.copyWith(...)` or call `instanceOfAnalystFeedbackInput.copyWith.fieldName(value)` for a single field.
class _$AnalystFeedbackInputCWProxyImpl
    implements _$AnalystFeedbackInputCWProxy {
  const _$AnalystFeedbackInputCWProxyImpl(this._value);

  final AnalystFeedbackInput _value;

  @override
  AnalystFeedbackInput rating(AnalystFeedbackInputRatingEnum rating) =>
      call(rating: rating);

  @override
  AnalystFeedbackInput comment(String? comment) => call(comment: comment);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystFeedbackInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystFeedbackInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystFeedbackInput call({
    Object? rating = const $CopyWithPlaceholder(),
    Object? comment = const $CopyWithPlaceholder(),
  }) {
    return AnalystFeedbackInput(
      rating: rating == const $CopyWithPlaceholder() || rating == null
          ? _value.rating
          // ignore: cast_nullable_to_non_nullable
          : rating as AnalystFeedbackInputRatingEnum,
      comment: comment == const $CopyWithPlaceholder()
          ? _value.comment
          // ignore: cast_nullable_to_non_nullable
          : comment as String?,
    );
  }
}

extension $AnalystFeedbackInputCopyWith on AnalystFeedbackInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystFeedbackInput.copyWith(...)` or `instanceOfAnalystFeedbackInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystFeedbackInputCWProxy get copyWith =>
      _$AnalystFeedbackInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystFeedbackInput _$AnalystFeedbackInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalystFeedbackInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['rating']);
  final val = AnalystFeedbackInput(
    rating: $checkedConvert(
      'rating',
      (v) => $enumDecode(_$AnalystFeedbackInputRatingEnumEnumMap, v),
    ),
    comment: $checkedConvert('comment', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$AnalystFeedbackInputToJson(
  AnalystFeedbackInput instance,
) => <String, dynamic>{
  'rating': _$AnalystFeedbackInputRatingEnumEnumMap[instance.rating]!,
  'comment': ?instance.comment,
};

const _$AnalystFeedbackInputRatingEnumEnumMap = {
  AnalystFeedbackInputRatingEnum.useful: 'useful',
  AnalystFeedbackInputRatingEnum.notUseful: 'not_useful',
  AnalystFeedbackInputRatingEnum.wrong: 'wrong',
};
