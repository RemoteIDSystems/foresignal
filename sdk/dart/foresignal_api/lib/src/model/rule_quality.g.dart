// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_quality.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleQualityCWProxy {
  RuleQuality ruleId(String ruleId);

  RuleQuality since(DateTime since);

  RuleQuality alerts(int alerts);

  RuleQuality labeled(int labeled);

  RuleQuality labeledShare(num? labeledShare);

  RuleQuality positive(int positive);

  RuleQuality negative(int negative);

  RuleQuality neutral(int neutral);

  RuleQuality precision(num? precision);

  RuleQuality precisionCi(RuleQualityPrecisionCi? precisionCi);

  RuleQuality medianTimeToAck(String? medianTimeToAck);

  RuleQuality medianTimeToResolve(String? medianTimeToResolve);

  RuleQuality flapping(int? flapping);

  RuleQuality duplicates(int? duplicates);

  RuleQuality severityDistribution(Map<String, int>? severityDistribution);

  RuleQuality feedbackMapping(
    Map<String, RuleQualityFeedbackMappingEnum>? feedbackMapping,
  );

  RuleQuality caveats(List<Caveat>? caveats);

  RuleQuality signals(RuleQualitySignals? signals);

  RuleQuality signalScore(num? signalScore);

  RuleQuality daily(List<RuleQualityDaily>? daily);

  RuleQuality signalWeights(Map<String, num>? signalWeights);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleQuality(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleQuality(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleQuality call({
    String ruleId,
    DateTime since,
    int alerts,
    int labeled,
    num? labeledShare,
    int positive,
    int negative,
    int neutral,
    num? precision,
    RuleQualityPrecisionCi? precisionCi,
    String? medianTimeToAck,
    String? medianTimeToResolve,
    int? flapping,
    int? duplicates,
    Map<String, int>? severityDistribution,
    Map<String, RuleQualityFeedbackMappingEnum>? feedbackMapping,
    List<Caveat>? caveats,
    RuleQualitySignals? signals,
    num? signalScore,
    List<RuleQualityDaily>? daily,
    Map<String, num>? signalWeights,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleQuality.copyWith(...)` or call `instanceOfRuleQuality.copyWith.fieldName(value)` for a single field.
class _$RuleQualityCWProxyImpl implements _$RuleQualityCWProxy {
  const _$RuleQualityCWProxyImpl(this._value);

  final RuleQuality _value;

  @override
  RuleQuality ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  RuleQuality since(DateTime since) => call(since: since);

  @override
  RuleQuality alerts(int alerts) => call(alerts: alerts);

  @override
  RuleQuality labeled(int labeled) => call(labeled: labeled);

  @override
  RuleQuality labeledShare(num? labeledShare) =>
      call(labeledShare: labeledShare);

  @override
  RuleQuality positive(int positive) => call(positive: positive);

  @override
  RuleQuality negative(int negative) => call(negative: negative);

  @override
  RuleQuality neutral(int neutral) => call(neutral: neutral);

  @override
  RuleQuality precision(num? precision) => call(precision: precision);

  @override
  RuleQuality precisionCi(RuleQualityPrecisionCi? precisionCi) =>
      call(precisionCi: precisionCi);

  @override
  RuleQuality medianTimeToAck(String? medianTimeToAck) =>
      call(medianTimeToAck: medianTimeToAck);

  @override
  RuleQuality medianTimeToResolve(String? medianTimeToResolve) =>
      call(medianTimeToResolve: medianTimeToResolve);

  @override
  RuleQuality flapping(int? flapping) => call(flapping: flapping);

  @override
  RuleQuality duplicates(int? duplicates) => call(duplicates: duplicates);

  @override
  RuleQuality severityDistribution(Map<String, int>? severityDistribution) =>
      call(severityDistribution: severityDistribution);

  @override
  RuleQuality feedbackMapping(
    Map<String, RuleQualityFeedbackMappingEnum>? feedbackMapping,
  ) => call(feedbackMapping: feedbackMapping);

  @override
  RuleQuality caveats(List<Caveat>? caveats) => call(caveats: caveats);

  @override
  RuleQuality signals(RuleQualitySignals? signals) => call(signals: signals);

  @override
  RuleQuality signalScore(num? signalScore) => call(signalScore: signalScore);

  @override
  RuleQuality daily(List<RuleQualityDaily>? daily) => call(daily: daily);

  @override
  RuleQuality signalWeights(Map<String, num>? signalWeights) =>
      call(signalWeights: signalWeights);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleQuality(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleQuality(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleQuality call({
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? since = const $CopyWithPlaceholder(),
    Object? alerts = const $CopyWithPlaceholder(),
    Object? labeled = const $CopyWithPlaceholder(),
    Object? labeledShare = const $CopyWithPlaceholder(),
    Object? positive = const $CopyWithPlaceholder(),
    Object? negative = const $CopyWithPlaceholder(),
    Object? neutral = const $CopyWithPlaceholder(),
    Object? precision = const $CopyWithPlaceholder(),
    Object? precisionCi = const $CopyWithPlaceholder(),
    Object? medianTimeToAck = const $CopyWithPlaceholder(),
    Object? medianTimeToResolve = const $CopyWithPlaceholder(),
    Object? flapping = const $CopyWithPlaceholder(),
    Object? duplicates = const $CopyWithPlaceholder(),
    Object? severityDistribution = const $CopyWithPlaceholder(),
    Object? feedbackMapping = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? signals = const $CopyWithPlaceholder(),
    Object? signalScore = const $CopyWithPlaceholder(),
    Object? daily = const $CopyWithPlaceholder(),
    Object? signalWeights = const $CopyWithPlaceholder(),
  }) {
    return RuleQuality(
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      since: since == const $CopyWithPlaceholder() || since == null
          ? _value.since
          // ignore: cast_nullable_to_non_nullable
          : since as DateTime,
      alerts: alerts == const $CopyWithPlaceholder() || alerts == null
          ? _value.alerts
          // ignore: cast_nullable_to_non_nullable
          : alerts as int,
      labeled: labeled == const $CopyWithPlaceholder() || labeled == null
          ? _value.labeled
          // ignore: cast_nullable_to_non_nullable
          : labeled as int,
      labeledShare: labeledShare == const $CopyWithPlaceholder()
          ? _value.labeledShare
          // ignore: cast_nullable_to_non_nullable
          : labeledShare as num?,
      positive: positive == const $CopyWithPlaceholder() || positive == null
          ? _value.positive
          // ignore: cast_nullable_to_non_nullable
          : positive as int,
      negative: negative == const $CopyWithPlaceholder() || negative == null
          ? _value.negative
          // ignore: cast_nullable_to_non_nullable
          : negative as int,
      neutral: neutral == const $CopyWithPlaceholder() || neutral == null
          ? _value.neutral
          // ignore: cast_nullable_to_non_nullable
          : neutral as int,
      precision: precision == const $CopyWithPlaceholder()
          ? _value.precision
          // ignore: cast_nullable_to_non_nullable
          : precision as num?,
      precisionCi: precisionCi == const $CopyWithPlaceholder()
          ? _value.precisionCi
          // ignore: cast_nullable_to_non_nullable
          : precisionCi as RuleQualityPrecisionCi?,
      medianTimeToAck: medianTimeToAck == const $CopyWithPlaceholder()
          ? _value.medianTimeToAck
          // ignore: cast_nullable_to_non_nullable
          : medianTimeToAck as String?,
      medianTimeToResolve: medianTimeToResolve == const $CopyWithPlaceholder()
          ? _value.medianTimeToResolve
          // ignore: cast_nullable_to_non_nullable
          : medianTimeToResolve as String?,
      flapping: flapping == const $CopyWithPlaceholder()
          ? _value.flapping
          // ignore: cast_nullable_to_non_nullable
          : flapping as int?,
      duplicates: duplicates == const $CopyWithPlaceholder()
          ? _value.duplicates
          // ignore: cast_nullable_to_non_nullable
          : duplicates as int?,
      severityDistribution: severityDistribution == const $CopyWithPlaceholder()
          ? _value.severityDistribution
          // ignore: cast_nullable_to_non_nullable
          : severityDistribution as Map<String, int>?,
      feedbackMapping: feedbackMapping == const $CopyWithPlaceholder()
          ? _value.feedbackMapping
          // ignore: cast_nullable_to_non_nullable
          : feedbackMapping as Map<String, RuleQualityFeedbackMappingEnum>?,
      caveats: caveats == const $CopyWithPlaceholder()
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>?,
      signals: signals == const $CopyWithPlaceholder()
          ? _value.signals
          // ignore: cast_nullable_to_non_nullable
          : signals as RuleQualitySignals?,
      signalScore: signalScore == const $CopyWithPlaceholder()
          ? _value.signalScore
          // ignore: cast_nullable_to_non_nullable
          : signalScore as num?,
      daily: daily == const $CopyWithPlaceholder()
          ? _value.daily
          // ignore: cast_nullable_to_non_nullable
          : daily as List<RuleQualityDaily>?,
      signalWeights: signalWeights == const $CopyWithPlaceholder()
          ? _value.signalWeights
          // ignore: cast_nullable_to_non_nullable
          : signalWeights as Map<String, num>?,
    );
  }
}

extension $RuleQualityCopyWith on RuleQuality {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleQuality.copyWith(...)` or `instanceOfRuleQuality.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleQualityCWProxy get copyWith => _$RuleQualityCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleQuality _$RuleQualityFromJson(Map<String, dynamic> json) => $checkedCreate(
  'RuleQuality',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const [
        'ruleId',
        'since',
        'alerts',
        'labeled',
        'positive',
        'negative',
        'neutral',
      ],
    );
    final val = RuleQuality(
      ruleId: $checkedConvert('ruleId', (v) => v as String),
      since: $checkedConvert('since', (v) => DateTime.parse(v as String)),
      alerts: $checkedConvert('alerts', (v) => (v as num).toInt()),
      labeled: $checkedConvert('labeled', (v) => (v as num).toInt()),
      labeledShare: $checkedConvert('labeledShare', (v) => v as num?),
      positive: $checkedConvert('positive', (v) => (v as num).toInt()),
      negative: $checkedConvert('negative', (v) => (v as num).toInt()),
      neutral: $checkedConvert('neutral', (v) => (v as num).toInt()),
      precision: $checkedConvert('precision', (v) => v as num?),
      precisionCi: $checkedConvert(
        'precisionCi',
        (v) => v == null
            ? null
            : RuleQualityPrecisionCi.fromJson(v as Map<String, dynamic>),
      ),
      medianTimeToAck: $checkedConvert('medianTimeToAck', (v) => v as String?),
      medianTimeToResolve: $checkedConvert(
        'medianTimeToResolve',
        (v) => v as String?,
      ),
      flapping: $checkedConvert('flapping', (v) => (v as num?)?.toInt()),
      duplicates: $checkedConvert('duplicates', (v) => (v as num?)?.toInt()),
      severityDistribution: $checkedConvert(
        'severityDistribution',
        (v) => (v as Map<String, dynamic>?)?.map(
          (k, e) => MapEntry(k, (e as num).toInt()),
        ),
      ),
      feedbackMapping: $checkedConvert(
        'feedbackMapping',
        (v) => (v as Map<String, dynamic>?)?.map(
          (k, e) => MapEntry(
            k,
            $enumDecode(_$RuleQualityFeedbackMappingEnumEnumMap, e),
          ),
        ),
      ),
      caveats: $checkedConvert(
        'caveats',
        (v) => (v as List<dynamic>?)
            ?.map((e) => Caveat.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      signals: $checkedConvert(
        'signals',
        (v) => v == null
            ? null
            : RuleQualitySignals.fromJson(v as Map<String, dynamic>),
      ),
      signalScore: $checkedConvert('signalScore', (v) => v as num?),
      daily: $checkedConvert(
        'daily',
        (v) => (v as List<dynamic>?)
            ?.map((e) => RuleQualityDaily.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      signalWeights: $checkedConvert(
        'signalWeights',
        (v) =>
            (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as num)),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$RuleQualityToJson(RuleQuality instance) =>
    <String, dynamic>{
      'ruleId': instance.ruleId,
      'since': instance.since.toIso8601String(),
      'alerts': instance.alerts,
      'labeled': instance.labeled,
      'labeledShare': ?instance.labeledShare,
      'positive': instance.positive,
      'negative': instance.negative,
      'neutral': instance.neutral,
      'precision': ?instance.precision,
      'precisionCi': ?instance.precisionCi?.toJson(),
      'medianTimeToAck': ?instance.medianTimeToAck,
      'medianTimeToResolve': ?instance.medianTimeToResolve,
      'flapping': ?instance.flapping,
      'duplicates': ?instance.duplicates,
      'severityDistribution': ?instance.severityDistribution,
      'feedbackMapping': ?instance.feedbackMapping?.map(
        (k, e) => MapEntry(k, _$RuleQualityFeedbackMappingEnumEnumMap[e]!),
      ),
      'caveats': ?instance.caveats?.map((e) => e.toJson()).toList(),
      'signals': ?instance.signals?.toJson(),
      'signalScore': ?instance.signalScore,
      'daily': ?instance.daily?.map((e) => e.toJson()).toList(),
      'signalWeights': ?instance.signalWeights,
    };

const _$RuleQualityFeedbackMappingEnumEnumMap = {
  RuleQualityFeedbackMappingEnum.positive: 'positive',
  RuleQualityFeedbackMappingEnum.negative: 'negative',
  RuleQualityFeedbackMappingEnum.neutral: 'neutral',
};
