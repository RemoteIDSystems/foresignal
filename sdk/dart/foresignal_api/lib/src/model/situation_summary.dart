//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/severity.dart';
import 'package:foresignal_api/src/model/situation_status.dart';
import 'package:foresignal_api/src/model/situation_theme.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationSummary {
  /// Returns a new [SituationSummary] instance.
  SituationSummary({

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


  final SituationSummaryOriginEnum origin;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationSummary &&
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
      other.updatedAt == updatedAt;

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
        updatedAt.hashCode;

  factory SituationSummary.fromJson(Map<String, dynamic> json) => _$SituationSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$SituationSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SituationSummaryOriginEnum {
@JsonValue(r'auto')
auto(r'auto'),
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'analyst')
analyst(r'analyst');

const SituationSummaryOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


