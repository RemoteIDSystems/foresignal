//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_provider_test_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushProviderTestRequest {
  /// Returns a new [PushProviderTestRequest] instance.
  PushProviderTestRequest({

     this.deviceId,
  });

  @JsonKey(
    
    name: r'deviceId',
    required: false,
    includeIfNull: false,
  )


  final String? deviceId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushProviderTestRequest &&
      other.deviceId == deviceId;

    @override
    int get hashCode =>
        (deviceId == null ? 0 : deviceId.hashCode);

  factory PushProviderTestRequest.fromJson(Map<String, dynamic> json) => _$PushProviderTestRequestFromJson(json);

  Map<String, dynamic> toJson() => _$PushProviderTestRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

