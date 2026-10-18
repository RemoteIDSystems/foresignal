//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'backfill_job.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BackfillJob {
  /// Returns a new [BackfillJob] instance.
  BackfillJob({

    required  this.id,

    required  this.sourceId,

    required  this.status,

    required  this.from,

     this.cursor,

     this.pagesFetched,

     this.versionsWritten,

    required  this.createdAt,

     this.startedAt,

     this.finishedAt,

     this.error,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final BackfillJobStatusEnum status;



  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



      /// Oldest report date fetched so far
  @JsonKey(
    
    name: r'cursor',
    required: false,
    includeIfNull: false,
  )


  final DateTime? cursor;



  @JsonKey(
    
    name: r'pagesFetched',
    required: false,
    includeIfNull: false,
  )


  final int? pagesFetched;



  @JsonKey(
    
    name: r'versionsWritten',
    required: false,
    includeIfNull: false,
  )


  final int? versionsWritten;



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
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BackfillJob &&
      other.id == id &&
      other.sourceId == sourceId &&
      other.status == status &&
      other.from == from &&
      other.cursor == cursor &&
      other.pagesFetched == pagesFetched &&
      other.versionsWritten == versionsWritten &&
      other.createdAt == createdAt &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.error == error;

    @override
    int get hashCode =>
        id.hashCode +
        sourceId.hashCode +
        status.hashCode +
        from.hashCode +
        (cursor == null ? 0 : cursor.hashCode) +
        pagesFetched.hashCode +
        versionsWritten.hashCode +
        createdAt.hashCode +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        (error == null ? 0 : error.hashCode);

  factory BackfillJob.fromJson(Map<String, dynamic> json) => _$BackfillJobFromJson(json);

  Map<String, dynamic> toJson() => _$BackfillJobToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BackfillJobStatusEnum {
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

const BackfillJobStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


