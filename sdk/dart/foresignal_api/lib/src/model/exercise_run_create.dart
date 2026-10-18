//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exercise_run_create.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExerciseRunCreate {
  /// Returns a new [ExerciseRunCreate] instance.
  ExerciseRunCreate({

    required  this.name,

     this.ruleIds,

     this.tag,

     this.from,

     this.to,

     this.fromSeq,

     this.toSeq,

     this.speed,

     this.channelIds,

     this.enrich = false,

     this.dryRun = false,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// Rules to replay (or use tag)
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


  final ExerciseRunCreateSpeedEnum? speed;



      /// Channels to deliver to, in addition to channels with acceptExercise=true
  @JsonKey(
    
    name: r'channelIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? channelIds;



      /// Allow 030 enrichment of exercise alerts
  @JsonKey(
    defaultValue: false,
    name: r'enrich',
    required: false,
    includeIfNull: false,
  )


  final bool? enrich;



      /// Return projected counts per rule and channel without delivering
  @JsonKey(
    defaultValue: false,
    name: r'dryRun',
    required: false,
    includeIfNull: false,
  )


  final bool? dryRun;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExerciseRunCreate &&
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
      other.dryRun == dryRun;

    @override
    int get hashCode =>
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
        dryRun.hashCode;

  factory ExerciseRunCreate.fromJson(Map<String, dynamic> json) => _$ExerciseRunCreateFromJson(json);

  Map<String, dynamic> toJson() => _$ExerciseRunCreateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExerciseRunCreateSpeedEnum {
@JsonValue(r'realtime')
realtime(r'realtime'),
@JsonValue(r'x10')
x10(r'x10'),
@JsonValue(r'x60')
x60(r'x60'),
@JsonValue(r'max')
max(r'max');

const ExerciseRunCreateSpeedEnum(this.value);

final String value;

@override
String toString() => value;
}


