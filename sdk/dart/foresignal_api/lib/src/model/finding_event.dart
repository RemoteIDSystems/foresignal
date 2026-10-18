//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/finding_event_finding.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'finding_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FindingEvent {
  /// Returns a new [FindingEvent] instance.
  FindingEvent({

    required  this.event,

    required  this.finding,

    required  this.at,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final FindingEventEventEnum event;



  @JsonKey(
    
    name: r'finding',
    required: true,
    includeIfNull: false,
  )


  final FindingEventFinding finding;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FindingEvent &&
      other.event == event &&
      other.finding == finding &&
      other.at == at;

    @override
    int get hashCode =>
        event.hashCode +
        finding.hashCode +
        at.hashCode;

  factory FindingEvent.fromJson(Map<String, dynamic> json) => _$FindingEventFromJson(json);

  Map<String, dynamic> toJson() => _$FindingEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FindingEventEventEnum {
@JsonValue(r'finding.created')
findingPeriodCreated(r'finding.created'),
@JsonValue(r'finding.updated')
findingPeriodUpdated(r'finding.updated'),
@JsonValue(r'finding.dismissed')
findingPeriodDismissed(r'finding.dismissed');

const FindingEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


