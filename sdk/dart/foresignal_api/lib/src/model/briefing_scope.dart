//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'briefing_scope.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BriefingScope {
  /// Returns a new [BriefingScope] instance.
  BriefingScope({

     this.sources,

     this.kinds,

     this.bbox,

     this.ruleIds,

     this.includeHealth = true,

     this.placeIds,

     this.viewId,

     this.country,

     this.admin1,

     this.county,

     this.places,

     this.weatherProducts,

     this.includeAlertQuality = false,
  });

  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final String? bbox;



  @JsonKey(
    
    name: r'ruleIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? ruleIds;



  @JsonKey(
    defaultValue: true,
    name: r'includeHealth',
    required: false,
    includeIfNull: false,
  )


  final bool? includeHealth;



      /// Places (036) whose areas bound the scope; digest headings name the place
  @JsonKey(
    
    name: r'placeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? placeIds;



      /// Saved view id or slug (036)
  @JsonKey(
    
    name: r'viewId',
    required: false,
    includeIfNull: false,
  )


  final String? viewId;



      /// ISO2 filter (040)
  @JsonKey(
    
    name: r'country',
    required: false,
    includeIfNull: false,
  )


  final List<String>? country;



      /// ISO 3166-2 filter (040)
  @JsonKey(
    
    name: r'admin1',
    required: false,
    includeIfNull: false,
  )


  final List<String>? admin1;



      /// FIPS5 filter (040)
  @JsonKey(
    
    name: r'county',
    required: false,
    includeIfNull: false,
  )


  final List<String>? county;



      /// Place ids for the \"Weather at places\" section (empty = none); unknown ids → 400
  @JsonKey(
    
    name: r'places',
    required: false,
    includeIfNull: false,
  )


  final List<String>? places;



      /// Products for the section (default: those backing place-weather: mrms-reflectivity, mrms-mesh-60m, mrms-precip-rate, hrrr-smoke, gfs-gust, gfs-apcp); they choose the radar and MESH values and the GFS and HRRR rows (lightning, NWS alerts and the NWS forecast are always shown); unknown ids → 400
  @JsonKey(
    
    name: r'weatherProducts',
    required: false,
    includeIfNull: false,
  )


  final List<String>? weatherProducts;



      /// Include the weekly alert-quality section: lowest precision (at least 10 labeled), noisiest rules by alerts per day, rules with open tuning suggestions (115)
  @JsonKey(
    defaultValue: false,
    name: r'includeAlertQuality',
    required: false,
    includeIfNull: false,
  )


  final bool? includeAlertQuality;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BriefingScope &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.bbox == bbox &&
      other.ruleIds == ruleIds &&
      other.includeHealth == includeHealth &&
      other.placeIds == placeIds &&
      other.viewId == viewId &&
      other.country == country &&
      other.admin1 == admin1 &&
      other.county == county &&
      other.places == places &&
      other.weatherProducts == weatherProducts &&
      other.includeAlertQuality == includeAlertQuality;

    @override
    int get hashCode =>
        sources.hashCode +
        kinds.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        ruleIds.hashCode +
        includeHealth.hashCode +
        placeIds.hashCode +
        (viewId == null ? 0 : viewId.hashCode) +
        country.hashCode +
        admin1.hashCode +
        county.hashCode +
        places.hashCode +
        weatherProducts.hashCode +
        includeAlertQuality.hashCode;

  factory BriefingScope.fromJson(Map<String, dynamic> json) => _$BriefingScopeFromJson(json);

  Map<String, dynamic> toJson() => _$BriefingScopeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

