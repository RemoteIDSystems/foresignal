//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_mapping_geometry_one_of1.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordMappingGeometryOneOf1 {
  /// Returns a new [RecordMappingGeometryOneOf1] instance.
  RecordMappingGeometryOneOf1({

    required  this.geojson,

     this.fallback,
  });

      /// JSONPath of a GeoJSON geometry
  @JsonKey(
    
    name: r'geojson',
    required: true,
    includeIfNull: false,
  )


  final String geojson;



      /// [lon, lat] used when lon/lat (or geojson) give no geometry
  @JsonKey(
    
    name: r'fallback',
    required: false,
    includeIfNull: false,
  )


  final List<num>? fallback;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordMappingGeometryOneOf1 &&
      other.geojson == geojson &&
      other.fallback == fallback;

    @override
    int get hashCode =>
        geojson.hashCode +
        fallback.hashCode;

  factory RecordMappingGeometryOneOf1.fromJson(Map<String, dynamic> json) => _$RecordMappingGeometryOneOf1FromJson(json);

  Map<String, dynamic> toJson() => _$RecordMappingGeometryOneOf1ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

