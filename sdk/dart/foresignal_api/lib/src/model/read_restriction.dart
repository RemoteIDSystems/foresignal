//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'read_restriction.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReadRestriction {
  /// Returns a new [ReadRestriction] instance.
  ReadRestriction({

     this.sources,

     this.kinds,

     this.bbox,

     this.endpoints,

     this.federation = false,

     this.includeExercise = false,

     this.area,

     this.placeIds,

     this.excludePrivate = true,

     this.maxRequestsPerMinute,
  });

      /// Allowed source ids (must exist); null = any
  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



      /// Allowed kinds (must exist); null = any
  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



      /// [minLon, minLat, maxLon, maxLat] (WGS84)
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



      /// Allowed operations as `METHOD /v1/path/pattern` (Ant-style; method may be `*`). Each pattern must match at least one spec operation the token's scope can reach; no match → 403 ENDPOINT_NOT_ALLOWED (023) / RESTRICTED (035).
  @JsonKey(
    
    name: r'endpoints',
    required: false,
    includeIfNull: false,
  )


  final List<String>? endpoints;



      /// Allows the federation feed endpoints (with read scope)
  @JsonKey(
    defaultValue: false,
    name: r'federation',
    required: false,
    includeIfNull: false,
  )


  final bool? federation;



      /// Federation feed includes exercise records (115)
  @JsonKey(
    defaultValue: false,
    name: r'includeExercise',
    required: false,
    includeIfNull: false,
  )


  final bool? includeExercise;



      /// MultiPolygon (WGS84, ≤ 2,000 vertices) limiting reads; combined with `bbox` when both are set
  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final Geometry? area;



      /// Places (036) whose areas, unioned, bound what may be read (intersected with area and bbox); unknown places fail closed. Refused for the anonymous profile and feed keys (places are never anonymous)
  @JsonKey(
    
    name: r'placeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? placeIds;



      /// Exclude private sources. Always true for anonymous callers and feed keys; for tokens the default leaves private visibility to the 023 gate (agent and admin tokens see private sources unless this is true)
  @JsonKey(
    defaultValue: true,
    name: r'excludePrivate',
    required: false,
    includeIfNull: false,
  )


  final bool? excludePrivate;



      /// Overrides the `read` rate-limit class for this principal
          // minimum: 1
  @JsonKey(
    
    name: r'maxRequestsPerMinute',
    required: false,
    includeIfNull: false,
  )


  final int? maxRequestsPerMinute;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReadRestriction &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.bbox == bbox &&
      other.endpoints == endpoints &&
      other.federation == federation &&
      other.includeExercise == includeExercise &&
      other.area == area &&
      other.placeIds == placeIds &&
      other.excludePrivate == excludePrivate &&
      other.maxRequestsPerMinute == maxRequestsPerMinute;

    @override
    int get hashCode =>
        (sources == null ? 0 : sources.hashCode) +
        (kinds == null ? 0 : kinds.hashCode) +
        (bbox == null ? 0 : bbox.hashCode) +
        (endpoints == null ? 0 : endpoints.hashCode) +
        federation.hashCode +
        includeExercise.hashCode +
        (area == null ? 0 : area.hashCode) +
        (placeIds == null ? 0 : placeIds.hashCode) +
        excludePrivate.hashCode +
        (maxRequestsPerMinute == null ? 0 : maxRequestsPerMinute.hashCode);

  factory ReadRestriction.fromJson(Map<String, dynamic> json) => _$ReadRestrictionFromJson(json);

  Map<String, dynamic> toJson() => _$ReadRestrictionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

