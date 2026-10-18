//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_backtest_result_anomaly.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleBacktestResultAnomaly {
  /// Returns a new [RuleBacktestResultAnomaly] instance.
  RuleBacktestResultAnomaly({

     this.warmupConsumed,

     this.byClassification,

     this.coverageDrops,
  });

  @JsonKey(
    
    name: r'warmupConsumed',
    required: false,
    includeIfNull: false,
  )


  final String? warmupConsumed;



  @JsonKey(
    
    name: r'byClassification',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byClassification;



  @JsonKey(
    
    name: r'coverageDrops',
    required: false,
    includeIfNull: false,
  )


  final int? coverageDrops;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleBacktestResultAnomaly &&
      other.warmupConsumed == warmupConsumed &&
      other.byClassification == byClassification &&
      other.coverageDrops == coverageDrops;

    @override
    int get hashCode =>
        warmupConsumed.hashCode +
        byClassification.hashCode +
        coverageDrops.hashCode;

  factory RuleBacktestResultAnomaly.fromJson(Map<String, dynamic> json) => _$RuleBacktestResultAnomalyFromJson(json);

  Map<String, dynamic> toJson() => _$RuleBacktestResultAnomalyToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

