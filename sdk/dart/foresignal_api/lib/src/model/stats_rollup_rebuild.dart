//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_rollup_rebuild.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsRollupRebuild {
  /// Returns a new [StatsRollupRebuild] instance.
  StatsRollupRebuild({

    required  this.from,

    required  this.to,

     this.hours,

     this.days,

    required  this.status,
  });

  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



  @JsonKey(
    
    name: r'hours',
    required: false,
    includeIfNull: false,
  )


  final int? hours;



  @JsonKey(
    
    name: r'days',
    required: false,
    includeIfNull: false,
  )


  final int? days;



      /// Always queued: the rebuild runs in the background
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final StatsRollupRebuildStatusEnum status;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsRollupRebuild &&
      other.from == from &&
      other.to == to &&
      other.hours == hours &&
      other.days == days &&
      other.status == status;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode +
        hours.hashCode +
        days.hashCode +
        status.hashCode;

  factory StatsRollupRebuild.fromJson(Map<String, dynamic> json) => _$StatsRollupRebuildFromJson(json);

  Map<String, dynamic> toJson() => _$StatsRollupRebuildToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Always queued: the rebuild runs in the background
enum StatsRollupRebuildStatusEnum {
@JsonValue(r'queued')
queued(r'queued'),
@JsonValue(r'running')
running(r'running');

const StatsRollupRebuildStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


