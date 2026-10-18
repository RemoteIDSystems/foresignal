//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_backtest_result_per_day_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleBacktestResultPerDayInner {
  /// Returns a new [RuleBacktestResultPerDayInner] instance.
  RuleBacktestResultPerDayInner({

    required  this.date,

    required  this.count,
  });

  @JsonKey(
    
    name: r'date',
    required: true,
    includeIfNull: false,
  )


  final DateTime date;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleBacktestResultPerDayInner &&
      other.date == date &&
      other.count == count;

    @override
    int get hashCode =>
        date.hashCode +
        count.hashCode;

  factory RuleBacktestResultPerDayInner.fromJson(Map<String, dynamic> json) => _$RuleBacktestResultPerDayInnerFromJson(json);

  Map<String, dynamic> toJson() => _$RuleBacktestResultPerDayInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

