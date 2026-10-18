//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/native_push_payload_fcm_message_android.dart';
import 'package:foresignal_api/src/model/native_push_payload_fcm_message_data.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'native_push_payload_fcm_message.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NativePushPayloadFcmMessage {
  /// Returns a new [NativePushPayloadFcmMessage] instance.
  NativePushPayloadFcmMessage({

    required  this.token,

    required  this.data,

     this.android,

     this.apns,
  });

      /// The FCM registration token (the v1 API's discovery document marks `token` deprecated in favor of `fid` but still accepts it)
  @JsonKey(
    
    name: r'token',
    required: true,
    includeIfNull: false,
  )


  final String token;



  @JsonKey(
    
    name: r'data',
    required: true,
    includeIfNull: false,
  )


  final NativePushPayloadFcmMessageData data;



  @JsonKey(
    
    name: r'android',
    required: false,
    includeIfNull: false,
  )


  final NativePushPayloadFcmMessageAndroid? android;



      /// For iOS/macOS devices registered via FCM: the APNs headers and aps payload
  @JsonKey(
    
    name: r'apns',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? apns;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NativePushPayloadFcmMessage &&
      other.token == token &&
      other.data == data &&
      other.android == android &&
      other.apns == apns;

    @override
    int get hashCode =>
        token.hashCode +
        data.hashCode +
        android.hashCode +
        apns.hashCode;

  factory NativePushPayloadFcmMessage.fromJson(Map<String, dynamic> json) => _$NativePushPayloadFcmMessageFromJson(json);

  Map<String, dynamic> toJson() => _$NativePushPayloadFcmMessageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

