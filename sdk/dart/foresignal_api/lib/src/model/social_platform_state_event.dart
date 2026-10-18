//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_platform_id.dart';
import 'package:foresignal_api/src/model/social_spend.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_platform_state_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPlatformStateEvent {
  /// Returns a new [SocialPlatformStateEvent] instance.
  SocialPlatformStateEvent({

    required  this.event,

    required  this.platformId,

    required  this.state,

     this.previousState,

     this.reason,

     this.spend,

    required  this.at,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final SocialPlatformStateEventEventEnum event;



  @JsonKey(
    
    name: r'platformId',
    required: true,
    includeIfNull: false,
  )


  final SocialPlatformId platformId;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final String state;



  @JsonKey(
    
    name: r'previousState',
    required: false,
    includeIfNull: false,
  )


  final String? previousState;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



  @JsonKey(
    
    name: r'spend',
    required: false,
    includeIfNull: false,
  )


  final SocialSpend? spend;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPlatformStateEvent &&
      other.event == event &&
      other.platformId == platformId &&
      other.state == state &&
      other.previousState == previousState &&
      other.reason == reason &&
      other.spend == spend &&
      other.at == at;

    @override
    int get hashCode =>
        event.hashCode +
        platformId.hashCode +
        state.hashCode +
        (previousState == null ? 0 : previousState.hashCode) +
        (reason == null ? 0 : reason.hashCode) +
        (spend == null ? 0 : spend.hashCode) +
        at.hashCode;

  factory SocialPlatformStateEvent.fromJson(Map<String, dynamic> json) => _$SocialPlatformStateEventFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPlatformStateEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialPlatformStateEventEventEnum {
@JsonValue(r'social.platform.state')
socialPeriodPlatformPeriodState(r'social.platform.state');

const SocialPlatformStateEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


