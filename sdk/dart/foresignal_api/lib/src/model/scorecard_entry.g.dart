// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scorecard_entry.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ScorecardEntryCWProxy {
  ScorecardEntry forecasterId(String forecasterId);

  ScorecardEntry forecasterType(
    ScorecardEntryForecasterTypeEnum? forecasterType,
  );

  ScorecardEntry category(String? category);

  ScorecardEntry nResolved(int nResolved);

  ScorecardEntry nForecasts(int? nForecasts);

  ScorecardEntry label(ScorecardEntryLabelEnum label);

  ScorecardEntry brier(num? brier);

  ScorecardEntry logScore(num? logScore);

  ScorecardEntry rps(num? rps);

  ScorecardEntry crps(num? crps);

  ScorecardEntry timeAveragedBrier(num? timeAveragedBrier);

  ScorecardEntry intervalCoverage(
    ScorecardEntryIntervalCoverage? intervalCoverage,
  );

  ScorecardEntry murphy(ScorecardEntryMurphy? murphy);

  ScorecardEntry skill(ScorecardEntrySkill? skill);

  ScorecardEntry bias(ScorecardEntryBias? bias);

  ScorecardEntry recalibration(ScorecardEntryRecalibration? recalibration);

  ScorecardEntry ensembleWeight(num? ensembleWeight);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ScorecardEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ScorecardEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  ScorecardEntry call({
    String forecasterId,
    ScorecardEntryForecasterTypeEnum? forecasterType,
    String? category,
    int nResolved,
    int? nForecasts,
    ScorecardEntryLabelEnum label,
    num? brier,
    num? logScore,
    num? rps,
    num? crps,
    num? timeAveragedBrier,
    ScorecardEntryIntervalCoverage? intervalCoverage,
    ScorecardEntryMurphy? murphy,
    ScorecardEntrySkill? skill,
    ScorecardEntryBias? bias,
    ScorecardEntryRecalibration? recalibration,
    num? ensembleWeight,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfScorecardEntry.copyWith(...)` or call `instanceOfScorecardEntry.copyWith.fieldName(value)` for a single field.
class _$ScorecardEntryCWProxyImpl implements _$ScorecardEntryCWProxy {
  const _$ScorecardEntryCWProxyImpl(this._value);

  final ScorecardEntry _value;

  @override
  ScorecardEntry forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  ScorecardEntry forecasterType(
    ScorecardEntryForecasterTypeEnum? forecasterType,
  ) => call(forecasterType: forecasterType);

  @override
  ScorecardEntry category(String? category) => call(category: category);

  @override
  ScorecardEntry nResolved(int nResolved) => call(nResolved: nResolved);

  @override
  ScorecardEntry nForecasts(int? nForecasts) => call(nForecasts: nForecasts);

  @override
  ScorecardEntry label(ScorecardEntryLabelEnum label) => call(label: label);

  @override
  ScorecardEntry brier(num? brier) => call(brier: brier);

  @override
  ScorecardEntry logScore(num? logScore) => call(logScore: logScore);

  @override
  ScorecardEntry rps(num? rps) => call(rps: rps);

  @override
  ScorecardEntry crps(num? crps) => call(crps: crps);

  @override
  ScorecardEntry timeAveragedBrier(num? timeAveragedBrier) =>
      call(timeAveragedBrier: timeAveragedBrier);

  @override
  ScorecardEntry intervalCoverage(
    ScorecardEntryIntervalCoverage? intervalCoverage,
  ) => call(intervalCoverage: intervalCoverage);

  @override
  ScorecardEntry murphy(ScorecardEntryMurphy? murphy) => call(murphy: murphy);

  @override
  ScorecardEntry skill(ScorecardEntrySkill? skill) => call(skill: skill);

  @override
  ScorecardEntry bias(ScorecardEntryBias? bias) => call(bias: bias);

  @override
  ScorecardEntry recalibration(ScorecardEntryRecalibration? recalibration) =>
      call(recalibration: recalibration);

  @override
  ScorecardEntry ensembleWeight(num? ensembleWeight) =>
      call(ensembleWeight: ensembleWeight);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ScorecardEntry(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ScorecardEntry(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ScorecardEntry call({
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? forecasterType = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? nResolved = const $CopyWithPlaceholder(),
    Object? nForecasts = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? brier = const $CopyWithPlaceholder(),
    Object? logScore = const $CopyWithPlaceholder(),
    Object? rps = const $CopyWithPlaceholder(),
    Object? crps = const $CopyWithPlaceholder(),
    Object? timeAveragedBrier = const $CopyWithPlaceholder(),
    Object? intervalCoverage = const $CopyWithPlaceholder(),
    Object? murphy = const $CopyWithPlaceholder(),
    Object? skill = const $CopyWithPlaceholder(),
    Object? bias = const $CopyWithPlaceholder(),
    Object? recalibration = const $CopyWithPlaceholder(),
    Object? ensembleWeight = const $CopyWithPlaceholder(),
  }) {
    return ScorecardEntry(
      forecasterId:
          forecasterId == const $CopyWithPlaceholder() || forecasterId == null
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String,
      forecasterType: forecasterType == const $CopyWithPlaceholder()
          ? _value.forecasterType
          // ignore: cast_nullable_to_non_nullable
          : forecasterType as ScorecardEntryForecasterTypeEnum?,
      category: category == const $CopyWithPlaceholder()
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String?,
      nResolved: nResolved == const $CopyWithPlaceholder() || nResolved == null
          ? _value.nResolved
          // ignore: cast_nullable_to_non_nullable
          : nResolved as int,
      nForecasts: nForecasts == const $CopyWithPlaceholder()
          ? _value.nForecasts
          // ignore: cast_nullable_to_non_nullable
          : nForecasts as int?,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as ScorecardEntryLabelEnum,
      brier: brier == const $CopyWithPlaceholder()
          ? _value.brier
          // ignore: cast_nullable_to_non_nullable
          : brier as num?,
      logScore: logScore == const $CopyWithPlaceholder()
          ? _value.logScore
          // ignore: cast_nullable_to_non_nullable
          : logScore as num?,
      rps: rps == const $CopyWithPlaceholder()
          ? _value.rps
          // ignore: cast_nullable_to_non_nullable
          : rps as num?,
      crps: crps == const $CopyWithPlaceholder()
          ? _value.crps
          // ignore: cast_nullable_to_non_nullable
          : crps as num?,
      timeAveragedBrier: timeAveragedBrier == const $CopyWithPlaceholder()
          ? _value.timeAveragedBrier
          // ignore: cast_nullable_to_non_nullable
          : timeAveragedBrier as num?,
      intervalCoverage: intervalCoverage == const $CopyWithPlaceholder()
          ? _value.intervalCoverage
          // ignore: cast_nullable_to_non_nullable
          : intervalCoverage as ScorecardEntryIntervalCoverage?,
      murphy: murphy == const $CopyWithPlaceholder()
          ? _value.murphy
          // ignore: cast_nullable_to_non_nullable
          : murphy as ScorecardEntryMurphy?,
      skill: skill == const $CopyWithPlaceholder()
          ? _value.skill
          // ignore: cast_nullable_to_non_nullable
          : skill as ScorecardEntrySkill?,
      bias: bias == const $CopyWithPlaceholder()
          ? _value.bias
          // ignore: cast_nullable_to_non_nullable
          : bias as ScorecardEntryBias?,
      recalibration: recalibration == const $CopyWithPlaceholder()
          ? _value.recalibration
          // ignore: cast_nullable_to_non_nullable
          : recalibration as ScorecardEntryRecalibration?,
      ensembleWeight: ensembleWeight == const $CopyWithPlaceholder()
          ? _value.ensembleWeight
          // ignore: cast_nullable_to_non_nullable
          : ensembleWeight as num?,
    );
  }
}

extension $ScorecardEntryCopyWith on ScorecardEntry {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfScorecardEntry.copyWith(...)` or `instanceOfScorecardEntry.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ScorecardEntryCWProxy get copyWith => _$ScorecardEntryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ScorecardEntry _$ScorecardEntryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ScorecardEntry', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['forecasterId', 'nResolved', 'label']);
  final val = ScorecardEntry(
    forecasterId: $checkedConvert('forecasterId', (v) => v as String),
    forecasterType: $checkedConvert(
      'forecasterType',
      (v) => $enumDecodeNullable(_$ScorecardEntryForecasterTypeEnumEnumMap, v),
    ),
    category: $checkedConvert('category', (v) => v as String?),
    nResolved: $checkedConvert('nResolved', (v) => (v as num).toInt()),
    nForecasts: $checkedConvert('nForecasts', (v) => (v as num?)?.toInt()),
    label: $checkedConvert(
      'label',
      (v) => $enumDecode(_$ScorecardEntryLabelEnumEnumMap, v),
    ),
    brier: $checkedConvert('brier', (v) => v as num?),
    logScore: $checkedConvert('logScore', (v) => v as num?),
    rps: $checkedConvert('rps', (v) => v as num?),
    crps: $checkedConvert('crps', (v) => v as num?),
    timeAveragedBrier: $checkedConvert('timeAveragedBrier', (v) => v as num?),
    intervalCoverage: $checkedConvert(
      'intervalCoverage',
      (v) => v == null
          ? null
          : ScorecardEntryIntervalCoverage.fromJson(v as Map<String, dynamic>),
    ),
    murphy: $checkedConvert(
      'murphy',
      (v) => v == null
          ? null
          : ScorecardEntryMurphy.fromJson(v as Map<String, dynamic>),
    ),
    skill: $checkedConvert(
      'skill',
      (v) => v == null
          ? null
          : ScorecardEntrySkill.fromJson(v as Map<String, dynamic>),
    ),
    bias: $checkedConvert(
      'bias',
      (v) => v == null
          ? null
          : ScorecardEntryBias.fromJson(v as Map<String, dynamic>),
    ),
    recalibration: $checkedConvert(
      'recalibration',
      (v) => v == null
          ? null
          : ScorecardEntryRecalibration.fromJson(v as Map<String, dynamic>),
    ),
    ensembleWeight: $checkedConvert('ensembleWeight', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$ScorecardEntryToJson(ScorecardEntry instance) =>
    <String, dynamic>{
      'forecasterId': instance.forecasterId,
      'forecasterType':
          ?_$ScorecardEntryForecasterTypeEnumEnumMap[instance.forecasterType],
      'category': ?instance.category,
      'nResolved': instance.nResolved,
      'nForecasts': ?instance.nForecasts,
      'label': _$ScorecardEntryLabelEnumEnumMap[instance.label]!,
      'brier': ?instance.brier,
      'logScore': ?instance.logScore,
      'rps': ?instance.rps,
      'crps': ?instance.crps,
      'timeAveragedBrier': ?instance.timeAveragedBrier,
      'intervalCoverage': ?instance.intervalCoverage?.toJson(),
      'murphy': ?instance.murphy?.toJson(),
      'skill': ?instance.skill?.toJson(),
      'bias': ?instance.bias?.toJson(),
      'recalibration': ?instance.recalibration?.toJson(),
      'ensembleWeight': ?instance.ensembleWeight,
    };

const _$ScorecardEntryForecasterTypeEnumEnumMap = {
  ScorecardEntryForecasterTypeEnum.baseRate: 'base-rate',
  ScorecardEntryForecasterTypeEnum.climatology: 'climatology',
  ScorecardEntryForecasterTypeEnum.persistence: 'persistence',
  ScorecardEntryForecasterTypeEnum.statistical: 'statistical',
  ScorecardEntryForecasterTypeEnum.precursor: 'precursor',
  ScorecardEntryForecasterTypeEnum.analog: 'analog',
  ScorecardEntryForecasterTypeEnum.impact: 'impact',
  ScorecardEntryForecasterTypeEnum.indicatorBayes: 'indicator-bayes',
  ScorecardEntryForecasterTypeEnum.llm: 'llm',
  ScorecardEntryForecasterTypeEnum.operator_: 'operator',
  ScorecardEntryForecasterTypeEnum.external_: 'external',
  ScorecardEntryForecasterTypeEnum.ensemble: 'ensemble',
};

const _$ScorecardEntryLabelEnumEnumMap = {
  ScorecardEntryLabelEnum.ok: 'ok',
  ScorecardEntryLabelEnum.insufficient: 'insufficient',
};
