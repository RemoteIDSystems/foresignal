//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/finding_severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'finding_event_finding.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FindingEventFinding {
  /// Returns a new [FindingEventFinding] instance.
  FindingEventFinding({

    required  this.id,

    required  this.findingType,

    required  this.title,

    required  this.severity,

    required  this.status,

     this.regionCodes,

    required  this.detectedAt,

     this.situationId,

     this.private,
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


  final FindingEventFindingFindingTypeEnum findingType;



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


  final FindingEventFindingStatusEnum status;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



  @JsonKey(
    
    name: r'detectedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime detectedAt;



      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FindingEventFinding &&
      other.id == id &&
      other.findingType == findingType &&
      other.title == title &&
      other.severity == severity &&
      other.status == status &&
      other.regionCodes == regionCodes &&
      other.detectedAt == detectedAt &&
      other.situationId == situationId &&
      other.private == private;

    @override
    int get hashCode =>
        id.hashCode +
        findingType.hashCode +
        title.hashCode +
        severity.hashCode +
        status.hashCode +
        regionCodes.hashCode +
        detectedAt.hashCode +
        (situationId == null ? 0 : situationId.hashCode) +
        private.hashCode;

  factory FindingEventFinding.fromJson(Map<String, dynamic> json) => _$FindingEventFindingFromJson(json);

  Map<String, dynamic> toJson() => _$FindingEventFindingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FindingEventFindingFindingTypeEnum {
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
@JsonValue(r'social-velocity')
socialVelocity(r'social-velocity'),
@JsonValue(r'media-reuse')
mediaReuse(r'media-reuse'),
@JsonValue(r'social-coordination')
socialCoordination(r'social-coordination'),
@JsonValue(r'attention-gap')
attentionGap(r'attention-gap');

const FindingEventFindingFindingTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum FindingEventFindingStatusEnum {
@JsonValue(r'open')
open(r'open'),
@JsonValue(r'superseded')
superseded(r'superseded'),
@JsonValue(r'dismissed')
dismissed(r'dismissed');

const FindingEventFindingStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


