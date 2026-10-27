//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_budget_provider.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmBudgetProvider {
  /// Returns a new [LlmBudgetProvider] instance.
  LlmBudgetProvider({

    required  this.providerId,

    required  this.name,

    required  this.shared,

     this.capUsd,

    required  this.spentUsd,

     this.remainingUsd,
  });

      /// The provider id (an `LlmProvider.id`)
  @JsonKey(
    
    name: r'providerId',
    required: true,
    includeIfNull: false,
  )


  final String providerId;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// True for an admin-managed shared provider, false for the user's personal provider
  @JsonKey(
    
    name: r'shared',
    required: true,
    includeIfNull: false,
  )


  final bool shared;



      /// The caller's monthly USD cap, if set
  @JsonKey(
    
    name: r'capUsd',
    required: false,
    includeIfNull: false,
  )


  final num? capUsd;



      /// Spent this UTC month
  @JsonKey(
    
    name: r'spentUsd',
    required: true,
    includeIfNull: false,
  )


  final num spentUsd;



      /// cap minus spend; null when uncapped
  @JsonKey(
    
    name: r'remainingUsd',
    required: false,
    includeIfNull: false,
  )


  final num? remainingUsd;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmBudgetProvider &&
      other.providerId == providerId &&
      other.name == name &&
      other.shared == shared &&
      other.capUsd == capUsd &&
      other.spentUsd == spentUsd &&
      other.remainingUsd == remainingUsd;

    @override
    int get hashCode =>
        providerId.hashCode +
        name.hashCode +
        shared.hashCode +
        (capUsd == null ? 0 : capUsd.hashCode) +
        spentUsd.hashCode +
        (remainingUsd == null ? 0 : remainingUsd.hashCode);

  factory LlmBudgetProvider.fromJson(Map<String, dynamic> json) => _$LlmBudgetProviderFromJson(json);

  Map<String, dynamic> toJson() => _$LlmBudgetProviderToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

