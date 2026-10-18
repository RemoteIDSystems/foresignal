//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'country_ref.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CountryRef {
  /// Returns a new [CountryRef] instance.
  CountryRef({

    required  this.iso2,

    required  this.iso3,

     this.isoNumeric,

     this.fips,

    required  this.name,

     this.nameLong,

     this.continent,

    required  this.labelLon,

    required  this.labelLat,

     this.aliases,
  });

  @JsonKey(
    
    name: r'iso2',
    required: true,
    includeIfNull: false,
  )


  final String iso2;



  @JsonKey(
    
    name: r'iso3',
    required: true,
    includeIfNull: false,
  )


  final String iso3;



  @JsonKey(
    
    name: r'isoNumeric',
    required: false,
    includeIfNull: false,
  )


  final String? isoNumeric;



  @JsonKey(
    
    name: r'fips',
    required: false,
    includeIfNull: false,
  )


  final String? fips;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'nameLong',
    required: false,
    includeIfNull: false,
  )


  final String? nameLong;



  @JsonKey(
    
    name: r'continent',
    required: false,
    includeIfNull: false,
  )


  final String? continent;



  @JsonKey(
    
    name: r'labelLon',
    required: true,
    includeIfNull: false,
  )


  final num labelLon;



  @JsonKey(
    
    name: r'labelLat',
    required: true,
    includeIfNull: false,
  )


  final num labelLat;



  @JsonKey(
    
    name: r'aliases',
    required: false,
    includeIfNull: false,
  )


  final List<String>? aliases;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CountryRef &&
      other.iso2 == iso2 &&
      other.iso3 == iso3 &&
      other.isoNumeric == isoNumeric &&
      other.fips == fips &&
      other.name == name &&
      other.nameLong == nameLong &&
      other.continent == continent &&
      other.labelLon == labelLon &&
      other.labelLat == labelLat &&
      other.aliases == aliases;

    @override
    int get hashCode =>
        iso2.hashCode +
        iso3.hashCode +
        (isoNumeric == null ? 0 : isoNumeric.hashCode) +
        (fips == null ? 0 : fips.hashCode) +
        name.hashCode +
        (nameLong == null ? 0 : nameLong.hashCode) +
        (continent == null ? 0 : continent.hashCode) +
        labelLon.hashCode +
        labelLat.hashCode +
        aliases.hashCode;

  factory CountryRef.fromJson(Map<String, dynamic> json) => _$CountryRefFromJson(json);

  Map<String, dynamic> toJson() => _$CountryRefToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

