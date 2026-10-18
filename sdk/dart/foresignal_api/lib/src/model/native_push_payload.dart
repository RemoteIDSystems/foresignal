//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/native_push_payload_fcm.dart';
import 'package:foresignal_api/src/model/native_push_payload_apns.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'native_push_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NativePushPayload {
  /// Returns a new [NativePushPayload] instance.
  NativePushPayload({

     this.apns,

     this.fcm,

     this.exercise = false,
  });

  @JsonKey(
    
    name: r'apns',
    required: false,
    includeIfNull: false,
  )


  final NativePushPayloadApns? apns;



  @JsonKey(
    
    name: r'fcm',
    required: false,
    includeIfNull: false,
  )


  final NativePushPayloadFcm? fcm;



      /// Exercise; never time-sensitive or critical
  @JsonKey(
    defaultValue: false,
    name: r'exercise',
    required: false,
    includeIfNull: false,
  )


  final bool? exercise;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NativePushPayload &&
      other.apns == apns &&
      other.fcm == fcm &&
      other.exercise == exercise;

    @override
    int get hashCode =>
        apns.hashCode +
        fcm.hashCode +
        exercise.hashCode;

  factory NativePushPayload.fromJson(Map<String, dynamic> json) => _$NativePushPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$NativePushPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

