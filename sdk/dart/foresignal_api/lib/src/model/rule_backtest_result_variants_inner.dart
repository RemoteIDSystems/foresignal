//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/rule_backtest_result_overlap.dart';
import 'package:foresignal_api/src/model/rule_backtest_result_per_day_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_backtest_result_variants_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleBacktestResultVariantsInner {
  /// Returns a new [RuleBacktestResultVariantsInner] instance.
  RuleBacktestResultVariantsInner({

    required  this.index,

    required  this.patch_,

    required  this.wouldFire,

     this.perDay,

     this.overlap,
  });

  @JsonKey(
    
    name: r'index',
    required: true,
    includeIfNull: false,
  )


  final int index;



  @JsonKey(
    
    name: r'patch',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> patch_;



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
    
    name: r'overlap',
    required: false,
    includeIfNull: false,
  )


  final RuleBacktestResultOverlap? overlap;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleBacktestResultVariantsInner &&
      other.index == index &&
      other.patch_ == patch_ &&
      other.wouldFire == wouldFire &&
      other.perDay == perDay &&
      other.overlap == overlap;

    @override
    int get hashCode =>
        index.hashCode +
        patch_.hashCode +
        wouldFire.hashCode +
        perDay.hashCode +
        overlap.hashCode;

  factory RuleBacktestResultVariantsInner.fromJson(Map<String, dynamic> json) => _$RuleBacktestResultVariantsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$RuleBacktestResultVariantsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

