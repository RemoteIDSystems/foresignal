//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/llm_budget_provider.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'me_llm_budget.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MeLlmBudget {
  /// Returns a new [MeLlmBudget] instance.
  MeLlmBudget({

    required  this.providers,

    required  this.totalSpentUsd,

     this.asOf,
  });

  @JsonKey(
    
    name: r'providers',
    required: true,
    includeIfNull: false,
  )


  final List<LlmBudgetProvider> providers;



      /// Total spend this UTC month across all providers
  @JsonKey(
    
    name: r'totalSpentUsd',
    required: true,
    includeIfNull: false,
  )


  final num totalSpentUsd;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MeLlmBudget &&
      other.providers == providers &&
      other.totalSpentUsd == totalSpentUsd &&
      other.asOf == asOf;

    @override
    int get hashCode =>
        providers.hashCode +
        totalSpentUsd.hashCode +
        asOf.hashCode;

  factory MeLlmBudget.fromJson(Map<String, dynamic> json) => _$MeLlmBudgetFromJson(json);

  Map<String, dynamic> toJson() => _$MeLlmBudgetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

