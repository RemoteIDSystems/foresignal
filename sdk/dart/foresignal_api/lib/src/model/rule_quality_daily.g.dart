// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_quality_daily.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleQualityDailyCWProxy {
  RuleQualityDaily ruleId(String ruleId);

  RuleQualityDaily day(DateTime day);

  RuleQualityDaily fired(int fired);

  RuleQualityDaily feedback(RuleQualityDailyFeedback? feedback);

  RuleQualityDaily signals(RuleQualityDailySignals? signals);

  RuleQualityDaily medianAckSeconds(num? medianAckSeconds);

  RuleQualityDaily flaps(int? flaps);

  RuleQualityDaily suppressedByCooldown(int? suppressedByCooldown);

  RuleQualityDaily suppressedByRateCap(int? suppressedByRateCap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleQualityDaily(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleQualityDaily(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleQualityDaily call({
    String ruleId,
    DateTime day,
    int fired,
    RuleQualityDailyFeedback? feedback,
    RuleQualityDailySignals? signals,
    num? medianAckSeconds,
    int? flaps,
    int? suppressedByCooldown,
    int? suppressedByRateCap,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleQualityDaily.copyWith(...)` or call `instanceOfRuleQualityDaily.copyWith.fieldName(value)` for a single field.
class _$RuleQualityDailyCWProxyImpl implements _$RuleQualityDailyCWProxy {
  const _$RuleQualityDailyCWProxyImpl(this._value);

  final RuleQualityDaily _value;

  @override
  RuleQualityDaily ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  RuleQualityDaily day(DateTime day) => call(day: day);

  @override
  RuleQualityDaily fired(int fired) => call(fired: fired);

  @override
  RuleQualityDaily feedback(RuleQualityDailyFeedback? feedback) =>
      call(feedback: feedback);

  @override
  RuleQualityDaily signals(RuleQualityDailySignals? signals) =>
      call(signals: signals);

  @override
  RuleQualityDaily medianAckSeconds(num? medianAckSeconds) =>
      call(medianAckSeconds: medianAckSeconds);

  @override
  RuleQualityDaily flaps(int? flaps) => call(flaps: flaps);

  @override
  RuleQualityDaily suppressedByCooldown(int? suppressedByCooldown) =>
      call(suppressedByCooldown: suppressedByCooldown);

  @override
  RuleQualityDaily suppressedByRateCap(int? suppressedByRateCap) =>
      call(suppressedByRateCap: suppressedByRateCap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleQualityDaily(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleQualityDaily(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleQualityDaily call({
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? day = const $CopyWithPlaceholder(),
    Object? fired = const $CopyWithPlaceholder(),
    Object? feedback = const $CopyWithPlaceholder(),
    Object? signals = const $CopyWithPlaceholder(),
    Object? medianAckSeconds = const $CopyWithPlaceholder(),
    Object? flaps = const $CopyWithPlaceholder(),
    Object? suppressedByCooldown = const $CopyWithPlaceholder(),
    Object? suppressedByRateCap = const $CopyWithPlaceholder(),
  }) {
    return RuleQualityDaily(
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      day: day == const $CopyWithPlaceholder() || day == null
          ? _value.day
          // ignore: cast_nullable_to_non_nullable
          : day as DateTime,
      fired: fired == const $CopyWithPlaceholder() || fired == null
          ? _value.fired
          // ignore: cast_nullable_to_non_nullable
          : fired as int,
      feedback: feedback == const $CopyWithPlaceholder()
          ? _value.feedback
          // ignore: cast_nullable_to_non_nullable
          : feedback as RuleQualityDailyFeedback?,
      signals: signals == const $CopyWithPlaceholder()
          ? _value.signals
          // ignore: cast_nullable_to_non_nullable
          : signals as RuleQualityDailySignals?,
      medianAckSeconds: medianAckSeconds == const $CopyWithPlaceholder()
          ? _value.medianAckSeconds
          // ignore: cast_nullable_to_non_nullable
          : medianAckSeconds as num?,
      flaps: flaps == const $CopyWithPlaceholder()
          ? _value.flaps
          // ignore: cast_nullable_to_non_nullable
          : flaps as int?,
      suppressedByCooldown: suppressedByCooldown == const $CopyWithPlaceholder()
          ? _value.suppressedByCooldown
          // ignore: cast_nullable_to_non_nullable
          : suppressedByCooldown as int?,
      suppressedByRateCap: suppressedByRateCap == const $CopyWithPlaceholder()
          ? _value.suppressedByRateCap
          // ignore: cast_nullable_to_non_nullable
          : suppressedByRateCap as int?,
    );
  }
}

extension $RuleQualityDailyCopyWith on RuleQualityDaily {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleQualityDaily.copyWith(...)` or `instanceOfRuleQualityDaily.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleQualityDailyCWProxy get copyWith => _$RuleQualityDailyCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleQualityDaily _$RuleQualityDailyFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RuleQualityDaily', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ruleId', 'day', 'fired']);
      final val = RuleQualityDaily(
        ruleId: $checkedConvert('ruleId', (v) => v as String),
        day: $checkedConvert('day', (v) => DateTime.parse(v as String)),
        fired: $checkedConvert('fired', (v) => (v as num).toInt()),
        feedback: $checkedConvert(
          'feedback',
          (v) => v == null
              ? null
              : RuleQualityDailyFeedback.fromJson(v as Map<String, dynamic>),
        ),
        signals: $checkedConvert(
          'signals',
          (v) => v == null
              ? null
              : RuleQualityDailySignals.fromJson(v as Map<String, dynamic>),
        ),
        medianAckSeconds: $checkedConvert('medianAckSeconds', (v) => v as num?),
        flaps: $checkedConvert('flaps', (v) => (v as num?)?.toInt()),
        suppressedByCooldown: $checkedConvert(
          'suppressedByCooldown',
          (v) => (v as num?)?.toInt(),
        ),
        suppressedByRateCap: $checkedConvert(
          'suppressedByRateCap',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$RuleQualityDailyToJson(RuleQualityDaily instance) =>
    <String, dynamic>{
      'ruleId': instance.ruleId,
      'day': instance.day.toIso8601String(),
      'fired': instance.fired,
      'feedback': ?instance.feedback?.toJson(),
      'signals': ?instance.signals?.toJson(),
      'medianAckSeconds': ?instance.medianAckSeconds,
      'flaps': ?instance.flaps,
      'suppressedByCooldown': ?instance.suppressedByCooldown,
      'suppressedByRateCap': ?instance.suppressedByRateCap,
    };
