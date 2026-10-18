//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trigger_rule_all_of_feedback_counts.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TriggerRuleAllOfFeedbackCounts {
  /// Returns a new [TriggerRuleAllOfFeedbackCounts] instance.
  TriggerRuleAllOfFeedbackCounts({

     this.useful,

     this.noise,

     this.late_,

     this.falsePositive,
  });

          // minimum: 0
  @JsonKey(
    
    name: r'useful',
    required: false,
    includeIfNull: false,
  )


  final int? useful;



          // minimum: 0
  @JsonKey(
    
    name: r'noise',
    required: false,
    includeIfNull: false,
  )


  final int? noise;



          // minimum: 0
  @JsonKey(
    
    name: r'late',
    required: false,
    includeIfNull: false,
  )


  final int? late_;



          // minimum: 0
  @JsonKey(
    
    name: r'false_positive',
    required: false,
    includeIfNull: false,
  )


  final int? falsePositive;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TriggerRuleAllOfFeedbackCounts &&
      other.useful == useful &&
      other.noise == noise &&
      other.late_ == late_ &&
      other.falsePositive == falsePositive;

    @override
    int get hashCode =>
        useful.hashCode +
        noise.hashCode +
        late_.hashCode +
        falsePositive.hashCode;

  factory TriggerRuleAllOfFeedbackCounts.fromJson(Map<String, dynamic> json) => _$TriggerRuleAllOfFeedbackCountsFromJson(json);

  Map<String, dynamic> toJson() => _$TriggerRuleAllOfFeedbackCountsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

