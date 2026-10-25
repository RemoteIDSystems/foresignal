//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_provider_balance_amounts_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmProviderBalanceAmountsInner {
  /// Returns a new [LlmProviderBalanceAmountsInner] instance.
  LlmProviderBalanceAmountsInner({

    required  this.amount,

    required  this.currency,
  });

  @JsonKey(
    
    name: r'amount',
    required: true,
    includeIfNull: false,
  )


  final num amount;



      /// ISO 4217 code
  @JsonKey(
    
    name: r'currency',
    required: true,
    includeIfNull: false,
  )


  final String currency;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmProviderBalanceAmountsInner &&
      other.amount == amount &&
      other.currency == currency;

    @override
    int get hashCode =>
        amount.hashCode +
        currency.hashCode;

  factory LlmProviderBalanceAmountsInner.fromJson(Map<String, dynamic> json) => _$LlmProviderBalanceAmountsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$LlmProviderBalanceAmountsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

