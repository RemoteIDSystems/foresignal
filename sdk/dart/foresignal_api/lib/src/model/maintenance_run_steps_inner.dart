//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'maintenance_run_steps_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MaintenanceRunStepsInner {
  /// Returns a new [MaintenanceRunStepsInner] instance.
  MaintenanceRunStepsInner({

    required  this.step,

    required  this.status,

     this.startedAt,

     this.finishedAt,

     this.jobId,

     this.result,

     this.error,
  });

  @JsonKey(
    
    name: r'step',
    required: true,
    includeIfNull: false,
  )


  final MaintenanceRunStepsInnerStepEnum step;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final MaintenanceRunStepsInnerStatusEnum status;



  @JsonKey(
    
    name: r'startedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startedAt;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



  @JsonKey(
    
    name: r'jobId',
    required: false,
    includeIfNull: false,
  )


  final String? jobId;



  @JsonKey(
    
    name: r'result',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? result;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MaintenanceRunStepsInner &&
      other.step == step &&
      other.status == status &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.jobId == jobId &&
      other.result == result &&
      other.error == error;

    @override
    int get hashCode =>
        step.hashCode +
        status.hashCode +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        (jobId == null ? 0 : jobId.hashCode) +
        (result == null ? 0 : result.hashCode) +
        (error == null ? 0 : error.hashCode);

  factory MaintenanceRunStepsInner.fromJson(Map<String, dynamic> json) => _$MaintenanceRunStepsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$MaintenanceRunStepsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MaintenanceRunStepsInnerStepEnum {
@JsonValue(r'observability-reestimation')
observabilityReestimation(r'observability-reestimation'),
@JsonValue(r'benchmark-historical')
benchmarkHistorical(r'benchmark-historical'),
@JsonValue(r'base-rate-refresh')
baseRateRefresh(r'base-rate-refresh'),
@JsonValue(r'precursor-retest')
precursorRetest(r'precursor-retest'),
@JsonValue(r'platt-refit')
plattRefit(r'platt-refit'),
@JsonValue(r'ensemble-weights')
ensembleWeights(r'ensemble-weights'),
@JsonValue(r'extremizing-check')
extremizingCheck(r'extremizing-check'),
@JsonValue(r'index-weight-fit')
indexWeightFit(r'index-weight-fit'),
@JsonValue(r'series-method-reselection')
seriesMethodReselection(r'series-method-reselection'),
@JsonValue(r'hypothesis-archival-proposals')
hypothesisArchivalProposals(r'hypothesis-archival-proposals'),
@JsonValue(r'llm-retrospective-evaluation')
llmRetrospectiveEvaluation(r'llm-retrospective-evaluation');

const MaintenanceRunStepsInnerStepEnum(this.value);

final String value;

@override
String toString() => value;
}


enum MaintenanceRunStepsInnerStatusEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'done')
done(r'done'),
@JsonValue(r'failed')
failed(r'failed'),
@JsonValue(r'skipped')
skipped(r'skipped');

const MaintenanceRunStepsInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


