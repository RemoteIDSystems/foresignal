//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_event_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelEventSummary {
  /// Returns a new [IntelEventSummary] instance.
  IntelEventSummary({

    required  this.id,

    required  this.type,

    required  this.title,

    required  this.status,

    required  this.severity,

    required  this.confidence,

    required  this.confidenceLabel,

     this.confidenceMethod,

     this.calibrated,

    required  this.evidenceQuality,

    required  this.memberCount,

     this.signalFamilies,

    required  this.startTime,

     this.endTime,

     this.lastEvidenceAt,

     this.centroid,

     this.regionTags,

     this.private,

     this.origin,

     this.mergedInto,

    required  this.version,

    required  this.updatedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final IntelEventSummaryTypeEnum type;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final IntelEventSummaryStatusEnum status;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final Severity severity;



      /// Score from EventScorer; not a calibrated probability unless `calibrated`
          // minimum: 0
          // maximum: 0.95
  @JsonKey(
    
    name: r'confidence',
    required: true,
    includeIfNull: false,
  )


  final num confidence;



      /// low < 0.4 ≤ moderate < 0.7 ≤ high
  @JsonKey(
    
    name: r'confidenceLabel',
    required: true,
    includeIfNull: false,
  )


  final IntelEventSummaryConfidenceLabelEnum confidenceLabel;



  @JsonKey(
    
    name: r'confidenceMethod',
    required: false,
    includeIfNull: false,
  )


  final String? confidenceMethod;



      /// Set only by 103 validation
  @JsonKey(
    
    name: r'calibrated',
    required: false,
    includeIfNull: false,
  )


  final bool? calibrated;



  @JsonKey(
    
    name: r'evidenceQuality',
    required: true,
    includeIfNull: false,
  )


  final IntelEventSummaryEvidenceQualityEnum evidenceQuality;



  @JsonKey(
    
    name: r'memberCount',
    required: true,
    includeIfNull: false,
  )


  final int memberCount;



  @JsonKey(
    
    name: r'signalFamilies',
    required: false,
    includeIfNull: false,
  )


  final List<String>? signalFamilies;



  @JsonKey(
    
    name: r'startTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime startTime;



  @JsonKey(
    
    name: r'endTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? endTime;



  @JsonKey(
    
    name: r'lastEvidenceAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastEvidenceAt;



  @JsonKey(
    
    name: r'centroid',
    required: false,
    includeIfNull: false,
  )


  final Geometry? centroid;



  @JsonKey(
    
    name: r'regionTags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionTags;



  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



  @JsonKey(
    
    name: r'origin',
    required: false,
    includeIfNull: false,
  )


  final IntelEventSummaryOriginEnum? origin;



      /// ULID identifier
  @JsonKey(
    
    name: r'mergedInto',
    required: false,
    includeIfNull: false,
  )


  final String? mergedInto;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelEventSummary &&
      other.id == id &&
      other.type == type &&
      other.title == title &&
      other.status == status &&
      other.severity == severity &&
      other.confidence == confidence &&
      other.confidenceLabel == confidenceLabel &&
      other.confidenceMethod == confidenceMethod &&
      other.calibrated == calibrated &&
      other.evidenceQuality == evidenceQuality &&
      other.memberCount == memberCount &&
      other.signalFamilies == signalFamilies &&
      other.startTime == startTime &&
      other.endTime == endTime &&
      other.lastEvidenceAt == lastEvidenceAt &&
      other.centroid == centroid &&
      other.regionTags == regionTags &&
      other.private == private &&
      other.origin == origin &&
      other.mergedInto == mergedInto &&
      other.version == version &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        title.hashCode +
        status.hashCode +
        severity.hashCode +
        confidence.hashCode +
        confidenceLabel.hashCode +
        confidenceMethod.hashCode +
        calibrated.hashCode +
        evidenceQuality.hashCode +
        memberCount.hashCode +
        signalFamilies.hashCode +
        startTime.hashCode +
        (endTime == null ? 0 : endTime.hashCode) +
        (lastEvidenceAt == null ? 0 : lastEvidenceAt.hashCode) +
        (centroid == null ? 0 : centroid.hashCode) +
        regionTags.hashCode +
        private.hashCode +
        origin.hashCode +
        (mergedInto == null ? 0 : mergedInto.hashCode) +
        version.hashCode +
        updatedAt.hashCode;

  factory IntelEventSummary.fromJson(Map<String, dynamic> json) => _$IntelEventSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$IntelEventSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelEventSummaryTypeEnum {
@JsonValue(r'maritime-disruption')
maritimeDisruption(r'maritime-disruption'),
@JsonValue(r'aviation-anomaly')
aviationAnomaly(r'aviation-anomaly'),
@JsonValue(r'infrastructure-disruption')
infrastructureDisruption(r'infrastructure-disruption'),
@JsonValue(r'weather-impact')
weatherImpact(r'weather-impact'),
@JsonValue(r'seismic')
seismic(r'seismic'),
@JsonValue(r'custom')
custom(r'custom');

const IntelEventSummaryTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelEventSummaryStatusEnum {
@JsonValue(r'emerging')
emerging(r'emerging'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'resolved')
resolved(r'resolved'),
@JsonValue(r'dismissed')
dismissed(r'dismissed');

const IntelEventSummaryStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


/// low < 0.4 ≤ moderate < 0.7 ≤ high
enum IntelEventSummaryConfidenceLabelEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'moderate')
moderate(r'moderate'),
@JsonValue(r'high')
high(r'high');

const IntelEventSummaryConfidenceLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelEventSummaryEvidenceQualityEnum {
@JsonValue(r'strong')
strong(r'strong'),
@JsonValue(r'moderate')
moderate(r'moderate'),
@JsonValue(r'weak')
weak(r'weak'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const IntelEventSummaryEvidenceQualityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelEventSummaryOriginEnum {
@JsonValue(r'recipe')
recipe(r'recipe'),
@JsonValue(r'analyst')
analyst(r'analyst'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'operator')
operator_(r'operator');

const IntelEventSummaryOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


