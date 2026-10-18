//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'replay_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReplayResult {
  /// Returns a new [ReplayResult] instance.
  ReplayResult({

    required  this.jobId,

    required  this.manifestId,

    required  this.status,

     this.outcome,

     this.reason,

     this.diffSummary,

     this.message,

     this.startedAt,

     this.finishedAt,
  });

  @JsonKey(
    
    name: r'jobId',
    required: true,
    includeIfNull: false,
  )


  final String jobId;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: true,
    includeIfNull: false,
  )


  final String manifestId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ReplayResultStatusEnum status;



  @JsonKey(
    
    name: r'outcome',
    required: false,
    includeIfNull: false,
  )


  final ReplayResultOutcomeEnum? outcome;



      /// When not-replayable
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final ReplayResultReasonEnum? reason;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReplayResult &&
      other.jobId == jobId &&
      other.manifestId == manifestId &&
      other.status == status &&
      other.outcome == outcome &&
      other.reason == reason &&
      other.diffSummary == diffSummary &&
      other.message == message &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt;

    @override
    int get hashCode =>
        jobId.hashCode +
        manifestId.hashCode +
        status.hashCode +
        (outcome == null ? 0 : outcome.hashCode) +
        (reason == null ? 0 : reason.hashCode) +
        (diffSummary == null ? 0 : diffSummary.hashCode) +
        (message == null ? 0 : message.hashCode) +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode);

  factory ReplayResult.fromJson(Map<String, dynamic> json) => _$ReplayResultFromJson(json);

  Map<String, dynamic> toJson() => _$ReplayResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReplayResultStatusEnum {
@JsonValue(r'queued')
queued(r'queued'),
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'done')
done(r'done'),
@JsonValue(r'failed')
failed(r'failed');

const ReplayResultStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ReplayResultOutcomeEnum {
@JsonValue(r'identical')
identical(r'identical'),
@JsonValue(r'differs')
differs(r'differs'),
@JsonValue(r'not-replayable')
notReplayable(r'not-replayable');

const ReplayResultOutcomeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// When not-replayable
enum ReplayResultReasonEnum {
@JsonValue(r'inputs-purged')
inputsPurged(r'inputs-purged'),
@JsonValue(r'component-removed')
componentRemoved(r'component-removed'),
@JsonValue(r'llm-output')
llmOutput(r'llm-output');

const ReplayResultReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


