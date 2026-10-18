//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/benchmark_component_result.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_run.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkRun {
  /// Returns a new [BenchmarkRun] instance.
  BenchmarkRun({

    required  this.id,

    required  this.suite,

    required  this.suiteVersion,

     this.scorerVersion,

    required  this.seed,

    required  this.components,

    required  this.jobStatus,

     this.progress,

    required  this.status,

     this.results,

     this.manifestId,

     this.createdBy,

     this.startedAt,

     this.finishedAt,

     this.error,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'suite',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkRunSuiteEnum suite;



  @JsonKey(
    
    name: r'suiteVersion',
    required: true,
    includeIfNull: false,
  )


  final String suiteVersion;



  @JsonKey(
    
    name: r'scorerVersion',
    required: false,
    includeIfNull: false,
  )


  final String? scorerVersion;



  @JsonKey(
    
    name: r'seed',
    required: true,
    includeIfNull: false,
  )


  final int seed;



  @JsonKey(
    
    name: r'components',
    required: true,
    includeIfNull: false,
  )


  final List<String> components;



      /// 103 job lifecycle
  @JsonKey(
    
    name: r'jobStatus',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkRunJobStatusEnum jobStatus;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'progress',
    required: false,
    includeIfNull: false,
  )


  final num? progress;



      /// Worst component status; null until the run is done
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: true,
  )


  final BenchmarkRunStatusEnum? status;



  @JsonKey(
    
    name: r'results',
    required: false,
    includeIfNull: false,
  )


  final List<BenchmarkComponentResult>? results;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



      /// Principal or `scheduler`
  @JsonKey(
    
    name: r'createdBy',
    required: false,
    includeIfNull: false,
  )


  final String? createdBy;



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
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkRun &&
      other.id == id &&
      other.suite == suite &&
      other.suiteVersion == suiteVersion &&
      other.scorerVersion == scorerVersion &&
      other.seed == seed &&
      other.components == components &&
      other.jobStatus == jobStatus &&
      other.progress == progress &&
      other.status == status &&
      other.results == results &&
      other.manifestId == manifestId &&
      other.createdBy == createdBy &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.error == error;

    @override
    int get hashCode =>
        id.hashCode +
        suite.hashCode +
        suiteVersion.hashCode +
        scorerVersion.hashCode +
        seed.hashCode +
        components.hashCode +
        jobStatus.hashCode +
        progress.hashCode +
        (status == null ? 0 : status.hashCode) +
        results.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode) +
        (createdBy == null ? 0 : createdBy.hashCode) +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        (error == null ? 0 : error.hashCode);

  factory BenchmarkRun.fromJson(Map<String, dynamic> json) => _$BenchmarkRunFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkRunToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BenchmarkRunSuiteEnum {
@JsonValue(r'synthetic')
synthetic(r'synthetic'),
@JsonValue(r'historical')
historical(r'historical'),
@JsonValue(r'all')
all(r'all');

const BenchmarkRunSuiteEnum(this.value);

final String value;

@override
String toString() => value;
}


/// 103 job lifecycle
enum BenchmarkRunJobStatusEnum {
@JsonValue(r'queued')
queued(r'queued'),
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'done')
done(r'done'),
@JsonValue(r'failed')
failed(r'failed'),
@JsonValue(r'cancelled')
cancelled(r'cancelled');

const BenchmarkRunJobStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Worst component status; null until the run is done
enum BenchmarkRunStatusEnum {
@JsonValue(r'pass')
pass(r'pass'),
@JsonValue(r'warn')
warn(r'warn'),
@JsonValue(r'fail')
fail(r'fail');

const BenchmarkRunStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


