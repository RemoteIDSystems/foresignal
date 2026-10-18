//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_cycle_input_schedule.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisCycleInputSchedule {
  /// Returns a new [AnalysisCycleInputSchedule] instance.
  AnalysisCycleInputSchedule({

    required  this.cron,

    required  this.timezone,
  });

  @JsonKey(
    
    name: r'cron',
    required: true,
    includeIfNull: false,
  )


  final String cron;



  @JsonKey(
    
    name: r'timezone',
    required: true,
    includeIfNull: false,
  )


  final String timezone;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisCycleInputSchedule &&
      other.cron == cron &&
      other.timezone == timezone;

    @override
    int get hashCode =>
        cron.hashCode +
        timezone.hashCode;

  factory AnalysisCycleInputSchedule.fromJson(Map<String, dynamic> json) => _$AnalysisCycleInputScheduleFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisCycleInputScheduleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

