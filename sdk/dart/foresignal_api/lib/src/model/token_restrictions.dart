//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'token_restrictions.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TokenRestrictions {
  /// Returns a new [TokenRestrictions] instance.
  TokenRestrictions({

     this.sources,

     this.kinds,

     this.bbox,

     this.endpoints,

     this.federation = false,

     this.includeExercise = false,
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





    @override
    bool operator ==(Object other) => identical(this, other) || other is TokenRestrictions &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.bbox == bbox &&
      other.endpoints == endpoints &&
      other.federation == federation &&
      other.includeExercise == includeExercise;

    @override
    int get hashCode =>
        (sources == null ? 0 : sources.hashCode) +
        (kinds == null ? 0 : kinds.hashCode) +
        (bbox == null ? 0 : bbox.hashCode) +
        (endpoints == null ? 0 : endpoints.hashCode) +
        federation.hashCode +
        includeExercise.hashCode;

  factory TokenRestrictions.fromJson(Map<String, dynamic> json) => _$TokenRestrictionsFromJson(json);

  Map<String, dynamic> toJson() => _$TokenRestrictionsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

