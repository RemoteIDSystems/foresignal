//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'normalized_record_preview_geometry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NormalizedRecordPreviewGeometry {
  /// Returns a new [NormalizedRecordPreviewGeometry] instance.
  NormalizedRecordPreviewGeometry({

    required  this.type,

     this.coordinates,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final NormalizedRecordPreviewGeometryTypeEnum type;



  @JsonKey(
    
    name: r'coordinates',
    required: false,
    includeIfNull: false,
  )


  final List<Object>? coordinates;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NormalizedRecordPreviewGeometry &&
      other.type == type &&
      other.coordinates == coordinates;

    @override
    int get hashCode =>
        type.hashCode +
        coordinates.hashCode;

  factory NormalizedRecordPreviewGeometry.fromJson(Map<String, dynamic> json) => _$NormalizedRecordPreviewGeometryFromJson(json);

  Map<String, dynamic> toJson() => _$NormalizedRecordPreviewGeometryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum NormalizedRecordPreviewGeometryTypeEnum {
@JsonValue(r'Point')
point(r'Point'),
@JsonValue(r'MultiPoint')
multiPoint(r'MultiPoint'),
@JsonValue(r'LineString')
lineString(r'LineString'),
@JsonValue(r'MultiLineString')
multiLineString(r'MultiLineString'),
@JsonValue(r'Polygon')
polygon(r'Polygon'),
@JsonValue(r'MultiPolygon')
multiPolygon(r'MultiPolygon'),
@JsonValue(r'GeometryCollection')
geometryCollection(r'GeometryCollection');

const NormalizedRecordPreviewGeometryTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


