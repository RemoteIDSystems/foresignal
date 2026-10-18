//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/finding_base_rate.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analytic_finding_inference.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalyticFindingInference {
  /// Returns a new [AnalyticFindingInference] instance.
  AnalyticFindingInference({

    required  this.detector,

    required  this.stats,

    required  this.baseRate,

    required  this.explanation,
  });

  @JsonKey(
    
    name: r'detector',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef detector;



      /// Method numbers by type: change-point `{changeAt, levelBefore, levelAfter, effect, detectionDelay, bocpdPosterior}`; pre-event-signal `{car, z, placeboP, corradoP, window, linkedEventId}`; unexplained-move `{z, linkedEventId, leadDays}`; co-occurrence `{families, pValues, fisherP, adjustedP, expectedJointRate, lift}`; precursor-active `{precursorId, expectedFrom, expectedTo, probability}`
  @JsonKey(
    
    name: r'stats',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> stats;



  @JsonKey(
    
    name: r'baseRate',
    required: true,
    includeIfNull: false,
  )


  final FindingBaseRate baseRate;



      /// Deterministic templated explanation
  @JsonKey(
    
    name: r'explanation',
    required: true,
    includeIfNull: false,
  )


  final String explanation;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalyticFindingInference &&
      other.detector == detector &&
      other.stats == stats &&
      other.baseRate == baseRate &&
      other.explanation == explanation;

    @override
    int get hashCode =>
        detector.hashCode +
        stats.hashCode +
        baseRate.hashCode +
        explanation.hashCode;

  factory AnalyticFindingInference.fromJson(Map<String, dynamic> json) => _$AnalyticFindingInferenceFromJson(json);

  Map<String, dynamic> toJson() => _$AnalyticFindingInferenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

