//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_study_result_events_inner_car.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStudyResultEventsInnerCar {
  /// Returns a new [EventStudyResultEventsInnerCar] instance.
  EventStudyResultEventsInnerCar({

     this.preEvent,

     this.event,
  });

  @JsonKey(
    
    name: r'preEvent',
    required: false,
    includeIfNull: false,
  )


  final num? preEvent;



  @JsonKey(
    
    name: r'event',
    required: false,
    includeIfNull: false,
  )


  final num? event;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStudyResultEventsInnerCar &&
      other.preEvent == preEvent &&
      other.event == event;

    @override
    int get hashCode =>
        (preEvent == null ? 0 : preEvent.hashCode) +
        (event == null ? 0 : event.hashCode);

  factory EventStudyResultEventsInnerCar.fromJson(Map<String, dynamic> json) => _$EventStudyResultEventsInnerCarFromJson(json);

  Map<String, dynamic> toJson() => _$EventStudyResultEventsInnerCarToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

