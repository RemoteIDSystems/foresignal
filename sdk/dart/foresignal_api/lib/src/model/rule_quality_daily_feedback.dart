//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_quality_daily_feedback.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleQualityDailyFeedback {
  /// Returns a new [RuleQualityDailyFeedback] instance.
  RuleQualityDailyFeedback({

     this.useful,

     this.noise,

     this.late_,

     this.falsePositive,
  });

  @JsonKey(
    
    name: r'useful',
    required: false,
    includeIfNull: false,
  )


  final int? useful;



  @JsonKey(
    
    name: r'noise',
    required: false,
    includeIfNull: false,
  )


  final int? noise;



  @JsonKey(
    
    name: r'late',
    required: false,
    includeIfNull: false,
  )


  final int? late_;



  @JsonKey(
    
    name: r'false_positive',
    required: false,
    includeIfNull: false,
  )


  final int? falsePositive;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleQualityDailyFeedback &&
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

  factory RuleQualityDailyFeedback.fromJson(Map<String, dynamic> json) => _$RuleQualityDailyFeedbackFromJson(json);

  Map<String, dynamic> toJson() => _$RuleQualityDailyFeedbackToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

