//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_provider_budget_rate_limits.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmProviderBudgetRateLimits {
  /// Returns a new [LlmProviderBudgetRateLimits] instance.
  LlmProviderBudgetRateLimits({

    required  this.requestsPerMinute,

    required  this.tokensPerDay,

    required  this.tokensToday,
  });

  @JsonKey(
    
    name: r'requestsPerMinute',
    required: true,
    includeIfNull: false,
  )


  final int requestsPerMinute;



  @JsonKey(
    
    name: r'tokensPerDay',
    required: true,
    includeIfNull: false,
  )


  final int tokensPerDay;



  @JsonKey(
    
    name: r'tokensToday',
    required: true,
    includeIfNull: false,
  )


  final int tokensToday;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmProviderBudgetRateLimits &&
      other.requestsPerMinute == requestsPerMinute &&
      other.tokensPerDay == tokensPerDay &&
      other.tokensToday == tokensToday;

    @override
    int get hashCode =>
        requestsPerMinute.hashCode +
        tokensPerDay.hashCode +
        tokensToday.hashCode;

  factory LlmProviderBudgetRateLimits.fromJson(Map<String, dynamic> json) => _$LlmProviderBudgetRateLimitsFromJson(json);

  Map<String, dynamic> toJson() => _$LlmProviderBudgetRateLimitsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

