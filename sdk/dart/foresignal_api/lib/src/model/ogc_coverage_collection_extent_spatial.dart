//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_coverage_collection_extent_spatial.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcCoverageCollectionExtentSpatial {
  /// Returns a new [OgcCoverageCollectionExtentSpatial] instance.
  OgcCoverageCollectionExtentSpatial({

     this.bbox,

     this.crs,
  });

  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<List<num>>? bbox;



  @JsonKey(
    
    name: r'crs',
    required: false,
    includeIfNull: false,
  )


  final String? crs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcCoverageCollectionExtentSpatial &&
      other.bbox == bbox &&
      other.crs == crs;

    @override
    int get hashCode =>
        bbox.hashCode +
        crs.hashCode;

  factory OgcCoverageCollectionExtentSpatial.fromJson(Map<String, dynamic> json) => _$OgcCoverageCollectionExtentSpatialFromJson(json);

  Map<String, dynamic> toJson() => _$OgcCoverageCollectionExtentSpatialToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

