// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'regional_index_def_skill.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionalIndexDefSkillCWProxy {
  RegionalIndexDefSkill evaluatedAt(DateTime evaluatedAt);

  RegionalIndexDefSkill n(int n);

  RegionalIndexDefSkill pinballLoss(num? pinballLoss);

  RegionalIndexDefSkill logScore(num? logScore);

  RegionalIndexDefSkill baselinePinballLoss(num? baselinePinballLoss);

  RegionalIndexDefSkill baselineLogScore(num? baselineLogScore);

  RegionalIndexDefSkill skillPinball(num? skillPinball);

  RegionalIndexDefSkill skillLog(num? skillLog);

  RegionalIndexDefSkill baseline(String? baseline);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDefSkill(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDefSkill(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionalIndexDefSkill call({
    DateTime evaluatedAt,
    int n,
    num? pinballLoss,
    num? logScore,
    num? baselinePinballLoss,
    num? baselineLogScore,
    num? skillPinball,
    num? skillLog,
    String? baseline,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionalIndexDefSkill.copyWith(...)` or call `instanceOfRegionalIndexDefSkill.copyWith.fieldName(value)` for a single field.
class _$RegionalIndexDefSkillCWProxyImpl
    implements _$RegionalIndexDefSkillCWProxy {
  const _$RegionalIndexDefSkillCWProxyImpl(this._value);

  final RegionalIndexDefSkill _value;

  @override
  RegionalIndexDefSkill evaluatedAt(DateTime evaluatedAt) =>
      call(evaluatedAt: evaluatedAt);

  @override
  RegionalIndexDefSkill n(int n) => call(n: n);

  @override
  RegionalIndexDefSkill pinballLoss(num? pinballLoss) =>
      call(pinballLoss: pinballLoss);

  @override
  RegionalIndexDefSkill logScore(num? logScore) => call(logScore: logScore);

  @override
  RegionalIndexDefSkill baselinePinballLoss(num? baselinePinballLoss) =>
      call(baselinePinballLoss: baselinePinballLoss);

  @override
  RegionalIndexDefSkill baselineLogScore(num? baselineLogScore) =>
      call(baselineLogScore: baselineLogScore);

  @override
  RegionalIndexDefSkill skillPinball(num? skillPinball) =>
      call(skillPinball: skillPinball);

  @override
  RegionalIndexDefSkill skillLog(num? skillLog) => call(skillLog: skillLog);

  @override
  RegionalIndexDefSkill baseline(String? baseline) => call(baseline: baseline);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalIndexDefSkill(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalIndexDefSkill(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionalIndexDefSkill call({
    Object? evaluatedAt = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? pinballLoss = const $CopyWithPlaceholder(),
    Object? logScore = const $CopyWithPlaceholder(),
    Object? baselinePinballLoss = const $CopyWithPlaceholder(),
    Object? baselineLogScore = const $CopyWithPlaceholder(),
    Object? skillPinball = const $CopyWithPlaceholder(),
    Object? skillLog = const $CopyWithPlaceholder(),
    Object? baseline = const $CopyWithPlaceholder(),
  }) {
    return RegionalIndexDefSkill(
      evaluatedAt:
          evaluatedAt == const $CopyWithPlaceholder() || evaluatedAt == null
          ? _value.evaluatedAt
          // ignore: cast_nullable_to_non_nullable
          : evaluatedAt as DateTime,
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
      pinballLoss: pinballLoss == const $CopyWithPlaceholder()
          ? _value.pinballLoss
          // ignore: cast_nullable_to_non_nullable
          : pinballLoss as num?,
      logScore: logScore == const $CopyWithPlaceholder()
          ? _value.logScore
          // ignore: cast_nullable_to_non_nullable
          : logScore as num?,
      baselinePinballLoss: baselinePinballLoss == const $CopyWithPlaceholder()
          ? _value.baselinePinballLoss
          // ignore: cast_nullable_to_non_nullable
          : baselinePinballLoss as num?,
      baselineLogScore: baselineLogScore == const $CopyWithPlaceholder()
          ? _value.baselineLogScore
          // ignore: cast_nullable_to_non_nullable
          : baselineLogScore as num?,
      skillPinball: skillPinball == const $CopyWithPlaceholder()
          ? _value.skillPinball
          // ignore: cast_nullable_to_non_nullable
          : skillPinball as num?,
      skillLog: skillLog == const $CopyWithPlaceholder()
          ? _value.skillLog
          // ignore: cast_nullable_to_non_nullable
          : skillLog as num?,
      baseline: baseline == const $CopyWithPlaceholder()
          ? _value.baseline
          // ignore: cast_nullable_to_non_nullable
          : baseline as String?,
    );
  }
}

extension $RegionalIndexDefSkillCopyWith on RegionalIndexDefSkill {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionalIndexDefSkill.copyWith(...)` or `instanceOfRegionalIndexDefSkill.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionalIndexDefSkillCWProxy get copyWith =>
      _$RegionalIndexDefSkillCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionalIndexDefSkill _$RegionalIndexDefSkillFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RegionalIndexDefSkill', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['evaluatedAt', 'n']);
  final val = RegionalIndexDefSkill(
    evaluatedAt: $checkedConvert(
      'evaluatedAt',
      (v) => DateTime.parse(v as String),
    ),
    n: $checkedConvert('n', (v) => (v as num).toInt()),
    pinballLoss: $checkedConvert('pinballLoss', (v) => v as num?),
    logScore: $checkedConvert('logScore', (v) => v as num?),
    baselinePinballLoss: $checkedConvert(
      'baselinePinballLoss',
      (v) => v as num?,
    ),
    baselineLogScore: $checkedConvert('baselineLogScore', (v) => v as num?),
    skillPinball: $checkedConvert('skillPinball', (v) => v as num?),
    skillLog: $checkedConvert('skillLog', (v) => v as num?),
    baseline: $checkedConvert(
      'baseline',
      (v) => v as String? ?? 'seasonal-naive outlook',
    ),
  );
  return val;
});

Map<String, dynamic> _$RegionalIndexDefSkillToJson(
  RegionalIndexDefSkill instance,
) => <String, dynamic>{
  'evaluatedAt': instance.evaluatedAt.toIso8601String(),
  'n': instance.n,
  'pinballLoss': ?instance.pinballLoss,
  'logScore': ?instance.logScore,
  'baselinePinballLoss': ?instance.baselinePinballLoss,
  'baselineLogScore': ?instance.baselineLogScore,
  'skillPinball': ?instance.skillPinball,
  'skillLog': ?instance.skillLog,
  'baseline': ?instance.baseline,
};
