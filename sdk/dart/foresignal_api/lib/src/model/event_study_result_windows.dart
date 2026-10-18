//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_study_result_windows.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStudyResultWindows {
  /// Returns a new [EventStudyResultWindows] instance.
  EventStudyResultWindows({

    required  this.estimation,

    required  this.preEvent,

    required  this.event,
  });

  @JsonKey(
    
    name: r'estimation',
    required: true,
    includeIfNull: false,
  )


  final List<int> estimation;



  @JsonKey(
    
    name: r'preEvent',
    required: true,
    includeIfNull: false,
  )


  final List<int> preEvent;



  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final List<int> event;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStudyResultWindows &&
      other.estimation == estimation &&
      other.preEvent == preEvent &&
      other.event == event;

    @override
    int get hashCode =>
        estimation.hashCode +
        preEvent.hashCode +
        event.hashCode;

  factory EventStudyResultWindows.fromJson(Map<String, dynamic> json) => _$EventStudyResultWindowsFromJson(json);

  Map<String, dynamic> toJson() => _$EventStudyResultWindowsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

