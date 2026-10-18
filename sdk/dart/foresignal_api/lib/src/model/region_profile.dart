//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'region_profile.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionProfile {
  /// Returns a new [RegionProfile] instance.
  RegionProfile({

    required  this.regionCode,

    required  this.reportingDensity,

    required  this.densityDecile,

    required  this.computedAt,
  });

      /// 040 region code: ISO 3166-1 alpha-2 country, `CC-admin1`, or `ZZ` for untagged
  @JsonKey(
    
    name: r'regionCode',
    required: true,
    includeIfNull: false,
  )


  final String regionCode;



      /// Weekly text-source event count ÷ median of similar-population countries
  @JsonKey(
    
    name: r'reportingDensity',
    required: true,
    includeIfNull: false,
  )


  final num reportingDensity;



          // minimum: 1
          // maximum: 10
  @JsonKey(
    
    name: r'densityDecile',
    required: true,
    includeIfNull: false,
  )


  final int densityDecile;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionProfile &&
      other.regionCode == regionCode &&
      other.reportingDensity == reportingDensity &&
      other.densityDecile == densityDecile &&
      other.computedAt == computedAt;

    @override
    int get hashCode =>
        regionCode.hashCode +
        reportingDensity.hashCode +
        densityDecile.hashCode +
        computedAt.hashCode;

  factory RegionProfile.fromJson(Map<String, dynamic> json) => _$RegionProfileFromJson(json);

  Map<String, dynamic> toJson() => _$RegionProfileToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

