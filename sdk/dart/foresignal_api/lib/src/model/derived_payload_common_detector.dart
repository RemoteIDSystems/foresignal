//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'derived_payload_common_detector.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DerivedPayloadCommonDetector {
  /// Returns a new [DerivedPayloadCommonDetector] instance.
  DerivedPayloadCommonDetector({

    required  this.id,

    required  this.version,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DerivedPayloadCommonDetector &&
      other.id == id &&
      other.version == version;

    @override
    int get hashCode =>
        id.hashCode +
        version.hashCode;

  factory DerivedPayloadCommonDetector.fromJson(Map<String, dynamic> json) => _$DerivedPayloadCommonDetectorFromJson(json);

  Map<String, dynamic> toJson() => _$DerivedPayloadCommonDetectorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

