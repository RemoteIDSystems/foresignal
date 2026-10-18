// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_quality_signals.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleQualitySignalsCWProxy {
  RuleQualitySignals ACK_FAST(int? ACK_FAST);

  RuleQualitySignals SNOOZED(int? SNOOZED);

  RuleQualitySignals FLAP(int? FLAP);

  RuleQualitySignals UNOPENED(int? UNOPENED);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleQualitySignals(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleQualitySignals(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleQualitySignals call({
    int? ACK_FAST,
    int? SNOOZED,
    int? FLAP,
    int? UNOPENED,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleQualitySignals.copyWith(...)` or call `instanceOfRuleQualitySignals.copyWith.fieldName(value)` for a single field.
class _$RuleQualitySignalsCWProxyImpl implements _$RuleQualitySignalsCWProxy {
  const _$RuleQualitySignalsCWProxyImpl(this._value);

  final RuleQualitySignals _value;

  @override
  RuleQualitySignals ACK_FAST(int? ACK_FAST) => call(ACK_FAST: ACK_FAST);

  @override
  RuleQualitySignals SNOOZED(int? SNOOZED) => call(SNOOZED: SNOOZED);

  @override
  RuleQualitySignals FLAP(int? FLAP) => call(FLAP: FLAP);

  @override
  RuleQualitySignals UNOPENED(int? UNOPENED) => call(UNOPENED: UNOPENED);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleQualitySignals(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleQualitySignals(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleQualitySignals call({
    Object? ACK_FAST = const $CopyWithPlaceholder(),
    Object? SNOOZED = const $CopyWithPlaceholder(),
    Object? FLAP = const $CopyWithPlaceholder(),
    Object? UNOPENED = const $CopyWithPlaceholder(),
  }) {
    return RuleQualitySignals(
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

extension $RuleQualitySignalsCopyWith on RuleQualitySignals {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleQualitySignals.copyWith(...)` or `instanceOfRuleQualitySignals.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleQualitySignalsCWProxy get copyWith =>
      _$RuleQualitySignalsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleQualitySignals _$RuleQualitySignalsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RuleQualitySignals', json, ($checkedConvert) {
      final val = RuleQualitySignals(
        ACK_FAST: $checkedConvert('ACK_FAST', (v) => (v as num?)?.toInt()),
        SNOOZED: $checkedConvert('SNOOZED', (v) => (v as num?)?.toInt()),
        FLAP: $checkedConvert('FLAP', (v) => (v as num?)?.toInt()),
        UNOPENED: $checkedConvert('UNOPENED', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$RuleQualitySignalsToJson(RuleQualitySignals instance) =>
    <String, dynamic>{
      'ACK_FAST': ?instance.ACK_FAST,
      'SNOOZED': ?instance.SNOOZED,
      'FLAP': ?instance.FLAP,
      'UNOPENED': ?instance.UNOPENED,
    };
