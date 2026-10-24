//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/intel_calibration.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'get_intel_calibration200_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GetIntelCalibration200Response {
  /// Returns a new [GetIntelCalibration200Response] instance.
  GetIntelCalibration200Response({

    required  this.scopes,
  });

  @JsonKey(
    
    name: r'scopes',
    required: true,
    includeIfNull: false,
  )


  final List<IntelCalibration> scopes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GetIntelCalibration200Response &&
      other.scopes == scopes;

    @override
    int get hashCode =>
        scopes.hashCode;

  factory GetIntelCalibration200Response.fromJson(Map<String, dynamic> json) => _$GetIntelCalibration200ResponseFromJson(json);

  Map<String, dynamic> toJson() => _$GetIntelCalibration200ResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

