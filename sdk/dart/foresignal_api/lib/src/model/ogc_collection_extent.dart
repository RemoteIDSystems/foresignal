//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_collection_extent_temporal.dart';
import 'package:foresignal_api/src/model/ogc_collection_extent_spatial.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_collection_extent.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcCollectionExtent {
  /// Returns a new [OgcCollectionExtent] instance.
  OgcCollectionExtent({

     this.spatial,

     this.temporal,
  });

  @JsonKey(
    
    name: r'spatial',
    required: false,
    includeIfNull: false,
  )


  final OgcCollectionExtentSpatial? spatial;



  @JsonKey(
    
    name: r'temporal',
    required: false,
    includeIfNull: false,
  )


  final OgcCollectionExtentTemporal? temporal;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcCollectionExtent &&
      other.spatial == spatial &&
      other.temporal == temporal;

    @override
    int get hashCode =>
        spatial.hashCode +
        temporal.hashCode;

  factory OgcCollectionExtent.fromJson(Map<String, dynamic> json) => _$OgcCollectionExtentFromJson(json);

  Map<String, dynamic> toJson() => _$OgcCollectionExtentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

