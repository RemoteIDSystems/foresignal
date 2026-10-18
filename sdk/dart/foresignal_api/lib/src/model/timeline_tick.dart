//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/timeline_tick_frames_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'timeline_tick.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TimelineTick {
  /// Returns a new [TimelineTick] instance.
  TimelineTick({

    required  this.time,

    required  this.kind,

    required  this.frames,
  });

  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final TimelineTickKindEnum kind;



  @JsonKey(
    
    name: r'frames',
    required: true,
    includeIfNull: false,
  )


  final List<TimelineTickFramesInner> frames;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TimelineTick &&
      other.time == time &&
      other.kind == kind &&
      other.frames == frames;

    @override
    int get hashCode =>
        time.hashCode +
        kind.hashCode +
        frames.hashCode;

  factory TimelineTick.fromJson(Map<String, dynamic> json) => _$TimelineTickFromJson(json);

  Map<String, dynamic> toJson() => _$TimelineTickToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TimelineTickKindEnum {
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'forecast')
forecast(r'forecast');

const TimelineTickKindEnum(this.value);

final String value;

@override
String toString() => value;
}


