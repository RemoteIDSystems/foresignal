//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'native_push_payload_apns_aps_alert.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NativePushPayloadApnsApsAlert {
  /// Returns a new [NativePushPayloadApnsApsAlert] instance.
  NativePushPayloadApnsApsAlert({

    required  this.title,

    required  this.body,
  });

  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'body',
    required: true,
    includeIfNull: false,
  )


  final String body;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NativePushPayloadApnsApsAlert &&
      other.title == title &&
      other.body == body;

    @override
    int get hashCode =>
        title.hashCode +
        body.hashCode;

  factory NativePushPayloadApnsApsAlert.fromJson(Map<String, dynamic> json) => _$NativePushPayloadApnsApsAlertFromJson(json);

  Map<String, dynamic> toJson() => _$NativePushPayloadApnsApsAlertToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

