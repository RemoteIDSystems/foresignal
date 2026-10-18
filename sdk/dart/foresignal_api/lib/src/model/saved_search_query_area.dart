//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'saved_search_query_area.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SavedSearchQueryArea {
  /// Returns a new [SavedSearchQueryArea] instance.
  SavedSearchQueryArea({

     this.bbox,

     this.geometry,

     this.placeId,
  });

      /// minLon,minLat,maxLon,maxLat
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final String? bbox;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



      /// 036 place id
  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SavedSearchQueryArea &&
      other.bbox == bbox &&
      other.geometry == geometry &&
      other.placeId == placeId;

    @override
    int get hashCode =>
        (bbox == null ? 0 : bbox.hashCode) +
        (geometry == null ? 0 : geometry.hashCode) +
        (placeId == null ? 0 : placeId.hashCode);

  factory SavedSearchQueryArea.fromJson(Map<String, dynamic> json) => _$SavedSearchQueryAreaFromJson(json);

  Map<String, dynamic> toJson() => _$SavedSearchQueryAreaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

