//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/event_study_window_stats.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_study_result_caar.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStudyResultCaar {
  /// Returns a new [EventStudyResultCaar] instance.
  EventStudyResultCaar({

    required  this.preEvent,

    required  this.event,
  });

  @JsonKey(
    
    name: r'preEvent',
    required: true,
    includeIfNull: false,
  )


  final EventStudyWindowStats preEvent;



  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final EventStudyWindowStats event;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStudyResultCaar &&
      other.preEvent == preEvent &&
      other.event == event;

    @override
    int get hashCode =>
        preEvent.hashCode +
        event.hashCode;

  factory EventStudyResultCaar.fromJson(Map<String, dynamic> json) => _$EventStudyResultCaarFromJson(json);

  Map<String, dynamic> toJson() => _$EventStudyResultCaarToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

