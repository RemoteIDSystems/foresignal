//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/native_push_payload_apns_aps_alert.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'native_push_payload_apns_aps.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NativePushPayloadApnsAps {
  /// Returns a new [NativePushPayloadApnsAps] instance.
  NativePushPayloadApnsAps({

    required  this.alert,

     this.sound,

     this.mutableContent,

     this.interruptionLevel,

     this.threadId,

     this.category,

     this.badge,
  });

  @JsonKey(
    
    name: r'alert',
    required: true,
    includeIfNull: false,
  )


  final NativePushPayloadApnsApsAlert alert;



  @JsonKey(
    
    name: r'sound',
    required: false,
    includeIfNull: false,
  )


  final NativePushPayloadApnsApsSoundEnum? sound;



  @JsonKey(
    
    name: r'mutable-content',
    required: false,
    includeIfNull: false,
  )


  final NativePushPayloadApnsApsMutableContentEnum? mutableContent;



  @JsonKey(
    
    name: r'interruption-level',
    required: false,
    includeIfNull: false,
  )


  final NativePushPayloadApnsApsInterruptionLevelEnum? interruptionLevel;



  @JsonKey(
    
    name: r'thread-id',
    required: false,
    includeIfNull: false,
  )


  final String? threadId;



      /// FORESIGNAL_<EVENT> (032); FORESIGNAL_ALERT_ACTIONABLE for firing alerts with actions (033)
  @JsonKey(
    
    name: r'category',
    required: false,
    includeIfNull: false,
  )


  final String? category;



      /// Unread count (033)
  @JsonKey(
    
    name: r'badge',
    required: false,
    includeIfNull: false,
  )


  final int? badge;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NativePushPayloadApnsAps &&
      other.alert == alert &&
      other.sound == sound &&
      other.mutableContent == mutableContent &&
      other.interruptionLevel == interruptionLevel &&
      other.threadId == threadId &&
      other.category == category &&
      other.badge == badge;

    @override
    int get hashCode =>
        alert.hashCode +
        sound.hashCode +
        mutableContent.hashCode +
        interruptionLevel.hashCode +
        threadId.hashCode +
        category.hashCode +
        badge.hashCode;

  factory NativePushPayloadApnsAps.fromJson(Map<String, dynamic> json) => _$NativePushPayloadApnsApsFromJson(json);

  Map<String, dynamic> toJson() => _$NativePushPayloadApnsApsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NativePushPayloadApnsApsSoundEnum {
@JsonValue(r'default')
default_(r'default');

const NativePushPayloadApnsApsSoundEnum(this.value);

final String value;

@override
String toString() => value;
}


enum NativePushPayloadApnsApsMutableContentEnum {
@JsonValue(1)
number1('1');

const NativePushPayloadApnsApsMutableContentEnum(this.value);

final String value;

@override
String toString() => value;
}


enum NativePushPayloadApnsApsInterruptionLevelEnum {
@JsonValue(r'passive')
passive(r'passive'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'time-sensitive')
timeSensitive(r'time-sensitive');

const NativePushPayloadApnsApsInterruptionLevelEnum(this.value);

final String value;

@override
String toString() => value;
}


