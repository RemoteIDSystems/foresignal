//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/llm_provider_balance_amounts_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_provider_balance.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmProviderBalance {
  /// Returns a new [LlmProviderBalance] instance.
  LlmProviderBalance({

    required  this.provider,

    required  this.kind,

    required  this.amounts,

    required  this.fetchedAt,

     this.error,
  });

      /// Who reported it
  @JsonKey(
    
    name: r'provider',
    required: true,
    includeIfNull: false,
  )


  final String provider;



      /// `account`: the account's balance or credits; `key-limit`: what is left of this key's own spending limit
  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



      /// One amount per currency; empty when it could not be fetched
  @JsonKey(
    
    name: r'amounts',
    required: true,
    includeIfNull: false,
  )


  final List<LlmProviderBalanceAmountsInner> amounts;



  @JsonKey(
    
    name: r'fetchedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime fetchedAt;



      /// Why it could not be fetched, or null
  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmProviderBalance &&
      other.provider == provider &&
      other.kind == kind &&
      other.amounts == amounts &&
      other.fetchedAt == fetchedAt &&
      other.error == error;

    @override
    int get hashCode =>
        provider.hashCode +
        kind.hashCode +
        amounts.hashCode +
        fetchedAt.hashCode +
        (error == null ? 0 : error.hashCode);

  factory LlmProviderBalance.fromJson(Map<String, dynamic> json) => _$LlmProviderBalanceFromJson(json);

  Map<String, dynamic> toJson() => _$LlmProviderBalanceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

