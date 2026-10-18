//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_batch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorBatch {
  /// Returns a new [PrecursorBatch] instance.
  PrecursorBatch({

    required  this.id,

     this.jobId,

    required  this.mode,

     this.trigger,

    required  this.status,

    required  this.startedAt,

     this.finishedAt,

    required  this.candidatesGenerated,

    required  this.tested,

    required  this.capped,

     this.insufficient,

    required  this.survivors,

    required  this.validated,

    required  this.q,

    required  this.method,

     this.windows,

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



      /// 103 job (`precursor-mining`)
  @JsonKey(
    
    name: r'jobId',
    required: false,
    includeIfNull: false,
  )


  final String? jobId;



  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final PrecursorBatchModeEnum mode;



  @JsonKey(
    
    name: r'trigger',
    required: false,
    includeIfNull: false,
  )


  final PrecursorBatchTriggerEnum? trigger;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final PrecursorBatchStatusEnum status;



  @JsonKey(
    
    name: r'startedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime startedAt;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



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


  final PrecursorBatchMethodEnum method;



      /// The global range and its split (`start`, `split`, `end`, `discoveryFraction`); for a re-test, `split` starts the re-test window
  @JsonKey(
    
    name: r'windows',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? windows;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorBatch &&
      other.id == id &&
      other.jobId == jobId &&
      other.mode == mode &&
      other.trigger == trigger &&
      other.status == status &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.candidatesGenerated == candidatesGenerated &&
      other.tested == tested &&
      other.capped == capped &&
      other.insufficient == insufficient &&
      other.survivors == survivors &&
      other.validated == validated &&
      other.q == q &&
      other.method == method &&
      other.windows == windows &&
      other.error == error &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        id.hashCode +
        (jobId == null ? 0 : jobId.hashCode) +
        mode.hashCode +
        trigger.hashCode +
        status.hashCode +
        startedAt.hashCode +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        candidatesGenerated.hashCode +
        tested.hashCode +
        capped.hashCode +
        insufficient.hashCode +
        survivors.hashCode +
        validated.hashCode +
        q.hashCode +
        method.hashCode +
        windows.hashCode +
        (error == null ? 0 : error.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory PrecursorBatch.fromJson(Map<String, dynamic> json) => _$PrecursorBatchFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorBatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorBatchModeEnum {
@JsonValue(r'mine')
mine(r'mine'),
@JsonValue(r'retest')
retest(r'retest');

const PrecursorBatchModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PrecursorBatchTriggerEnum {
@JsonValue(r'schedule')
schedule(r'schedule'),
@JsonValue(r'manual')
manual(r'manual'),
@JsonValue(r'cycle')
cycle(r'cycle');

const PrecursorBatchTriggerEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PrecursorBatchStatusEnum {
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'done')
done(r'done'),
@JsonValue(r'failed')
failed(r'failed'),
@JsonValue(r'cancelled')
cancelled(r'cancelled');

const PrecursorBatchStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PrecursorBatchMethodEnum {
@JsonValue(r'BH')
BH(r'BH'),
@JsonValue(r'BY')
BY(r'BY');

const PrecursorBatchMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


