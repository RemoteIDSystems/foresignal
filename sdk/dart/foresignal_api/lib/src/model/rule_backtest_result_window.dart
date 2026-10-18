//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_backtest_result_window.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleBacktestResultWindow {
  /// Returns a new [RuleBacktestResultWindow] instance.
  RuleBacktestResultWindow({

     this.steps,

     this.unknownSteps,
  });

  @JsonKey(
    
    name: r'steps',
    required: false,
    includeIfNull: false,
  )


  final int? steps;



  @JsonKey(
    
    name: r'unknownSteps',
    required: false,
    includeIfNull: false,
  )


  final int? unknownSteps;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleBacktestResultWindow &&
      other.steps == steps &&
      other.unknownSteps == unknownSteps;

    @override
    int get hashCode =>
        steps.hashCode +
        unknownSteps.hashCode;

  factory RuleBacktestResultWindow.fromJson(Map<String, dynamic> json) => _$RuleBacktestResultWindowFromJson(json);

  Map<String, dynamic> toJson() => _$RuleBacktestResultWindowToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

