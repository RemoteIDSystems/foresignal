//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trip_schedule.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TripSchedule {
  /// Returns a new [TripSchedule] instance.
  TripSchedule({

    required  this.type,

     this.departAt,

     this.days,

     this.departTime,

     this.timezone,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final TripScheduleTypeEnum type;



      /// once
  @JsonKey(
    
    name: r'departAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? departAt;



      /// recurring
  @JsonKey(
    
    name: r'days',
    required: false,
    includeIfNull: false,
  )


  final List<TripScheduleDaysEnum>? days;



      /// recurring, local HH:mm
  @JsonKey(
    
    name: r'departTime',
    required: false,
    includeIfNull: false,
  )


  final String? departTime;



      /// IANA zone for recurring schedules
  @JsonKey(
    
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TripSchedule &&
      other.type == type &&
      other.departAt == departAt &&
      other.days == days &&
      other.departTime == departTime &&
      other.timezone == timezone;

    @override
    int get hashCode =>
        type.hashCode +
        (departAt == null ? 0 : departAt.hashCode) +
        days.hashCode +
        (departTime == null ? 0 : departTime.hashCode) +
        (timezone == null ? 0 : timezone.hashCode);

  factory TripSchedule.fromJson(Map<String, dynamic> json) => _$TripScheduleFromJson(json);

  Map<String, dynamic> toJson() => _$TripScheduleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TripScheduleTypeEnum {
@JsonValue(r'once')
once(r'once'),
@JsonValue(r'recurring')
recurring(r'recurring');

const TripScheduleTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum TripScheduleDaysEnum {
@JsonValue(r'MO')
MO(r'MO'),
@JsonValue(r'TU')
TU(r'TU'),
@JsonValue(r'WE')
WE(r'WE'),
@JsonValue(r'TH')
TH(r'TH'),
@JsonValue(r'FR')
FR(r'FR'),
@JsonValue(r'SA')
SA(r'SA'),
@JsonValue(r'SU')
SU(r'SU');

const TripScheduleDaysEnum(this.value);

final String value;

@override
String toString() => value;
}


