//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/exposure_area.dart';
import 'package:foresignal_api/src/model/exposure_population.dart';
import 'package:foresignal_api/src/model/exposure_places.dart';
import 'package:foresignal_api/src/model/exposure_traffic.dart';
import 'package:foresignal_api/src/model/exposure_dependents.dart';
import 'package:foresignal_api/src/model/exposure_watchlist_entities.dart';
import 'package:foresignal_api/src/model/exposure_facilities.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exposure.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Exposure {
  /// Returns a new [Exposure] instance.
  Exposure({

    required  this.area,

    required  this.computedAt,

    required  this.facilities,

    required  this.places,

    required  this.watchlistEntities,

    required  this.traffic,

    required  this.population,

     this.dependents,
  });

  @JsonKey(
    
    name: r'area',
    required: true,
    includeIfNull: false,
  )


  final ExposureArea area;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;



  @JsonKey(
    
    name: r'facilities',
    required: true,
    includeIfNull: false,
  )


  final ExposureFacilities facilities;



  @JsonKey(
    
    name: r'places',
    required: true,
    includeIfNull: false,
  )


  final ExposurePlaces places;



  @JsonKey(
    
    name: r'watchlistEntities',
    required: true,
    includeIfNull: false,
  )


  final ExposureWatchlistEntities watchlistEntities;



  @JsonKey(
    
    name: r'traffic',
    required: true,
    includeIfNull: false,
  )


  final ExposureTraffic traffic;



  @JsonKey(
    
    name: r'population',
    required: true,
    includeIfNull: false,
  )


  final ExposurePopulation population;



  @JsonKey(
    
    name: r'dependents',
    required: false,
    includeIfNull: false,
  )


  final ExposureDependents? dependents;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Exposure &&
      other.area == area &&
      other.computedAt == computedAt &&
      other.facilities == facilities &&
      other.places == places &&
      other.watchlistEntities == watchlistEntities &&
      other.traffic == traffic &&
      other.population == population &&
      other.dependents == dependents;

    @override
    int get hashCode =>
        area.hashCode +
        computedAt.hashCode +
        facilities.hashCode +
        places.hashCode +
        watchlistEntities.hashCode +
        traffic.hashCode +
        population.hashCode +
        dependents.hashCode;

  factory Exposure.fromJson(Map<String, dynamic> json) => _$ExposureFromJson(json);

  Map<String, dynamic> toJson() => _$ExposureToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

