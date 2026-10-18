// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analog_outcomes.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalogOutcomesCWProxy {
  AnalogOutcomes durationDays(num? durationDays);

  AnalogOutcomes peakSeverity(Severity? peakSeverity);

  AnalogOutcomes followOnFamilies7d(List<String>? followOnFamilies7d);

  AnalogOutcomes followOnFamilies30d(List<String>? followOnFamilies30d);

  AnalogOutcomes hypothesisOutcome(
    AnalogOutcomesHypothesisOutcome? hypothesisOutcome,
  );

  AnalogOutcomes targetOutcomeOccurred(bool? targetOutcomeOccurred);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogOutcomes(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogOutcomes(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalogOutcomes call({
    num? durationDays,
    Severity? peakSeverity,
    List<String>? followOnFamilies7d,
    List<String>? followOnFamilies30d,
    AnalogOutcomesHypothesisOutcome? hypothesisOutcome,
    bool? targetOutcomeOccurred,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalogOutcomes.copyWith(...)` or call `instanceOfAnalogOutcomes.copyWith.fieldName(value)` for a single field.
class _$AnalogOutcomesCWProxyImpl implements _$AnalogOutcomesCWProxy {
  const _$AnalogOutcomesCWProxyImpl(this._value);

  final AnalogOutcomes _value;

  @override
  AnalogOutcomes durationDays(num? durationDays) =>
      call(durationDays: durationDays);

  @override
  AnalogOutcomes peakSeverity(Severity? peakSeverity) =>
      call(peakSeverity: peakSeverity);

  @override
  AnalogOutcomes followOnFamilies7d(List<String>? followOnFamilies7d) =>
      call(followOnFamilies7d: followOnFamilies7d);

  @override
  AnalogOutcomes followOnFamilies30d(List<String>? followOnFamilies30d) =>
      call(followOnFamilies30d: followOnFamilies30d);

  @override
  AnalogOutcomes hypothesisOutcome(
    AnalogOutcomesHypothesisOutcome? hypothesisOutcome,
  ) => call(hypothesisOutcome: hypothesisOutcome);

  @override
  AnalogOutcomes targetOutcomeOccurred(bool? targetOutcomeOccurred) =>
      call(targetOutcomeOccurred: targetOutcomeOccurred);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalogOutcomes(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalogOutcomes(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalogOutcomes call({
    Object? durationDays = const $CopyWithPlaceholder(),
    Object? peakSeverity = const $CopyWithPlaceholder(),
    Object? followOnFamilies7d = const $CopyWithPlaceholder(),
    Object? followOnFamilies30d = const $CopyWithPlaceholder(),
    Object? hypothesisOutcome = const $CopyWithPlaceholder(),
    Object? targetOutcomeOccurred = const $CopyWithPlaceholder(),
  }) {
    return AnalogOutcomes(
      durationDays: durationDays == const $CopyWithPlaceholder()
          ? _value.durationDays
          // ignore: cast_nullable_to_non_nullable
          : durationDays as num?,
      peakSeverity: peakSeverity == const $CopyWithPlaceholder()
          ? _value.peakSeverity
          // ignore: cast_nullable_to_non_nullable
          : peakSeverity as Severity?,
      followOnFamilies7d: followOnFamilies7d == const $CopyWithPlaceholder()
          ? _value.followOnFamilies7d
          // ignore: cast_nullable_to_non_nullable
          : followOnFamilies7d as List<String>?,
      followOnFamilies30d: followOnFamilies30d == const $CopyWithPlaceholder()
          ? _value.followOnFamilies30d
          // ignore: cast_nullable_to_non_nullable
          : followOnFamilies30d as List<String>?,
      hypothesisOutcome: hypothesisOutcome == const $CopyWithPlaceholder()
          ? _value.hypothesisOutcome
          // ignore: cast_nullable_to_non_nullable
          : hypothesisOutcome as AnalogOutcomesHypothesisOutcome?,
      targetOutcomeOccurred:
          targetOutcomeOccurred == const $CopyWithPlaceholder()
          ? _value.targetOutcomeOccurred
          // ignore: cast_nullable_to_non_nullable
          : targetOutcomeOccurred as bool?,
    );
  }
}

extension $AnalogOutcomesCopyWith on AnalogOutcomes {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalogOutcomes.copyWith(...)` or `instanceOfAnalogOutcomes.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalogOutcomesCWProxy get copyWith => _$AnalogOutcomesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalogOutcomes _$AnalogOutcomesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalogOutcomes', json, ($checkedConvert) {
      final val = AnalogOutcomes(
        durationDays: $checkedConvert('durationDays', (v) => v as num?),
        peakSeverity: $checkedConvert(
          'peakSeverity',
          (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
        ),
        followOnFamilies7d: $checkedConvert(
          'followOnFamilies7d',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        followOnFamilies30d: $checkedConvert(
          'followOnFamilies30d',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        hypothesisOutcome: $checkedConvert(
          'hypothesisOutcome',
          (v) => v == null
              ? null
              : AnalogOutcomesHypothesisOutcome.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
        targetOutcomeOccurred: $checkedConvert(
          'targetOutcomeOccurred',
          (v) => v as bool?,
        ),
      );
      return val;
    });

Map<String, dynamic> _$AnalogOutcomesToJson(AnalogOutcomes instance) =>
    <String, dynamic>{
      'durationDays': ?instance.durationDays,
      'peakSeverity': ?_$SeverityEnumMap[instance.peakSeverity],
      'followOnFamilies7d': ?instance.followOnFamilies7d,
      'followOnFamilies30d': ?instance.followOnFamilies30d,
      'hypothesisOutcome': ?instance.hypothesisOutcome?.toJson(),
      'targetOutcomeOccurred': ?instance.targetOutcomeOccurred,
    };

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
