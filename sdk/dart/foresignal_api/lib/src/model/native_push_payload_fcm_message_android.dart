//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'native_push_payload_fcm_message_android.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NativePushPayloadFcmMessageAndroid {
  /// Returns a new [NativePushPayloadFcmMessageAndroid] instance.
  NativePushPayloadFcmMessageAndroid({

     this.priority,

     this.ttl,

     this.collapseKey,
  });

  @JsonKey(
    
    name: r'priority',
    required: false,
    includeIfNull: false,
  )


  final NativePushPayloadFcmMessageAndroidPriorityEnum? priority;



  @JsonKey(
    
    name: r'ttl',
    required: false,
    includeIfNull: false,
  )


  final String? ttl;



  @JsonKey(
    
    name: r'collapse_key',
    required: false,
    includeIfNull: false,
  )


  final String? collapseKey;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NativePushPayloadFcmMessageAndroid &&
      other.priority == priority &&
      other.ttl == ttl &&
      other.collapseKey == collapseKey;

    @override
    int get hashCode =>
        priority.hashCode +
        ttl.hashCode +
        collapseKey.hashCode;

  factory NativePushPayloadFcmMessageAndroid.fromJson(Map<String, dynamic> json) => _$NativePushPayloadFcmMessageAndroidFromJson(json);

  Map<String, dynamic> toJson() => _$NativePushPayloadFcmMessageAndroidToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NativePushPayloadFcmMessageAndroidPriorityEnum {
@JsonValue(r'HIGH')
HIGH(r'HIGH'),
@JsonValue(r'NORMAL')
NORMAL(r'NORMAL');

const NativePushPayloadFcmMessageAndroidPriorityEnum(this.value);

final String value;

@override
String toString() => value;
}


