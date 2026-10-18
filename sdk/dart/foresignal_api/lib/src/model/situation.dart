//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/severity.dart';
import 'package:foresignal_api/src/model/situation_social_section.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/situation_speculation.dart';
import 'package:foresignal_api/src/model/coverage_stats.dart';
import 'package:foresignal_api/src/model/situation_status.dart';
import 'package:foresignal_api/src/model/situation_evidence.dart';
import 'package:foresignal_api/src/model/situation_theme.dart';
import 'package:foresignal_api/src/model/situation_inference.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Situation {
  /// Returns a new [Situation] instance.
  Situation({

    required  this.id,

    required  this.title,

    required  this.theme,

    required  this.status,

    required  this.severity,

     this.severityOverridden,

    required  this.regionCodes,

     this.bbox,

     this.centroid,

    required  this.startedAt,

    required  this.lastActivityAt,

     this.closedAt,

    required  this.memberCount,

    required  this.signalFamilies,

     this.corroborationShare,

    required  this.summary,

    required  this.origin,

     this.mergedInto,

    required  this.private,

    required  this.version,

     this.updatedAt,

     this.severityOverride,

    required  this.geometry,

     this.topicIds,

     this.entityIds,

     this.investigationIds,

     this.linkerVersion,

    required  this.createdAt,

    required  this.evidence,

    required  this.inference,

    required  this.speculation,

     this.caveats,

     this.coverage,

     this.social,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'theme',
    required: true,
    includeIfNull: false,
  )


  final SituationTheme theme;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SituationStatus status;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final Severity severity;



      /// True when an operator override sets the severity
  @JsonKey(
    
    name: r'severityOverridden',
    required: false,
    includeIfNull: false,
  )


  final bool? severityOverridden;



  @JsonKey(
    
    name: r'regionCodes',
    required: true,
    includeIfNull: false,
  )


  final List<String> regionCodes;



      /// minLon, minLat, maxLon, maxLat of the geometry
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



  @JsonKey(
    
    name: r'centroid',
    required: false,
    includeIfNull: false,
  )


  final Geometry? centroid;



  @JsonKey(
    
    name: r'startedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime startedAt;



  @JsonKey(
    
    name: r'lastActivityAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime lastActivityAt;



  @JsonKey(
    
    name: r'closedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? closedAt;



          // minimum: 0
  @JsonKey(
    
    name: r'memberCount',
    required: true,
    includeIfNull: false,
  )


  final int memberCount;



      /// Signal families of the core members
  @JsonKey(
    
    name: r'signalFamilies',
    required: true,
    includeIfNull: false,
  )


  final List<String> signalFamilies;



      /// corroborated ÷ (corroborated + uncorroborated); null when no member has a canonical event
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'corroborationShare',
    required: false,
    includeIfNull: false,
  )


  final num? corroborationShare;



      /// Deterministic templated summary (inference)
  @JsonKey(
    
    name: r'summary',
    required: true,
    includeIfNull: false,
  )


  final String summary;



  @JsonKey(
    
    name: r'origin',
    required: true,
    includeIfNull: false,
  )


  final SituationOriginEnum origin;



      /// ULID identifier
  @JsonKey(
    
    name: r'mergedInto',
    required: false,
    includeIfNull: false,
  )


  final String? mergedInto;



      /// True when any member is private; follows `EgressPolicy`
  @JsonKey(
    
    name: r'private',
    required: true,
    includeIfNull: false,
  )


  final bool private;



      /// Optimistic-locking version; send it back on changes
          // minimum: 0
  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



  @JsonKey(
    
    name: r'severityOverride',
    required: false,
    includeIfNull: false,
  )


  final Severity? severityOverride;



      /// Convex hull of member geometries buffered by 25 km (≤ 500 vertices)
  @JsonKey(
    
    name: r'geometry',
    required: true,
    includeIfNull: true,
  )


  final Geometry? geometry;



      /// 117 attention topics
  @JsonKey(
    
    name: r'topicIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? topicIds;



      /// 098 entities
  @JsonKey(
    
    name: r'entityIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? entityIds;



      /// 101 investigations
  @JsonKey(
    
    name: r'investigationIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? investigationIds;



  @JsonKey(
    
    name: r'linkerVersion',
    required: false,
    includeIfNull: false,
  )


  final String? linkerVersion;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'evidence',
    required: true,
    includeIfNull: false,
  )


  final SituationEvidence evidence;



  @JsonKey(
    
    name: r'inference',
    required: true,
    includeIfNull: false,
  )


  final SituationInference inference;



  @JsonKey(
    
    name: r'speculation',
    required: true,
    includeIfNull: false,
  )


  final SituationSpeculation speculation;



      /// e.g. `UNCORROBORATED_ONLY`, `LOW_REPORTING_DENSITY`
  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;



  @JsonKey(
    
    name: r'coverage',
    required: false,
    includeIfNull: false,
  )


  final CoverageStats? coverage;



      /// 137: top narratives with status, velocity, origin concentration and lead times, social evidence groups (separate from physical corroboration) and attention gap
  @JsonKey(
    
    name: r'social',
    required: false,
    includeIfNull: false,
  )


  final SituationSocialSection? social;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Situation &&
      other.id == id &&
      other.title == title &&
      other.theme == theme &&
      other.status == status &&
      other.severity == severity &&
      other.severityOverridden == severityOverridden &&
      other.regionCodes == regionCodes &&
      other.bbox == bbox &&
      other.centroid == centroid &&
      other.startedAt == startedAt &&
      other.lastActivityAt == lastActivityAt &&
      other.closedAt == closedAt &&
      other.memberCount == memberCount &&
      other.signalFamilies == signalFamilies &&
      other.corroborationShare == corroborationShare &&
      other.summary == summary &&
      other.origin == origin &&
      other.mergedInto == mergedInto &&
      other.private == private &&
      other.version == version &&
      other.updatedAt == updatedAt &&
      other.severityOverride == severityOverride &&
      other.geometry == geometry &&
      other.topicIds == topicIds &&
      other.entityIds == entityIds &&
      other.investigationIds == investigationIds &&
      other.linkerVersion == linkerVersion &&
      other.createdAt == createdAt &&
      other.evidence == evidence &&
      other.inference == inference &&
      other.speculation == speculation &&
      other.caveats == caveats &&
      other.coverage == coverage &&
      other.social == social;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        theme.hashCode +
        status.hashCode +
        severity.hashCode +
        severityOverridden.hashCode +
        regionCodes.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        (centroid == null ? 0 : centroid.hashCode) +
        startedAt.hashCode +
        lastActivityAt.hashCode +
        (closedAt == null ? 0 : closedAt.hashCode) +
        memberCount.hashCode +
        signalFamilies.hashCode +
        (corroborationShare == null ? 0 : corroborationShare.hashCode) +
        summary.hashCode +
        origin.hashCode +
        (mergedInto == null ? 0 : mergedInto.hashCode) +
        private.hashCode +
        version.hashCode +
        updatedAt.hashCode +
        (severityOverride == null ? 0 : severityOverride.hashCode) +
        (geometry == null ? 0 : geometry.hashCode) +
        topicIds.hashCode +
        entityIds.hashCode +
        investigationIds.hashCode +
        (linkerVersion == null ? 0 : linkerVersion.hashCode) +
        createdAt.hashCode +
        evidence.hashCode +
        inference.hashCode +
        speculation.hashCode +
        caveats.hashCode +
        (coverage == null ? 0 : coverage.hashCode) +
        (social == null ? 0 : social.hashCode);

  factory Situation.fromJson(Map<String, dynamic> json) => _$SituationFromJson(json);

  Map<String, dynamic> toJson() => _$SituationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SituationOriginEnum {
@JsonValue(r'auto')
auto(r'auto'),
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'analyst')
analyst(r'analyst');

const SituationOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


