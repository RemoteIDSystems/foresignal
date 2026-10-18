//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_backtest_result_actual.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleBacktestResultActual {
  /// Returns a new [RuleBacktestResultActual] instance.
  RuleBacktestResultActual({

     this.alerts,

     this.comparable,
  });

  @JsonKey(
    
    name: r'alerts',
    required: false,
    includeIfNull: false,
  )


  final int? alerts;



      /// Definition hash matches the alert's rule version or the rule is unchanged
  @JsonKey(
    
    name: r'comparable',
    required: false,
    includeIfNull: false,
  )


  final bool? comparable;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleBacktestResultActual &&
      other.alerts == alerts &&
      other.comparable == comparable;

    @override
    int get hashCode =>
        alerts.hashCode +
        comparable.hashCode;

  factory RuleBacktestResultActual.fromJson(Map<String, dynamic> json) => _$RuleBacktestResultActualFromJson(json);

  Map<String, dynamic> toJson() => _$RuleBacktestResultActualToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

