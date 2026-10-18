// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'regional_outlook_skill.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RegionalOutlookSkillCWProxy {
  RegionalOutlookSkill evaluatedWeeks(int? evaluatedWeeks);

  RegionalOutlookSkill n(int n);

  RegionalOutlookSkill meanPinballLoss(num? meanPinballLoss);

  RegionalOutlookSkill crps(num? crps);

  RegionalOutlookSkill skillVsSeasonalNaive(num? skillVsSeasonalNaive);

  RegionalOutlookSkill skillVsClimatology(num? skillVsClimatology);

  RegionalOutlookSkill intervalCoverageP10P90(num? intervalCoverageP10P90);

  RegionalOutlookSkill label(RegionalOutlookSkillLabelEnum label);

  RegionalOutlookSkill backtest(Map<String, Object>? backtest);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalOutlookSkill(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalOutlookSkill(...).copyWith(id: 12, name: "My name")
  /// ```
  RegionalOutlookSkill call({
    int? evaluatedWeeks,
    int n,
    num? meanPinballLoss,
    num? crps,
    num? skillVsSeasonalNaive,
    num? skillVsClimatology,
    num? intervalCoverageP10P90,
    RegionalOutlookSkillLabelEnum label,
    Map<String, Object>? backtest,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRegionalOutlookSkill.copyWith(...)` or call `instanceOfRegionalOutlookSkill.copyWith.fieldName(value)` for a single field.
class _$RegionalOutlookSkillCWProxyImpl
    implements _$RegionalOutlookSkillCWProxy {
  const _$RegionalOutlookSkillCWProxyImpl(this._value);

  final RegionalOutlookSkill _value;

  @override
  RegionalOutlookSkill evaluatedWeeks(int? evaluatedWeeks) =>
      call(evaluatedWeeks: evaluatedWeeks);

  @override
  RegionalOutlookSkill n(int n) => call(n: n);

  @override
  RegionalOutlookSkill meanPinballLoss(num? meanPinballLoss) =>
      call(meanPinballLoss: meanPinballLoss);

  @override
  RegionalOutlookSkill crps(num? crps) => call(crps: crps);

  @override
  RegionalOutlookSkill skillVsSeasonalNaive(num? skillVsSeasonalNaive) =>
      call(skillVsSeasonalNaive: skillVsSeasonalNaive);

  @override
  RegionalOutlookSkill skillVsClimatology(num? skillVsClimatology) =>
      call(skillVsClimatology: skillVsClimatology);

  @override
  RegionalOutlookSkill intervalCoverageP10P90(num? intervalCoverageP10P90) =>
      call(intervalCoverageP10P90: intervalCoverageP10P90);

  @override
  RegionalOutlookSkill label(RegionalOutlookSkillLabelEnum label) =>
      call(label: label);

  @override
  RegionalOutlookSkill backtest(Map<String, Object>? backtest) =>
      call(backtest: backtest);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RegionalOutlookSkill(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RegionalOutlookSkill(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RegionalOutlookSkill call({
    Object? evaluatedWeeks = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? meanPinballLoss = const $CopyWithPlaceholder(),
    Object? crps = const $CopyWithPlaceholder(),
    Object? skillVsSeasonalNaive = const $CopyWithPlaceholder(),
    Object? skillVsClimatology = const $CopyWithPlaceholder(),
    Object? intervalCoverageP10P90 = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? backtest = const $CopyWithPlaceholder(),
  }) {
    return RegionalOutlookSkill(
      evaluatedWeeks: evaluatedWeeks == const $CopyWithPlaceholder()
          ? _value.evaluatedWeeks
          // ignore: cast_nullable_to_non_nullable
          : evaluatedWeeks as int?,
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
      meanPinballLoss: meanPinballLoss == const $CopyWithPlaceholder()
          ? _value.meanPinballLoss
          // ignore: cast_nullable_to_non_nullable
          : meanPinballLoss as num?,
      crps: crps == const $CopyWithPlaceholder()
          ? _value.crps
          // ignore: cast_nullable_to_non_nullable
          : crps as num?,
      skillVsSeasonalNaive: skillVsSeasonalNaive == const $CopyWithPlaceholder()
          ? _value.skillVsSeasonalNaive
          // ignore: cast_nullable_to_non_nullable
          : skillVsSeasonalNaive as num?,
      skillVsClimatology: skillVsClimatology == const $CopyWithPlaceholder()
          ? _value.skillVsClimatology
          // ignore: cast_nullable_to_non_nullable
          : skillVsClimatology as num?,
      intervalCoverageP10P90:
          intervalCoverageP10P90 == const $CopyWithPlaceholder()
          ? _value.intervalCoverageP10P90
          // ignore: cast_nullable_to_non_nullable
          : intervalCoverageP10P90 as num?,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as RegionalOutlookSkillLabelEnum,
      backtest: backtest == const $CopyWithPlaceholder()
          ? _value.backtest
          // ignore: cast_nullable_to_non_nullable
          : backtest as Map<String, Object>?,
    );
  }
}

extension $RegionalOutlookSkillCopyWith on RegionalOutlookSkill {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRegionalOutlookSkill.copyWith(...)` or `instanceOfRegionalOutlookSkill.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RegionalOutlookSkillCWProxy get copyWith =>
      _$RegionalOutlookSkillCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RegionalOutlookSkill _$RegionalOutlookSkillFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RegionalOutlookSkill', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['n', 'label']);
  final val = RegionalOutlookSkill(
    evaluatedWeeks: $checkedConvert(
      'evaluatedWeeks',
      (v) => (v as num?)?.toInt(),
    ),
    n: $checkedConvert('n', (v) => (v as num).toInt()),
    meanPinballLoss: $checkedConvert('meanPinballLoss', (v) => v as num?),
    crps: $checkedConvert('crps', (v) => v as num?),
    skillVsSeasonalNaive: $checkedConvert(
      'skillVsSeasonalNaive',
      (v) => v as num?,
    ),
    skillVsClimatology: $checkedConvert('skillVsClimatology', (v) => v as num?),
    intervalCoverageP10P90: $checkedConvert(
      'intervalCoverageP10P90',
      (v) => v as num?,
    ),
    label: $checkedConvert(
      'label',
      (v) => $enumDecode(_$RegionalOutlookSkillLabelEnumEnumMap, v),
    ),
    backtest: $checkedConvert(
      'backtest',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$RegionalOutlookSkillToJson(
  RegionalOutlookSkill instance,
) => <String, dynamic>{
  'evaluatedWeeks': ?instance.evaluatedWeeks,
  'n': instance.n,
  'meanPinballLoss': ?instance.meanPinballLoss,
  'crps': ?instance.crps,
  'skillVsSeasonalNaive': ?instance.skillVsSeasonalNaive,
  'skillVsClimatology': ?instance.skillVsClimatology,
  'intervalCoverageP10P90': ?instance.intervalCoverageP10P90,
  'label': _$RegionalOutlookSkillLabelEnumEnumMap[instance.label]!,
  'backtest': ?instance.backtest,
};

const _$RegionalOutlookSkillLabelEnumEnumMap = {
  RegionalOutlookSkillLabelEnum.ok: 'ok',
  RegionalOutlookSkillLabelEnum.insufficient: 'insufficient',
};
