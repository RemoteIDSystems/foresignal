//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'active_window_windows_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ActiveWindowWindowsInner {
  /// Returns a new [ActiveWindowWindowsInner] instance.
  ActiveWindowWindowsInner({

    required  this.days,

    required  this.start,

    required  this.end,
  });

  @JsonKey(
    
    name: r'days',
    required: true,
    includeIfNull: false,
  )


  final List<ActiveWindowWindowsInnerDaysEnum> days;



  @JsonKey(
    
    name: r'start',
    required: true,
    includeIfNull: false,
  )


  final String start;



      /// end < start spans midnight
  @JsonKey(
    
    name: r'end',
    required: true,
    includeIfNull: false,
  )


  final String end;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ActiveWindowWindowsInner &&
      other.days == days &&
      other.start == start &&
      other.end == end;

    @override
    int get hashCode =>
        days.hashCode +
        start.hashCode +
        end.hashCode;

  factory ActiveWindowWindowsInner.fromJson(Map<String, dynamic> json) => _$ActiveWindowWindowsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ActiveWindowWindowsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ActiveWindowWindowsInnerDaysEnum {
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

const ActiveWindowWindowsInnerDaysEnum(this.value);

final String value;

@override
String toString() => value;
}


