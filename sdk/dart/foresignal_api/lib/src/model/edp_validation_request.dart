//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_manifest.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_validation_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpValidationRequest {
  /// Returns a new [EdpValidationRequest] instance.
  EdpValidationRequest({

     this.manifest,

     this.sourceId,

     this.items,
  });

  @JsonKey(
    
    name: r'manifest',
    required: false,
    includeIfNull: false,
  )


  final EdpManifest? manifest;



  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



  @JsonKey(
    
    name: r'items',
    required: false,
    includeIfNull: false,
  )


  final List<Map<String, Object>>? items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpValidationRequest &&
      other.manifest == manifest &&
      other.sourceId == sourceId &&
      other.items == items;

    @override
    int get hashCode =>
        (manifest == null ? 0 : manifest.hashCode) +
        (sourceId == null ? 0 : sourceId.hashCode) +
        items.hashCode;

  factory EdpValidationRequest.fromJson(Map<String, dynamic> json) => _$EdpValidationRequestFromJson(json);

  Map<String, dynamic> toJson() => _$EdpValidationRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

