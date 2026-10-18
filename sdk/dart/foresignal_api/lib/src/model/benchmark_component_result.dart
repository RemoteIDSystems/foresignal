//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/benchmark_component_result_scenarios_inner.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/benchmark_component_result_thresholds_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_component_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkComponentResult {
  /// Returns a new [BenchmarkComponentResult] instance.
  BenchmarkComponentResult({

    required  this.component,

    required  this.componentVersion,

    required  this.status,

    required  this.metrics,

     this.thresholds,

     this.scenarios,

     this.caveats,
  });

      /// Benchmarked component: `change-point`, `anomaly`, `event-study`, `co-occurrence`, `evidence`, `canonical-linking`, `precursor`, `situation-linker`, `forecaster:base-rate`, `forecaster:analog`, `forecaster:precursor` or `forecaster:ensemble`
  @JsonKey(
    
    name: r'component',
    required: true,
    includeIfNull: false,
  )


  final String component;



  @JsonKey(
    
    name: r'componentVersion',
    required: true,
    includeIfNull: false,
  )


  final String componentVersion;



      /// Result against the documented thresholds
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkComponentResultStatusEnum status;



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



  @JsonKey(
    
    name: r'scenarios',
    required: false,
    includeIfNull: false,
  )


  final List<BenchmarkComponentResultScenariosInner>? scenarios;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkComponentResult &&
      other.component == component &&
      other.componentVersion == componentVersion &&
      other.status == status &&
      other.metrics == metrics &&
      other.thresholds == thresholds &&
      other.scenarios == scenarios &&
      other.caveats == caveats;

    @override
    int get hashCode =>
        component.hashCode +
        componentVersion.hashCode +
        status.hashCode +
        metrics.hashCode +
        thresholds.hashCode +
        scenarios.hashCode +
        caveats.hashCode;

  factory BenchmarkComponentResult.fromJson(Map<String, dynamic> json) => _$BenchmarkComponentResultFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkComponentResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Result against the documented thresholds
enum BenchmarkComponentResultStatusEnum {
@JsonValue(r'pass')
pass(r'pass'),
@JsonValue(r'warn')
warn(r'warn'),
@JsonValue(r'fail')
fail(r'fail');

const BenchmarkComponentResultStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


