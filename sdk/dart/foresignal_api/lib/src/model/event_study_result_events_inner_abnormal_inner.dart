//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_study_result_events_inner_abnormal_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStudyResultEventsInnerAbnormalInner {
  /// Returns a new [EventStudyResultEventsInnerAbnormalInner] instance.
  EventStudyResultEventsInnerAbnormalInner({

    required  this.offset,

     this.value,

     this.cumulative,
  });

  @JsonKey(
    
    name: r'offset',
    required: true,
    includeIfNull: false,
  )


  final int offset;



  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final num? value;



  @JsonKey(
    
    name: r'cumulative',
    required: false,
    includeIfNull: false,
  )


  final num? cumulative;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStudyResultEventsInnerAbnormalInner &&
      other.offset == offset &&
      other.value == value &&
      other.cumulative == cumulative;

    @override
    int get hashCode =>
        offset.hashCode +
        (value == null ? 0 : value.hashCode) +
        (cumulative == null ? 0 : cumulative.hashCode);

  factory EventStudyResultEventsInnerAbnormalInner.fromJson(Map<String, dynamic> json) => _$EventStudyResultEventsInnerAbnormalInnerFromJson(json);

  Map<String, dynamic> toJson() => _$EventStudyResultEventsInnerAbnormalInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

