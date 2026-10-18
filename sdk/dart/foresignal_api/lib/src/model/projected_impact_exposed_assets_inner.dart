//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'projected_impact_exposed_assets_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProjectedImpactExposedAssetsInner {
  /// Returns a new [ProjectedImpactExposedAssetsInner] instance.
  ProjectedImpactExposedAssetsInner({

    required  this.ref,

     this.title,

     this.kind,

    required  this.pHazard,

     this.distanceKm,

     this.population,

     this.band,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pHazard',
    required: true,
    includeIfNull: false,
  )


  final num pHazard;



  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;



  @JsonKey(
    
    name: r'population',
    required: false,
    includeIfNull: false,
  )


  final int? population;



      /// The published band at the asset
  @JsonKey(
    
    name: r'band',
    required: false,
    includeIfNull: false,
  )


  final String? band;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProjectedImpactExposedAssetsInner &&
      other.ref == ref &&
      other.title == title &&
      other.kind == kind &&
      other.pHazard == pHazard &&
      other.distanceKm == distanceKm &&
      other.population == population &&
      other.band == band;

    @override
    int get hashCode =>
        ref.hashCode +
        title.hashCode +
        kind.hashCode +
        pHazard.hashCode +
        (distanceKm == null ? 0 : distanceKm.hashCode) +
        (population == null ? 0 : population.hashCode) +
        (band == null ? 0 : band.hashCode);

  factory ProjectedImpactExposedAssetsInner.fromJson(Map<String, dynamic> json) => _$ProjectedImpactExposedAssetsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ProjectedImpactExposedAssetsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

