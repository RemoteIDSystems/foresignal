//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/llm_spend_window.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_provider_budget_windows.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmProviderBudgetWindows {
  /// Returns a new [LlmProviderBudgetWindows] instance.
  LlmProviderBudgetWindows({

    required  this.hour,

    required  this.day,

    required  this.month,
  });

  @JsonKey(
    
    name: r'hour',
    required: true,
    includeIfNull: false,
  )


  final LlmSpendWindow hour;



  @JsonKey(
    
    name: r'day',
    required: true,
    includeIfNull: false,
  )


  final LlmSpendWindow day;



  @JsonKey(
    
    name: r'month',
    required: true,
    includeIfNull: false,
  )


  final LlmSpendWindow month;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmProviderBudgetWindows &&
      other.hour == hour &&
      other.day == day &&
      other.month == month;

    @override
    int get hashCode =>
        hour.hashCode +
        day.hashCode +
        month.hashCode;

  factory LlmProviderBudgetWindows.fromJson(Map<String, dynamic> json) => _$LlmProviderBudgetWindowsFromJson(json);

  Map<String, dynamic> toJson() => _$LlmProviderBudgetWindowsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

