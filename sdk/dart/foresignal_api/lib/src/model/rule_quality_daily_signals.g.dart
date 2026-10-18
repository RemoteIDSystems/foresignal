// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_quality_daily_signals.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleQualityDailySignalsCWProxy {
  RuleQualityDailySignals ACK_FAST(int? ACK_FAST);

  RuleQualityDailySignals SNOOZED(int? SNOOZED);

  RuleQualityDailySignals FLAP(int? FLAP);

  RuleQualityDailySignals UNOPENED(int? UNOPENED);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleQualityDailySignals(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleQualityDailySignals(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleQualityDailySignals call({
    int? ACK_FAST,
    int? SNOOZED,
    int? FLAP,
    int? UNOPENED,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleQualityDailySignals.copyWith(...)` or call `instanceOfRuleQualityDailySignals.copyWith.fieldName(value)` for a single field.
class _$RuleQualityDailySignalsCWProxyImpl
    implements _$RuleQualityDailySignalsCWProxy {
  const _$RuleQualityDailySignalsCWProxyImpl(this._value);

  final RuleQualityDailySignals _value;

  @override
  RuleQualityDailySignals ACK_FAST(int? ACK_FAST) => call(ACK_FAST: ACK_FAST);

  @override
  RuleQualityDailySignals SNOOZED(int? SNOOZED) => call(SNOOZED: SNOOZED);

  @override
  RuleQualityDailySignals FLAP(int? FLAP) => call(FLAP: FLAP);

  @override
  RuleQualityDailySignals UNOPENED(int? UNOPENED) => call(UNOPENED: UNOPENED);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleQualityDailySignals(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleQualityDailySignals(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleQualityDailySignals call({
    Object? ACK_FAST = const $CopyWithPlaceholder(),
    Object? SNOOZED = const $CopyWithPlaceholder(),
    Object? FLAP = const $CopyWithPlaceholder(),
    Object? UNOPENED = const $CopyWithPlaceholder(),
  }) {
    return RuleQualityDailySignals(
      ACK_FAST: ACK_FAST == const $CopyWithPlaceholder()
          ? _value.ACK_FAST
          // ignore: cast_nullable_to_non_nullable
          : ACK_FAST as int?,
      SNOOZED: SNOOZED == const $CopyWithPlaceholder()
          ? _value.SNOOZED
          // ignore: cast_nullable_to_non_nullable
          : SNOOZED as int?,
      FLAP: FLAP == const $CopyWithPlaceholder()
          ? _value.FLAP
          // ignore: cast_nullable_to_non_nullable
          : FLAP as int?,
      UNOPENED: UNOPENED == const $CopyWithPlaceholder()
          ? _value.UNOPENED
          // ignore: cast_nullable_to_non_nullable
          : UNOPENED as int?,
    );
  }
}

extension $RuleQualityDailySignalsCopyWith on RuleQualityDailySignals {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleQualityDailySignals.copyWith(...)` or `instanceOfRuleQualityDailySignals.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleQualityDailySignalsCWProxy get copyWith =>
      _$RuleQualityDailySignalsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleQualityDailySignals _$RuleQualityDailySignalsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RuleQualityDailySignals', json, ($checkedConvert) {
  final val = RuleQualityDailySignals(
    ACK_FAST: $checkedConvert('ACK_FAST', (v) => (v as num?)?.toInt()),
    SNOOZED: $checkedConvert('SNOOZED', (v) => (v as num?)?.toInt()),
    FLAP: $checkedConvert('FLAP', (v) => (v as num?)?.toInt()),
    UNOPENED: $checkedConvert('UNOPENED', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$RuleQualityDailySignalsToJson(
  RuleQualityDailySignals instance,
) => <String, dynamic>{
  'ACK_FAST': ?instance.ACK_FAST,
  'SNOOZED': ?instance.SNOOZED,
  'FLAP': ?instance.FLAP,
  'UNOPENED': ?instance.UNOPENED,
};
