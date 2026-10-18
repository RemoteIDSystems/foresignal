//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/rule_backtest_result_overlap.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_samples_inner.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_variants_inner.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_anomaly.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_actual.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_per_day_inner.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_window.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_top_entities_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_backtest_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleBacktestResult {
  /// Returns a new [RuleBacktestResult] instance.
  RuleBacktestResult({

    required  this.ruleType,

    required  this.wouldFire,

     this.perDay,

     this.topEntities,

     this.samples,

     this.actual,

     this.overlap,

     this.window,

     this.anomaly,

     this.variants,
  });

  @JsonKey(
    
    name: r'ruleType',
    required: true,
    includeIfNull: false,
  )


  final RuleBacktestResultRuleTypeEnum ruleType;



  @JsonKey(
    
    name: r'wouldFire',
    required: true,
    includeIfNull: false,
  )


  final int wouldFire;



  @JsonKey(
    
    name: r'perDay',
    required: false,
    includeIfNull: false,
  )


  final List<RuleBacktestResultPerDayInner>? perDay;



  @JsonKey(
    
    name: r'topEntities',
    required: false,
    includeIfNull: false,
  )


  final List<RuleBacktestResultTopEntitiesInner>? topEntities;



  @JsonKey(
    
    name: r'samples',
    required: false,
    includeIfNull: false,
  )


  final List<RuleBacktestResultSamplesInner>? samples;



  @JsonKey(
    
    name: r'actual',
    required: false,
    includeIfNull: false,
  )


  final RuleBacktestResultActual? actual;



  @JsonKey(
    
    name: r'overlap',
    required: false,
    includeIfNull: false,
  )


  final RuleBacktestResultOverlap? overlap;



  @JsonKey(
    
    name: r'window',
    required: false,
    includeIfNull: false,
  )


  final RuleBacktestResultWindow? window;



  @JsonKey(
    
    name: r'anomaly',
    required: false,
    includeIfNull: false,
  )


  final RuleBacktestResultAnomaly? anomaly;



  @JsonKey(
    
    name: r'variants',
    required: false,
    includeIfNull: false,
  )


  final List<RuleBacktestResultVariantsInner>? variants;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleBacktestResult &&
      other.ruleType == ruleType &&
      other.wouldFire == wouldFire &&
      other.perDay == perDay &&
      other.topEntities == topEntities &&
      other.samples == samples &&
      other.actual == actual &&
      other.overlap == overlap &&
      other.window == window &&
      other.anomaly == anomaly &&
      other.variants == variants;

    @override
    int get hashCode =>
        ruleType.hashCode +
        wouldFire.hashCode +
        perDay.hashCode +
        topEntities.hashCode +
        samples.hashCode +
        actual.hashCode +
        overlap.hashCode +
        (window == null ? 0 : window.hashCode) +
        (anomaly == null ? 0 : anomaly.hashCode) +
        variants.hashCode;

  factory RuleBacktestResult.fromJson(Map<String, dynamic> json) => _$RuleBacktestResultFromJson(json);

  Map<String, dynamic> toJson() => _$RuleBacktestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RuleBacktestResultRuleTypeEnum {
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'count')
count(r'count'),
@JsonValue(r'absence')
absence(r'absence'),
@JsonValue(r'anomaly')
anomaly(r'anomaly');

const RuleBacktestResultRuleTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


