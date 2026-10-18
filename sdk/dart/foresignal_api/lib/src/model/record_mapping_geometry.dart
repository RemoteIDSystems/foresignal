//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/record_mapping_geometry_one_of.dart';
import 'package:foresignal_api/src/model/mapping_value_spec.dart';
import 'package:foresignal_api/src/model/record_mapping_geometry_one_of2.dart';
import 'package:foresignal_api/src/model/record_mapping_geometry_one_of1.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_mapping_geometry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordMappingGeometry {
  /// Returns a new [RecordMappingGeometry] instance.
  RecordMappingGeometry({

    required  this.lon,

    required  this.lat,

     this.fallback,

    required  this.geojson,

    required  this.const_,
  });

  @JsonKey(
    
    name: r'lon',
    required: true,
    includeIfNull: false,
  )


  final MappingValueSpec lon;



  @JsonKey(
    
    name: r'lat',
    required: true,
    includeIfNull: false,
  )


  final MappingValueSpec lat;



      /// [lon, lat] used when lon/lat (or geojson) give no geometry
  @JsonKey(
    
    name: r'fallback',
    required: false,
    includeIfNull: false,
  )


  final List<num>? fallback;



      /// JSONPath of a GeoJSON geometry
  @JsonKey(
    
    name: r'geojson',
    required: true,
    includeIfNull: false,
  )


  final String geojson;



  @JsonKey(
    
    name: r'const',
    required: true,
    includeIfNull: false,
  )


  final List<num> const_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordMappingGeometry &&
      other.lon == lon &&
      other.lat == lat &&
      other.fallback == fallback &&
      other.geojson == geojson &&
      other.const_ == const_;

    @override
    int get hashCode =>
        lon.hashCode +
        lat.hashCode +
        fallback.hashCode +
        geojson.hashCode +
        const_.hashCode;

  factory RecordMappingGeometry.fromJson(Map<String, dynamic> json) => _$RecordMappingGeometryFromJson(json);

  Map<String, dynamic> toJson() => _$RecordMappingGeometryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

