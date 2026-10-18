//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/event_study_result_events_inner_car.dart';
import 'package:foresignal_api/src/model/event_study_result_events_inner_abnormal_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_study_result_events_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStudyResultEventsInner {
  /// Returns a new [EventStudyResultEventsInner] instance.
  EventStudyResultEventsInner({

    required  this.at,

     this.ref,

    required  this.clusterId,

     this.excluded,

     this.estimationPoints,

     this.abnormal,

     this.car,
  });

  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



  @JsonKey(
    
    name: r'clusterId',
    required: true,
    includeIfNull: false,
  )


  final int clusterId;



      /// Why the event was excluded, e.g. `FEW_EVENTS` or coverage
  @JsonKey(
    
    name: r'excluded',
    required: false,
    includeIfNull: false,
  )


  final String? excluded;



  @JsonKey(
    
    name: r'estimationPoints',
    required: false,
    includeIfNull: false,
  )


  final int? estimationPoints;



  @JsonKey(
    
    name: r'abnormal',
    required: false,
    includeIfNull: false,
  )


  final List<EventStudyResultEventsInnerAbnormalInner>? abnormal;



  @JsonKey(
    
    name: r'car',
    required: false,
    includeIfNull: false,
  )


  final EventStudyResultEventsInnerCar? car;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStudyResultEventsInner &&
      other.at == at &&
      other.ref == ref &&
      other.clusterId == clusterId &&
      other.excluded == excluded &&
      other.estimationPoints == estimationPoints &&
      other.abnormal == abnormal &&
      other.car == car;

    @override
    int get hashCode =>
        at.hashCode +
        (ref == null ? 0 : ref.hashCode) +
        clusterId.hashCode +
        (excluded == null ? 0 : excluded.hashCode) +
        estimationPoints.hashCode +
        abnormal.hashCode +
        car.hashCode;

  factory EventStudyResultEventsInner.fromJson(Map<String, dynamic> json) => _$EventStudyResultEventsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$EventStudyResultEventsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

