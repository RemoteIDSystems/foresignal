//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intelligence_event_inference.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelligenceEventInference {
  /// Returns a new [IntelligenceEventInference] instance.
  IntelligenceEventInference({

    required  this.confidence,

    required  this.confidenceLabel,

    required  this.confidenceMethod,

    required  this.calibrated,

    required  this.evidenceQuality,

    required  this.startTime,

     this.endTime,

     this.lastEvidenceAt,

     this.geometry,

     this.centroid,

     this.regionTags,

    required  this.explanation,
  });

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


  final IntelligenceEventInferenceConfidenceLabelEnum confidenceLabel;



  @JsonKey(
    
    name: r'confidenceMethod',
    required: true,
    includeIfNull: false,
  )


  final String confidenceMethod;



      /// Set only by 103 validation
  @JsonKey(
    
    name: r'calibrated',
    required: true,
    includeIfNull: false,
  )


  final bool calibrated;



  @JsonKey(
    
    name: r'evidenceQuality',
    required: true,
    includeIfNull: false,
  )


  final IntelligenceEventInferenceEvidenceQualityEnum evidenceQuality;



  @JsonKey(
    
    name: r'startTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime startTime;



      /// null while open
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
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



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



      /// Deterministic templated text citing members
  @JsonKey(
    
    name: r'explanation',
    required: true,
    includeIfNull: false,
  )


  final String explanation;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelligenceEventInference &&
      other.confidence == confidence &&
      other.confidenceLabel == confidenceLabel &&
      other.confidenceMethod == confidenceMethod &&
      other.calibrated == calibrated &&
      other.evidenceQuality == evidenceQuality &&
      other.startTime == startTime &&
      other.endTime == endTime &&
      other.lastEvidenceAt == lastEvidenceAt &&
      other.geometry == geometry &&
      other.centroid == centroid &&
      other.regionTags == regionTags &&
      other.explanation == explanation;

    @override
    int get hashCode =>
        confidence.hashCode +
        confidenceLabel.hashCode +
        confidenceMethod.hashCode +
        calibrated.hashCode +
        evidenceQuality.hashCode +
        startTime.hashCode +
        (endTime == null ? 0 : endTime.hashCode) +
        (lastEvidenceAt == null ? 0 : lastEvidenceAt.hashCode) +
        (geometry == null ? 0 : geometry.hashCode) +
        (centroid == null ? 0 : centroid.hashCode) +
        regionTags.hashCode +
        explanation.hashCode;

  factory IntelligenceEventInference.fromJson(Map<String, dynamic> json) => _$IntelligenceEventInferenceFromJson(json);

  Map<String, dynamic> toJson() => _$IntelligenceEventInferenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// low < 0.4 ≤ moderate < 0.7 ≤ high
enum IntelligenceEventInferenceConfidenceLabelEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'moderate')
moderate(r'moderate'),
@JsonValue(r'high')
high(r'high');

const IntelligenceEventInferenceConfidenceLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelligenceEventInferenceEvidenceQualityEnum {
@JsonValue(r'strong')
strong(r'strong'),
@JsonValue(r'moderate')
moderate(r'moderate'),
@JsonValue(r'weak')
weak(r'weak'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const IntelligenceEventInferenceEvidenceQualityEnum(this.value);

final String value;

@override
String toString() => value;
}


