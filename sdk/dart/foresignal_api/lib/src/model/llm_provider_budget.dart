//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/llm_provider_budget_rate_limits.dart';
import 'package:foresignal_api/src/model/llm_provider_budget_windows.dart';
import 'package:foresignal_api/src/model/llm_provider_balance.dart';
import 'package:foresignal_api/src/model/llm_pricing.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_provider_budget.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmProviderBudget {
  /// Returns a new [LlmProviderBudget] instance.
  LlmProviderBudget({

    required  this.providerId,

    required  this.currency,

    required  this.priced,

    required  this.pricing,

    required  this.windows,

    required  this.rateLimits,

    required  this.providerBalance,
  });

  @JsonKey(
    
    name: r'providerId',
    required: true,
    includeIfNull: false,
  )


  final String providerId;



  @JsonKey(
    
    name: r'currency',
    required: true,
    includeIfNull: false,
  )


  final LlmProviderBudgetCurrencyEnum currency;



      /// Whether any price is set; without prices spend stays 0
  @JsonKey(
    
    name: r'priced',
    required: true,
    includeIfNull: false,
  )


  final bool priced;



  @JsonKey(
    
    name: r'pricing',
    required: true,
    includeIfNull: false,
  )


  final LlmPricing pricing;



  @JsonKey(
    
    name: r'windows',
    required: true,
    includeIfNull: false,
  )


  final LlmProviderBudgetWindows windows;



  @JsonKey(
    
    name: r'rateLimits',
    required: true,
    includeIfNull: false,
  )


  final LlmProviderBudgetRateLimits rateLimits;



      /// The provider-reported balance, or null when the provider reports none
  @JsonKey(
    
    name: r'providerBalance',
    required: true,
    includeIfNull: true,
  )


  final LlmProviderBalance? providerBalance;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmProviderBudget &&
      other.providerId == providerId &&
      other.currency == currency &&
      other.priced == priced &&
      other.pricing == pricing &&
      other.windows == windows &&
      other.rateLimits == rateLimits &&
      other.providerBalance == providerBalance;

    @override
    int get hashCode =>
        providerId.hashCode +
        currency.hashCode +
        priced.hashCode +
        pricing.hashCode +
        windows.hashCode +
        rateLimits.hashCode +
        (providerBalance == null ? 0 : providerBalance.hashCode);

  factory LlmProviderBudget.fromJson(Map<String, dynamic> json) => _$LlmProviderBudgetFromJson(json);

  Map<String, dynamic> toJson() => _$LlmProviderBudgetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LlmProviderBudgetCurrencyEnum {
@JsonValue(r'USD')
USD(r'USD');

const LlmProviderBudgetCurrencyEnum(this.value);

final String value;

@override
String toString() => value;
}


