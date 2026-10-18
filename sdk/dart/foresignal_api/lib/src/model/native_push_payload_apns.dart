//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/native_push_payload_apns_e.dart';
import 'package:foresignal_api/src/model/native_push_payload_apns_aps.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'native_push_payload_apns.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NativePushPayloadApns {
  /// Returns a new [NativePushPayloadApns] instance.
  NativePushPayloadApns({

     this.aps,

     this.e,
  });

  @JsonKey(
    
    name: r'aps',
    required: false,
    includeIfNull: false,
  )


  final NativePushPayloadApnsAps? aps;



  @JsonKey(
    
    name: r'e',
    required: false,
    includeIfNull: false,
  )


  final NativePushPayloadApnsE? e;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NativePushPayloadApns &&
      other.aps == aps &&
      other.e == e;

    @override
    int get hashCode =>
        aps.hashCode +
        e.hashCode;

  factory NativePushPayloadApns.fromJson(Map<String, dynamic> json) => _$NativePushPayloadApnsFromJson(json);

  Map<String, dynamic> toJson() => _$NativePushPayloadApnsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

