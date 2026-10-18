//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_geometry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpGeometry {
  /// Returns a new [EdpGeometry] instance.
  EdpGeometry({

    required  this.type,

    required  this.coordinates,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final EdpGeometryTypeEnum type;



  @JsonKey(
    
    name: r'coordinates',
    required: true,
    includeIfNull: false,
  )


  final List<Object> coordinates;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpGeometry &&
      other.type == type &&
      other.coordinates == coordinates;

    @override
    int get hashCode =>
        type.hashCode +
        coordinates.hashCode;

  factory EdpGeometry.fromJson(Map<String, dynamic> json) => _$EdpGeometryFromJson(json);

  Map<String, dynamic> toJson() => _$EdpGeometryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpGeometryTypeEnum {
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
multiPolygon(r'MultiPolygon');

const EdpGeometryTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


