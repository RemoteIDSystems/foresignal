//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/region_near.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'region_tags.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionTags {
  /// Returns a new [RegionTags] instance.
  RegionTags({

     this.iso2,

     this.admin1,

     this.admin2,

     this.sea,

     this.near,

     this.countryName,

     this.admin1Name,

     this.countyName,

     this.attribution,
  });

      /// null at sea
  @JsonKey(
    
    name: r'iso2',
    required: false,
    includeIfNull: false,
  )


  final String? iso2;



      /// ISO 3166-2, e.g. US-TX
  @JsonKey(
    
    name: r'admin1',
    required: false,
    includeIfNull: false,
  )


  final String? admin1;



      /// US county FIPS5
  @JsonKey(
    
    name: r'admin2',
    required: false,
    includeIfNull: false,
  )


  final String? admin2;



  @JsonKey(
    
    name: r'sea',
    required: false,
    includeIfNull: false,
  )


  final String? sea;



  @JsonKey(
    
    name: r'near',
    required: false,
    includeIfNull: false,
  )


  final RegionNear? near;



  @JsonKey(
    
    name: r'countryName',
    required: false,
    includeIfNull: false,
  )


  final String? countryName;



  @JsonKey(
    
    name: r'admin1Name',
    required: false,
    includeIfNull: false,
  )


  final String? admin1Name;



  @JsonKey(
    
    name: r'countyName',
    required: false,
    includeIfNull: false,
  )


  final String? countyName;



      /// GeoNames attribution when `near` is present
  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionTags &&
      other.iso2 == iso2 &&
      other.admin1 == admin1 &&
      other.admin2 == admin2 &&
      other.sea == sea &&
      other.near == near &&
      other.countryName == countryName &&
      other.admin1Name == admin1Name &&
      other.countyName == countyName &&
      other.attribution == attribution;

    @override
    int get hashCode =>
        (iso2 == null ? 0 : iso2.hashCode) +
        (admin1 == null ? 0 : admin1.hashCode) +
        (admin2 == null ? 0 : admin2.hashCode) +
        (sea == null ? 0 : sea.hashCode) +
        (near == null ? 0 : near.hashCode) +
        (countryName == null ? 0 : countryName.hashCode) +
        (admin1Name == null ? 0 : admin1Name.hashCode) +
        (countyName == null ? 0 : countyName.hashCode) +
        (attribution == null ? 0 : attribution.hashCode);

  factory RegionTags.fromJson(Map<String, dynamic> json) => _$RegionTagsFromJson(json);

  Map<String, dynamic> toJson() => _$RegionTagsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

