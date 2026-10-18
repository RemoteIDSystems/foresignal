//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/benchmark_component_result_thresholds_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_component_result_scenarios_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkComponentResultScenariosInner {
  /// Returns a new [BenchmarkComponentResultScenariosInner] instance.
  BenchmarkComponentResultScenariosInner({

    required  this.scenario,

     this.caseId,

     this.seed,

    required  this.status,

    required  this.metrics,

     this.thresholds,

     this.truth,

     this.notes,
  });

      /// Synthetic scenario (`bench-scenarios` 1.0.0) or `historical` for a corpus case
  @JsonKey(
    
    name: r'scenario',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkComponentResultScenariosInnerScenarioEnum scenario;



      /// ULID identifier
  @JsonKey(
    
    name: r'caseId',
    required: false,
    includeIfNull: false,
  )


  final String? caseId;



  @JsonKey(
    
    name: r'seed',
    required: false,
    includeIfNull: false,
  )


  final int? seed;



      /// Result against the documented thresholds
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkComponentResultScenariosInnerStatusEnum status;



      /// Named metrics: `fdr`, `falseAlarmRate`, `recall`, `precision`, `delayMedianHours`, `delayP90Hours`, `correctUnknownShare`, `duplicateInflation`, `linkingPrecision`, `linkingRecall`, `spuriousMerges`, `falseValidationRate`, `lagRecoveredShare`, `rateRecoveredShare`, `brier`, `logScore`, `reliabilityMaxDeviation`, `skill`, …
  @JsonKey(
    
    name: r'metrics',
    required: true,
    includeIfNull: false,
  )


  final Map<String, num?> metrics;



  @JsonKey(
    
    name: r'thresholds',
    required: false,
    includeIfNull: false,
  )


  final List<BenchmarkComponentResultThresholdsInner>? thresholds;



      /// Planted truth record (times, sizes, lags, groups) or case expectations
  @JsonKey(
    
    name: r'truth',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? truth;



  @JsonKey(
    
    name: r'notes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? notes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkComponentResultScenariosInner &&
      other.scenario == scenario &&
      other.caseId == caseId &&
      other.seed == seed &&
      other.status == status &&
      other.metrics == metrics &&
      other.thresholds == thresholds &&
      other.truth == truth &&
      other.notes == notes;

    @override
    int get hashCode =>
        scenario.hashCode +
        (caseId == null ? 0 : caseId.hashCode) +
        (seed == null ? 0 : seed.hashCode) +
        status.hashCode +
        metrics.hashCode +
        thresholds.hashCode +
        truth.hashCode +
        notes.hashCode;

  factory BenchmarkComponentResultScenariosInner.fromJson(Map<String, dynamic> json) => _$BenchmarkComponentResultScenariosInnerFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkComponentResultScenariosInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Synthetic scenario (`bench-scenarios` 1.0.0) or `historical` for a corpus case
enum BenchmarkComponentResultScenariosInnerScenarioEnum {
@JsonValue(r'null-seasonal')
nullSeasonal(r'null-seasonal'),
@JsonValue(r'step-change')
stepChange(r'step-change'),
@JsonValue(r'spike')
spike(r'spike'),
@JsonValue(r'coverage-outage')
coverageOutage(r'coverage-outage'),
@JsonValue(r'duplicate-echo')
duplicateEcho(r'duplicate-echo'),
@JsonValue(r'late-revision')
lateRevision(r'late-revision'),
@JsonValue(r'common-driver')
commonDriver(r'common-driver'),
@JsonValue(r'true-precursor')
truePrecursor(r'true-precursor'),
@JsonValue(r'base-rate-drift')
baseRateDrift(r'base-rate-drift'),
@JsonValue(r'pre-event-move')
preEventMove(r'pre-event-move'),
@JsonValue(r'historical')
historical(r'historical');

const BenchmarkComponentResultScenariosInnerScenarioEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Result against the documented thresholds
enum BenchmarkComponentResultScenariosInnerStatusEnum {
@JsonValue(r'pass')
pass(r'pass'),
@JsonValue(r'warn')
warn(r'warn'),
@JsonValue(r'fail')
fail(r'fail');

const BenchmarkComponentResultScenariosInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


