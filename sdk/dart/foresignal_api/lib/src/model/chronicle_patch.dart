//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_area.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChroniclePatch {
  /// Returns a new [ChroniclePatch] instance.
  ChroniclePatch({

     this.name,

     this.description,

     this.visibility,

     this.tlp,

     this.area,

     this.retention,

     this.sizeCapBytes,

     this.situationIds,

     this.hypothesisSetIds,

     this.investigationIds,

     this.topicIds,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'visibility',
    required: false,
    includeIfNull: false,
  )


  final Visibility? visibility;



  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final ChronicleArea? area;



      /// `forever` (default) or an ISO-8601 duration kept after stop
  @JsonKey(
    
    name: r'retention',
    required: false,
    includeIfNull: false,
  )


  final String? retention;



          // minimum: 1
  @JsonKey(
    
    name: r'sizeCapBytes',
    required: false,
    includeIfNull: false,
  )


  final int? sizeCapBytes;



  @JsonKey(
    
    name: r'situationIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? situationIds;



  @JsonKey(
    
    name: r'hypothesisSetIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? hypothesisSetIds;



  @JsonKey(
    
    name: r'investigationIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? investigationIds;



  @JsonKey(
    
    name: r'topicIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? topicIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChroniclePatch &&
      other.name == name &&
      other.description == description &&
      other.visibility == visibility &&
      other.tlp == tlp &&
      other.area == area &&
      other.retention == retention &&
      other.sizeCapBytes == sizeCapBytes &&
      other.situationIds == situationIds &&
      other.hypothesisSetIds == hypothesisSetIds &&
      other.investigationIds == investigationIds &&
      other.topicIds == topicIds;

    @override
    int get hashCode =>
        name.hashCode +
        (description == null ? 0 : description.hashCode) +
        visibility.hashCode +
        tlp.hashCode +
        (area == null ? 0 : area.hashCode) +
        retention.hashCode +
        sizeCapBytes.hashCode +
        situationIds.hashCode +
        hypothesisSetIds.hashCode +
        investigationIds.hashCode +
        topicIds.hashCode;

  factory ChroniclePatch.fromJson(Map<String, dynamic> json) => _$ChroniclePatchFromJson(json);

  Map<String, dynamic> toJson() => _$ChroniclePatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

