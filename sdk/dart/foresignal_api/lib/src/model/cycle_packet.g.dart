// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cycle_packet.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CyclePacketCWProxy {
  CyclePacket version(String version);

  CyclePacket since(DateTime since);

  CyclePacket until(DateTime until);

  CyclePacket firstRun(bool? firstRun);

  CyclePacket situations(CyclePacketSituations? situations);

  CyclePacket hypotheses(CyclePacketHypotheses? hypotheses);

  CyclePacket findings(List<CyclePacketItem>? findings);

  CyclePacket forecasts(CyclePacketForecasts? forecasts);

  CyclePacket outlooks(CyclePacketOutlooks? outlooks);

  CyclePacket coverage(CyclePacketCoverage? coverage);

  CyclePacket collectionGaps(List<CollectionGap>? collectionGaps);

  CyclePacket verdicts(List<CyclePacketItem>? verdicts);

  CyclePacket previousJudgments(List<KeyJudgment>? previousJudgments);

  CyclePacket truncatedForLlm(Map<String, int>? truncatedForLlm);

  CyclePacket social(SocialVersusObserved? social);

  CyclePacket cadence(CyclePacketCadenceEnum? cadence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CyclePacket(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CyclePacket(...).copyWith(id: 12, name: "My name")
  /// ```
  CyclePacket call({
    String version,
    DateTime since,
    DateTime until,
    bool? firstRun,
    CyclePacketSituations? situations,
    CyclePacketHypotheses? hypotheses,
    List<CyclePacketItem>? findings,
    CyclePacketForecasts? forecasts,
    CyclePacketOutlooks? outlooks,
    CyclePacketCoverage? coverage,
    List<CollectionGap>? collectionGaps,
    List<CyclePacketItem>? verdicts,
    List<KeyJudgment>? previousJudgments,
    Map<String, int>? truncatedForLlm,
    SocialVersusObserved? social,
    CyclePacketCadenceEnum? cadence,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCyclePacket.copyWith(...)` or call `instanceOfCyclePacket.copyWith.fieldName(value)` for a single field.
class _$CyclePacketCWProxyImpl implements _$CyclePacketCWProxy {
  const _$CyclePacketCWProxyImpl(this._value);

  final CyclePacket _value;

  @override
  CyclePacket version(String version) => call(version: version);

  @override
  CyclePacket since(DateTime since) => call(since: since);

  @override
  CyclePacket until(DateTime until) => call(until: until);

  @override
  CyclePacket firstRun(bool? firstRun) => call(firstRun: firstRun);

  @override
  CyclePacket situations(CyclePacketSituations? situations) =>
      call(situations: situations);

  @override
  CyclePacket hypotheses(CyclePacketHypotheses? hypotheses) =>
      call(hypotheses: hypotheses);

  @override
  CyclePacket findings(List<CyclePacketItem>? findings) =>
      call(findings: findings);

  @override
  CyclePacket forecasts(CyclePacketForecasts? forecasts) =>
      call(forecasts: forecasts);

  @override
  CyclePacket outlooks(CyclePacketOutlooks? outlooks) =>
      call(outlooks: outlooks);

  @override
  CyclePacket coverage(CyclePacketCoverage? coverage) =>
      call(coverage: coverage);

  @override
  CyclePacket collectionGaps(List<CollectionGap>? collectionGaps) =>
      call(collectionGaps: collectionGaps);

  @override
  CyclePacket verdicts(List<CyclePacketItem>? verdicts) =>
      call(verdicts: verdicts);

  @override
  CyclePacket previousJudgments(List<KeyJudgment>? previousJudgments) =>
      call(previousJudgments: previousJudgments);

  @override
  CyclePacket truncatedForLlm(Map<String, int>? truncatedForLlm) =>
      call(truncatedForLlm: truncatedForLlm);

  @override
  CyclePacket social(SocialVersusObserved? social) => call(social: social);

  @override
  CyclePacket cadence(CyclePacketCadenceEnum? cadence) =>
      call(cadence: cadence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CyclePacket(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CyclePacket(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CyclePacket call({
    Object? version = const $CopyWithPlaceholder(),
    Object? since = const $CopyWithPlaceholder(),
    Object? until = const $CopyWithPlaceholder(),
    Object? firstRun = const $CopyWithPlaceholder(),
    Object? situations = const $CopyWithPlaceholder(),
    Object? hypotheses = const $CopyWithPlaceholder(),
    Object? findings = const $CopyWithPlaceholder(),
    Object? forecasts = const $CopyWithPlaceholder(),
    Object? outlooks = const $CopyWithPlaceholder(),
    Object? coverage = const $CopyWithPlaceholder(),
    Object? collectionGaps = const $CopyWithPlaceholder(),
    Object? verdicts = const $CopyWithPlaceholder(),
    Object? previousJudgments = const $CopyWithPlaceholder(),
    Object? truncatedForLlm = const $CopyWithPlaceholder(),
    Object? social = const $CopyWithPlaceholder(),
    Object? cadence = const $CopyWithPlaceholder(),
  }) {
    return CyclePacket(
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
      since: since == const $CopyWithPlaceholder() || since == null
          ? _value.since
          // ignore: cast_nullable_to_non_nullable
          : since as DateTime,
      until: until == const $CopyWithPlaceholder() || until == null
          ? _value.until
          // ignore: cast_nullable_to_non_nullable
          : until as DateTime,
      firstRun: firstRun == const $CopyWithPlaceholder()
          ? _value.firstRun
          // ignore: cast_nullable_to_non_nullable
          : firstRun as bool?,
      situations: situations == const $CopyWithPlaceholder()
          ? _value.situations
          // ignore: cast_nullable_to_non_nullable
          : situations as CyclePacketSituations?,
      hypotheses: hypotheses == const $CopyWithPlaceholder()
          ? _value.hypotheses
          // ignore: cast_nullable_to_non_nullable
          : hypotheses as CyclePacketHypotheses?,
      findings: findings == const $CopyWithPlaceholder()
          ? _value.findings
          // ignore: cast_nullable_to_non_nullable
          : findings as List<CyclePacketItem>?,
      forecasts: forecasts == const $CopyWithPlaceholder()
          ? _value.forecasts
          // ignore: cast_nullable_to_non_nullable
          : forecasts as CyclePacketForecasts?,
      outlooks: outlooks == const $CopyWithPlaceholder()
          ? _value.outlooks
          // ignore: cast_nullable_to_non_nullable
          : outlooks as CyclePacketOutlooks?,
      coverage: coverage == const $CopyWithPlaceholder()
          ? _value.coverage
          // ignore: cast_nullable_to_non_nullable
          : coverage as CyclePacketCoverage?,
      collectionGaps: collectionGaps == const $CopyWithPlaceholder()
          ? _value.collectionGaps
          // ignore: cast_nullable_to_non_nullable
          : collectionGaps as List<CollectionGap>?,
      verdicts: verdicts == const $CopyWithPlaceholder()
          ? _value.verdicts
          // ignore: cast_nullable_to_non_nullable
          : verdicts as List<CyclePacketItem>?,
      previousJudgments: previousJudgments == const $CopyWithPlaceholder()
          ? _value.previousJudgments
          // ignore: cast_nullable_to_non_nullable
          : previousJudgments as List<KeyJudgment>?,
      truncatedForLlm: truncatedForLlm == const $CopyWithPlaceholder()
          ? _value.truncatedForLlm
          // ignore: cast_nullable_to_non_nullable
          : truncatedForLlm as Map<String, int>?,
      social: social == const $CopyWithPlaceholder()
          ? _value.social
          // ignore: cast_nullable_to_non_nullable
          : social as SocialVersusObserved?,
      cadence: cadence == const $CopyWithPlaceholder()
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as CyclePacketCadenceEnum?,
    );
  }
}

extension $CyclePacketCopyWith on CyclePacket {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCyclePacket.copyWith(...)` or `instanceOfCyclePacket.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CyclePacketCWProxy get copyWith => _$CyclePacketCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CyclePacket _$CyclePacketFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CyclePacket', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['version', 'since', 'until']);
      final val = CyclePacket(
        version: $checkedConvert('version', (v) => v as String),
        since: $checkedConvert('since', (v) => DateTime.parse(v as String)),
        until: $checkedConvert('until', (v) => DateTime.parse(v as String)),
        firstRun: $checkedConvert('firstRun', (v) => v as bool?),
        situations: $checkedConvert(
          'situations',
          (v) => v == null
              ? null
              : CyclePacketSituations.fromJson(v as Map<String, dynamic>),
        ),
        hypotheses: $checkedConvert(
          'hypotheses',
          (v) => v == null
              ? null
              : CyclePacketHypotheses.fromJson(v as Map<String, dynamic>),
        ),
        findings: $checkedConvert(
          'findings',
          (v) => (v as List<dynamic>?)
              ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        forecasts: $checkedConvert(
          'forecasts',
          (v) => v == null
              ? null
              : CyclePacketForecasts.fromJson(v as Map<String, dynamic>),
        ),
        outlooks: $checkedConvert(
          'outlooks',
          (v) => v == null
              ? null
              : CyclePacketOutlooks.fromJson(v as Map<String, dynamic>),
        ),
        coverage: $checkedConvert(
          'coverage',
          (v) => v == null
              ? null
              : CyclePacketCoverage.fromJson(v as Map<String, dynamic>),
        ),
        collectionGaps: $checkedConvert(
          'collectionGaps',
          (v) => (v as List<dynamic>?)
              ?.map((e) => CollectionGap.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        verdicts: $checkedConvert(
          'verdicts',
          (v) => (v as List<dynamic>?)
              ?.map((e) => CyclePacketItem.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        previousJudgments: $checkedConvert(
          'previousJudgments',
          (v) => (v as List<dynamic>?)
              ?.map((e) => KeyJudgment.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        truncatedForLlm: $checkedConvert(
          'truncatedForLlm',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
        social: $checkedConvert(
          'social',
          (v) => v == null
              ? null
              : SocialVersusObserved.fromJson(v as Map<String, dynamic>),
        ),
        cadence: $checkedConvert(
          'cadence',
          (v) => $enumDecodeNullable(_$CyclePacketCadenceEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CyclePacketToJson(
  CyclePacket instance,
) => <String, dynamic>{
  'version': instance.version,
  'since': instance.since.toIso8601String(),
  'until': instance.until.toIso8601String(),
  'firstRun': ?instance.firstRun,
  'situations': ?instance.situations?.toJson(),
  'hypotheses': ?instance.hypotheses?.toJson(),
  'findings': ?instance.findings?.map((e) => e.toJson()).toList(),
  'forecasts': ?instance.forecasts?.toJson(),
  'outlooks': ?instance.outlooks?.toJson(),
  'coverage': ?instance.coverage?.toJson(),
  'collectionGaps': ?instance.collectionGaps?.map((e) => e.toJson()).toList(),
  'verdicts': ?instance.verdicts?.map((e) => e.toJson()).toList(),
  'previousJudgments': ?instance.previousJudgments
      ?.map((e) => e.toJson())
      .toList(),
  'truncatedForLlm': ?instance.truncatedForLlm,
  'social': ?instance.social?.toJson(),
  'cadence': ?_$CyclePacketCadenceEnumEnumMap[instance.cadence],
};

const _$CyclePacketCadenceEnumEnumMap = {
  CyclePacketCadenceEnum.daily: 'daily',
  CyclePacketCadenceEnum.weekly: 'weekly',
  CyclePacketCadenceEnum.monthly: 'monthly',
  CyclePacketCadenceEnum.onDemand: 'on-demand',
};
