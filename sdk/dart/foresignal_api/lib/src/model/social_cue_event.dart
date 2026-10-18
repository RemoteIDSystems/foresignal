//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_cue.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_cue_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialCueEvent {
  /// Returns a new [SocialCueEvent] instance.
  SocialCueEvent({

    required  this.event,

    required  this.cue,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final SocialCueEventEventEnum event;



  @JsonKey(
    
    name: r'cue',
    required: true,
    includeIfNull: false,
  )


  final SocialCue cue;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialCueEvent &&
      other.event == event &&
      other.cue == cue;

    @override
    int get hashCode =>
        event.hashCode +
        cue.hashCode;

  factory SocialCueEvent.fromJson(Map<String, dynamic> json) => _$SocialCueEventFromJson(json);

  Map<String, dynamic> toJson() => _$SocialCueEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialCueEventEventEnum {
@JsonValue(r'cue.completed')
cuePeriodCompleted(r'cue.completed');

const SocialCueEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


