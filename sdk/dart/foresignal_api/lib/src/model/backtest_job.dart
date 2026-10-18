//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/backtest_job_result.dart';
import 'package:foresignal_api/src/model/backtest_caveat.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'backtest_job.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BacktestJob {
  /// Returns a new [BacktestJob] instance.
  BacktestJob({

    required  this.id,

    required  this.type,

     this.targetId,

     this.definition,

    required  this.definitionHash,

    required  this.fromSeq,

    required  this.toSeq,

     this.from,

     this.to,

    required  this.status,

    required  this.progress,

    required  this.createdAt,

     this.startedAt,

     this.finishedAt,

     this.createdBy,

     this.detectorVersion,

    required  this.caveats,

     this.result,

     this.truncated,

     this.error,

     this.manifestId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final BacktestJobTypeEnum type;



  @JsonKey(
    
    name: r'targetId',
    required: false,
    includeIfNull: false,
  )


  final String? targetId;



      /// Snapshot taken at submit time
  @JsonKey(
    
    name: r'definition',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? definition;



  @JsonKey(
    
    name: r'definitionHash',
    required: true,
    includeIfNull: false,
  )


  final String definitionHash;



  @JsonKey(
    
    name: r'fromSeq',
    required: true,
    includeIfNull: false,
  )


  final int fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: true,
    includeIfNull: false,
  )


  final int toSeq;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final BacktestJobStatusEnum status;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'progress',
    required: true,
    includeIfNull: false,
  )


  final num progress;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



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
    
    name: r'createdBy',
    required: false,
    includeIfNull: false,
  )


  final String? createdBy;



  @JsonKey(
    
    name: r'detectorVersion',
    required: false,
    includeIfNull: false,
  )


  final String? detectorVersion;



  @JsonKey(
    
    name: r'caveats',
    required: true,
    includeIfNull: false,
  )


  final List<BacktestCaveat> caveats;



  @JsonKey(
    
    name: r'result',
    required: false,
    includeIfNull: false,
  )


  final BacktestJobResult? result;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



      /// The analysis manifest of this output (prompt 116); null for outputs written before it
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BacktestJob &&
      other.id == id &&
      other.type == type &&
      other.targetId == targetId &&
      other.definition == definition &&
      other.definitionHash == definitionHash &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.from == from &&
      other.to == to &&
      other.status == status &&
      other.progress == progress &&
      other.createdAt == createdAt &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.createdBy == createdBy &&
      other.detectorVersion == detectorVersion &&
      other.caveats == caveats &&
      other.result == result &&
      other.truncated == truncated &&
      other.error == error &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        (targetId == null ? 0 : targetId.hashCode) +
        definition.hashCode +
        definitionHash.hashCode +
        fromSeq.hashCode +
        toSeq.hashCode +
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode) +
        status.hashCode +
        progress.hashCode +
        createdAt.hashCode +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        createdBy.hashCode +
        detectorVersion.hashCode +
        caveats.hashCode +
        (result == null ? 0 : result.hashCode) +
        truncated.hashCode +
        (error == null ? 0 : error.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory BacktestJob.fromJson(Map<String, dynamic> json) => _$BacktestJobFromJson(json);

  Map<String, dynamic> toJson() => _$BacktestJobToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BacktestJobTypeEnum {
@JsonValue(r'rule')
rule(r'rule'),
@JsonValue(r'recipe')
recipe(r'recipe'),
@JsonValue(r'precursor')
precursor(r'precursor'),
@JsonValue(r'quality')
quality(r'quality'),
@JsonValue(r'event-study')
eventStudy(r'event-study'),
@JsonValue(r'precursor-mining')
precursorMining(r'precursor-mining'),
@JsonValue(r'forecast-evaluation')
forecastEvaluation(r'forecast-evaluation'),
@JsonValue(r'reference-import')
referenceImport(r'reference-import'),
@JsonValue(r'base-rate-refresh')
baseRateRefresh(r'base-rate-refresh'),
@JsonValue(r'manifest-replay')
manifestReplay(r'manifest-replay'),
@JsonValue(r'benchmark')
benchmark(r'benchmark');

const BacktestJobTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BacktestJobStatusEnum {
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

const BacktestJobStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


