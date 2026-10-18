//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/precursor_test_result_lag.dart';
import 'package:foresignal_api/src/model/rule_backtest_result.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_samples_inner.dart';
import 'package:foresignal_api/src/model/precursor_test_result_reverse.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_per_day_inner.dart';
import 'package:foresignal_api/src/model/replay_result.dart';
import 'package:foresignal_api/src/model/recipe_backtest_result_overlap.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_variants_inner.dart';
import 'package:foresignal_api/src/model/precursor_batch.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_actual.dart';
import 'package:foresignal_api/src/model/precursor_test_result_hit_rate_ci.dart';
import 'package:foresignal_api/src/model/precursor_test_result_multiple_testing.dart';
import 'package:foresignal_api/src/model/precursor_test_result.dart';
import 'package:foresignal_api/src/model/event_study_result_events_inner.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/event_study_result_caar.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/recipe_backtest_result.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_window.dart';
import 'package:foresignal_api/src/model/reference_import.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_anomaly.dart';
import 'package:foresignal_api/src/model/rule_quality_daily.dart';
import 'package:foresignal_api/src/model/base_rate_refresh_result.dart';
import 'package:foresignal_api/src/model/rule_quality_signals.dart';
import 'package:foresignal_api/src/model/rule_quality_precision_ci.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:foresignal_api/src/model/event_study_result.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_top_entities_inner.dart';
import 'package:foresignal_api/src/model/rule_quality.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'backtest_job_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BacktestJobResult {
  /// Returns a new [BacktestJobResult] instance.
  BacktestJobResult({

    required  this.ruleType,

    required  this.wouldFire,

     this.perDay,

     this.topEntities,

     this.samples,

     this.actual,

     this.overlap,

     this.window,

     this.anomaly,

     this.variants,

    required  this.recipeId,

     this.recipeVersion,

    required  this.events,

     this.countsByType,

    required  this.testerVersion,

    required  this.nAntecedent,

     this.nConsequent,

    required  this.hits,

     this.hitRate,

     this.hitRateCi,

     this.lag,

     this.baseRate,

     this.lift,

     this.pValue,

     this.permutations,

     this.seed,

     this.reverse,

     this.coOccurrenceNotPrecedence,

    required  this.verdict,

     this.multipleTesting,

    required  this.statement,

    required  this.caveats,

    required  this.ruleId,

    required  this.since,

    required  this.alerts,

    required  this.labelled,

     this.labelledShare,

    required  this.positive,

    required  this.negative,

    required  this.neutral,

     this.precision,

     this.precisionCi,

     this.medianTimeToAck,

     this.medianTimeToResolve,

     this.flapping,

     this.duplicates,

     this.severityDistribution,

     this.feedbackMapping,

     this.signals,

     this.signalScore,

     this.daily,

     this.signalWeights,

    required  this.jobId,

    required  this.manifestId,

    required  this.status,

     this.outcome,

     this.reason,

     this.diffSummary,

     this.message,

    required  this.startedAt,

     this.finishedAt,

    required  this.detector,

     this.definitionHash,

    required  this.target,

    required  this.frequency,

     this.transform,

    required  this.windows,

     this.range,

     this.asOfSeq,

     this.placeboSeed,

    required  this.nEvents,

     this.nClusters,

     this.nExcluded,

    required  this.caar,

     this.valuesWithheld,

     this.truncated,

    required  this.id,

    required  this.mode,

     this.trigger,

    required  this.candidatesGenerated,

    required  this.tested,

    required  this.capped,

     this.insufficient,

    required  this.survivors,

    required  this.validated,

    required  this.q,

    required  this.method,

     this.error,

    required  this.dataset,

     this.params,

     this.statusReason,

     this.progress,

     this.fileSha256,

    required  this.rowsRead,

    required  this.rowsWritten,

     this.periodFrom,

     this.periodTo,

    required  this.licence,

    required  this.egress,

    required  this.createdBy,

    required  this.createdAt,

    required  this.profiles,

    required  this.rates,

     this.skipped,

     this.removed,

    required  this.families,

     this.regions,

     this.computer,
  });

  @JsonKey(
    
    name: r'ruleType',
    required: true,
    includeIfNull: false,
  )


  final BacktestJobResultRuleTypeEnum ruleType;



  @JsonKey(
    
    name: r'wouldFire',
    required: true,
    includeIfNull: false,
  )


  final int wouldFire;



  @JsonKey(
    
    name: r'perDay',
    required: false,
    includeIfNull: false,
  )


  final List<RuleBacktestResultPerDayInner>? perDay;



  @JsonKey(
    
    name: r'topEntities',
    required: false,
    includeIfNull: false,
  )


  final List<RuleBacktestResultTopEntitiesInner>? topEntities;



  @JsonKey(
    
    name: r'samples',
    required: false,
    includeIfNull: false,
  )


  final List<RuleBacktestResultSamplesInner>? samples;



  @JsonKey(
    
    name: r'actual',
    required: false,
    includeIfNull: false,
  )


  final RuleBacktestResultActual? actual;



  @JsonKey(
    
    name: r'overlap',
    required: false,
    includeIfNull: false,
  )


  final RecipeBacktestResultOverlap? overlap;



  @JsonKey(
    
    name: r'window',
    required: false,
    includeIfNull: false,
  )


  final RuleBacktestResultWindow? window;



  @JsonKey(
    
    name: r'anomaly',
    required: false,
    includeIfNull: false,
  )


  final RuleBacktestResultAnomaly? anomaly;



  @JsonKey(
    
    name: r'variants',
    required: false,
    includeIfNull: false,
  )


  final List<RuleBacktestResultVariantsInner>? variants;



  @JsonKey(
    
    name: r'recipeId',
    required: true,
    includeIfNull: false,
  )


  final String recipeId;



  @JsonKey(
    
    name: r'recipeVersion',
    required: false,
    includeIfNull: false,
  )


  final int? recipeVersion;



  @JsonKey(
    
    name: r'events',
    required: true,
    includeIfNull: false,
  )


  final List<EventStudyResultEventsInner> events;



  @JsonKey(
    
    name: r'countsByType',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? countsByType;



  @JsonKey(
    
    name: r'testerVersion',
    required: true,
    includeIfNull: false,
  )


  final String testerVersion;



  @JsonKey(
    
    name: r'nAntecedent',
    required: true,
    includeIfNull: false,
  )


  final int nAntecedent;



  @JsonKey(
    
    name: r'nConsequent',
    required: false,
    includeIfNull: false,
  )


  final int? nConsequent;



  @JsonKey(
    
    name: r'hits',
    required: true,
    includeIfNull: false,
  )


  final int hits;



  @JsonKey(
    
    name: r'hitRate',
    required: false,
    includeIfNull: false,
  )


  final num? hitRate;



  @JsonKey(
    
    name: r'hitRateCi',
    required: false,
    includeIfNull: false,
  )


  final PrecursorTestResultHitRateCi? hitRateCi;



  @JsonKey(
    
    name: r'lag',
    required: false,
    includeIfNull: false,
  )


  final PrecursorTestResultLag? lag;



      /// Mean hit rate of 200 circular time-shifted copies
  @JsonKey(
    
    name: r'baseRate',
    required: false,
    includeIfNull: false,
  )


  final num? baseRate;



  @JsonKey(
    
    name: r'lift',
    required: false,
    includeIfNull: false,
  )


  final num? lift;



      /// (1 + #shifted ≥ observed) ÷ 201
  @JsonKey(
    
    name: r'pValue',
    required: false,
    includeIfNull: false,
  )


  final num? pValue;



  @JsonKey(
    
    name: r'permutations',
    required: false,
    includeIfNull: false,
  )


  final BacktestJobResultPermutationsEnum? permutations;



      /// Deterministic seed (hash of definition + range)
  @JsonKey(
    
    name: r'seed',
    required: false,
    includeIfNull: false,
  )


  final String? seed;



  @JsonKey(
    
    name: r'reverse',
    required: false,
    includeIfNull: false,
  )


  final PrecursorTestResultReverse? reverse;



  @JsonKey(
    
    name: r'coOccurrenceNotPrecedence',
    required: false,
    includeIfNull: false,
  )


  final bool? coOccurrenceNotPrecedence;



  @JsonKey(
    
    name: r'verdict',
    required: true,
    includeIfNull: false,
  )


  final BacktestJobResultVerdictEnum verdict;



  @JsonKey(
    
    name: r'multipleTesting',
    required: false,
    includeIfNull: false,
  )


  final PrecursorTestResultMultipleTesting? multipleTesting;



  @JsonKey(
    
    name: r'statement',
    required: true,
    includeIfNull: false,
  )


  final BacktestJobResultStatementEnum statement;



      /// 103 caveats plus `FEW_EVENTS`, `LAGGED_PUBLICATION`, `DAILY_RESOLUTION_ONLY`
  @JsonKey(
    
    name: r'caveats',
    required: true,
    includeIfNull: false,
  )


  final List<Caveat> caveats;



  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'since',
    required: true,
    includeIfNull: false,
  )


  final DateTime since;



  @JsonKey(
    
    name: r'alerts',
    required: true,
    includeIfNull: false,
  )


  final int alerts;



  @JsonKey(
    
    name: r'labelled',
    required: true,
    includeIfNull: false,
  )


  final int labelled;



  @JsonKey(
    
    name: r'labelledShare',
    required: false,
    includeIfNull: false,
  )


  final num? labelledShare;



  @JsonKey(
    
    name: r'positive',
    required: true,
    includeIfNull: false,
  )


  final int positive;



  @JsonKey(
    
    name: r'negative',
    required: true,
    includeIfNull: false,
  )


  final int negative;



  @JsonKey(
    
    name: r'neutral',
    required: true,
    includeIfNull: false,
  )


  final int neutral;



      /// positive ÷ (positive + negative); null when labelled < 5
  @JsonKey(
    
    name: r'precision',
    required: false,
    includeIfNull: false,
  )


  final num? precision;



  @JsonKey(
    
    name: r'precisionCi',
    required: false,
    includeIfNull: false,
  )


  final RuleQualityPrecisionCi? precisionCi;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'medianTimeToAck',
    required: false,
    includeIfNull: false,
  )


  final String? medianTimeToAck;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'medianTimeToResolve',
    required: false,
    includeIfNull: false,
  )


  final String? medianTimeToResolve;



      /// enter → exit within 10 min
  @JsonKey(
    
    name: r'flapping',
    required: false,
    includeIfNull: false,
  )


  final int? flapping;



      /// Same entity within 2 × cooldown
  @JsonKey(
    
    name: r'duplicates',
    required: false,
    includeIfNull: false,
  )


  final int? duplicates;



  @JsonKey(
    
    name: r'severityDistribution',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? severityDistribution;



  @JsonKey(
    
    name: r'feedbackMapping',
    required: false,
    includeIfNull: false,
  )


  final Map<String, BacktestJobResultFeedbackMappingEnum>? feedbackMapping;



  @JsonKey(
    
    name: r'signals',
    required: false,
    includeIfNull: false,
  )


  final RuleQualitySignals? signals;



      /// Weighted implicit-signal score (ACK_FAST +0.5, SNOOZED −0.5, FLAP −0.5, UNOPENED −0.25), shown next to, never mixed into, feedback precision
  @JsonKey(
    
    name: r'signalScore',
    required: false,
    includeIfNull: false,
  )


  final num? signalScore;



      /// Daily trend rows since `since`
  @JsonKey(
    
    name: r'daily',
    required: false,
    includeIfNull: false,
  )


  final List<RuleQualityDaily>? daily;



      /// The weight table of signalScore (ACK_FAST 0.5, SNOOZED -0.5, FLAP -0.5, UNOPENED -0.25); signalScore = sum(weight x count) / alerts
  @JsonKey(
    
    name: r'signalWeights',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num>? signalWeights;



      /// ULID identifier
  @JsonKey(
    
    name: r'jobId',
    required: true,
    includeIfNull: true,
  )


  final String? jobId;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: true,
    includeIfNull: true,
  )


  final String? manifestId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final BacktestJobResultStatusEnum status;



  @JsonKey(
    
    name: r'outcome',
    required: false,
    includeIfNull: false,
  )


  final BacktestJobResultOutcomeEnum? outcome;



      /// When not-replayable
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final BacktestJobResultReasonEnum? reason;



      /// When differs
  @JsonKey(
    
    name: r'diffSummary',
    required: false,
    includeIfNull: false,
  )


  final String? diffSummary;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'startedAt',
    required: true,
    includeIfNull: true,
  )


  final DateTime? startedAt;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



  @JsonKey(
    
    name: r'detector',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef detector;



  @JsonKey(
    
    name: r'definitionHash',
    required: false,
    includeIfNull: false,
  )


  final String? definitionHash;



      /// Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left, center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement, every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the platform-language denominator; complete and paged collection only)
  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final String target;



  @JsonKey(
    
    name: r'frequency',
    required: true,
    includeIfNull: false,
  )


  final BacktestJobResultFrequencyEnum frequency;



  @JsonKey(
    
    name: r'transform',
    required: false,
    includeIfNull: false,
  )


  final BacktestJobResultTransformEnum? transform;



      /// The global range and its split (`start`, `split`, `end`, `discoveryFraction`); for a re-test, `split` starts the re-test window
  @JsonKey(
    
    name: r'windows',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> windows;



  @JsonKey(
    
    name: r'range',
    required: false,
    includeIfNull: false,
  )


  final TimeRange? range;



  @JsonKey(
    
    name: r'asOfSeq',
    required: false,
    includeIfNull: false,
  )


  final int? asOfSeq;



      /// Deterministic seed (hash of definition and range)
  @JsonKey(
    
    name: r'placeboSeed',
    required: false,
    includeIfNull: false,
  )


  final String? placeboSeed;



  @JsonKey(
    
    name: r'nEvents',
    required: true,
    includeIfNull: false,
  )


  final int nEvents;



  @JsonKey(
    
    name: r'nClusters',
    required: false,
    includeIfNull: false,
  )


  final int? nClusters;



  @JsonKey(
    
    name: r'nExcluded',
    required: false,
    includeIfNull: false,
  )


  final int? nExcluded;



  @JsonKey(
    
    name: r'caar',
    required: true,
    includeIfNull: false,
  )


  final EventStudyResultCaar caar;



  @JsonKey(
    
    name: r'valuesWithheld',
    required: false,
    includeIfNull: false,
  )


  final bool? valuesWithheld;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final BacktestJobResultModeEnum mode;



  @JsonKey(
    
    name: r'trigger',
    required: false,
    includeIfNull: false,
  )


  final BacktestJobResultTriggerEnum? trigger;



  @JsonKey(
    
    name: r'candidatesGenerated',
    required: true,
    includeIfNull: false,
  )


  final int candidatesGenerated;



  @JsonKey(
    
    name: r'tested',
    required: true,
    includeIfNull: false,
  )


  final int tested;



      /// Candidates dropped by `analysis.precursors.maxTestsPerRun`
  @JsonKey(
    
    name: r'capped',
    required: true,
    includeIfNull: false,
  )


  final int capped;



      /// Candidates skipped as insufficient (range under 60 days, coverage gaps over 30%, fewer than 10 antecedents or too little series overlap)
  @JsonKey(
    
    name: r'insufficient',
    required: false,
    includeIfNull: false,
  )


  final int? insufficient;



  @JsonKey(
    
    name: r'survivors',
    required: true,
    includeIfNull: false,
  )


  final int survivors;



  @JsonKey(
    
    name: r'validated',
    required: true,
    includeIfNull: false,
  )


  final int validated;



  @JsonKey(
    
    name: r'q',
    required: true,
    includeIfNull: false,
  )


  final num q;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final BacktestJobResultMethodEnum method;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



  @JsonKey(
    
    name: r'dataset',
    required: true,
    includeIfNull: false,
  )


  final BacktestJobResultDatasetEnum dataset;



  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? params;



      /// e.g. `credentials absent` for ACLED
  @JsonKey(
    
    name: r'statusReason',
    required: false,
    includeIfNull: false,
  )


  final String? statusReason;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'progress',
    required: false,
    includeIfNull: false,
  )


  final num? progress;



  @JsonKey(
    
    name: r'fileSha256',
    required: false,
    includeIfNull: false,
  )


  final String? fileSha256;



  @JsonKey(
    
    name: r'rowsRead',
    required: true,
    includeIfNull: false,
  )


  final int rowsRead;



  @JsonKey(
    
    name: r'rowsWritten',
    required: true,
    includeIfNull: false,
  )


  final int rowsWritten;



  @JsonKey(
    
    name: r'periodFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? periodFrom;



  @JsonKey(
    
    name: r'periodTo',
    required: false,
    includeIfNull: false,
  )


  final DateTime? periodTo;



      /// e.g. `CC-BY-4.0`, `ACLED-EULA`, `public-domain`
  @JsonKey(
    
    name: r'licence',
    required: true,
    includeIfNull: false,
  )


  final String licence;



  @JsonKey(
    
    name: r'egress',
    required: true,
    includeIfNull: false,
  )


  final BacktestJobResultEgressEnum egress;



  @JsonKey(
    
    name: r'createdBy',
    required: true,
    includeIfNull: false,
  )


  final String createdBy;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



      /// Region profiles written
  @JsonKey(
    
    name: r'profiles',
    required: true,
    includeIfNull: false,
  )


  final int profiles;



      /// Automatic rates written (family × region × window)
  @JsonKey(
    
    name: r'rates',
    required: true,
    includeIfNull: false,
  )


  final int rates;



      /// Family-region pairs with under 52 covered weeks
  @JsonKey(
    
    name: r'skipped',
    required: false,
    includeIfNull: false,
  )


  final int? skipped;



      /// Automatic rates no longer produced and removed
  @JsonKey(
    
    name: r'removed',
    required: false,
    includeIfNull: false,
  )


  final int? removed;



  @JsonKey(
    
    name: r'families',
    required: true,
    includeIfNull: false,
  )


  final List<String> families;



      /// Regions with at least one automatic rate
  @JsonKey(
    
    name: r'regions',
    required: false,
    includeIfNull: false,
  )


  final int? regions;



  @JsonKey(
    
    name: r'computer',
    required: false,
    includeIfNull: false,
  )


  final DetectorRef? computer;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BacktestJobResult &&
      other.ruleType == ruleType &&
      other.wouldFire == wouldFire &&
      other.perDay == perDay &&
      other.topEntities == topEntities &&
      other.samples == samples &&
      other.actual == actual &&
      other.overlap == overlap &&
      other.window == window &&
      other.anomaly == anomaly &&
      other.variants == variants &&
      other.recipeId == recipeId &&
      other.recipeVersion == recipeVersion &&
      other.events == events &&
      other.countsByType == countsByType &&
      other.testerVersion == testerVersion &&
      other.nAntecedent == nAntecedent &&
      other.nConsequent == nConsequent &&
      other.hits == hits &&
      other.hitRate == hitRate &&
      other.hitRateCi == hitRateCi &&
      other.lag == lag &&
      other.baseRate == baseRate &&
      other.lift == lift &&
      other.pValue == pValue &&
      other.permutations == permutations &&
      other.seed == seed &&
      other.reverse == reverse &&
      other.coOccurrenceNotPrecedence == coOccurrenceNotPrecedence &&
      other.verdict == verdict &&
      other.multipleTesting == multipleTesting &&
      other.statement == statement &&
      other.caveats == caveats &&
      other.ruleId == ruleId &&
      other.since == since &&
      other.alerts == alerts &&
      other.labelled == labelled &&
      other.labelledShare == labelledShare &&
      other.positive == positive &&
      other.negative == negative &&
      other.neutral == neutral &&
      other.precision == precision &&
      other.precisionCi == precisionCi &&
      other.medianTimeToAck == medianTimeToAck &&
      other.medianTimeToResolve == medianTimeToResolve &&
      other.flapping == flapping &&
      other.duplicates == duplicates &&
      other.severityDistribution == severityDistribution &&
      other.feedbackMapping == feedbackMapping &&
      other.signals == signals &&
      other.signalScore == signalScore &&
      other.daily == daily &&
      other.signalWeights == signalWeights &&
      other.jobId == jobId &&
      other.manifestId == manifestId &&
      other.status == status &&
      other.outcome == outcome &&
      other.reason == reason &&
      other.diffSummary == diffSummary &&
      other.message == message &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.detector == detector &&
      other.definitionHash == definitionHash &&
      other.target == target &&
      other.frequency == frequency &&
      other.transform == transform &&
      other.windows == windows &&
      other.range == range &&
      other.asOfSeq == asOfSeq &&
      other.placeboSeed == placeboSeed &&
      other.nEvents == nEvents &&
      other.nClusters == nClusters &&
      other.nExcluded == nExcluded &&
      other.caar == caar &&
      other.valuesWithheld == valuesWithheld &&
      other.truncated == truncated &&
      other.id == id &&
      other.mode == mode &&
      other.trigger == trigger &&
      other.candidatesGenerated == candidatesGenerated &&
      other.tested == tested &&
      other.capped == capped &&
      other.insufficient == insufficient &&
      other.survivors == survivors &&
      other.validated == validated &&
      other.q == q &&
      other.method == method &&
      other.error == error &&
      other.dataset == dataset &&
      other.params == params &&
      other.statusReason == statusReason &&
      other.progress == progress &&
      other.fileSha256 == fileSha256 &&
      other.rowsRead == rowsRead &&
      other.rowsWritten == rowsWritten &&
      other.periodFrom == periodFrom &&
      other.periodTo == periodTo &&
      other.licence == licence &&
      other.egress == egress &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt &&
      other.profiles == profiles &&
      other.rates == rates &&
      other.skipped == skipped &&
      other.removed == removed &&
      other.families == families &&
      other.regions == regions &&
      other.computer == computer;

    @override
    int get hashCode =>
        ruleType.hashCode +
        wouldFire.hashCode +
        perDay.hashCode +
        topEntities.hashCode +
        samples.hashCode +
        actual.hashCode +
        overlap.hashCode +
        (window == null ? 0 : window.hashCode) +
        (anomaly == null ? 0 : anomaly.hashCode) +
        variants.hashCode +
        recipeId.hashCode +
        recipeVersion.hashCode +
        events.hashCode +
        countsByType.hashCode +
        testerVersion.hashCode +
        nAntecedent.hashCode +
        nConsequent.hashCode +
        hits.hashCode +
        (hitRate == null ? 0 : hitRate.hashCode) +
        hitRateCi.hashCode +
        lag.hashCode +
        (baseRate == null ? 0 : baseRate.hashCode) +
        (lift == null ? 0 : lift.hashCode) +
        (pValue == null ? 0 : pValue.hashCode) +
        permutations.hashCode +
        seed.hashCode +
        reverse.hashCode +
        coOccurrenceNotPrecedence.hashCode +
        verdict.hashCode +
        multipleTesting.hashCode +
        statement.hashCode +
        caveats.hashCode +
        ruleId.hashCode +
        since.hashCode +
        alerts.hashCode +
        labelled.hashCode +
        labelledShare.hashCode +
        positive.hashCode +
        negative.hashCode +
        neutral.hashCode +
        (precision == null ? 0 : precision.hashCode) +
        (precisionCi == null ? 0 : precisionCi.hashCode) +
        (medianTimeToAck == null ? 0 : medianTimeToAck.hashCode) +
        (medianTimeToResolve == null ? 0 : medianTimeToResolve.hashCode) +
        flapping.hashCode +
        duplicates.hashCode +
        severityDistribution.hashCode +
        feedbackMapping.hashCode +
        signals.hashCode +
        (signalScore == null ? 0 : signalScore.hashCode) +
        daily.hashCode +
        signalWeights.hashCode +
        (jobId == null ? 0 : jobId.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode) +
        status.hashCode +
        (outcome == null ? 0 : outcome.hashCode) +
        (reason == null ? 0 : reason.hashCode) +
        (diffSummary == null ? 0 : diffSummary.hashCode) +
        (message == null ? 0 : message.hashCode) +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        detector.hashCode +
        definitionHash.hashCode +
        target.hashCode +
        frequency.hashCode +
        transform.hashCode +
        windows.hashCode +
        range.hashCode +
        asOfSeq.hashCode +
        placeboSeed.hashCode +
        nEvents.hashCode +
        nClusters.hashCode +
        nExcluded.hashCode +
        caar.hashCode +
        valuesWithheld.hashCode +
        truncated.hashCode +
        id.hashCode +
        mode.hashCode +
        trigger.hashCode +
        candidatesGenerated.hashCode +
        tested.hashCode +
        capped.hashCode +
        insufficient.hashCode +
        survivors.hashCode +
        validated.hashCode +
        q.hashCode +
        method.hashCode +
        (error == null ? 0 : error.hashCode) +
        dataset.hashCode +
        params.hashCode +
        (statusReason == null ? 0 : statusReason.hashCode) +
        (progress == null ? 0 : progress.hashCode) +
        (fileSha256 == null ? 0 : fileSha256.hashCode) +
        rowsRead.hashCode +
        rowsWritten.hashCode +
        (periodFrom == null ? 0 : periodFrom.hashCode) +
        (periodTo == null ? 0 : periodTo.hashCode) +
        licence.hashCode +
        egress.hashCode +
        createdBy.hashCode +
        createdAt.hashCode +
        profiles.hashCode +
        rates.hashCode +
        skipped.hashCode +
        removed.hashCode +
        families.hashCode +
        regions.hashCode +
        computer.hashCode;

  factory BacktestJobResult.fromJson(Map<String, dynamic> json) => _$BacktestJobResultFromJson(json);

  Map<String, dynamic> toJson() => _$BacktestJobResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BacktestJobResultRuleTypeEnum {
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'count')
count(r'count'),
@JsonValue(r'absence')
absence(r'absence'),
@JsonValue(r'anomaly')
anomaly(r'anomaly');

const BacktestJobResultRuleTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultPermutationsEnum {
@JsonValue(200)
number200('200');

const BacktestJobResultPermutationsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultVerdictEnum {
@JsonValue(r'insufficient')
insufficient(r'insufficient'),
@JsonValue(r'no-evidence-of-precedence')
noEvidenceOfPrecedence(r'no-evidence-of-precedence'),
@JsonValue(r'weak')
weak(r'weak'),
@JsonValue(r'notable')
notable(r'notable');

const BacktestJobResultVerdictEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultStatementEnum {
@JsonValue(r'association in stored data; not causation')
associationInStoredDataSemicolonNotCausation(r'association in stored data; not causation');

const BacktestJobResultStatementEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultFeedbackMappingEnum {
@JsonValue(r'positive')
positive(r'positive'),
@JsonValue(r'negative')
negative(r'negative'),
@JsonValue(r'neutral')
neutral(r'neutral');

const BacktestJobResultFeedbackMappingEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultStatusEnum {
@JsonValue(r'queued')
queued(r'queued'),
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'done')
done(r'done'),
@JsonValue(r'failed')
failed(r'failed'),
@JsonValue(r'skipped')
skipped(r'skipped'),
@JsonValue(r'cancelled')
cancelled(r'cancelled');

const BacktestJobResultStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultOutcomeEnum {
@JsonValue(r'identical')
identical(r'identical'),
@JsonValue(r'differs')
differs(r'differs'),
@JsonValue(r'not-replayable')
notReplayable(r'not-replayable');

const BacktestJobResultOutcomeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// When not-replayable
enum BacktestJobResultReasonEnum {
@JsonValue(r'inputs-purged')
inputsPurged(r'inputs-purged'),
@JsonValue(r'component-removed')
componentRemoved(r'component-removed'),
@JsonValue(r'llm-output')
llmOutput(r'llm-output');

const BacktestJobResultReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultFrequencyEnum {
@JsonValue(r'day')
day(r'day'),
@JsonValue(r'week')
week(r'week');

const BacktestJobResultFrequencyEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultTransformEnum {
@JsonValue(r'log-return')
logReturn(r'log-return'),
@JsonValue(r'log-level-standardised')
logLevelStandardised(r'log-level-standardised'),
@JsonValue(r'weekly-difference-standardised')
weeklyDifferenceStandardised(r'weekly-difference-standardised'),
@JsonValue(r'anscombe-residual')
anscombeResidual(r'anscombe-residual');

const BacktestJobResultTransformEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultModeEnum {
@JsonValue(r'mine')
mine(r'mine'),
@JsonValue(r'retest')
retest(r'retest');

const BacktestJobResultModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultTriggerEnum {
@JsonValue(r'schedule')
schedule(r'schedule'),
@JsonValue(r'manual')
manual(r'manual'),
@JsonValue(r'cycle')
cycle(r'cycle');

const BacktestJobResultTriggerEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultMethodEnum {
@JsonValue(r'BH')
BH(r'BH'),
@JsonValue(r'BY')
BY(r'BY');

const BacktestJobResultMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultDatasetEnum {
@JsonValue(r'ucdp-ged')
ucdpGed(r'ucdp-ged'),
@JsonValue(r'acled')
acled(r'acled'),
@JsonValue(r'usgs-comcat')
usgsComcat(r'usgs-comcat'),
@JsonValue(r'gdacs')
gdacs(r'gdacs');

const BacktestJobResultDatasetEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobResultEgressEnum {
@JsonValue(r'OPEN')
OPEN(r'OPEN'),
@JsonValue(r'LOCAL_ONLY')
LOCAL_ONLY(r'LOCAL_ONLY');

const BacktestJobResultEgressEnum(this.value);

final String value;

@override
String toString() => value;
}


