//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/hazard_forecast_probability_field_inner.dart';
import 'package:foresignal_api/src/model/hazard_forecast_probability_range.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hazard_forecast.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HazardForecast {
  /// Returns a new [HazardForecast] instance.
  HazardForecast({

    required  this.hazardType,

    required  this.sourceRef,

     this.issuer = 'official',

    required  this.issuedAt,

    required  this.validFrom,

    required  this.validTo,

     this.geometry,

     this.probabilityField,

    required  this.probabilityModel,

     this.category,

     this.probabilityRange,

     this.note,

     this.hazardKey,

     this.sourceRefs,

     this.pointRadiusKm,

     this.details,
  });

  @JsonKey(
    
    name: r'hazardType',
    required: true,
    includeIfNull: false,
  )


  final HazardForecastHazardTypeEnum hazardType;



      /// Pinned record reference `record:{sourceId}/{externalId}@{seq}`
  @JsonKey(
    
    name: r'sourceRef',
    required: true,
    includeIfNull: false,
  )


  final String sourceRef;



  @JsonKey(
    defaultValue: 'official',
    name: r'issuer',
    required: false,
    includeIfNull: false,
  )


  final String? issuer;



  @JsonKey(
    
    name: r'issuedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime issuedAt;



  @JsonKey(
    
    name: r'validFrom',
    required: true,
    includeIfNull: false,
  )


  final DateTime validFrom;



  @JsonKey(
    
    name: r'validTo',
    required: true,
    includeIfNull: false,
  )


  final DateTime validTo;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



      /// Published probability bands, highest first; geometries only on getProjectedImpact (lists leave them out)
  @JsonKey(
    
    name: r'probabilityField',
    required: false,
    includeIfNull: false,
  )


  final List<HazardForecastProbabilityFieldInner>? probabilityField;



      /// `none` for cones and categorical products (context only, never a probability)
  @JsonKey(
    
    name: r'probabilityModel',
    required: true,
    includeIfNull: false,
  )


  final HazardForecastProbabilityModelEnum probabilityModel;



      /// Producer category (e.g. ERO `moderate`, flood category)
  @JsonKey(
    
    name: r'category',
    required: false,
    includeIfNull: false,
  )


  final String? category;



  @JsonKey(
    
    name: r'probabilityRange',
    required: false,
    includeIfNull: false,
  )


  final HazardForecastProbabilityRange? probabilityRange;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;



      /// The hazard's identity across versions (debounce, supersession, question dedup), e.g. nhc-wind-probs:34, spc-outlooks:day1:torn, wpc-ero-d1, nwps-gauges:{lid}
  @JsonKey(
    
    name: r'hazardKey',
    required: false,
    includeIfNull: false,
  )


  final String? hazardKey;



      /// Every pinned record behind the forecast (sourceRef is the newest)
  @JsonKey(
    
    name: r'sourceRefs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sourceRefs;



      /// Radius a published point probability applies within (SPC and WPC: 25 miles = 40.2336 km)
  @JsonKey(
    
    name: r'pointRadiusKm',
    required: false,
    includeIfNull: false,
  )


  final num? pointRadiusKm;



      /// Source-specific facts: NHC thresholdKt and horizonHours; SPC day and layer; WPC day and categoryName; a gauge's basis (forecast or persistence), categories, calibration {basis, minCategory, n, hits, p, lower, upper, minSamples} and reason when no probability is derived (below-category, uncalibrated, no-geometry); a storm cell's reason (no-position-error)
  @JsonKey(
    
    name: r'details',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? details;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HazardForecast &&
      other.hazardType == hazardType &&
      other.sourceRef == sourceRef &&
      other.issuer == issuer &&
      other.issuedAt == issuedAt &&
      other.validFrom == validFrom &&
      other.validTo == validTo &&
      other.geometry == geometry &&
      other.probabilityField == probabilityField &&
      other.probabilityModel == probabilityModel &&
      other.category == category &&
      other.probabilityRange == probabilityRange &&
      other.note == note &&
      other.hazardKey == hazardKey &&
      other.sourceRefs == sourceRefs &&
      other.pointRadiusKm == pointRadiusKm &&
      other.details == details;

    @override
    int get hashCode =>
        hazardType.hashCode +
        sourceRef.hashCode +
        issuer.hashCode +
        issuedAt.hashCode +
        validFrom.hashCode +
        validTo.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        probabilityField.hashCode +
        probabilityModel.hashCode +
        (category == null ? 0 : category.hashCode) +
        (probabilityRange == null ? 0 : probabilityRange.hashCode) +
        (note == null ? 0 : note.hashCode) +
        hazardKey.hashCode +
        sourceRefs.hashCode +
        (pointRadiusKm == null ? 0 : pointRadiusKm.hashCode) +
        details.hashCode;

  factory HazardForecast.fromJson(Map<String, dynamic> json) => _$HazardForecastFromJson(json);

  Map<String, dynamic> toJson() => _$HazardForecastToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HazardForecastHazardTypeEnum {
@JsonValue(r'tropical-wind-34')
tropicalWind34(r'tropical-wind-34'),
@JsonValue(r'tropical-wind-50')
tropicalWind50(r'tropical-wind-50'),
@JsonValue(r'tropical-wind-64')
tropicalWind64(r'tropical-wind-64'),
@JsonValue(r'tropical-track')
tropicalTrack(r'tropical-track'),
@JsonValue(r'tornado')
tornado(r'tornado'),
@JsonValue(r'wind')
wind(r'wind'),
@JsonValue(r'hail')
hail(r'hail'),
@JsonValue(r'severe-categorical')
severeCategorical(r'severe-categorical'),
@JsonValue(r'excessive-rain')
excessiveRain(r'excessive-rain'),
@JsonValue(r'river-flood')
riverFlood(r'river-flood'),
@JsonValue(r'storm-cell')
stormCell(r'storm-cell');

const HazardForecastHazardTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `none` for cones and categorical products (context only, never a probability)
enum HazardForecastProbabilityModelEnum {
@JsonValue(r'published-polygons')
publishedPolygons(r'published-polygons'),
@JsonValue(r'published-point')
publishedPoint(r'published-point'),
@JsonValue(r'published-category-range')
publishedCategoryRange(r'published-category-range'),
@JsonValue(r'gauge-category')
gaugeCategory(r'gauge-category'),
@JsonValue(r'track-error-ellipse')
trackErrorEllipse(r'track-error-ellipse'),
@JsonValue(r'none')
none(r'none');

const HazardForecastProbabilityModelEnum(this.value);

final String value;

@override
String toString() => value;
}


