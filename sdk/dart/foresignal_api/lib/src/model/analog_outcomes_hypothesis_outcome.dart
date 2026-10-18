//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analog_outcomes_hypothesis_outcome.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalogOutcomesHypothesisOutcome {
  /// Returns a new [AnalogOutcomesHypothesisOutcome] instance.
  AnalogOutcomesHypothesisOutcome({

    required  this.setId,

    required  this.outcome,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'setId',
    required: true,
    includeIfNull: false,
  )


  final String setId;



  @JsonKey(
    
    name: r'outcome',
    required: true,
    includeIfNull: false,
  )


  final String outcome;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalogOutcomesHypothesisOutcome &&
      other.setId == setId &&
      other.outcome == outcome;

    @override
    int get hashCode =>
        setId.hashCode +
        outcome.hashCode;

  factory AnalogOutcomesHypothesisOutcome.fromJson(Map<String, dynamic> json) => _$AnalogOutcomesHypothesisOutcomeFromJson(json);

  Map<String, dynamic> toJson() => _$AnalogOutcomesHypothesisOutcomeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

