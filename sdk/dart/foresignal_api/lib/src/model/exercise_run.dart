//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/exercise_stats.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exercise_run.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExerciseRun {
  /// Returns a new [ExerciseRun] instance.
  ExerciseRun({

    required  this.id,

    required  this.name,

     this.ruleIds,

     this.tag,

     this.from,

     this.to,

     this.fromSeq,

     this.toSeq,

     this.speed,

     this.channelIds,

     this.enrich,

     this.dryRun,

    required  this.status,

     this.createdBy,

    required  this.createdAt,

     this.startedAt,

     this.finishedAt,

     this.stats,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'ruleIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? ruleIds;



  @JsonKey(
    
    name: r'tag',
    required: false,
    includeIfNull: false,
  )


  final String? tag;



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
    
    name: r'fromSeq',
    required: false,
    includeIfNull: false,
  )


  final int? fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: false,
    includeIfNull: false,
  )


  final int? toSeq;



  @JsonKey(
    
    name: r'speed',
    required: false,
    includeIfNull: false,
  )


  final ExerciseRunSpeedEnum? speed;



  @JsonKey(
    
    name: r'channelIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? channelIds;



  @JsonKey(
    
    name: r'enrich',
    required: false,
    includeIfNull: false,
  )


  final bool? enrich;



  @JsonKey(
    
    name: r'dryRun',
    required: false,
    includeIfNull: false,
  )


  final bool? dryRun;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ExerciseRunStatusEnum status;



  @JsonKey(
    
    name: r'createdBy',
    required: false,
    includeIfNull: false,
  )


  final String? createdBy;



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
    
    name: r'stats',
    required: false,
    includeIfNull: false,
  )


  final ExerciseStats? stats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExerciseRun &&
      other.id == id &&
      other.name == name &&
      other.ruleIds == ruleIds &&
      other.tag == tag &&
      other.from == from &&
      other.to == to &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.speed == speed &&
      other.channelIds == channelIds &&
      other.enrich == enrich &&
      other.dryRun == dryRun &&
      other.status == status &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.stats == stats;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        ruleIds.hashCode +
        (tag == null ? 0 : tag.hashCode) +
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode) +
        (fromSeq == null ? 0 : fromSeq.hashCode) +
        (toSeq == null ? 0 : toSeq.hashCode) +
        speed.hashCode +
        channelIds.hashCode +
        enrich.hashCode +
        dryRun.hashCode +
        status.hashCode +
        createdBy.hashCode +
        createdAt.hashCode +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        stats.hashCode;

  factory ExerciseRun.fromJson(Map<String, dynamic> json) => _$ExerciseRunFromJson(json);

  Map<String, dynamic> toJson() => _$ExerciseRunToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExerciseRunSpeedEnum {
@JsonValue(r'realtime')
realtime(r'realtime'),
@JsonValue(r'x10')
x10(r'x10'),
@JsonValue(r'x60')
x60(r'x60'),
@JsonValue(r'max')
max(r'max');

const ExerciseRunSpeedEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ExerciseRunStatusEnum {
@JsonValue(r'PLANNED')
PLANNED(r'PLANNED'),
@JsonValue(r'RUNNING')
RUNNING(r'RUNNING'),
@JsonValue(r'DONE')
DONE(r'DONE'),
@JsonValue(r'CANCELLED')
CANCELLED(r'CANCELLED'),
@JsonValue(r'FAILED')
FAILED(r'FAILED');

const ExerciseRunStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


