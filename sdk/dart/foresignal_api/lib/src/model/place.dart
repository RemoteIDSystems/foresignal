//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/place_input_attributes.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Place {
  /// Returns a new [Place] instance.
  Place({

    required  this.id,

    required  this.name,

    required  this.shape,

    required  this.geometry,

     this.radiusKm = 20,

     this.bufferM,

     this.timezone,

     this.kind,

     this.tags,

     this.products,

     this.sensitive = true,

     this.attributes,

     this.area,

    required  this.version,

    required  this.createdAt,

    required  this.updatedAt,

     this.external_,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'shape',
    required: true,
    includeIfNull: false,
  )


  final PlaceShapeEnum shape;



      /// Point (point), Polygon/MultiPolygon (polygon) or LineString (route), WGS84
  @JsonKey(
    
    name: r'geometry',
    required: true,
    includeIfNull: false,
  )


  final Geometry geometry;



      /// point only
          // minimum: 0.1
          // maximum: 500
  @JsonKey(
    defaultValue: 20,
    name: r'radiusKm',
    required: false,
    includeIfNull: false,
  )


  final num? radiusKm;



      /// route only
          // minimum: 10
          // maximum: 50000
  @JsonKey(
    
    name: r'bufferM',
    required: false,
    includeIfNull: false,
  )


  final num? bufferM;



      /// IANA zone (default: server timezone)
  @JsonKey(
    
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final PlaceKindEnum? kind;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



      /// Weather product ids for place-weather (052)
  @JsonKey(
    
    name: r'products',
    required: false,
    includeIfNull: false,
  )


  final List<String>? products;



  @JsonKey(
    defaultValue: true,
    name: r'sensitive',
    required: false,
    includeIfNull: false,
  )


  final bool? sensitive;



  @JsonKey(
    
    name: r'attributes',
    required: false,
    includeIfNull: false,
  )


  final PlaceInputAttributes? attributes;



      /// Derived MultiPolygon (geography buffer for points and routes; fixed, ≤ 10,000 vertices)
  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final Geometry? area;



      /// Bumped on every save
  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;



      /// Latest values or documents of external streams bound to this place (138)
  @JsonKey(
    
    name: r'external',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? external_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Place &&
      other.id == id &&
      other.name == name &&
      other.shape == shape &&
      other.geometry == geometry &&
      other.radiusKm == radiusKm &&
      other.bufferM == bufferM &&
      other.timezone == timezone &&
      other.kind == kind &&
      other.tags == tags &&
      other.products == products &&
      other.sensitive == sensitive &&
      other.attributes == attributes &&
      other.area == area &&
      other.version == version &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.external_ == external_;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        shape.hashCode +
        geometry.hashCode +
        radiusKm.hashCode +
        bufferM.hashCode +
        (timezone == null ? 0 : timezone.hashCode) +
        kind.hashCode +
        tags.hashCode +
        products.hashCode +
        sensitive.hashCode +
        attributes.hashCode +
        area.hashCode +
        version.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        (external_ == null ? 0 : external_.hashCode);

  factory Place.fromJson(Map<String, dynamic> json) => _$PlaceFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PlaceShapeEnum {
@JsonValue(r'point')
point(r'point'),
@JsonValue(r'polygon')
polygon(r'polygon'),
@JsonValue(r'route')
route(r'route');

const PlaceShapeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PlaceKindEnum {
@JsonValue(r'user')
user(r'user'),
@JsonValue(r'airport')
airport(r'airport'),
@JsonValue(r'asset')
asset(r'asset');

const PlaceKindEnum(this.value);

final String value;

@override
String toString() => value;
}


