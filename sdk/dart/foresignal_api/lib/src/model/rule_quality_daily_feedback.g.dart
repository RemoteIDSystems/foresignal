// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_quality_daily_feedback.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleQualityDailyFeedbackCWProxy {
  RuleQualityDailyFeedback useful(int? useful);

  RuleQualityDailyFeedback noise(int? noise);

  RuleQualityDailyFeedback late_(int? late_);

  RuleQualityDailyFeedback falsePositive(int? falsePositive);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleQualityDailyFeedback(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleQualityDailyFeedback(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleQualityDailyFeedback call({
    int? useful,
    int? noise,
    int? late_,
    int? falsePositive,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleQualityDailyFeedback.copyWith(...)` or call `instanceOfRuleQualityDailyFeedback.copyWith.fieldName(value)` for a single field.
class _$RuleQualityDailyFeedbackCWProxyImpl
    implements _$RuleQualityDailyFeedbackCWProxy {
  const _$RuleQualityDailyFeedbackCWProxyImpl(this._value);

  final RuleQualityDailyFeedback _value;

  @override
  RuleQualityDailyFeedback useful(int? useful) => call(useful: useful);

  @override
  RuleQualityDailyFeedback noise(int? noise) => call(noise: noise);

  @override
  RuleQualityDailyFeedback late_(int? late_) => call(late_: late_);

  @override
  RuleQualityDailyFeedback falsePositive(int? falsePositive) =>
      call(falsePositive: falsePositive);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleQualityDailyFeedback(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleQualityDailyFeedback(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleQualityDailyFeedback call({
    Object? useful = const $CopyWithPlaceholder(),
    Object? noise = const $CopyWithPlaceholder(),
    Object? late_ = const $CopyWithPlaceholder(),
    Object? falsePositive = const $CopyWithPlaceholder(),
  }) {
    return RuleQualityDailyFeedback(
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

extension $RuleQualityDailyFeedbackCopyWith on RuleQualityDailyFeedback {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleQualityDailyFeedback.copyWith(...)` or `instanceOfRuleQualityDailyFeedback.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleQualityDailyFeedbackCWProxy get copyWith =>
      _$RuleQualityDailyFeedbackCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleQualityDailyFeedback _$RuleQualityDailyFeedbackFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RuleQualityDailyFeedback', json, ($checkedConvert) {
  final val = RuleQualityDailyFeedback(
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

Map<String, dynamic> _$RuleQualityDailyFeedbackToJson(
  RuleQualityDailyFeedback instance,
) => <String, dynamic>{
  'useful': ?instance.useful,
  'noise': ?instance.noise,
  'late': ?instance.late_,
  'false_positive': ?instance.falsePositive,
};
