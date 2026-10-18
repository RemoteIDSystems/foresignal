// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trigger_rule_all_of_feedback_counts.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TriggerRuleAllOfFeedbackCountsCWProxy {
  TriggerRuleAllOfFeedbackCounts useful(int? useful);

  TriggerRuleAllOfFeedbackCounts noise(int? noise);

  TriggerRuleAllOfFeedbackCounts late_(int? late_);

  TriggerRuleAllOfFeedbackCounts falsePositive(int? falsePositive);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerRuleAllOfFeedbackCounts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerRuleAllOfFeedbackCounts(...).copyWith(id: 12, name: "My name")
  /// ```
  TriggerRuleAllOfFeedbackCounts call({
    int? useful,
    int? noise,
    int? late_,
    int? falsePositive,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTriggerRuleAllOfFeedbackCounts.copyWith(...)` or call `instanceOfTriggerRuleAllOfFeedbackCounts.copyWith.fieldName(value)` for a single field.
class _$TriggerRuleAllOfFeedbackCountsCWProxyImpl
    implements _$TriggerRuleAllOfFeedbackCountsCWProxy {
  const _$TriggerRuleAllOfFeedbackCountsCWProxyImpl(this._value);

  final TriggerRuleAllOfFeedbackCounts _value;

  @override
  TriggerRuleAllOfFeedbackCounts useful(int? useful) => call(useful: useful);

  @override
  TriggerRuleAllOfFeedbackCounts noise(int? noise) => call(noise: noise);

  @override
  TriggerRuleAllOfFeedbackCounts late_(int? late_) => call(late_: late_);

  @override
  TriggerRuleAllOfFeedbackCounts falsePositive(int? falsePositive) =>
      call(falsePositive: falsePositive);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerRuleAllOfFeedbackCounts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerRuleAllOfFeedbackCounts(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TriggerRuleAllOfFeedbackCounts call({
    Object? useful = const $CopyWithPlaceholder(),
    Object? noise = const $CopyWithPlaceholder(),
    Object? late_ = const $CopyWithPlaceholder(),
    Object? falsePositive = const $CopyWithPlaceholder(),
  }) {
    return TriggerRuleAllOfFeedbackCounts(
      useful: useful == const $CopyWithPlaceholder()
          ? _value.useful
          // ignore: cast_nullable_to_non_nullable
          : useful as int?,
      noise: noise == const $CopyWithPlaceholder()
          ? _value.noise
          // ignore: cast_nullable_to_non_nullable
          : noise as int?,
      late_: late_ == const $CopyWithPlaceholder()
          ? _value.late_
          // ignore: cast_nullable_to_non_nullable
          : late_ as int?,
      falsePositive: falsePositive == const $CopyWithPlaceholder()
          ? _value.falsePositive
          // ignore: cast_nullable_to_non_nullable
          : falsePositive as int?,
    );
  }
}

extension $TriggerRuleAllOfFeedbackCountsCopyWith
    on TriggerRuleAllOfFeedbackCounts {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTriggerRuleAllOfFeedbackCounts.copyWith(...)` or `instanceOfTriggerRuleAllOfFeedbackCounts.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TriggerRuleAllOfFeedbackCountsCWProxy get copyWith =>
      _$TriggerRuleAllOfFeedbackCountsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TriggerRuleAllOfFeedbackCounts _$TriggerRuleAllOfFeedbackCountsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TriggerRuleAllOfFeedbackCounts', json, ($checkedConvert) {
  final val = TriggerRuleAllOfFeedbackCounts(
    useful: $checkedConvert('useful', (v) => (v as num?)?.toInt()),
    noise: $checkedConvert('noise', (v) => (v as num?)?.toInt()),
    late_: $checkedConvert('late', (v) => (v as num?)?.toInt()),
    falsePositive: $checkedConvert(
      'false_positive',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
}, fieldKeyMap: const {'late_': 'late', 'falsePositive': 'false_positive'});

Map<String, dynamic> _$TriggerRuleAllOfFeedbackCountsToJson(
  TriggerRuleAllOfFeedbackCounts instance,
) => <String, dynamic>{
  'useful': ?instance.useful,
  'noise': ?instance.noise,
  'late': ?instance.late_,
  'false_positive': ?instance.falsePositive,
};
