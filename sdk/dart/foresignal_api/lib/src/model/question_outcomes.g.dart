// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'question_outcomes.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$QuestionOutcomesCWProxy {
  QuestionOutcomes labels(List<String>? labels);

  QuestionOutcomes unit(String? unit);

  QuestionOutcomes lower(num? lower);

  QuestionOutcomes upper(num? upper);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `QuestionOutcomes(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// QuestionOutcomes(...).copyWith(id: 12, name: "My name")
  /// ```
  QuestionOutcomes call({
    List<String>? labels,
    String? unit,
    num? lower,
    num? upper,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfQuestionOutcomes.copyWith(...)` or call `instanceOfQuestionOutcomes.copyWith.fieldName(value)` for a single field.
class _$QuestionOutcomesCWProxyImpl implements _$QuestionOutcomesCWProxy {
  const _$QuestionOutcomesCWProxyImpl(this._value);

  final QuestionOutcomes _value;

  @override
  QuestionOutcomes labels(List<String>? labels) => call(labels: labels);

  @override
  QuestionOutcomes unit(String? unit) => call(unit: unit);

  @override
  QuestionOutcomes lower(num? lower) => call(lower: lower);

  @override
  QuestionOutcomes upper(num? upper) => call(upper: upper);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `QuestionOutcomes(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// QuestionOutcomes(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  QuestionOutcomes call({
    Object? labels = const $CopyWithPlaceholder(),
    Object? unit = const $CopyWithPlaceholder(),
    Object? lower = const $CopyWithPlaceholder(),
    Object? upper = const $CopyWithPlaceholder(),
  }) {
    return QuestionOutcomes(
      labels: labels == const $CopyWithPlaceholder()
          ? _value.labels
          // ignore: cast_nullable_to_non_nullable
          : labels as List<String>?,
      unit: unit == const $CopyWithPlaceholder()
          ? _value.unit
          // ignore: cast_nullable_to_non_nullable
          : unit as String?,
      lower: lower == const $CopyWithPlaceholder()
          ? _value.lower
          // ignore: cast_nullable_to_non_nullable
          : lower as num?,
      upper: upper == const $CopyWithPlaceholder()
          ? _value.upper
          // ignore: cast_nullable_to_non_nullable
          : upper as num?,
    );
  }
}

extension $QuestionOutcomesCopyWith on QuestionOutcomes {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfQuestionOutcomes.copyWith(...)` or `instanceOfQuestionOutcomes.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$QuestionOutcomesCWProxy get copyWith => _$QuestionOutcomesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

QuestionOutcomes _$QuestionOutcomesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('QuestionOutcomes', json, ($checkedConvert) {
      final val = QuestionOutcomes(
        labels: $checkedConvert(
          'labels',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        unit: $checkedConvert('unit', (v) => v as String?),
        lower: $checkedConvert('lower', (v) => v as num?),
        upper: $checkedConvert('upper', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$QuestionOutcomesToJson(QuestionOutcomes instance) =>
    <String, dynamic>{
      'labels': ?instance.labels,
      'unit': ?instance.unit,
      'lower': ?instance.lower,
      'upper': ?instance.upper,
    };
