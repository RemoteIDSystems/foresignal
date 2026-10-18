//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'quiet_hours.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class QuietHours {
  /// Returns a new [QuietHours] instance.
  QuietHours({

    required  this.start,

    required  this.end,

     this.days,

     this.bypassSeverity,
  });

  @JsonKey(
    
    name: r'start',
    required: true,
    includeIfNull: false,
  )


  final String start;



  @JsonKey(
    
    name: r'end',
    required: true,
    includeIfNull: false,
  )


  final String end;



      /// Days the window starts on; empty or omitted = every day
  @JsonKey(
    
    name: r'days',
    required: false,
    includeIfNull: false,
  )


  final List<QuietHoursDaysEnum>? days;



      /// Severity at or above which messages are sent anyway
  @JsonKey(
    
    name: r'bypassSeverity',
    required: false,
    includeIfNull: false,
  )


  final QuietHoursBypassSeverityEnum? bypassSeverity;





    @override
    bool operator ==(Object other) => identical(this, other) || other is QuietHours &&
      other.start == start &&
      other.end == end &&
      other.days == days &&
      other.bypassSeverity == bypassSeverity;

    @override
    int get hashCode =>
        start.hashCode +
        end.hashCode +
        days.hashCode +
        (bypassSeverity == null ? 0 : bypassSeverity.hashCode);

  factory QuietHours.fromJson(Map<String, dynamic> json) => _$QuietHoursFromJson(json);

  Map<String, dynamic> toJson() => _$QuietHoursToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum QuietHoursDaysEnum {
@JsonValue(r'MON')
MON(r'MON'),
@JsonValue(r'TUE')
TUE(r'TUE'),
@JsonValue(r'WED')
WED(r'WED'),
@JsonValue(r'THU')
THU(r'THU'),
@JsonValue(r'FRI')
FRI(r'FRI'),
@JsonValue(r'SAT')
SAT(r'SAT'),
@JsonValue(r'SUN')
SUN(r'SUN');

const QuietHoursDaysEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Severity at or above which messages are sent anyway
enum QuietHoursBypassSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const QuietHoursBypassSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


