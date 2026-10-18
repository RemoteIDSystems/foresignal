//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_backtest_result_overlap.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleBacktestResultOverlap {
  /// Returns a new [RuleBacktestResultOverlap] instance.
  RuleBacktestResultOverlap({

     this.both,

     this.onlyBacktest,

     this.onlyActual,
  });

  @JsonKey(
    
    name: r'both',
    required: false,
    includeIfNull: false,
  )


  final int? both;



  @JsonKey(
    
    name: r'onlyBacktest',
    required: false,
    includeIfNull: false,
  )


  final int? onlyBacktest;



  @JsonKey(
    
    name: r'onlyActual',
    required: false,
    includeIfNull: false,
  )


  final int? onlyActual;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleBacktestResultOverlap &&
      other.both == both &&
      other.onlyBacktest == onlyBacktest &&
      other.onlyActual == onlyActual;

    @override
    int get hashCode =>
        both.hashCode +
        onlyBacktest.hashCode +
        onlyActual.hashCode;

  factory RuleBacktestResultOverlap.fromJson(Map<String, dynamic> json) => _$RuleBacktestResultOverlapFromJson(json);

  Map<String, dynamic> toJson() => _$RuleBacktestResultOverlapToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

