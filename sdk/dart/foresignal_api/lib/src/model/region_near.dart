//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'region_near.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionNear {
  /// Returns a new [RegionNear] instance.
  RegionNear({

    required  this.geonameid,

    required  this.name,

     this.iso2,

     this.admin1,

    required  this.distanceKm,

     this.bearingDeg,

    required  this.compass16,

     this.population,

     this.text,
  });

  @JsonKey(
    
    name: r'geonameid',
    required: true,
    includeIfNull: false,
  )


  final int geonameid;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'iso2',
    required: false,
    includeIfNull: false,
  )


  final String? iso2;



  @JsonKey(
    
    name: r'admin1',
    required: false,
    includeIfNull: false,
  )


  final String? admin1;



  @JsonKey(
    
    name: r'distanceKm',
    required: true,
    includeIfNull: false,
  )


  final num distanceKm;



  @JsonKey(
    
    name: r'bearingDeg',
    required: false,
    includeIfNull: false,
  )


  final num? bearingDeg;



  @JsonKey(
    
    name: r'compass16',
    required: true,
    includeIfNull: false,
  )


  final RegionNearCompass16Enum compass16;



  @JsonKey(
    
    name: r'population',
    required: false,
    includeIfNull: false,
  )


  final int? population;



  @JsonKey(
    
    name: r'text',
    required: false,
    includeIfNull: false,
  )


  final String? text;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionNear &&
      other.geonameid == geonameid &&
      other.name == name &&
      other.iso2 == iso2 &&
      other.admin1 == admin1 &&
      other.distanceKm == distanceKm &&
      other.bearingDeg == bearingDeg &&
      other.compass16 == compass16 &&
      other.population == population &&
      other.text == text;

    @override
    int get hashCode =>
        geonameid.hashCode +
        name.hashCode +
        (iso2 == null ? 0 : iso2.hashCode) +
        (admin1 == null ? 0 : admin1.hashCode) +
        distanceKm.hashCode +
        bearingDeg.hashCode +
        compass16.hashCode +
        (population == null ? 0 : population.hashCode) +
        text.hashCode;

  factory RegionNear.fromJson(Map<String, dynamic> json) => _$RegionNearFromJson(json);

  Map<String, dynamic> toJson() => _$RegionNearToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RegionNearCompass16Enum {
@JsonValue(r'N')
N(r'N'),
@JsonValue(r'NNE')
NNE(r'NNE'),
@JsonValue(r'NE')
NE(r'NE'),
@JsonValue(r'ENE')
ENE(r'ENE'),
@JsonValue(r'E')
E(r'E'),
@JsonValue(r'ESE')
ESE(r'ESE'),
@JsonValue(r'SE')
SE(r'SE'),
@JsonValue(r'SSE')
SSE(r'SSE'),
@JsonValue(r'S')
S(r'S'),
@JsonValue(r'SSW')
SSW(r'SSW'),
@JsonValue(r'SW')
SW(r'SW'),
@JsonValue(r'WSW')
WSW(r'WSW'),
@JsonValue(r'W')
W(r'W'),
@JsonValue(r'WNW')
WNW(r'WNW'),
@JsonValue(r'NW')
NW(r'NW'),
@JsonValue(r'NNW')
NNW(r'NNW');

const RegionNearCompass16Enum(this.value);

final String value;

@override
String toString() => value;
}


