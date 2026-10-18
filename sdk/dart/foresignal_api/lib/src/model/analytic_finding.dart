//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/analytic_finding_evidence.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/analytic_finding_inference.dart';
import 'package:foresignal_api/src/model/analytic_finding_dismissal.dart';
import 'package:foresignal_api/src/model/finding_severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analytic_finding.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalyticFinding {
  /// Returns a new [AnalyticFinding] instance.
  AnalyticFinding({

    required  this.id,

    required  this.findingType,

    required  this.title,

    required  this.severity,

    required  this.status,

    required  this.detectedAt,

     this.windowStart,

     this.windowEnd,

    required  this.regionCodes,

     this.geometry,

    required  this.private,

     this.situationId,

     this.precursorId,

     this.questionIds,

     this.supersededBy,

     this.dismissal,

     this.asOfSeq,

    required  this.manifestId,

    required  this.evidence,

    required  this.inference,

    required  this.caveats,

     this.createdAt,

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
    
    name: r'findingType',
    required: true,
    includeIfNull: false,
  )


  final AnalyticFindingFindingTypeEnum findingType;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final FindingSeverity severity;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final AnalyticFindingStatusEnum status;



  @JsonKey(
    
    name: r'detectedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime detectedAt;



  @JsonKey(
    
    name: r'windowStart',
    required: false,
    includeIfNull: false,
  )


  final DateTime? windowStart;



  @JsonKey(
    
    name: r'windowEnd',
    required: false,
    includeIfNull: false,
  )


  final DateTime? windowEnd;



  @JsonKey(
    
    name: r'regionCodes',
    required: true,
    includeIfNull: false,
  )


  final List<String> regionCodes;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'private',
    required: true,
    includeIfNull: false,
  )


  final bool private;



      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



      /// ULID identifier
  @JsonKey(
    
    name: r'precursorId',
    required: false,
    includeIfNull: false,
  )


  final String? precursorId;



  @JsonKey(
    
    name: r'questionIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? questionIds;



      /// ULID identifier
  @JsonKey(
    
    name: r'supersededBy',
    required: false,
    includeIfNull: false,
  )


  final String? supersededBy;



  @JsonKey(
    
    name: r'dismissal',
    required: false,
    includeIfNull: false,
  )


  final AnalyticFindingDismissal? dismissal;



      /// Seq the detector read up to
  @JsonKey(
    
    name: r'asOfSeq',
    required: false,
    includeIfNull: false,
  )


  final int? asOfSeq;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: true,
    includeIfNull: false,
  )


  final String manifestId;



  @JsonKey(
    
    name: r'evidence',
    required: true,
    includeIfNull: false,
  )


  final AnalyticFindingEvidence evidence;



  @JsonKey(
    
    name: r'inference',
    required: true,
    includeIfNull: false,
  )


  final AnalyticFindingInference inference;



      /// 103 codes plus `LAGGED_PUBLICATION`, `FEW_EVENTS`, `MULTIPLE_TESTS`, `LOW_REPORTING_DENSITY`, `DAILY_RESOLUTION_ONLY`, `LOW_DETECTABILITY`
  @JsonKey(
    
    name: r'caveats',
    required: true,
    includeIfNull: false,
  )


  final List<Caveat> caveats;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalyticFinding &&
      other.id == id &&
      other.findingType == findingType &&
      other.title == title &&
      other.severity == severity &&
      other.status == status &&
      other.detectedAt == detectedAt &&
      other.windowStart == windowStart &&
      other.windowEnd == windowEnd &&
      other.regionCodes == regionCodes &&
      other.geometry == geometry &&
      other.private == private &&
      other.situationId == situationId &&
      other.precursorId == precursorId &&
      other.questionIds == questionIds &&
      other.supersededBy == supersededBy &&
      other.dismissal == dismissal &&
      other.asOfSeq == asOfSeq &&
      other.manifestId == manifestId &&
      other.evidence == evidence &&
      other.inference == inference &&
      other.caveats == caveats &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        findingType.hashCode +
        title.hashCode +
        severity.hashCode +
        status.hashCode +
        detectedAt.hashCode +
        (windowStart == null ? 0 : windowStart.hashCode) +
        (windowEnd == null ? 0 : windowEnd.hashCode) +
        regionCodes.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        private.hashCode +
        (situationId == null ? 0 : situationId.hashCode) +
        (precursorId == null ? 0 : precursorId.hashCode) +
        questionIds.hashCode +
        (supersededBy == null ? 0 : supersededBy.hashCode) +
        (dismissal == null ? 0 : dismissal.hashCode) +
        asOfSeq.hashCode +
        manifestId.hashCode +
        evidence.hashCode +
        inference.hashCode +
        caveats.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory AnalyticFinding.fromJson(Map<String, dynamic> json) => _$AnalyticFindingFromJson(json);

  Map<String, dynamic> toJson() => _$AnalyticFindingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalyticFindingFindingTypeEnum {
@JsonValue(r'change-point')
changePoint(r'change-point'),
@JsonValue(r'pre-event-signal')
preEventSignal(r'pre-event-signal'),
@JsonValue(r'unexplained-move')
unexplainedMove(r'unexplained-move'),
@JsonValue(r'co-occurrence')
coOccurrence(r'co-occurrence'),
@JsonValue(r'precursor-active')
precursorActive(r'precursor-active'),
@JsonValue(r'forecast-exceedance')
forecastExceedance(r'forecast-exceedance'),
@JsonValue(r'outlook-exceedance')
outlookExceedance(r'outlook-exceedance'),
@JsonValue(r'projected-impact')
projectedImpact(r'projected-impact'),
@JsonValue(r'coverage-blindspot')
coverageBlindspot(r'coverage-blindspot'),
@JsonValue(r'coverage-divergence')
coverageDivergence(r'coverage-divergence'),
@JsonValue(r'social-velocity')
socialVelocity(r'social-velocity'),
@JsonValue(r'media-reuse')
mediaReuse(r'media-reuse'),
@JsonValue(r'social-coordination')
socialCoordination(r'social-coordination'),
@JsonValue(r'attention-gap')
attentionGap(r'attention-gap');

const AnalyticFindingFindingTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalyticFindingStatusEnum {
@JsonValue(r'open')
open(r'open'),
@JsonValue(r'superseded')
superseded(r'superseded'),
@JsonValue(r'dismissed')
dismissed(r'dismissed');

const AnalyticFindingStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


