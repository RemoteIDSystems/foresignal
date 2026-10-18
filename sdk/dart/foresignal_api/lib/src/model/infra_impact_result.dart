//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/infra_impact_result_summary.dart';
import 'package:foresignal_api/src/model/infra_node.dart';
import 'package:foresignal_api/src/model/infra_impact_result_dependents_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_impact_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraImpactResult {
  /// Returns a new [InfraImpactResult] instance.
  InfraImpactResult({

    required  this.buildId,

     this.approximateArea,

    required  this.direct,

    required  this.dependents,

    required  this.summary,

    required  this.caveats,

    required  this.attribution,

    required  this.truncated,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'buildId',
    required: true,
    includeIfNull: false,
  )


  final String buildId;



      /// Seeds came from a region polygon
  @JsonKey(
    
    name: r'approximateArea',
    required: false,
    includeIfNull: false,
  )


  final bool? approximateArea;



  @JsonKey(
    
    name: r'direct',
    required: true,
    includeIfNull: false,
  )


  final List<InfraNode> direct;



  @JsonKey(
    
    name: r'dependents',
    required: true,
    includeIfNull: false,
  )


  final List<InfraImpactResultDependentsInner> dependents;



  @JsonKey(
    
    name: r'summary',
    required: true,
    includeIfNull: false,
  )


  final InfraImpactResultSummary summary;



      /// Always includes open data / heuristic links; region-level area; OSM extract date; disabled rules
  @JsonKey(
    
    name: r'caveats',
    required: true,
    includeIfNull: false,
  )


  final List<Caveat> caveats;



  @JsonKey(
    
    name: r'attribution',
    required: true,
    includeIfNull: false,
  )


  final List<String> attribution;



  @JsonKey(
    
    name: r'truncated',
    required: true,
    includeIfNull: false,
  )


  final bool truncated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraImpactResult &&
      other.buildId == buildId &&
      other.approximateArea == approximateArea &&
      other.direct == direct &&
      other.dependents == dependents &&
      other.summary == summary &&
      other.caveats == caveats &&
      other.attribution == attribution &&
      other.truncated == truncated;

    @override
    int get hashCode =>
        buildId.hashCode +
        approximateArea.hashCode +
        direct.hashCode +
        dependents.hashCode +
        summary.hashCode +
        caveats.hashCode +
        attribution.hashCode +
        truncated.hashCode;

  factory InfraImpactResult.fromJson(Map<String, dynamic> json) => _$InfraImpactResultFromJson(json);

  Map<String, dynamic> toJson() => _$InfraImpactResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

