//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/narrative_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'narrative_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NarrativeEvent {
  /// Returns a new [NarrativeEvent] instance.
  NarrativeEvent({

    required  this.event,

    required  this.narrative,

     this.changed,

    required  this.at,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final NarrativeEventEventEnum event;



  @JsonKey(
    
    name: r'narrative',
    required: true,
    includeIfNull: false,
  )


  final NarrativeSummary narrative;



  @JsonKey(
    
    name: r'changed',
    required: false,
    includeIfNull: false,
  )


  final List<NarrativeEventChangedEnum>? changed;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NarrativeEvent &&
      other.event == event &&
      other.narrative == narrative &&
      other.changed == changed &&
      other.at == at;

    @override
    int get hashCode =>
        event.hashCode +
        narrative.hashCode +
        changed.hashCode +
        at.hashCode;

  factory NarrativeEvent.fromJson(Map<String, dynamic> json) => _$NarrativeEventFromJson(json);

  Map<String, dynamic> toJson() => _$NarrativeEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NarrativeEventEventEnum {
@JsonValue(r'narrative.emerged')
narrativePeriodEmerged(r'narrative.emerged'),
@JsonValue(r'narrative.updated')
narrativePeriodUpdated(r'narrative.updated');

const NarrativeEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


enum NarrativeEventChangedEnum {
@JsonValue(r'state')
state(r'state'),
@JsonValue(r'status')
status(r'status'),
@JsonValue(r'postCount')
postCount(r'postCount'),
@JsonValue(r'evidenceGroupCount')
evidenceGroupCount(r'evidenceGroupCount'),
@JsonValue(r'velocity')
velocity(r'velocity'),
@JsonValue(r'merged')
merged(r'merged'),
@JsonValue(r'split')
split(r'split'),
@JsonValue(r'title')
title(r'title');

const NarrativeEventChangedEnum(this.value);

final String value;

@override
String toString() => value;
}


