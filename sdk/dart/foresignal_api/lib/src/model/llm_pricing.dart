//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_pricing.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmPricing {
  /// Returns a new [LlmPricing] instance.
  LlmPricing({

     this.inputUsdPerMillionTokens,

     this.outputUsdPerMillionTokens,

     this.cachedInputUsdPerMillionTokens,

     this.embeddingUsdPerMillionTokens,
  });

      /// USD per million input tokens
          // minimum: 0
          // maximum: 100000
  @JsonKey(
    
    name: r'inputUsdPerMillionTokens',
    required: false,
    includeIfNull: false,
  )


  final num? inputUsdPerMillionTokens;



      /// USD per million output tokens (thinking included)
          // minimum: 0
          // maximum: 100000
  @JsonKey(
    
    name: r'outputUsdPerMillionTokens',
    required: false,
    includeIfNull: false,
  )


  final num? outputUsdPerMillionTokens;



      /// USD per million cached input tokens; the input price when unset
          // minimum: 0
          // maximum: 100000
  @JsonKey(
    
    name: r'cachedInputUsdPerMillionTokens',
    required: false,
    includeIfNull: false,
  )


  final num? cachedInputUsdPerMillionTokens;



      /// USD per million embedding input tokens; refused for anthropic, which has no embeddings endpoint
          // minimum: 0
          // maximum: 100000
  @JsonKey(
    
    name: r'embeddingUsdPerMillionTokens',
    required: false,
    includeIfNull: false,
  )


  final num? embeddingUsdPerMillionTokens;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmPricing &&
      other.inputUsdPerMillionTokens == inputUsdPerMillionTokens &&
      other.outputUsdPerMillionTokens == outputUsdPerMillionTokens &&
      other.cachedInputUsdPerMillionTokens == cachedInputUsdPerMillionTokens &&
      other.embeddingUsdPerMillionTokens == embeddingUsdPerMillionTokens;

    @override
    int get hashCode =>
        (inputUsdPerMillionTokens == null ? 0 : inputUsdPerMillionTokens.hashCode) +
        (outputUsdPerMillionTokens == null ? 0 : outputUsdPerMillionTokens.hashCode) +
        (cachedInputUsdPerMillionTokens == null ? 0 : cachedInputUsdPerMillionTokens.hashCode) +
        (embeddingUsdPerMillionTokens == null ? 0 : embeddingUsdPerMillionTokens.hashCode);

  factory LlmPricing.fromJson(Map<String, dynamic> json) => _$LlmPricingFromJson(json);

  Map<String, dynamic> toJson() => _$LlmPricingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

