//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'regional_outlook_skill.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionalOutlookSkill {
  /// Returns a new [RegionalOutlookSkill] instance.
  RegionalOutlookSkill({

     this.evaluatedWeeks,

    required  this.n,

     this.meanPinballLoss,

     this.crps,

     this.skillVsSeasonalNaive,

     this.skillVsClimatology,

     this.intervalCoverageP10P90,

    required  this.label,

     this.backtest,
  });

  @JsonKey(
    
    name: r'evaluatedWeeks',
    required: false,
    includeIfNull: false,
  )


  final int? evaluatedWeeks;



  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;



  @JsonKey(
    
    name: r'meanPinballLoss',
    required: false,
    includeIfNull: false,
  )


  final num? meanPinballLoss;



  @JsonKey(
    
    name: r'crps',
    required: false,
    includeIfNull: false,
  )


  final num? crps;



  @JsonKey(
    
    name: r'skillVsSeasonalNaive',
    required: false,
    includeIfNull: false,
  )


  final num? skillVsSeasonalNaive;



  @JsonKey(
    
    name: r'skillVsClimatology',
    required: false,
    includeIfNull: false,
  )


  final num? skillVsClimatology;



  @JsonKey(
    
    name: r'intervalCoverageP10P90',
    required: false,
    includeIfNull: false,
  )


  final num? intervalCoverageP10P90;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final RegionalOutlookSkillLabelEnum label;



      /// The monthly rolling-origin evaluation (last 26 weeks) against seasonal naive and climatology
  @JsonKey(
    
    name: r'backtest',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? backtest;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionalOutlookSkill &&
      other.evaluatedWeeks == evaluatedWeeks &&
      other.n == n &&
      other.meanPinballLoss == meanPinballLoss &&
      other.crps == crps &&
      other.skillVsSeasonalNaive == skillVsSeasonalNaive &&
      other.skillVsClimatology == skillVsClimatology &&
      other.intervalCoverageP10P90 == intervalCoverageP10P90 &&
      other.label == label &&
      other.backtest == backtest;

    @override
    int get hashCode =>
        evaluatedWeeks.hashCode +
        n.hashCode +
        (meanPinballLoss == null ? 0 : meanPinballLoss.hashCode) +
        (crps == null ? 0 : crps.hashCode) +
        (skillVsSeasonalNaive == null ? 0 : skillVsSeasonalNaive.hashCode) +
        (skillVsClimatology == null ? 0 : skillVsClimatology.hashCode) +
        (intervalCoverageP10P90 == null ? 0 : intervalCoverageP10P90.hashCode) +
        label.hashCode +
        (backtest == null ? 0 : backtest.hashCode);

  factory RegionalOutlookSkill.fromJson(Map<String, dynamic> json) => _$RegionalOutlookSkillFromJson(json);

  Map<String, dynamic> toJson() => _$RegionalOutlookSkillToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RegionalOutlookSkillLabelEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const RegionalOutlookSkillLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


