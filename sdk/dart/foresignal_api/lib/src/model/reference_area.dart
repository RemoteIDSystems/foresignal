//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'reference_area.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReferenceArea {
  /// Returns a new [ReferenceArea] instance.
  ReferenceArea({

    required  this.kind,

     this.code,

    required  this.name,

    required  this.label,

     this.countryIso2,

     this.countryName,

     this.admin1,

     this.admin1Name,

    required  this.lon,

    required  this.lat,

     this.placeId,

     this.population,
  });

      /// country and admin1 are geofenceRef codes; a place is a point
  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final ReferenceAreaKindEnum kind;



      /// The geofenceRef code: a country's ISO2 code or an admin-1 code; null for a place
  @JsonKey(
    
    name: r'code',
    required: false,
    includeIfNull: false,
  )


  final String? code;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// The name with its admin-1 region and country, for display
  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;



  @JsonKey(
    
    name: r'countryIso2',
    required: false,
    includeIfNull: false,
  )


  final String? countryIso2;



  @JsonKey(
    
    name: r'countryName',
    required: false,
    includeIfNull: false,
  )


  final String? countryName;



      /// The admin-1 code of an admin-1 region, or of the region a place lies in
  @JsonKey(
    
    name: r'admin1',
    required: false,
    includeIfNull: false,
  )


  final String? admin1;



  @JsonKey(
    
    name: r'admin1Name',
    required: false,
    includeIfNull: false,
  )


  final String? admin1Name;



      /// A point inside the area (a country's label point, an admin-1 region's point on surface, a place's location)
  @JsonKey(
    
    name: r'lon',
    required: true,
    includeIfNull: false,
  )


  final num lon;



  @JsonKey(
    
    name: r'lat',
    required: true,
    includeIfNull: false,
  )


  final num lat;



      /// A place's GeoNames id
  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final int? placeId;



      /// A place's population, when known
  @JsonKey(
    
    name: r'population',
    required: false,
    includeIfNull: false,
  )


  final int? population;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReferenceArea &&
      other.kind == kind &&
      other.code == code &&
      other.name == name &&
      other.label == label &&
      other.countryIso2 == countryIso2 &&
      other.countryName == countryName &&
      other.admin1 == admin1 &&
      other.admin1Name == admin1Name &&
      other.lon == lon &&
      other.lat == lat &&
      other.placeId == placeId &&
      other.population == population;

    @override
    int get hashCode =>
        kind.hashCode +
        (code == null ? 0 : code.hashCode) +
        name.hashCode +
        label.hashCode +
        (countryIso2 == null ? 0 : countryIso2.hashCode) +
        (countryName == null ? 0 : countryName.hashCode) +
        (admin1 == null ? 0 : admin1.hashCode) +
        (admin1Name == null ? 0 : admin1Name.hashCode) +
        lon.hashCode +
        lat.hashCode +
        (placeId == null ? 0 : placeId.hashCode) +
        (population == null ? 0 : population.hashCode);

  factory ReferenceArea.fromJson(Map<String, dynamic> json) => _$ReferenceAreaFromJson(json);

  Map<String, dynamic> toJson() => _$ReferenceAreaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// country and admin1 are geofenceRef codes; a place is a point
enum ReferenceAreaKindEnum {
@JsonValue(r'country')
country(r'country'),
@JsonValue(r'admin1')
admin1(r'admin1'),
@JsonValue(r'place')
place(r'place');

const ReferenceAreaKindEnum(this.value);

final String value;

@override
String toString() => value;
}


