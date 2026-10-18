//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'regional_index_def_skill.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionalIndexDefSkill {
  /// Returns a new [RegionalIndexDefSkill] instance.
  RegionalIndexDefSkill({

    required  this.evaluatedAt,

    required  this.n,

     this.pinballLoss,

     this.logScore,

     this.baselinePinballLoss,

     this.baselineLogScore,

     this.skillPinball,

     this.skillLog,

     this.baseline = 'seasonal-naive outlook',
  });

  @JsonKey(
    
    name: r'evaluatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime evaluatedAt;



  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;



  @JsonKey(
    
    name: r'pinballLoss',
    required: false,
    includeIfNull: false,
  )


  final num? pinballLoss;



  @JsonKey(
    
    name: r'logScore',
    required: false,
    includeIfNull: false,
  )


  final num? logScore;



  @JsonKey(
    
    name: r'baselinePinballLoss',
    required: false,
    includeIfNull: false,
  )


  final num? baselinePinballLoss;



  @JsonKey(
    
    name: r'baselineLogScore',
    required: false,
    includeIfNull: false,
  )


  final num? baselineLogScore;



  @JsonKey(
    
    name: r'skillPinball',
    required: false,
    includeIfNull: false,
  )


  final num? skillPinball;



  @JsonKey(
    
    name: r'skillLog',
    required: false,
    includeIfNull: false,
  )


  final num? skillLog;



  @JsonKey(
    defaultValue: 'seasonal-naive outlook',
    name: r'baseline',
    required: false,
    includeIfNull: false,
  )


  final String? baseline;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionalIndexDefSkill &&
      other.evaluatedAt == evaluatedAt &&
      other.n == n &&
      other.pinballLoss == pinballLoss &&
      other.logScore == logScore &&
      other.baselinePinballLoss == baselinePinballLoss &&
      other.baselineLogScore == baselineLogScore &&
      other.skillPinball == skillPinball &&
      other.skillLog == skillLog &&
      other.baseline == baseline;

    @override
    int get hashCode =>
        evaluatedAt.hashCode +
        n.hashCode +
        (pinballLoss == null ? 0 : pinballLoss.hashCode) +
        (logScore == null ? 0 : logScore.hashCode) +
        (baselinePinballLoss == null ? 0 : baselinePinballLoss.hashCode) +
        (baselineLogScore == null ? 0 : baselineLogScore.hashCode) +
        (skillPinball == null ? 0 : skillPinball.hashCode) +
        (skillLog == null ? 0 : skillLog.hashCode) +
        baseline.hashCode;

  factory RegionalIndexDefSkill.fromJson(Map<String, dynamic> json) => _$RegionalIndexDefSkillFromJson(json);

  Map<String, dynamic> toJson() => _$RegionalIndexDefSkillToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

