//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'geometry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Geometry {
  /// Returns a new [Geometry] instance.
  Geometry({

    required  this.type,

     this.coordinates,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final GeometryTypeEnum type;



  @JsonKey(
    
    name: r'coordinates',
    required: false,
    includeIfNull: false,
  )


  final List<Object>? coordinates;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Geometry &&
      other.type == type &&
      other.coordinates == coordinates;

    @override
    int get hashCode =>
        type.hashCode +
        coordinates.hashCode;

  factory Geometry.fromJson(Map<String, dynamic> json) => _$GeometryFromJson(json);

  Map<String, dynamic> toJson() => _$GeometryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum GeometryTypeEnum {
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

const GeometryTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


