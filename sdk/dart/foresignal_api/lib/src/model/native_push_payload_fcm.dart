//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/native_push_payload_fcm_message.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'native_push_payload_fcm.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NativePushPayloadFcm {
  /// Returns a new [NativePushPayloadFcm] instance.
  NativePushPayloadFcm({

     this.message,
  });

  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final NativePushPayloadFcmMessage? message;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NativePushPayloadFcm &&
      other.message == message;

    @override
    int get hashCode =>
        message.hashCode;

  factory NativePushPayloadFcm.fromJson(Map<String, dynamic> json) => _$NativePushPayloadFcmFromJson(json);

  Map<String, dynamic> toJson() => _$NativePushPayloadFcmToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

