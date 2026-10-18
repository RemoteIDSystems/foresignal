// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trigger_test_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TriggerTestResultCWProxy {
  TriggerTestResult ruleId(String ruleId);

  TriggerTestResult evaluatedFrom(DateTime? evaluatedFrom);

  TriggerTestResult matches(List<Record> matches);

  TriggerTestResult truncated(bool? truncated);

  TriggerTestResult fromSeq(int? fromSeq);

  TriggerTestResult toSeq(int? toSeq);

  TriggerTestResult matchCount(int? matchCount);

  TriggerTestResult simulated(bool? simulated);

  TriggerTestResult wouldFire(int? wouldFire);

  TriggerTestResult wouldSuppress(int? wouldSuppress);

  TriggerTestResult truncatedByRetention(bool? truncatedByRetention);

  TriggerTestResult enrich(bool? enrich);

  TriggerTestResult warnings(List<TriggerTestResultWarningsInner>? warnings);

  TriggerTestResult approximate(bool? approximate);

  TriggerTestResult approximateReason(String? approximateReason);

  TriggerTestResult windowEvaluations(
    List<TriggerTestResultWindowEvaluationsInner>? windowEvaluations,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  TriggerTestResult call({
    String ruleId,
    DateTime? evaluatedFrom,
    List<Record> matches,
    bool? truncated,
    int? fromSeq,
    int? toSeq,
    int? matchCount,
    bool? simulated,
    int? wouldFire,
    int? wouldSuppress,
    bool? truncatedByRetention,
    bool? enrich,
    List<TriggerTestResultWarningsInner>? warnings,
    bool? approximate,
    String? approximateReason,
    List<TriggerTestResultWindowEvaluationsInner>? windowEvaluations,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTriggerTestResult.copyWith(...)` or call `instanceOfTriggerTestResult.copyWith.fieldName(value)` for a single field.
class _$TriggerTestResultCWProxyImpl implements _$TriggerTestResultCWProxy {
  const _$TriggerTestResultCWProxyImpl(this._value);

  final TriggerTestResult _value;

  @override
  TriggerTestResult ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  TriggerTestResult evaluatedFrom(DateTime? evaluatedFrom) =>
      call(evaluatedFrom: evaluatedFrom);

  @override
  TriggerTestResult matches(List<Record> matches) => call(matches: matches);

  @override
  TriggerTestResult truncated(bool? truncated) => call(truncated: truncated);

  @override
  TriggerTestResult fromSeq(int? fromSeq) => call(fromSeq: fromSeq);

  @override
  TriggerTestResult toSeq(int? toSeq) => call(toSeq: toSeq);

  @override
  TriggerTestResult matchCount(int? matchCount) => call(matchCount: matchCount);

  @override
  TriggerTestResult simulated(bool? simulated) => call(simulated: simulated);

  @override
  TriggerTestResult wouldFire(int? wouldFire) => call(wouldFire: wouldFire);

  @override
  TriggerTestResult wouldSuppress(int? wouldSuppress) =>
      call(wouldSuppress: wouldSuppress);

  @override
  TriggerTestResult truncatedByRetention(bool? truncatedByRetention) =>
      call(truncatedByRetention: truncatedByRetention);

  @override
  TriggerTestResult enrich(bool? enrich) => call(enrich: enrich);

  @override
  TriggerTestResult warnings(List<TriggerTestResultWarningsInner>? warnings) =>
      call(warnings: warnings);

  @override
  TriggerTestResult approximate(bool? approximate) =>
      call(approximate: approximate);

  @override
  TriggerTestResult approximateReason(String? approximateReason) =>
      call(approximateReason: approximateReason);

  @override
  TriggerTestResult windowEvaluations(
    List<TriggerTestResultWindowEvaluationsInner>? windowEvaluations,
  ) => call(windowEvaluations: windowEvaluations);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TriggerTestResult call({
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? evaluatedFrom = const $CopyWithPlaceholder(),
    Object? matches = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? matchCount = const $CopyWithPlaceholder(),
    Object? simulated = const $CopyWithPlaceholder(),
    Object? wouldFire = const $CopyWithPlaceholder(),
    Object? wouldSuppress = const $CopyWithPlaceholder(),
    Object? truncatedByRetention = const $CopyWithPlaceholder(),
    Object? enrich = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? approximate = const $CopyWithPlaceholder(),
    Object? approximateReason = const $CopyWithPlaceholder(),
    Object? windowEvaluations = const $CopyWithPlaceholder(),
  }) {
    return TriggerTestResult(
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      evaluatedFrom: evaluatedFrom == const $CopyWithPlaceholder()
          ? _value.evaluatedFrom
          // ignore: cast_nullable_to_non_nullable
          : evaluatedFrom as DateTime?,
      matches: matches == const $CopyWithPlaceholder() || matches == null
          ? _value.matches
          // ignore: cast_nullable_to_non_nullable
          : matches as List<Record>,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      fromSeq: fromSeq == const $CopyWithPlaceholder()
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int?,
      toSeq: toSeq == const $CopyWithPlaceholder()
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int?,
      matchCount: matchCount == const $CopyWithPlaceholder()
          ? _value.matchCount
          // ignore: cast_nullable_to_non_nullable
          : matchCount as int?,
      simulated: simulated == const $CopyWithPlaceholder()
          ? _value.simulated
          // ignore: cast_nullable_to_non_nullable
          : simulated as bool?,
      wouldFire: wouldFire == const $CopyWithPlaceholder()
          ? _value.wouldFire
          // ignore: cast_nullable_to_non_nullable
          : wouldFire as int?,
      wouldSuppress: wouldSuppress == const $CopyWithPlaceholder()
          ? _value.wouldSuppress
          // ignore: cast_nullable_to_non_nullable
          : wouldSuppress as int?,
      truncatedByRetention: truncatedByRetention == const $CopyWithPlaceholder()
          ? _value.truncatedByRetention
          // ignore: cast_nullable_to_non_nullable
          : truncatedByRetention as bool?,
      enrich: enrich == const $CopyWithPlaceholder()
          ? _value.enrich
          // ignore: cast_nullable_to_non_nullable
          : enrich as bool?,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<TriggerTestResultWarningsInner>?,
      approximate: approximate == const $CopyWithPlaceholder()
          ? _value.approximate
          // ignore: cast_nullable_to_non_nullable
          : approximate as bool?,
      approximateReason: approximateReason == const $CopyWithPlaceholder()
          ? _value.approximateReason
          // ignore: cast_nullable_to_non_nullable
          : approximateReason as String?,
      windowEvaluations: windowEvaluations == const $CopyWithPlaceholder()
          ? _value.windowEvaluations
          // ignore: cast_nullable_to_non_nullable
          : windowEvaluations as List<TriggerTestResultWindowEvaluationsInner>?,
    );
  }
}

extension $TriggerTestResultCopyWith on TriggerTestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTriggerTestResult.copyWith(...)` or `instanceOfTriggerTestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TriggerTestResultCWProxy get copyWith =>
      _$TriggerTestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TriggerTestResult _$TriggerTestResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TriggerTestResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ruleId', 'matches']);
      final val = TriggerTestResult(
        ruleId: $checkedConvert('ruleId', (v) => v as String),
        evaluatedFrom: $checkedConvert(
          'evaluatedFrom',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        matches: $checkedConvert(
          'matches',
          (v) => (v as List<dynamic>)
              .map((e) => Record.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        truncated: $checkedConvert('truncated', (v) => v as bool?),
        fromSeq: $checkedConvert('fromSeq', (v) => (v as num?)?.toInt()),
        toSeq: $checkedConvert('toSeq', (v) => (v as num?)?.toInt()),
        matchCount: $checkedConvert('matchCount', (v) => (v as num?)?.toInt()),
        simulated: $checkedConvert('simulated', (v) => v as bool?),
        wouldFire: $checkedConvert('wouldFire', (v) => (v as num?)?.toInt()),
        wouldSuppress: $checkedConvert(
          'wouldSuppress',
          (v) => (v as num?)?.toInt(),
        ),
        truncatedByRetention: $checkedConvert(
          'truncatedByRetention',
          (v) => v as bool?,
        ),
        enrich: $checkedConvert('enrich', (v) => v as bool?),
        warnings: $checkedConvert(
          'warnings',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => TriggerTestResultWarningsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        approximate: $checkedConvert('approximate', (v) => v as bool?),
        approximateReason: $checkedConvert(
          'approximateReason',
          (v) => v as String?,
        ),
        windowEvaluations: $checkedConvert(
          'windowEvaluations',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => TriggerTestResultWindowEvaluationsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TriggerTestResultToJson(TriggerTestResult instance) =>
    <String, dynamic>{
      'ruleId': instance.ruleId,
      'evaluatedFrom': ?instance.evaluatedFrom?.toIso8601String(),
      'matches': instance.matches.map((e) => e.toJson()).toList(),
      'truncated': ?instance.truncated,
      'fromSeq': ?instance.fromSeq,
      'toSeq': ?instance.toSeq,
      'matchCount': ?instance.matchCount,
      'simulated': ?instance.simulated,
      'wouldFire': ?instance.wouldFire,
      'wouldSuppress': ?instance.wouldSuppress,
      'truncatedByRetention': ?instance.truncatedByRetention,
      'enrich': ?instance.enrich,
      'warnings': ?instance.warnings?.map((e) => e.toJson()).toList(),
      'approximate': ?instance.approximate,
      'approximateReason': ?instance.approximateReason,
      'windowEvaluations': ?instance.windowEvaluations
          ?.map((e) => e.toJson())
          .toList(),
    };
