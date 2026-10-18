//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/mapping_value_spec.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_mapping_geometry_one_of.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordMappingGeometryOneOf {
  /// Returns a new [RecordMappingGeometryOneOf] instance.
  RecordMappingGeometryOneOf({

    required  this.lon,

    required  this.lat,

     this.fallback,
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





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordMappingGeometryOneOf &&
      other.lon == lon &&
      other.lat == lat &&
      other.fallback == fallback;

    @override
    int get hashCode =>
        lon.hashCode +
        lat.hashCode +
        fallback.hashCode;

  factory RecordMappingGeometryOneOf.fromJson(Map<String, dynamic> json) => _$RecordMappingGeometryOneOfFromJson(json);

  Map<String, dynamic> toJson() => _$RecordMappingGeometryOneOfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

