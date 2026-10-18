//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_spend.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialSpend {
  /// Returns a new [SocialSpend] instance.
  SocialSpend({

    required  this.currency,

    required  this.spentThisMonth,

     this.monthlyCapUsd,

     this.projectedThisMonth,

     this.standingCountsEstimateMonthly,

     this.pricesAsOf,

     this.refusedThisMonth,
  });

  @JsonKey(
    
    name: r'currency',
    required: true,
    includeIfNull: false,
  )


  final SocialSpendCurrencyEnum currency;



      /// US dollars
          // minimum: 0
  @JsonKey(
    
    name: r'spentThisMonth',
    required: true,
    includeIfNull: false,
  )


  final num spentThisMonth;



      /// Calls are refused with BUDGET_EXCEEDED when spent + estimate would exceed it
          // minimum: 0
  @JsonKey(
    
    name: r'monthlyCapUsd',
    required: false,
    includeIfNull: false,
  )


  final num? monthlyCapUsd;



      /// Projected month-end spend at the current rate
          // minimum: 0
  @JsonKey(
    
    name: r'projectedThisMonth',
    required: false,
    includeIfNull: false,
  )


  final num? projectedThisMonth;



      /// X: monthly cost of hourly counts for the current standing topics, shown before they are enabled
          // minimum: 0
  @JsonKey(
    
    name: r'standingCountsEstimateMonthly',
    required: false,
    includeIfNull: false,
  )


  final num? standingCountsEstimateMonthly;



      /// Date the seeded or edited prices were taken from the pricing page
  @JsonKey(
    
    name: r'pricesAsOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? pricesAsOf;



          // minimum: 0
  @JsonKey(
    
    name: r'refusedThisMonth',
    required: false,
    includeIfNull: false,
  )


  final int? refusedThisMonth;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialSpend &&
      other.currency == currency &&
      other.spentThisMonth == spentThisMonth &&
      other.monthlyCapUsd == monthlyCapUsd &&
      other.projectedThisMonth == projectedThisMonth &&
      other.standingCountsEstimateMonthly == standingCountsEstimateMonthly &&
      other.pricesAsOf == pricesAsOf &&
      other.refusedThisMonth == refusedThisMonth;

    @override
    int get hashCode =>
        currency.hashCode +
        spentThisMonth.hashCode +
        (monthlyCapUsd == null ? 0 : monthlyCapUsd.hashCode) +
        (projectedThisMonth == null ? 0 : projectedThisMonth.hashCode) +
        (standingCountsEstimateMonthly == null ? 0 : standingCountsEstimateMonthly.hashCode) +
        (pricesAsOf == null ? 0 : pricesAsOf.hashCode) +
        refusedThisMonth.hashCode;

  factory SocialSpend.fromJson(Map<String, dynamic> json) => _$SocialSpendFromJson(json);

  Map<String, dynamic> toJson() => _$SocialSpendToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialSpendCurrencyEnum {
@JsonValue(r'USD')
USD(r'USD');

const SocialSpendCurrencyEnum(this.value);

final String value;

@override
String toString() => value;
}


