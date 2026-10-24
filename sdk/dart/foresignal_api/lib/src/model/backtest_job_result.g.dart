// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'backtest_job_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BacktestJobResultCWProxy {
  BacktestJobResult ruleType(BacktestJobResultRuleTypeEnum ruleType);

  BacktestJobResult wouldFire(int wouldFire);

  BacktestJobResult perDay(List<RuleBacktestResultPerDayInner>? perDay);

  BacktestJobResult topEntities(
    List<RuleBacktestResultTopEntitiesInner>? topEntities,
  );

  BacktestJobResult samples(List<RuleBacktestResultSamplesInner>? samples);

  BacktestJobResult actual(RuleBacktestResultActual? actual);

  BacktestJobResult overlap(RecipeBacktestResultOverlap? overlap);

  BacktestJobResult window(RuleBacktestResultWindow? window);

  BacktestJobResult anomaly(RuleBacktestResultAnomaly? anomaly);

  BacktestJobResult variants(List<RuleBacktestResultVariantsInner>? variants);

  BacktestJobResult recipeId(String recipeId);

  BacktestJobResult recipeVersion(int? recipeVersion);

  BacktestJobResult events(List<EventStudyResultEventsInner> events);

  BacktestJobResult countsByType(Map<String, int>? countsByType);

  BacktestJobResult testerVersion(String testerVersion);

  BacktestJobResult nAntecedent(int nAntecedent);

  BacktestJobResult nConsequent(int? nConsequent);

  BacktestJobResult hits(int hits);

  BacktestJobResult hitRate(num? hitRate);

  BacktestJobResult hitRateCi(PrecursorTestResultHitRateCi? hitRateCi);

  BacktestJobResult lag(PrecursorTestResultLag? lag);

  BacktestJobResult baseRate(num? baseRate);

  BacktestJobResult lift(num? lift);

  BacktestJobResult pValue(num? pValue);

  BacktestJobResult permutations(
    BacktestJobResultPermutationsEnum? permutations,
  );

  BacktestJobResult seed(String? seed);

  BacktestJobResult reverse(PrecursorTestResultReverse? reverse);

  BacktestJobResult coOccurrenceNotPrecedence(bool? coOccurrenceNotPrecedence);

  BacktestJobResult verdict(BacktestJobResultVerdictEnum verdict);

  BacktestJobResult multipleTesting(
    PrecursorTestResultMultipleTesting? multipleTesting,
  );

  BacktestJobResult statement(BacktestJobResultStatementEnum statement);

  BacktestJobResult caveats(List<Caveat> caveats);

  BacktestJobResult ruleId(String ruleId);

  BacktestJobResult since(DateTime since);

  BacktestJobResult alerts(int alerts);

  BacktestJobResult labeled(int labeled);

  BacktestJobResult labeledShare(num? labeledShare);

  BacktestJobResult positive(int positive);

  BacktestJobResult negative(int negative);

  BacktestJobResult neutral(int neutral);

  BacktestJobResult precision(num? precision);

  BacktestJobResult precisionCi(RuleQualityPrecisionCi? precisionCi);

  BacktestJobResult medianTimeToAck(String? medianTimeToAck);

  BacktestJobResult medianTimeToResolve(String? medianTimeToResolve);

  BacktestJobResult flapping(int? flapping);

  BacktestJobResult duplicates(int? duplicates);

  BacktestJobResult severityDistribution(
    Map<String, int>? severityDistribution,
  );

  BacktestJobResult feedbackMapping(
    Map<String, BacktestJobResultFeedbackMappingEnum>? feedbackMapping,
  );

  BacktestJobResult signals(RuleQualitySignals? signals);

  BacktestJobResult signalScore(num? signalScore);

  BacktestJobResult daily(List<RuleQualityDaily>? daily);

  BacktestJobResult signalWeights(Map<String, num>? signalWeights);

  BacktestJobResult jobId(String? jobId);

  BacktestJobResult manifestId(String? manifestId);

  BacktestJobResult status(BacktestJobResultStatusEnum status);

  BacktestJobResult outcome(BacktestJobResultOutcomeEnum? outcome);

  BacktestJobResult reason(BacktestJobResultReasonEnum? reason);

  BacktestJobResult diffSummary(String? diffSummary);

  BacktestJobResult message(String? message);

  BacktestJobResult startedAt(DateTime? startedAt);

  BacktestJobResult finishedAt(DateTime? finishedAt);

  BacktestJobResult detector(DetectorRef detector);

  BacktestJobResult definitionHash(String? definitionHash);

  BacktestJobResult target(String target);

  BacktestJobResult frequency(BacktestJobResultFrequencyEnum frequency);

  BacktestJobResult transform(BacktestJobResultTransformEnum? transform);

  BacktestJobResult windows(Map<String, Object> windows);

  BacktestJobResult range(TimeRange? range);

  BacktestJobResult asOfSeq(int? asOfSeq);

  BacktestJobResult placeboSeed(String? placeboSeed);

  BacktestJobResult nEvents(int nEvents);

  BacktestJobResult nClusters(int? nClusters);

  BacktestJobResult nExcluded(int? nExcluded);

  BacktestJobResult caar(EventStudyResultCaar caar);

  BacktestJobResult valuesWithheld(bool? valuesWithheld);

  BacktestJobResult truncated(bool? truncated);

  BacktestJobResult id(String id);

  BacktestJobResult mode(BacktestJobResultModeEnum mode);

  BacktestJobResult trigger(BacktestJobResultTriggerEnum? trigger);

  BacktestJobResult candidatesGenerated(int candidatesGenerated);

  BacktestJobResult tested(int tested);

  BacktestJobResult capped(int capped);

  BacktestJobResult insufficient(int? insufficient);

  BacktestJobResult survivors(int survivors);

  BacktestJobResult validated(int validated);

  BacktestJobResult q(num q);

  BacktestJobResult method(BacktestJobResultMethodEnum method);

  BacktestJobResult error(String? error);

  BacktestJobResult dataset(BacktestJobResultDatasetEnum dataset);

  BacktestJobResult params(Map<String, Object>? params);

  BacktestJobResult statusReason(String? statusReason);

  BacktestJobResult progress(num? progress);

  BacktestJobResult fileSha256(String? fileSha256);

  BacktestJobResult rowsRead(int rowsRead);

  BacktestJobResult rowsWritten(int rowsWritten);

  BacktestJobResult periodFrom(DateTime? periodFrom);

  BacktestJobResult periodTo(DateTime? periodTo);

  BacktestJobResult licence(String licence);

  BacktestJobResult egress(BacktestJobResultEgressEnum egress);

  BacktestJobResult createdBy(String createdBy);

  BacktestJobResult createdAt(DateTime createdAt);

  BacktestJobResult profiles(int profiles);

  BacktestJobResult rates(int rates);

  BacktestJobResult skipped(int? skipped);

  BacktestJobResult removed(int? removed);

  BacktestJobResult families(List<String> families);

  BacktestJobResult regions(int? regions);

  BacktestJobResult computer(DetectorRef? computer);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BacktestJobResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BacktestJobResult(...).copyWith(id: 12, name: "My name")
  /// ```
  BacktestJobResult call({
    BacktestJobResultRuleTypeEnum ruleType,
    int wouldFire,
    List<RuleBacktestResultPerDayInner>? perDay,
    List<RuleBacktestResultTopEntitiesInner>? topEntities,
    List<RuleBacktestResultSamplesInner>? samples,
    RuleBacktestResultActual? actual,
    RecipeBacktestResultOverlap? overlap,
    RuleBacktestResultWindow? window,
    RuleBacktestResultAnomaly? anomaly,
    List<RuleBacktestResultVariantsInner>? variants,
    String recipeId,
    int? recipeVersion,
    List<EventStudyResultEventsInner> events,
    Map<String, int>? countsByType,
    String testerVersion,
    int nAntecedent,
    int? nConsequent,
    int hits,
    num? hitRate,
    PrecursorTestResultHitRateCi? hitRateCi,
    PrecursorTestResultLag? lag,
    num? baseRate,
    num? lift,
    num? pValue,
    BacktestJobResultPermutationsEnum? permutations,
    String? seed,
    PrecursorTestResultReverse? reverse,
    bool? coOccurrenceNotPrecedence,
    BacktestJobResultVerdictEnum verdict,
    PrecursorTestResultMultipleTesting? multipleTesting,
    BacktestJobResultStatementEnum statement,
    List<Caveat> caveats,
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
    Map<String, BacktestJobResultFeedbackMappingEnum>? feedbackMapping,
    RuleQualitySignals? signals,
    num? signalScore,
    List<RuleQualityDaily>? daily,
    Map<String, num>? signalWeights,
    String? jobId,
    String? manifestId,
    BacktestJobResultStatusEnum status,
    BacktestJobResultOutcomeEnum? outcome,
    BacktestJobResultReasonEnum? reason,
    String? diffSummary,
    String? message,
    DateTime? startedAt,
    DateTime? finishedAt,
    DetectorRef detector,
    String? definitionHash,
    String target,
    BacktestJobResultFrequencyEnum frequency,
    BacktestJobResultTransformEnum? transform,
    Map<String, Object> windows,
    TimeRange? range,
    int? asOfSeq,
    String? placeboSeed,
    int nEvents,
    int? nClusters,
    int? nExcluded,
    EventStudyResultCaar caar,
    bool? valuesWithheld,
    bool? truncated,
    String id,
    BacktestJobResultModeEnum mode,
    BacktestJobResultTriggerEnum? trigger,
    int candidatesGenerated,
    int tested,
    int capped,
    int? insufficient,
    int survivors,
    int validated,
    num q,
    BacktestJobResultMethodEnum method,
    String? error,
    BacktestJobResultDatasetEnum dataset,
    Map<String, Object>? params,
    String? statusReason,
    num? progress,
    String? fileSha256,
    int rowsRead,
    int rowsWritten,
    DateTime? periodFrom,
    DateTime? periodTo,
    String licence,
    BacktestJobResultEgressEnum egress,
    String createdBy,
    DateTime createdAt,
    int profiles,
    int rates,
    int? skipped,
    int? removed,
    List<String> families,
    int? regions,
    DetectorRef? computer,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBacktestJobResult.copyWith(...)` or call `instanceOfBacktestJobResult.copyWith.fieldName(value)` for a single field.
class _$BacktestJobResultCWProxyImpl implements _$BacktestJobResultCWProxy {
  const _$BacktestJobResultCWProxyImpl(this._value);

  final BacktestJobResult _value;

  @override
  BacktestJobResult ruleType(BacktestJobResultRuleTypeEnum ruleType) =>
      call(ruleType: ruleType);

  @override
  BacktestJobResult wouldFire(int wouldFire) => call(wouldFire: wouldFire);

  @override
  BacktestJobResult perDay(List<RuleBacktestResultPerDayInner>? perDay) =>
      call(perDay: perDay);

  @override
  BacktestJobResult topEntities(
    List<RuleBacktestResultTopEntitiesInner>? topEntities,
  ) => call(topEntities: topEntities);

  @override
  BacktestJobResult samples(List<RuleBacktestResultSamplesInner>? samples) =>
      call(samples: samples);

  @override
  BacktestJobResult actual(RuleBacktestResultActual? actual) =>
      call(actual: actual);

  @override
  BacktestJobResult overlap(RecipeBacktestResultOverlap? overlap) =>
      call(overlap: overlap);

  @override
  BacktestJobResult window(RuleBacktestResultWindow? window) =>
      call(window: window);

  @override
  BacktestJobResult anomaly(RuleBacktestResultAnomaly? anomaly) =>
      call(anomaly: anomaly);

  @override
  BacktestJobResult variants(List<RuleBacktestResultVariantsInner>? variants) =>
      call(variants: variants);

  @override
  BacktestJobResult recipeId(String recipeId) => call(recipeId: recipeId);

  @override
  BacktestJobResult recipeVersion(int? recipeVersion) =>
      call(recipeVersion: recipeVersion);

  @override
  BacktestJobResult events(List<EventStudyResultEventsInner> events) =>
      call(events: events);

  @override
  BacktestJobResult countsByType(Map<String, int>? countsByType) =>
      call(countsByType: countsByType);

  @override
  BacktestJobResult testerVersion(String testerVersion) =>
      call(testerVersion: testerVersion);

  @override
  BacktestJobResult nAntecedent(int nAntecedent) =>
      call(nAntecedent: nAntecedent);

  @override
  BacktestJobResult nConsequent(int? nConsequent) =>
      call(nConsequent: nConsequent);

  @override
  BacktestJobResult hits(int hits) => call(hits: hits);

  @override
  BacktestJobResult hitRate(num? hitRate) => call(hitRate: hitRate);

  @override
  BacktestJobResult hitRateCi(PrecursorTestResultHitRateCi? hitRateCi) =>
      call(hitRateCi: hitRateCi);

  @override
  BacktestJobResult lag(PrecursorTestResultLag? lag) => call(lag: lag);

  @override
  BacktestJobResult baseRate(num? baseRate) => call(baseRate: baseRate);

  @override
  BacktestJobResult lift(num? lift) => call(lift: lift);

  @override
  BacktestJobResult pValue(num? pValue) => call(pValue: pValue);

  @override
  BacktestJobResult permutations(
    BacktestJobResultPermutationsEnum? permutations,
  ) => call(permutations: permutations);

  @override
  BacktestJobResult seed(String? seed) => call(seed: seed);

  @override
  BacktestJobResult reverse(PrecursorTestResultReverse? reverse) =>
      call(reverse: reverse);

  @override
  BacktestJobResult coOccurrenceNotPrecedence(
    bool? coOccurrenceNotPrecedence,
  ) => call(coOccurrenceNotPrecedence: coOccurrenceNotPrecedence);

  @override
  BacktestJobResult verdict(BacktestJobResultVerdictEnum verdict) =>
      call(verdict: verdict);

  @override
  BacktestJobResult multipleTesting(
    PrecursorTestResultMultipleTesting? multipleTesting,
  ) => call(multipleTesting: multipleTesting);

  @override
  BacktestJobResult statement(BacktestJobResultStatementEnum statement) =>
      call(statement: statement);

  @override
  BacktestJobResult caveats(List<Caveat> caveats) => call(caveats: caveats);

  @override
  BacktestJobResult ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  BacktestJobResult since(DateTime since) => call(since: since);

  @override
  BacktestJobResult alerts(int alerts) => call(alerts: alerts);

  @override
  BacktestJobResult labeled(int labeled) => call(labeled: labeled);

  @override
  BacktestJobResult labeledShare(num? labeledShare) =>
      call(labeledShare: labeledShare);

  @override
  BacktestJobResult positive(int positive) => call(positive: positive);

  @override
  BacktestJobResult negative(int negative) => call(negative: negative);

  @override
  BacktestJobResult neutral(int neutral) => call(neutral: neutral);

  @override
  BacktestJobResult precision(num? precision) => call(precision: precision);

  @override
  BacktestJobResult precisionCi(RuleQualityPrecisionCi? precisionCi) =>
      call(precisionCi: precisionCi);

  @override
  BacktestJobResult medianTimeToAck(String? medianTimeToAck) =>
      call(medianTimeToAck: medianTimeToAck);

  @override
  BacktestJobResult medianTimeToResolve(String? medianTimeToResolve) =>
      call(medianTimeToResolve: medianTimeToResolve);

  @override
  BacktestJobResult flapping(int? flapping) => call(flapping: flapping);

  @override
  BacktestJobResult duplicates(int? duplicates) => call(duplicates: duplicates);

  @override
  BacktestJobResult severityDistribution(
    Map<String, int>? severityDistribution,
  ) => call(severityDistribution: severityDistribution);

  @override
  BacktestJobResult feedbackMapping(
    Map<String, BacktestJobResultFeedbackMappingEnum>? feedbackMapping,
  ) => call(feedbackMapping: feedbackMapping);

  @override
  BacktestJobResult signals(RuleQualitySignals? signals) =>
      call(signals: signals);

  @override
  BacktestJobResult signalScore(num? signalScore) =>
      call(signalScore: signalScore);

  @override
  BacktestJobResult daily(List<RuleQualityDaily>? daily) => call(daily: daily);

  @override
  BacktestJobResult signalWeights(Map<String, num>? signalWeights) =>
      call(signalWeights: signalWeights);

  @override
  BacktestJobResult jobId(String? jobId) => call(jobId: jobId);

  @override
  BacktestJobResult manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  @override
  BacktestJobResult status(BacktestJobResultStatusEnum status) =>
      call(status: status);

  @override
  BacktestJobResult outcome(BacktestJobResultOutcomeEnum? outcome) =>
      call(outcome: outcome);

  @override
  BacktestJobResult reason(BacktestJobResultReasonEnum? reason) =>
      call(reason: reason);

  @override
  BacktestJobResult diffSummary(String? diffSummary) =>
      call(diffSummary: diffSummary);

  @override
  BacktestJobResult message(String? message) => call(message: message);

  @override
  BacktestJobResult startedAt(DateTime? startedAt) =>
      call(startedAt: startedAt);

  @override
  BacktestJobResult finishedAt(DateTime? finishedAt) =>
      call(finishedAt: finishedAt);

  @override
  BacktestJobResult detector(DetectorRef detector) => call(detector: detector);

  @override
  BacktestJobResult definitionHash(String? definitionHash) =>
      call(definitionHash: definitionHash);

  @override
  BacktestJobResult target(String target) => call(target: target);

  @override
  BacktestJobResult frequency(BacktestJobResultFrequencyEnum frequency) =>
      call(frequency: frequency);

  @override
  BacktestJobResult transform(BacktestJobResultTransformEnum? transform) =>
      call(transform: transform);

  @override
  BacktestJobResult windows(Map<String, Object> windows) =>
      call(windows: windows);

  @override
  BacktestJobResult range(TimeRange? range) => call(range: range);

  @override
  BacktestJobResult asOfSeq(int? asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  BacktestJobResult placeboSeed(String? placeboSeed) =>
      call(placeboSeed: placeboSeed);

  @override
  BacktestJobResult nEvents(int nEvents) => call(nEvents: nEvents);

  @override
  BacktestJobResult nClusters(int? nClusters) => call(nClusters: nClusters);

  @override
  BacktestJobResult nExcluded(int? nExcluded) => call(nExcluded: nExcluded);

  @override
  BacktestJobResult caar(EventStudyResultCaar caar) => call(caar: caar);

  @override
  BacktestJobResult valuesWithheld(bool? valuesWithheld) =>
      call(valuesWithheld: valuesWithheld);

  @override
  BacktestJobResult truncated(bool? truncated) => call(truncated: truncated);

  @override
  BacktestJobResult id(String id) => call(id: id);

  @override
  BacktestJobResult mode(BacktestJobResultModeEnum mode) => call(mode: mode);

  @override
  BacktestJobResult trigger(BacktestJobResultTriggerEnum? trigger) =>
      call(trigger: trigger);

  @override
  BacktestJobResult candidatesGenerated(int candidatesGenerated) =>
      call(candidatesGenerated: candidatesGenerated);

  @override
  BacktestJobResult tested(int tested) => call(tested: tested);

  @override
  BacktestJobResult capped(int capped) => call(capped: capped);

  @override
  BacktestJobResult insufficient(int? insufficient) =>
      call(insufficient: insufficient);

  @override
  BacktestJobResult survivors(int survivors) => call(survivors: survivors);

  @override
  BacktestJobResult validated(int validated) => call(validated: validated);

  @override
  BacktestJobResult q(num q) => call(q: q);

  @override
  BacktestJobResult method(BacktestJobResultMethodEnum method) =>
      call(method: method);

  @override
  BacktestJobResult error(String? error) => call(error: error);

  @override
  BacktestJobResult dataset(BacktestJobResultDatasetEnum dataset) =>
      call(dataset: dataset);

  @override
  BacktestJobResult params(Map<String, Object>? params) => call(params: params);

  @override
  BacktestJobResult statusReason(String? statusReason) =>
      call(statusReason: statusReason);

  @override
  BacktestJobResult progress(num? progress) => call(progress: progress);

  @override
  BacktestJobResult fileSha256(String? fileSha256) =>
      call(fileSha256: fileSha256);

  @override
  BacktestJobResult rowsRead(int rowsRead) => call(rowsRead: rowsRead);

  @override
  BacktestJobResult rowsWritten(int rowsWritten) =>
      call(rowsWritten: rowsWritten);

  @override
  BacktestJobResult periodFrom(DateTime? periodFrom) =>
      call(periodFrom: periodFrom);

  @override
  BacktestJobResult periodTo(DateTime? periodTo) => call(periodTo: periodTo);

  @override
  BacktestJobResult licence(String licence) => call(licence: licence);

  @override
  BacktestJobResult egress(BacktestJobResultEgressEnum egress) =>
      call(egress: egress);

  @override
  BacktestJobResult createdBy(String createdBy) => call(createdBy: createdBy);

  @override
  BacktestJobResult createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  BacktestJobResult profiles(int profiles) => call(profiles: profiles);

  @override
  BacktestJobResult rates(int rates) => call(rates: rates);

  @override
  BacktestJobResult skipped(int? skipped) => call(skipped: skipped);

  @override
  BacktestJobResult removed(int? removed) => call(removed: removed);

  @override
  BacktestJobResult families(List<String> families) => call(families: families);

  @override
  BacktestJobResult regions(int? regions) => call(regions: regions);

  @override
  BacktestJobResult computer(DetectorRef? computer) => call(computer: computer);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BacktestJobResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BacktestJobResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BacktestJobResult call({
    Object? ruleType = const $CopyWithPlaceholder(),
    Object? wouldFire = const $CopyWithPlaceholder(),
    Object? perDay = const $CopyWithPlaceholder(),
    Object? topEntities = const $CopyWithPlaceholder(),
    Object? samples = const $CopyWithPlaceholder(),
    Object? actual = const $CopyWithPlaceholder(),
    Object? overlap = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
    Object? anomaly = const $CopyWithPlaceholder(),
    Object? variants = const $CopyWithPlaceholder(),
    Object? recipeId = const $CopyWithPlaceholder(),
    Object? recipeVersion = const $CopyWithPlaceholder(),
    Object? events = const $CopyWithPlaceholder(),
    Object? countsByType = const $CopyWithPlaceholder(),
    Object? testerVersion = const $CopyWithPlaceholder(),
    Object? nAntecedent = const $CopyWithPlaceholder(),
    Object? nConsequent = const $CopyWithPlaceholder(),
    Object? hits = const $CopyWithPlaceholder(),
    Object? hitRate = const $CopyWithPlaceholder(),
    Object? hitRateCi = const $CopyWithPlaceholder(),
    Object? lag = const $CopyWithPlaceholder(),
    Object? baseRate = const $CopyWithPlaceholder(),
    Object? lift = const $CopyWithPlaceholder(),
    Object? pValue = const $CopyWithPlaceholder(),
    Object? permutations = const $CopyWithPlaceholder(),
    Object? seed = const $CopyWithPlaceholder(),
    Object? reverse = const $CopyWithPlaceholder(),
    Object? coOccurrenceNotPrecedence = const $CopyWithPlaceholder(),
    Object? verdict = const $CopyWithPlaceholder(),
    Object? multipleTesting = const $CopyWithPlaceholder(),
    Object? statement = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
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
    Object? signals = const $CopyWithPlaceholder(),
    Object? signalScore = const $CopyWithPlaceholder(),
    Object? daily = const $CopyWithPlaceholder(),
    Object? signalWeights = const $CopyWithPlaceholder(),
    Object? jobId = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? outcome = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? diffSummary = const $CopyWithPlaceholder(),
    Object? message = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? definitionHash = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? frequency = const $CopyWithPlaceholder(),
    Object? transform = const $CopyWithPlaceholder(),
    Object? windows = const $CopyWithPlaceholder(),
    Object? range = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? placeboSeed = const $CopyWithPlaceholder(),
    Object? nEvents = const $CopyWithPlaceholder(),
    Object? nClusters = const $CopyWithPlaceholder(),
    Object? nExcluded = const $CopyWithPlaceholder(),
    Object? caar = const $CopyWithPlaceholder(),
    Object? valuesWithheld = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? mode = const $CopyWithPlaceholder(),
    Object? trigger = const $CopyWithPlaceholder(),
    Object? candidatesGenerated = const $CopyWithPlaceholder(),
    Object? tested = const $CopyWithPlaceholder(),
    Object? capped = const $CopyWithPlaceholder(),
    Object? insufficient = const $CopyWithPlaceholder(),
    Object? survivors = const $CopyWithPlaceholder(),
    Object? validated = const $CopyWithPlaceholder(),
    Object? q = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? dataset = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
    Object? statusReason = const $CopyWithPlaceholder(),
    Object? progress = const $CopyWithPlaceholder(),
    Object? fileSha256 = const $CopyWithPlaceholder(),
    Object? rowsRead = const $CopyWithPlaceholder(),
    Object? rowsWritten = const $CopyWithPlaceholder(),
    Object? periodFrom = const $CopyWithPlaceholder(),
    Object? periodTo = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? egress = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? profiles = const $CopyWithPlaceholder(),
    Object? rates = const $CopyWithPlaceholder(),
    Object? skipped = const $CopyWithPlaceholder(),
    Object? removed = const $CopyWithPlaceholder(),
    Object? families = const $CopyWithPlaceholder(),
    Object? regions = const $CopyWithPlaceholder(),
    Object? computer = const $CopyWithPlaceholder(),
  }) {
    return BacktestJobResult(
      ruleType: ruleType == const $CopyWithPlaceholder() || ruleType == null
          ? _value.ruleType
          // ignore: cast_nullable_to_non_nullable
          : ruleType as BacktestJobResultRuleTypeEnum,
      wouldFire: wouldFire == const $CopyWithPlaceholder() || wouldFire == null
          ? _value.wouldFire
          // ignore: cast_nullable_to_non_nullable
          : wouldFire as int,
      perDay: perDay == const $CopyWithPlaceholder()
          ? _value.perDay
          // ignore: cast_nullable_to_non_nullable
          : perDay as List<RuleBacktestResultPerDayInner>?,
      topEntities: topEntities == const $CopyWithPlaceholder()
          ? _value.topEntities
          // ignore: cast_nullable_to_non_nullable
          : topEntities as List<RuleBacktestResultTopEntitiesInner>?,
      samples: samples == const $CopyWithPlaceholder()
          ? _value.samples
          // ignore: cast_nullable_to_non_nullable
          : samples as List<RuleBacktestResultSamplesInner>?,
      actual: actual == const $CopyWithPlaceholder()
          ? _value.actual
          // ignore: cast_nullable_to_non_nullable
          : actual as RuleBacktestResultActual?,
      overlap: overlap == const $CopyWithPlaceholder()
          ? _value.overlap
          // ignore: cast_nullable_to_non_nullable
          : overlap as RecipeBacktestResultOverlap?,
      window: window == const $CopyWithPlaceholder()
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as RuleBacktestResultWindow?,
      anomaly: anomaly == const $CopyWithPlaceholder()
          ? _value.anomaly
          // ignore: cast_nullable_to_non_nullable
          : anomaly as RuleBacktestResultAnomaly?,
      variants: variants == const $CopyWithPlaceholder()
          ? _value.variants
          // ignore: cast_nullable_to_non_nullable
          : variants as List<RuleBacktestResultVariantsInner>?,
      recipeId: recipeId == const $CopyWithPlaceholder() || recipeId == null
          ? _value.recipeId
          // ignore: cast_nullable_to_non_nullable
          : recipeId as String,
      recipeVersion: recipeVersion == const $CopyWithPlaceholder()
          ? _value.recipeVersion
          // ignore: cast_nullable_to_non_nullable
          : recipeVersion as int?,
      events: events == const $CopyWithPlaceholder() || events == null
          ? _value.events
          // ignore: cast_nullable_to_non_nullable
          : events as List<EventStudyResultEventsInner>,
      countsByType: countsByType == const $CopyWithPlaceholder()
          ? _value.countsByType
          // ignore: cast_nullable_to_non_nullable
          : countsByType as Map<String, int>?,
      testerVersion:
          testerVersion == const $CopyWithPlaceholder() || testerVersion == null
          ? _value.testerVersion
          // ignore: cast_nullable_to_non_nullable
          : testerVersion as String,
      nAntecedent:
          nAntecedent == const $CopyWithPlaceholder() || nAntecedent == null
          ? _value.nAntecedent
          // ignore: cast_nullable_to_non_nullable
          : nAntecedent as int,
      nConsequent: nConsequent == const $CopyWithPlaceholder()
          ? _value.nConsequent
          // ignore: cast_nullable_to_non_nullable
          : nConsequent as int?,
      hits: hits == const $CopyWithPlaceholder() || hits == null
          ? _value.hits
          // ignore: cast_nullable_to_non_nullable
          : hits as int,
      hitRate: hitRate == const $CopyWithPlaceholder()
          ? _value.hitRate
          // ignore: cast_nullable_to_non_nullable
          : hitRate as num?,
      hitRateCi: hitRateCi == const $CopyWithPlaceholder()
          ? _value.hitRateCi
          // ignore: cast_nullable_to_non_nullable
          : hitRateCi as PrecursorTestResultHitRateCi?,
      lag: lag == const $CopyWithPlaceholder()
          ? _value.lag
          // ignore: cast_nullable_to_non_nullable
          : lag as PrecursorTestResultLag?,
      baseRate: baseRate == const $CopyWithPlaceholder()
          ? _value.baseRate
          // ignore: cast_nullable_to_non_nullable
          : baseRate as num?,
      lift: lift == const $CopyWithPlaceholder()
          ? _value.lift
          // ignore: cast_nullable_to_non_nullable
          : lift as num?,
      pValue: pValue == const $CopyWithPlaceholder()
          ? _value.pValue
          // ignore: cast_nullable_to_non_nullable
          : pValue as num?,
      permutations: permutations == const $CopyWithPlaceholder()
          ? _value.permutations
          // ignore: cast_nullable_to_non_nullable
          : permutations as BacktestJobResultPermutationsEnum?,
      seed: seed == const $CopyWithPlaceholder()
          ? _value.seed
          // ignore: cast_nullable_to_non_nullable
          : seed as String?,
      reverse: reverse == const $CopyWithPlaceholder()
          ? _value.reverse
          // ignore: cast_nullable_to_non_nullable
          : reverse as PrecursorTestResultReverse?,
      coOccurrenceNotPrecedence:
          coOccurrenceNotPrecedence == const $CopyWithPlaceholder()
          ? _value.coOccurrenceNotPrecedence
          // ignore: cast_nullable_to_non_nullable
          : coOccurrenceNotPrecedence as bool?,
      verdict: verdict == const $CopyWithPlaceholder() || verdict == null
          ? _value.verdict
          // ignore: cast_nullable_to_non_nullable
          : verdict as BacktestJobResultVerdictEnum,
      multipleTesting: multipleTesting == const $CopyWithPlaceholder()
          ? _value.multipleTesting
          // ignore: cast_nullable_to_non_nullable
          : multipleTesting as PrecursorTestResultMultipleTesting?,
      statement: statement == const $CopyWithPlaceholder() || statement == null
          ? _value.statement
          // ignore: cast_nullable_to_non_nullable
          : statement as BacktestJobResultStatementEnum,
      caveats: caveats == const $CopyWithPlaceholder() || caveats == null
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>,
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
          : feedbackMapping
                as Map<String, BacktestJobResultFeedbackMappingEnum>?,
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
      jobId: jobId == const $CopyWithPlaceholder()
          ? _value.jobId
          // ignore: cast_nullable_to_non_nullable
          : jobId as String?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as BacktestJobResultStatusEnum,
      outcome: outcome == const $CopyWithPlaceholder()
          ? _value.outcome
          // ignore: cast_nullable_to_non_nullable
          : outcome as BacktestJobResultOutcomeEnum?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as BacktestJobResultReasonEnum?,
      diffSummary: diffSummary == const $CopyWithPlaceholder()
          ? _value.diffSummary
          // ignore: cast_nullable_to_non_nullable
          : diffSummary as String?,
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as String?,
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      detector: detector == const $CopyWithPlaceholder() || detector == null
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DetectorRef,
      definitionHash: definitionHash == const $CopyWithPlaceholder()
          ? _value.definitionHash
          // ignore: cast_nullable_to_non_nullable
          : definitionHash as String?,
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as String,
      frequency: frequency == const $CopyWithPlaceholder() || frequency == null
          ? _value.frequency
          // ignore: cast_nullable_to_non_nullable
          : frequency as BacktestJobResultFrequencyEnum,
      transform: transform == const $CopyWithPlaceholder()
          ? _value.transform
          // ignore: cast_nullable_to_non_nullable
          : transform as BacktestJobResultTransformEnum?,
      windows: windows == const $CopyWithPlaceholder() || windows == null
          ? _value.windows
          // ignore: cast_nullable_to_non_nullable
          : windows as Map<String, Object>,
      range: range == const $CopyWithPlaceholder()
          ? _value.range
          // ignore: cast_nullable_to_non_nullable
          : range as TimeRange?,
      asOfSeq: asOfSeq == const $CopyWithPlaceholder()
          ? _value.asOfSeq
          // ignore: cast_nullable_to_non_nullable
          : asOfSeq as int?,
      placeboSeed: placeboSeed == const $CopyWithPlaceholder()
          ? _value.placeboSeed
          // ignore: cast_nullable_to_non_nullable
          : placeboSeed as String?,
      nEvents: nEvents == const $CopyWithPlaceholder() || nEvents == null
          ? _value.nEvents
          // ignore: cast_nullable_to_non_nullable
          : nEvents as int,
      nClusters: nClusters == const $CopyWithPlaceholder()
          ? _value.nClusters
          // ignore: cast_nullable_to_non_nullable
          : nClusters as int?,
      nExcluded: nExcluded == const $CopyWithPlaceholder()
          ? _value.nExcluded
          // ignore: cast_nullable_to_non_nullable
          : nExcluded as int?,
      caar: caar == const $CopyWithPlaceholder() || caar == null
          ? _value.caar
          // ignore: cast_nullable_to_non_nullable
          : caar as EventStudyResultCaar,
      valuesWithheld: valuesWithheld == const $CopyWithPlaceholder()
          ? _value.valuesWithheld
          // ignore: cast_nullable_to_non_nullable
          : valuesWithheld as bool?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      mode: mode == const $CopyWithPlaceholder() || mode == null
          ? _value.mode
          // ignore: cast_nullable_to_non_nullable
          : mode as BacktestJobResultModeEnum,
      trigger: trigger == const $CopyWithPlaceholder()
          ? _value.trigger
          // ignore: cast_nullable_to_non_nullable
          : trigger as BacktestJobResultTriggerEnum?,
      candidatesGenerated:
          candidatesGenerated == const $CopyWithPlaceholder() ||
              candidatesGenerated == null
          ? _value.candidatesGenerated
          // ignore: cast_nullable_to_non_nullable
          : candidatesGenerated as int,
      tested: tested == const $CopyWithPlaceholder() || tested == null
          ? _value.tested
          // ignore: cast_nullable_to_non_nullable
          : tested as int,
      capped: capped == const $CopyWithPlaceholder() || capped == null
          ? _value.capped
          // ignore: cast_nullable_to_non_nullable
          : capped as int,
      insufficient: insufficient == const $CopyWithPlaceholder()
          ? _value.insufficient
          // ignore: cast_nullable_to_non_nullable
          : insufficient as int?,
      survivors: survivors == const $CopyWithPlaceholder() || survivors == null
          ? _value.survivors
          // ignore: cast_nullable_to_non_nullable
          : survivors as int,
      validated: validated == const $CopyWithPlaceholder() || validated == null
          ? _value.validated
          // ignore: cast_nullable_to_non_nullable
          : validated as int,
      q: q == const $CopyWithPlaceholder() || q == null
          ? _value.q
          // ignore: cast_nullable_to_non_nullable
          : q as num,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as BacktestJobResultMethodEnum,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
      dataset: dataset == const $CopyWithPlaceholder() || dataset == null
          ? _value.dataset
          // ignore: cast_nullable_to_non_nullable
          : dataset as BacktestJobResultDatasetEnum,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as Map<String, Object>?,
      statusReason: statusReason == const $CopyWithPlaceholder()
          ? _value.statusReason
          // ignore: cast_nullable_to_non_nullable
          : statusReason as String?,
      progress: progress == const $CopyWithPlaceholder()
          ? _value.progress
          // ignore: cast_nullable_to_non_nullable
          : progress as num?,
      fileSha256: fileSha256 == const $CopyWithPlaceholder()
          ? _value.fileSha256
          // ignore: cast_nullable_to_non_nullable
          : fileSha256 as String?,
      rowsRead: rowsRead == const $CopyWithPlaceholder() || rowsRead == null
          ? _value.rowsRead
          // ignore: cast_nullable_to_non_nullable
          : rowsRead as int,
      rowsWritten:
          rowsWritten == const $CopyWithPlaceholder() || rowsWritten == null
          ? _value.rowsWritten
          // ignore: cast_nullable_to_non_nullable
          : rowsWritten as int,
      periodFrom: periodFrom == const $CopyWithPlaceholder()
          ? _value.periodFrom
          // ignore: cast_nullable_to_non_nullable
          : periodFrom as DateTime?,
      periodTo: periodTo == const $CopyWithPlaceholder()
          ? _value.periodTo
          // ignore: cast_nullable_to_non_nullable
          : periodTo as DateTime?,
      licence: licence == const $CopyWithPlaceholder() || licence == null
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String,
      egress: egress == const $CopyWithPlaceholder() || egress == null
          ? _value.egress
          // ignore: cast_nullable_to_non_nullable
          : egress as BacktestJobResultEgressEnum,
      createdBy: createdBy == const $CopyWithPlaceholder() || createdBy == null
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      profiles: profiles == const $CopyWithPlaceholder() || profiles == null
          ? _value.profiles
          // ignore: cast_nullable_to_non_nullable
          : profiles as int,
      rates: rates == const $CopyWithPlaceholder() || rates == null
          ? _value.rates
          // ignore: cast_nullable_to_non_nullable
          : rates as int,
      skipped: skipped == const $CopyWithPlaceholder()
          ? _value.skipped
          // ignore: cast_nullable_to_non_nullable
          : skipped as int?,
      removed: removed == const $CopyWithPlaceholder()
          ? _value.removed
          // ignore: cast_nullable_to_non_nullable
          : removed as int?,
      families: families == const $CopyWithPlaceholder() || families == null
          ? _value.families
          // ignore: cast_nullable_to_non_nullable
          : families as List<String>,
      regions: regions == const $CopyWithPlaceholder()
          ? _value.regions
          // ignore: cast_nullable_to_non_nullable
          : regions as int?,
      computer: computer == const $CopyWithPlaceholder()
          ? _value.computer
          // ignore: cast_nullable_to_non_nullable
          : computer as DetectorRef?,
    );
  }
}

extension $BacktestJobResultCopyWith on BacktestJobResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBacktestJobResult.copyWith(...)` or `instanceOfBacktestJobResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BacktestJobResultCWProxy get copyWith =>
      _$BacktestJobResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BacktestJobResult _$BacktestJobResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BacktestJobResult', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'ruleType',
      'wouldFire',
      'recipeId',
      'events',
      'testerVersion',
      'nAntecedent',
      'hits',
      'verdict',
      'statement',
      'caveats',
      'ruleId',
      'since',
      'alerts',
      'labeled',
      'positive',
      'negative',
      'neutral',
      'jobId',
      'manifestId',
      'status',
      'startedAt',
      'detector',
      'target',
      'frequency',
      'windows',
      'nEvents',
      'caar',
      'id',
      'mode',
      'candidatesGenerated',
      'tested',
      'capped',
      'survivors',
      'validated',
      'q',
      'method',
      'dataset',
      'rowsRead',
      'rowsWritten',
      'licence',
      'egress',
      'createdBy',
      'createdAt',
      'profiles',
      'rates',
      'families',
    ],
  );
  final val = BacktestJobResult(
    ruleType: $checkedConvert(
      'ruleType',
      (v) => $enumDecode(_$BacktestJobResultRuleTypeEnumEnumMap, v),
    ),
    wouldFire: $checkedConvert('wouldFire', (v) => (v as num).toInt()),
    perDay: $checkedConvert(
      'perDay',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => RuleBacktestResultPerDayInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    topEntities: $checkedConvert(
      'topEntities',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => RuleBacktestResultTopEntitiesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    samples: $checkedConvert(
      'samples',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => RuleBacktestResultSamplesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    actual: $checkedConvert(
      'actual',
      (v) => v == null
          ? null
          : RuleBacktestResultActual.fromJson(v as Map<String, dynamic>),
    ),
    overlap: $checkedConvert(
      'overlap',
      (v) => v == null
          ? null
          : RecipeBacktestResultOverlap.fromJson(v as Map<String, dynamic>),
    ),
    window: $checkedConvert(
      'window',
      (v) => v == null
          ? null
          : RuleBacktestResultWindow.fromJson(v as Map<String, dynamic>),
    ),
    anomaly: $checkedConvert(
      'anomaly',
      (v) => v == null
          ? null
          : RuleBacktestResultAnomaly.fromJson(v as Map<String, dynamic>),
    ),
    variants: $checkedConvert(
      'variants',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => RuleBacktestResultVariantsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    recipeId: $checkedConvert('recipeId', (v) => v as String),
    recipeVersion: $checkedConvert(
      'recipeVersion',
      (v) => (v as num?)?.toInt(),
    ),
    events: $checkedConvert(
      'events',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                EventStudyResultEventsInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    countsByType: $checkedConvert(
      'countsByType',
      (v) => (v as Map<String, dynamic>?)?.map(
        (k, e) => MapEntry(k, (e as num).toInt()),
      ),
    ),
    testerVersion: $checkedConvert('testerVersion', (v) => v as String),
    nAntecedent: $checkedConvert('nAntecedent', (v) => (v as num).toInt()),
    nConsequent: $checkedConvert('nConsequent', (v) => (v as num?)?.toInt()),
    hits: $checkedConvert('hits', (v) => (v as num).toInt()),
    hitRate: $checkedConvert('hitRate', (v) => v as num?),
    hitRateCi: $checkedConvert(
      'hitRateCi',
      (v) => v == null
          ? null
          : PrecursorTestResultHitRateCi.fromJson(v as Map<String, dynamic>),
    ),
    lag: $checkedConvert(
      'lag',
      (v) => v == null
          ? null
          : PrecursorTestResultLag.fromJson(v as Map<String, dynamic>),
    ),
    baseRate: $checkedConvert('baseRate', (v) => v as num?),
    lift: $checkedConvert('lift', (v) => v as num?),
    pValue: $checkedConvert('pValue', (v) => v as num?),
    permutations: $checkedConvert(
      'permutations',
      (v) => $enumDecodeNullable(_$BacktestJobResultPermutationsEnumEnumMap, v),
    ),
    seed: $checkedConvert('seed', (v) => v as String?),
    reverse: $checkedConvert(
      'reverse',
      (v) => v == null
          ? null
          : PrecursorTestResultReverse.fromJson(v as Map<String, dynamic>),
    ),
    coOccurrenceNotPrecedence: $checkedConvert(
      'coOccurrenceNotPrecedence',
      (v) => v as bool?,
    ),
    verdict: $checkedConvert(
      'verdict',
      (v) => $enumDecode(_$BacktestJobResultVerdictEnumEnumMap, v),
    ),
    multipleTesting: $checkedConvert(
      'multipleTesting',
      (v) => v == null
          ? null
          : PrecursorTestResultMultipleTesting.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
    statement: $checkedConvert(
      'statement',
      (v) => $enumDecode(_$BacktestJobResultStatementEnumEnumMap, v),
    ),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>)
          .map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
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
          $enumDecode(_$BacktestJobResultFeedbackMappingEnumEnumMap, e),
        ),
      ),
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
      (v) => (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as num)),
    ),
    jobId: $checkedConvert('jobId', (v) => v as String?),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$BacktestJobResultStatusEnumEnumMap, v),
    ),
    outcome: $checkedConvert(
      'outcome',
      (v) => $enumDecodeNullable(_$BacktestJobResultOutcomeEnumEnumMap, v),
    ),
    reason: $checkedConvert(
      'reason',
      (v) => $enumDecodeNullable(_$BacktestJobResultReasonEnumEnumMap, v),
    ),
    diffSummary: $checkedConvert('diffSummary', (v) => v as String?),
    message: $checkedConvert('message', (v) => v as String?),
    startedAt: $checkedConvert(
      'startedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    detector: $checkedConvert(
      'detector',
      (v) => DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
    definitionHash: $checkedConvert('definitionHash', (v) => v as String?),
    target: $checkedConvert('target', (v) => v as String),
    frequency: $checkedConvert(
      'frequency',
      (v) => $enumDecode(_$BacktestJobResultFrequencyEnumEnumMap, v),
    ),
    transform: $checkedConvert(
      'transform',
      (v) => $enumDecodeNullable(_$BacktestJobResultTransformEnumEnumMap, v),
    ),
    windows: $checkedConvert(
      'windows',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    range: $checkedConvert(
      'range',
      (v) => v == null ? null : TimeRange.fromJson(v as Map<String, dynamic>),
    ),
    asOfSeq: $checkedConvert('asOfSeq', (v) => (v as num?)?.toInt()),
    placeboSeed: $checkedConvert('placeboSeed', (v) => v as String?),
    nEvents: $checkedConvert('nEvents', (v) => (v as num).toInt()),
    nClusters: $checkedConvert('nClusters', (v) => (v as num?)?.toInt()),
    nExcluded: $checkedConvert('nExcluded', (v) => (v as num?)?.toInt()),
    caar: $checkedConvert(
      'caar',
      (v) => EventStudyResultCaar.fromJson(v as Map<String, dynamic>),
    ),
    valuesWithheld: $checkedConvert('valuesWithheld', (v) => v as bool?),
    truncated: $checkedConvert('truncated', (v) => v as bool?),
    id: $checkedConvert('id', (v) => v as String),
    mode: $checkedConvert(
      'mode',
      (v) => $enumDecode(_$BacktestJobResultModeEnumEnumMap, v),
    ),
    trigger: $checkedConvert(
      'trigger',
      (v) => $enumDecodeNullable(_$BacktestJobResultTriggerEnumEnumMap, v),
    ),
    candidatesGenerated: $checkedConvert(
      'candidatesGenerated',
      (v) => (v as num).toInt(),
    ),
    tested: $checkedConvert('tested', (v) => (v as num).toInt()),
    capped: $checkedConvert('capped', (v) => (v as num).toInt()),
    insufficient: $checkedConvert('insufficient', (v) => (v as num?)?.toInt()),
    survivors: $checkedConvert('survivors', (v) => (v as num).toInt()),
    validated: $checkedConvert('validated', (v) => (v as num).toInt()),
    q: $checkedConvert('q', (v) => v as num),
    method: $checkedConvert(
      'method',
      (v) => $enumDecode(_$BacktestJobResultMethodEnumEnumMap, v),
    ),
    error: $checkedConvert('error', (v) => v as String?),
    dataset: $checkedConvert(
      'dataset',
      (v) => $enumDecode(_$BacktestJobResultDatasetEnumEnumMap, v),
    ),
    params: $checkedConvert(
      'params',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    statusReason: $checkedConvert('statusReason', (v) => v as String?),
    progress: $checkedConvert('progress', (v) => v as num?),
    fileSha256: $checkedConvert('fileSha256', (v) => v as String?),
    rowsRead: $checkedConvert('rowsRead', (v) => (v as num).toInt()),
    rowsWritten: $checkedConvert('rowsWritten', (v) => (v as num).toInt()),
    periodFrom: $checkedConvert(
      'periodFrom',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    periodTo: $checkedConvert(
      'periodTo',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    licence: $checkedConvert('licence', (v) => v as String),
    egress: $checkedConvert(
      'egress',
      (v) => $enumDecode(_$BacktestJobResultEgressEnumEnumMap, v),
    ),
    createdBy: $checkedConvert('createdBy', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    profiles: $checkedConvert('profiles', (v) => (v as num).toInt()),
    rates: $checkedConvert('rates', (v) => (v as num).toInt()),
    skipped: $checkedConvert('skipped', (v) => (v as num?)?.toInt()),
    removed: $checkedConvert('removed', (v) => (v as num?)?.toInt()),
    families: $checkedConvert(
      'families',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    regions: $checkedConvert('regions', (v) => (v as num?)?.toInt()),
    computer: $checkedConvert(
      'computer',
      (v) => v == null ? null : DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$BacktestJobResultToJson(BacktestJobResult instance) =>
    <String, dynamic>{
      'ruleType': _$BacktestJobResultRuleTypeEnumEnumMap[instance.ruleType]!,
      'wouldFire': instance.wouldFire,
      'perDay': ?instance.perDay?.map((e) => e.toJson()).toList(),
      'topEntities': ?instance.topEntities?.map((e) => e.toJson()).toList(),
      'samples': ?instance.samples?.map((e) => e.toJson()).toList(),
      'actual': ?instance.actual?.toJson(),
      'overlap': ?instance.overlap?.toJson(),
      'window': ?instance.window?.toJson(),
      'anomaly': ?instance.anomaly?.toJson(),
      'variants': ?instance.variants?.map((e) => e.toJson()).toList(),
      'recipeId': instance.recipeId,
      'recipeVersion': ?instance.recipeVersion,
      'events': instance.events.map((e) => e.toJson()).toList(),
      'countsByType': ?instance.countsByType,
      'testerVersion': instance.testerVersion,
      'nAntecedent': instance.nAntecedent,
      'nConsequent': ?instance.nConsequent,
      'hits': instance.hits,
      'hitRate': ?instance.hitRate,
      'hitRateCi': ?instance.hitRateCi?.toJson(),
      'lag': ?instance.lag?.toJson(),
      'baseRate': ?instance.baseRate,
      'lift': ?instance.lift,
      'pValue': ?instance.pValue,
      'permutations':
          ?_$BacktestJobResultPermutationsEnumEnumMap[instance.permutations],
      'seed': ?instance.seed,
      'reverse': ?instance.reverse?.toJson(),
      'coOccurrenceNotPrecedence': ?instance.coOccurrenceNotPrecedence,
      'verdict': _$BacktestJobResultVerdictEnumEnumMap[instance.verdict]!,
      'multipleTesting': ?instance.multipleTesting?.toJson(),
      'statement': _$BacktestJobResultStatementEnumEnumMap[instance.statement]!,
      'caveats': instance.caveats.map((e) => e.toJson()).toList(),
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
        (k, e) =>
            MapEntry(k, _$BacktestJobResultFeedbackMappingEnumEnumMap[e]!),
      ),
      'signals': ?instance.signals?.toJson(),
      'signalScore': ?instance.signalScore,
      'daily': ?instance.daily?.map((e) => e.toJson()).toList(),
      'signalWeights': ?instance.signalWeights,
      'jobId': instance.jobId,
      'manifestId': instance.manifestId,
      'status': _$BacktestJobResultStatusEnumEnumMap[instance.status]!,
      'outcome': ?_$BacktestJobResultOutcomeEnumEnumMap[instance.outcome],
      'reason': ?_$BacktestJobResultReasonEnumEnumMap[instance.reason],
      'diffSummary': ?instance.diffSummary,
      'message': ?instance.message,
      'startedAt': instance.startedAt?.toIso8601String(),
      'finishedAt': ?instance.finishedAt?.toIso8601String(),
      'detector': instance.detector.toJson(),
      'definitionHash': ?instance.definitionHash,
      'target': instance.target,
      'frequency': _$BacktestJobResultFrequencyEnumEnumMap[instance.frequency]!,
      'transform': ?_$BacktestJobResultTransformEnumEnumMap[instance.transform],
      'windows': instance.windows,
      'range': ?instance.range?.toJson(),
      'asOfSeq': ?instance.asOfSeq,
      'placeboSeed': ?instance.placeboSeed,
      'nEvents': instance.nEvents,
      'nClusters': ?instance.nClusters,
      'nExcluded': ?instance.nExcluded,
      'caar': instance.caar.toJson(),
      'valuesWithheld': ?instance.valuesWithheld,
      'truncated': ?instance.truncated,
      'id': instance.id,
      'mode': _$BacktestJobResultModeEnumEnumMap[instance.mode]!,
      'trigger': ?_$BacktestJobResultTriggerEnumEnumMap[instance.trigger],
      'candidatesGenerated': instance.candidatesGenerated,
      'tested': instance.tested,
      'capped': instance.capped,
      'insufficient': ?instance.insufficient,
      'survivors': instance.survivors,
      'validated': instance.validated,
      'q': instance.q,
      'method': _$BacktestJobResultMethodEnumEnumMap[instance.method]!,
      'error': ?instance.error,
      'dataset': _$BacktestJobResultDatasetEnumEnumMap[instance.dataset]!,
      'params': ?instance.params,
      'statusReason': ?instance.statusReason,
      'progress': ?instance.progress,
      'fileSha256': ?instance.fileSha256,
      'rowsRead': instance.rowsRead,
      'rowsWritten': instance.rowsWritten,
      'periodFrom': ?instance.periodFrom?.toIso8601String(),
      'periodTo': ?instance.periodTo?.toIso8601String(),
      'licence': instance.licence,
      'egress': _$BacktestJobResultEgressEnumEnumMap[instance.egress]!,
      'createdBy': instance.createdBy,
      'createdAt': instance.createdAt.toIso8601String(),
      'profiles': instance.profiles,
      'rates': instance.rates,
      'skipped': ?instance.skipped,
      'removed': ?instance.removed,
      'families': instance.families,
      'regions': ?instance.regions,
      'computer': ?instance.computer?.toJson(),
    };

const _$BacktestJobResultRuleTypeEnumEnumMap = {
  BacktestJobResultRuleTypeEnum.record: 'record',
  BacktestJobResultRuleTypeEnum.count: 'count',
  BacktestJobResultRuleTypeEnum.absence: 'absence',
  BacktestJobResultRuleTypeEnum.anomaly: 'anomaly',
};

const _$BacktestJobResultPermutationsEnumEnumMap = {
  BacktestJobResultPermutationsEnum.number200: 200,
};

const _$BacktestJobResultVerdictEnumEnumMap = {
  BacktestJobResultVerdictEnum.insufficient: 'insufficient',
  BacktestJobResultVerdictEnum.noEvidenceOfPrecedence:
      'no-evidence-of-precedence',
  BacktestJobResultVerdictEnum.weak: 'weak',
  BacktestJobResultVerdictEnum.notable: 'notable',
};

const _$BacktestJobResultStatementEnumEnumMap = {
  BacktestJobResultStatementEnum.associationInStoredDataSemicolonNotCausation:
      'association in stored data; not causation',
};

const _$BacktestJobResultFeedbackMappingEnumEnumMap = {
  BacktestJobResultFeedbackMappingEnum.positive: 'positive',
  BacktestJobResultFeedbackMappingEnum.negative: 'negative',
  BacktestJobResultFeedbackMappingEnum.neutral: 'neutral',
};

const _$BacktestJobResultStatusEnumEnumMap = {
  BacktestJobResultStatusEnum.queued: 'queued',
  BacktestJobResultStatusEnum.running: 'running',
  BacktestJobResultStatusEnum.done: 'done',
  BacktestJobResultStatusEnum.failed: 'failed',
  BacktestJobResultStatusEnum.skipped: 'skipped',
  BacktestJobResultStatusEnum.cancelled: 'cancelled',
};

const _$BacktestJobResultOutcomeEnumEnumMap = {
  BacktestJobResultOutcomeEnum.identical: 'identical',
  BacktestJobResultOutcomeEnum.differs: 'differs',
  BacktestJobResultOutcomeEnum.notReplayable: 'not-replayable',
};

const _$BacktestJobResultReasonEnumEnumMap = {
  BacktestJobResultReasonEnum.inputsPurged: 'inputs-purged',
  BacktestJobResultReasonEnum.componentRemoved: 'component-removed',
  BacktestJobResultReasonEnum.llmOutput: 'llm-output',
};

const _$BacktestJobResultFrequencyEnumEnumMap = {
  BacktestJobResultFrequencyEnum.day: 'day',
  BacktestJobResultFrequencyEnum.week: 'week',
};

const _$BacktestJobResultTransformEnumEnumMap = {
  BacktestJobResultTransformEnum.logReturn: 'log-return',
  BacktestJobResultTransformEnum.logLevelStandardised: 'log-level-standardised',
  BacktestJobResultTransformEnum.weeklyDifferenceStandardised:
      'weekly-difference-standardised',
  BacktestJobResultTransformEnum.anscombeResidual: 'anscombe-residual',
};

const _$BacktestJobResultModeEnumEnumMap = {
  BacktestJobResultModeEnum.mine: 'mine',
  BacktestJobResultModeEnum.retest: 'retest',
};

const _$BacktestJobResultTriggerEnumEnumMap = {
  BacktestJobResultTriggerEnum.schedule: 'schedule',
  BacktestJobResultTriggerEnum.manual: 'manual',
  BacktestJobResultTriggerEnum.cycle: 'cycle',
};

const _$BacktestJobResultMethodEnumEnumMap = {
  BacktestJobResultMethodEnum.BH: 'BH',
  BacktestJobResultMethodEnum.BY: 'BY',
};

const _$BacktestJobResultDatasetEnumEnumMap = {
  BacktestJobResultDatasetEnum.ucdpGed: 'ucdp-ged',
  BacktestJobResultDatasetEnum.acled: 'acled',
  BacktestJobResultDatasetEnum.usgsComcat: 'usgs-comcat',
  BacktestJobResultDatasetEnum.gdacs: 'gdacs',
};

const _$BacktestJobResultEgressEnumEnumMap = {
  BacktestJobResultEgressEnum.OPEN: 'OPEN',
  BacktestJobResultEgressEnum.LOCAL_ONLY: 'LOCAL_ONLY',
};
