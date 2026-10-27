//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'user_data_restriction.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UserDataRestriction {
  /// Returns a new [UserDataRestriction] instance.
  UserDataRestriction({

     this.sources,

     this.kinds,

     this.bbox,

     this.area,

     this.placeIds,
  });

      /// Allowed source ids (must exist); null = any
  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



      /// Allowed kinds (must exist); null = any
  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



      /// [minLon, minLat, maxLon, maxLat] (WGS84)
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



      /// MultiPolygon (WGS84, ≤ 2,000 vertices); combined with `bbox` when both are set
  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final Geometry? area;



      /// Places whose areas, unioned, bound what may be read; unknown places fail closed
  @JsonKey(
    
    name: r'placeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? placeIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UserDataRestriction &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.bbox == bbox &&
      other.area == area &&
      other.placeIds == placeIds;

    @override
    int get hashCode =>
        (sources == null ? 0 : sources.hashCode) +
        (kinds == null ? 0 : kinds.hashCode) +
        (bbox == null ? 0 : bbox.hashCode) +
        (area == null ? 0 : area.hashCode) +
        (placeIds == null ? 0 : placeIds.hashCode);

  factory UserDataRestriction.fromJson(Map<String, dynamic> json) => _$UserDataRestrictionFromJson(json);

  Map<String, dynamic> toJson() => _$UserDataRestrictionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

