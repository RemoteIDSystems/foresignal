//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'places_aoi.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlacesAoi {
  /// Returns a new [PlacesAoi] instance.
  PlacesAoi({

    required  this.version,

     this.geometry,

    required  this.envelope,

    required  this.placeCount,

     this.bufferKm,

    required  this.isDefault,
  });

  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



      /// MultiPolygon: each place area buffered to at least max(radiusKm, weather.aoiBufferKm)
  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



      /// [minLon, minLat, maxLon, maxLat] (WGS84)
  @JsonKey(
    
    name: r'envelope',
    required: true,
    includeIfNull: false,
  )


  final List<num> envelope;



  @JsonKey(
    
    name: r'placeCount',
    required: true,
    includeIfNull: false,
  )


  final int placeCount;



      /// weather.aoiBufferKm (default 150)
  @JsonKey(
    
    name: r'bufferKm',
    required: false,
    includeIfNull: false,
  )


  final num? bufferKm;



      /// True when there are no places and weather.defaultAoi is used
  @JsonKey(
    
    name: r'isDefault',
    required: true,
    includeIfNull: false,
  )


  final bool isDefault;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlacesAoi &&
      other.version == version &&
      other.geometry == geometry &&
      other.envelope == envelope &&
      other.placeCount == placeCount &&
      other.bufferKm == bufferKm &&
      other.isDefault == isDefault;

    @override
    int get hashCode =>
        version.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        envelope.hashCode +
        placeCount.hashCode +
        bufferKm.hashCode +
        isDefault.hashCode;

  factory PlacesAoi.fromJson(Map<String, dynamic> json) => _$PlacesAoiFromJson(json);

  Map<String, dynamic> toJson() => _$PlacesAoiToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

