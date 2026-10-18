//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/declarative_push_payload_notification.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'declarative_push_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DeclarativePushPayload {
  /// Returns a new [DeclarativePushPayload] instance.
  DeclarativePushPayload({

    required  this.webPush,

    required  this.notification,
  });

  @JsonKey(
    
    name: r'web_push',
    required: true,
    includeIfNull: false,
  )


  final DeclarativePushPayloadWebPushEnum webPush;



  @JsonKey(
    
    name: r'notification',
    required: true,
    includeIfNull: false,
  )


  final DeclarativePushPayloadNotification notification;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DeclarativePushPayload &&
      other.webPush == webPush &&
      other.notification == notification;

    @override
    int get hashCode =>
        webPush.hashCode +
        notification.hashCode;

  factory DeclarativePushPayload.fromJson(Map<String, dynamic> json) => _$DeclarativePushPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$DeclarativePushPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DeclarativePushPayloadWebPushEnum {
@JsonValue(8030)
number8030('8030');

const DeclarativePushPayloadWebPushEnum(this.value);

final String value;

@override
String toString() => value;
}


