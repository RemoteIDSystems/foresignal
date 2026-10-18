//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:foresignal_api/src/model/analog_outcomes_hypothesis_outcome.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analog_outcomes.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalogOutcomes {
  /// Returns a new [AnalogOutcomes] instance.
  AnalogOutcomes({

     this.durationDays,

     this.peakSeverity,

     this.followOnFamilies7d,

     this.followOnFamilies30d,

     this.hypothesisOutcome,

     this.targetOutcomeOccurred,
  });

  @JsonKey(
    
    name: r'durationDays',
    required: false,
    includeIfNull: false,
  )


  final num? durationDays;



  @JsonKey(
    
    name: r'peakSeverity',
    required: false,
    includeIfNull: false,
  )


  final Severity? peakSeverity;



  @JsonKey(
    
    name: r'followOnFamilies7d',
    required: false,
    includeIfNull: false,
  )


  final List<String>? followOnFamilies7d;



  @JsonKey(
    
    name: r'followOnFamilies30d',
    required: false,
    includeIfNull: false,
  )


  final List<String>? followOnFamilies30d;



  @JsonKey(
    
    name: r'hypothesisOutcome',
    required: false,
    includeIfNull: false,
  )


  final AnalogOutcomesHypothesisOutcome? hypothesisOutcome;



      /// Whether the requested outcome occurred
  @JsonKey(
    
    name: r'targetOutcomeOccurred',
    required: false,
    includeIfNull: false,
  )


  final bool? targetOutcomeOccurred;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalogOutcomes &&
      other.durationDays == durationDays &&
      other.peakSeverity == peakSeverity &&
      other.followOnFamilies7d == followOnFamilies7d &&
      other.followOnFamilies30d == followOnFamilies30d &&
      other.hypothesisOutcome == hypothesisOutcome &&
      other.targetOutcomeOccurred == targetOutcomeOccurred;

    @override
    int get hashCode =>
        (durationDays == null ? 0 : durationDays.hashCode) +
        (peakSeverity == null ? 0 : peakSeverity.hashCode) +
        followOnFamilies7d.hashCode +
        followOnFamilies30d.hashCode +
        (hypothesisOutcome == null ? 0 : hypothesisOutcome.hashCode) +
        (targetOutcomeOccurred == null ? 0 : targetOutcomeOccurred.hashCode);

  factory AnalogOutcomes.fromJson(Map<String, dynamic> json) => _$AnalogOutcomesFromJson(json);

  Map<String, dynamic> toJson() => _$AnalogOutcomesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

