//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/projected_impact_dependency_paths_inner.dart';
import 'package:foresignal_api/src/model/hazard_forecast.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/projected_impact_exposed_assets_inner.dart';
import 'package:foresignal_api/src/model/projected_impact_skipped_inner.dart';
import 'package:foresignal_api/src/model/finding_severity.dart';
import 'package:foresignal_api/src/model/projected_impact_inputs_inner.dart';
import 'package:foresignal_api/src/model/projected_impact_question_window.dart';
import 'package:foresignal_api/src/model/projected_impact_interval.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'projected_impact.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProjectedImpact {
  /// Returns a new [ProjectedImpact] instance.
  ProjectedImpact({

    required  this.findingId,

     this.ref,

    required  this.hazard,

    required  this.impactFamily,

    required  this.admin1,

     this.regionClass,

    required  this.pHazard,

    required  this.pConditional,

    required  this.pImpact,

    required  this.interval,

    required  this.n,

     this.severity,

     this.exposedAssets,

     this.dependencyPaths,

     this.inputs,

     this.skipped,

    required  this.labels,

     this.questionId,

    required  this.status,

     this.supersededBy,

    required  this.detectedAt,

     this.manifestId,

     this.version,

     this.caveats,

     this.admin1Name,

     this.lagHours,

     this.questionWindow,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'findingId',
    required: true,
    includeIfNull: false,
  )


  final String findingId;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



  @JsonKey(
    
    name: r'hazard',
    required: true,
    includeIfNull: false,
  )


  final HazardForecast hazard;



  @JsonKey(
    
    name: r'impactFamily',
    required: true,
    includeIfNull: false,
  )


  final String impactFamily;



  @JsonKey(
    
    name: r'admin1',
    required: true,
    includeIfNull: false,
  )


  final String admin1;



  @JsonKey(
    
    name: r'regionClass',
    required: false,
    includeIfNull: false,
  )


  final String? regionClass;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pHazard',
    required: true,
    includeIfNull: false,
  )


  final num pHazard;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pConditional',
    required: true,
    includeIfNull: false,
  )


  final num pConditional;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'pImpact',
    required: true,
    includeIfNull: false,
  )


  final num pImpact;



  @JsonKey(
    
    name: r'interval',
    required: true,
    includeIfNull: false,
  )


  final ProjectedImpactInterval interval;



  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final FindingSeverity? severity;



  @JsonKey(
    
    name: r'exposedAssets',
    required: false,
    includeIfNull: false,
  )


  final List<ProjectedImpactExposedAssetsInner>? exposedAssets;



  @JsonKey(
    
    name: r'dependencyPaths',
    required: false,
    includeIfNull: false,
  )


  final List<ProjectedImpactDependencyPathsInner>? dependencyPaths;



  @JsonKey(
    
    name: r'inputs',
    required: false,
    includeIfNull: false,
  )


  final List<ProjectedImpactInputsInner>? inputs;



  @JsonKey(
    
    name: r'skipped',
    required: false,
    includeIfNull: false,
  )


  final List<ProjectedImpactSkippedInner>? skipped;



      /// Always includes \"modelled; heuristic links may be wrong; not an official warning\"
  @JsonKey(
    
    name: r'labels',
    required: true,
    includeIfNull: false,
  )


  final List<String> labels;



      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: false,
    includeIfNull: false,
  )


  final String? questionId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ProjectedImpactStatusEnum status;



      /// ULID identifier
  @JsonKey(
    
    name: r'supersededBy',
    required: false,
    includeIfNull: false,
  )


  final String? supersededBy;



  @JsonKey(
    
    name: r'detectedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime detectedAt;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final String? version;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;



  @JsonKey(
    
    name: r'admin1Name',
    required: false,
    includeIfNull: false,
  )


  final String? admin1Name;



      /// Hours after the hazard within which the impact counts (the pair's lag)
  @JsonKey(
    
    name: r'lagHours',
    required: false,
    includeIfNull: false,
  )


  final int? lagHours;



  @JsonKey(
    
    name: r'questionWindow',
    required: false,
    includeIfNull: false,
  )


  final ProjectedImpactQuestionWindow? questionWindow;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProjectedImpact &&
      other.findingId == findingId &&
      other.ref == ref &&
      other.hazard == hazard &&
      other.impactFamily == impactFamily &&
      other.admin1 == admin1 &&
      other.regionClass == regionClass &&
      other.pHazard == pHazard &&
      other.pConditional == pConditional &&
      other.pImpact == pImpact &&
      other.interval == interval &&
      other.n == n &&
      other.severity == severity &&
      other.exposedAssets == exposedAssets &&
      other.dependencyPaths == dependencyPaths &&
      other.inputs == inputs &&
      other.skipped == skipped &&
      other.labels == labels &&
      other.questionId == questionId &&
      other.status == status &&
      other.supersededBy == supersededBy &&
      other.detectedAt == detectedAt &&
      other.manifestId == manifestId &&
      other.version == version &&
      other.caveats == caveats &&
      other.admin1Name == admin1Name &&
      other.lagHours == lagHours &&
      other.questionWindow == questionWindow;

    @override
    int get hashCode =>
        findingId.hashCode +
        ref.hashCode +
        hazard.hashCode +
        impactFamily.hashCode +
        admin1.hashCode +
        (regionClass == null ? 0 : regionClass.hashCode) +
        pHazard.hashCode +
        pConditional.hashCode +
        pImpact.hashCode +
        interval.hashCode +
        n.hashCode +
        severity.hashCode +
        exposedAssets.hashCode +
        dependencyPaths.hashCode +
        inputs.hashCode +
        skipped.hashCode +
        labels.hashCode +
        (questionId == null ? 0 : questionId.hashCode) +
        status.hashCode +
        (supersededBy == null ? 0 : supersededBy.hashCode) +
        detectedAt.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode) +
        version.hashCode +
        caveats.hashCode +
        (admin1Name == null ? 0 : admin1Name.hashCode) +
        lagHours.hashCode +
        questionWindow.hashCode;

  factory ProjectedImpact.fromJson(Map<String, dynamic> json) => _$ProjectedImpactFromJson(json);

  Map<String, dynamic> toJson() => _$ProjectedImpactToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ProjectedImpactStatusEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'superseded')
superseded(r'superseded');

const ProjectedImpactStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


