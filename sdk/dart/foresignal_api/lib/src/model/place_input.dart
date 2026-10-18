//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/place_input_attributes.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceInput {
  /// Returns a new [PlaceInput] instance.
  PlaceInput({

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


  final PlaceInputShapeEnum shape;



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


  final PlaceInputKindEnum? kind;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceInput &&
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
      other.attributes == attributes;

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
        attributes.hashCode;

  factory PlaceInput.fromJson(Map<String, dynamic> json) => _$PlaceInputFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PlaceInputShapeEnum {
@JsonValue(r'point')
point(r'point'),
@JsonValue(r'polygon')
polygon(r'polygon'),
@JsonValue(r'route')
route(r'route');

const PlaceInputShapeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PlaceInputKindEnum {
@JsonValue(r'user')
user(r'user'),
@JsonValue(r'airport')
airport(r'airport'),
@JsonValue(r'asset')
asset(r'asset');

const PlaceInputKindEnum(this.value);

final String value;

@override
String toString() => value;
}


