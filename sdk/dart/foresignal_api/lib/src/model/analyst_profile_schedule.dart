//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_profile_schedule.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystProfileSchedule {
  /// Returns a new [AnalystProfileSchedule] instance.
  AnalystProfileSchedule({

     this.cron,

     this.timezone,

     this.every,
  });

  @JsonKey(
    
    name: r'cron',
    required: false,
    includeIfNull: false,
  )


  final String? cron;



  @JsonKey(
    
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;



      /// Duration ≥ 10m
  @JsonKey(
    
    name: r'every',
    required: false,
    includeIfNull: false,
  )


  final String? every;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystProfileSchedule &&
      other.cron == cron &&
      other.timezone == timezone &&
      other.every == every;

    @override
    int get hashCode =>
        (cron == null ? 0 : cron.hashCode) +
        (timezone == null ? 0 : timezone.hashCode) +
        (every == null ? 0 : every.hashCode);

  factory AnalystProfileSchedule.fromJson(Map<String, dynamic> json) => _$AnalystProfileScheduleFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystProfileScheduleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

