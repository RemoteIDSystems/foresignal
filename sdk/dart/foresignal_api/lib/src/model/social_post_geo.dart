//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_post_geo.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPostGeo {
  /// Returns a new [SocialPostGeo] instance.
  SocialPostGeo({

     this.geometry,

     this.placeName,

     this.placeId,
  });

  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'placeName',
    required: false,
    includeIfNull: false,
  )


  final String? placeName;



      /// Platform place id
  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPostGeo &&
      other.geometry == geometry &&
      other.placeName == placeName &&
      other.placeId == placeId;

    @override
    int get hashCode =>
        (geometry == null ? 0 : geometry.hashCode) +
        (placeName == null ? 0 : placeName.hashCode) +
        (placeId == null ? 0 : placeId.hashCode);

  factory SocialPostGeo.fromJson(Map<String, dynamic> json) => _$SocialPostGeoFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPostGeoToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

