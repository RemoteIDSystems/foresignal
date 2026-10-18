//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exercise_stats_skipped_rules_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExerciseStatsSkippedRulesInner {
  /// Returns a new [ExerciseStatsSkippedRulesInner] instance.
  ExerciseStatsSkippedRulesInner({

    required  this.ruleId,

    required  this.reason,
  });

  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExerciseStatsSkippedRulesInner &&
      other.ruleId == ruleId &&
      other.reason == reason;

    @override
    int get hashCode =>
        ruleId.hashCode +
        reason.hashCode;

  factory ExerciseStatsSkippedRulesInner.fromJson(Map<String, dynamic> json) => _$ExerciseStatsSkippedRulesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ExerciseStatsSkippedRulesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

