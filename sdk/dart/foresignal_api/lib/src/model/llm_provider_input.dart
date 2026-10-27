//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/llm_provider_input_options.dart';
import 'package:foresignal_api/src/model/llm_pricing.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_provider_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmProviderInput {
  /// Returns a new [LlmProviderInput] instance.
  LlmProviderInput({

    required  this.id,

    required  this.type,

     this.baseUrl,

     this.chatModel,

     this.embeddingModel,

     this.embeddingDimensions,

     this.apiKey,

     this.options,

     this.requestsPerMinute = 30,

     this.tokensPerDay = 2000000,

     this.enabled = true,

     this.knowledgeCutoff,

     this.pricing,

     this.hourlyCapUsd,

     this.dailyCapUsd,

     this.monthlyCapUsd,

     this.shared,

     this.perUserHourlyCapUsd,

     this.perUserDailyCapUsd,

     this.perUserMonthlyCapUsd,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final LlmProviderInputTypeEnum type;



      /// Defaults: anthropic → https://api.anthropic.com, ollama → http://ollama:11434
  @JsonKey(
    
    name: r'baseUrl',
    required: false,
    includeIfNull: false,
  )


  final String? baseUrl;



  @JsonKey(
    
    name: r'chatModel',
    required: false,
    includeIfNull: false,
  )


  final String? chatModel;



  @JsonKey(
    
    name: r'embeddingModel',
    required: false,
    includeIfNull: false,
  )


  final String? embeddingModel;



          // minimum: 8
          // maximum: 4096
  @JsonKey(
    
    name: r'embeddingDimensions',
    required: false,
    includeIfNull: false,
  )


  final int? embeddingDimensions;



      /// Write-only
  @JsonKey(
    
    name: r'apiKey',
    required: false,
    includeIfNull: false,
  )


  final String? apiKey;



  @JsonKey(
    
    name: r'options',
    required: false,
    includeIfNull: false,
  )


  final LlmProviderInputOptions? options;



          // minimum: 1
  @JsonKey(
    defaultValue: 30,
    name: r'requestsPerMinute',
    required: false,
    includeIfNull: false,
  )


  final int? requestsPerMinute;



          // minimum: 1000
  @JsonKey(
    defaultValue: 2000000,
    name: r'tokensPerDay',
    required: false,
    includeIfNull: false,
  )


  final int? tokensPerDay;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// Training-data cutoff of the chat model (`YYYY-MM-DD`)
  @JsonKey(
    
    name: r'knowledgeCutoff',
    required: false,
    includeIfNull: false,
  )


  final DateTime? knowledgeCutoff;



      /// Prices in USD per million tokens (162); null or omitted means unpriced. Required for a USD cap.
  @JsonKey(
    
    name: r'pricing',
    required: false,
    includeIfNull: false,
  )


  final LlmPricing? pricing;



      /// The most this provider may spend in the UTC clock hour (it resets at the top of the hour), in USD, at most 4 decimal places; null means no cap (162). Spend is the provider's self-metered tokens at its `pricing`, so a cap needs pricing. Once a window's spend, with the estimated cost of the calls in flight, reaches its cap, calls are refused 429 LLM_BUDGET_EXCEEDED with Retry-After at the window's reset. Applies on top of requestsPerMinute and tokensPerDay.
          // minimum: 0
          // maximum: 10000000000
  @JsonKey(
    
    name: r'hourlyCapUsd',
    required: false,
    includeIfNull: false,
  )


  final num? hourlyCapUsd;



      /// The most this provider may spend in the UTC day (it resets at 00:00 UTC, as tokensPerDay), in USD, at most 4 decimal places; null means no cap (162). Spend is the provider's self-metered tokens at its `pricing`, so a cap needs pricing. Once a window's spend, with the estimated cost of the calls in flight, reaches its cap, calls are refused 429 LLM_BUDGET_EXCEEDED with Retry-After at the window's reset. Applies on top of requestsPerMinute and tokensPerDay.
          // minimum: 0
          // maximum: 10000000000
  @JsonKey(
    
    name: r'dailyCapUsd',
    required: false,
    includeIfNull: false,
  )


  final num? dailyCapUsd;



      /// The most this provider may spend in the UTC calendar month (it resets at 00:00 UTC on the 1st), in USD, at most 4 decimal places; null means no cap (162). Spend is the provider's self-metered tokens at its `pricing`, so a cap needs pricing. Once a window's spend, with the estimated cost of the calls in flight, reaches its cap, calls are refused 429 LLM_BUDGET_EXCEEDED with Retry-After at the window's reset. Applies on top of requestsPerMinute and tokensPerDay.
          // minimum: 0
          // maximum: 10000000000
  @JsonKey(
    
    name: r'monthlyCapUsd',
    required: false,
    includeIfNull: false,
  )


  final num? monthlyCapUsd;



      /// On create: true for a shared provider (needs `llm.shared.manage`), false for a personal one owned by the caller (needs `self.llm`). Absent, a user holding `self.llm` gets a personal provider and anyone else a shared one; an API token always creates shared providers. Ignored on replace, which keeps the kind (R2-011).
  @JsonKey(
    
    name: r'shared',
    required: false,
    includeIfNull: false,
  )


  final bool? shared;



      /// The most each user may spend on this shared provider in the UTC clock hour (it resets at the top of the hour), in USD, at most 4 decimal places; null means no per-user cap (R2-011). It counts only that user's own calls, so one user cannot exhaust the shared budget while the others continue; the provider's own caps still bound the total. Needs `pricing`; refused for a personal provider, whose own caps are already its owner's. Once reached, that user's calls are refused 429 LLM_BUDGET_EXCEEDED with Retry-After at the window's reset.
          // minimum: 0
          // maximum: 10000000000
  @JsonKey(
    
    name: r'perUserHourlyCapUsd',
    required: false,
    includeIfNull: false,
  )


  final num? perUserHourlyCapUsd;



      /// The most each user may spend on this shared provider in the UTC day (it resets at 00:00 UTC), in USD, at most 4 decimal places; null means no per-user cap (R2-011). It counts only that user's own calls, so one user cannot exhaust the shared budget while the others continue; the provider's own caps still bound the total. Needs `pricing`; refused for a personal provider, whose own caps are already its owner's. Once reached, that user's calls are refused 429 LLM_BUDGET_EXCEEDED with Retry-After at the window's reset.
          // minimum: 0
          // maximum: 10000000000
  @JsonKey(
    
    name: r'perUserDailyCapUsd',
    required: false,
    includeIfNull: false,
  )


  final num? perUserDailyCapUsd;



      /// The most each user may spend on this shared provider in the UTC calendar month (it resets at 00:00 UTC on the 1st), in USD, at most 4 decimal places; null means no per-user cap (R2-011). It counts only that user's own calls, so one user cannot exhaust the shared budget while the others continue; the provider's own caps still bound the total. Needs `pricing`; refused for a personal provider, whose own caps are already its owner's. Once reached, that user's calls are refused 429 LLM_BUDGET_EXCEEDED with Retry-After at the window's reset.
          // minimum: 0
          // maximum: 10000000000
  @JsonKey(
    
    name: r'perUserMonthlyCapUsd',
    required: false,
    includeIfNull: false,
  )


  final num? perUserMonthlyCapUsd;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmProviderInput &&
      other.id == id &&
      other.type == type &&
      other.baseUrl == baseUrl &&
      other.chatModel == chatModel &&
      other.embeddingModel == embeddingModel &&
      other.embeddingDimensions == embeddingDimensions &&
      other.apiKey == apiKey &&
      other.options == options &&
      other.requestsPerMinute == requestsPerMinute &&
      other.tokensPerDay == tokensPerDay &&
      other.enabled == enabled &&
      other.knowledgeCutoff == knowledgeCutoff &&
      other.pricing == pricing &&
      other.hourlyCapUsd == hourlyCapUsd &&
      other.dailyCapUsd == dailyCapUsd &&
      other.monthlyCapUsd == monthlyCapUsd &&
      other.shared == shared &&
      other.perUserHourlyCapUsd == perUserHourlyCapUsd &&
      other.perUserDailyCapUsd == perUserDailyCapUsd &&
      other.perUserMonthlyCapUsd == perUserMonthlyCapUsd;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        (baseUrl == null ? 0 : baseUrl.hashCode) +
        (chatModel == null ? 0 : chatModel.hashCode) +
        (embeddingModel == null ? 0 : embeddingModel.hashCode) +
        (embeddingDimensions == null ? 0 : embeddingDimensions.hashCode) +
        apiKey.hashCode +
        options.hashCode +
        requestsPerMinute.hashCode +
        tokensPerDay.hashCode +
        enabled.hashCode +
        (knowledgeCutoff == null ? 0 : knowledgeCutoff.hashCode) +
        (pricing == null ? 0 : pricing.hashCode) +
        (hourlyCapUsd == null ? 0 : hourlyCapUsd.hashCode) +
        (dailyCapUsd == null ? 0 : dailyCapUsd.hashCode) +
        (monthlyCapUsd == null ? 0 : monthlyCapUsd.hashCode) +
        shared.hashCode +
        (perUserHourlyCapUsd == null ? 0 : perUserHourlyCapUsd.hashCode) +
        (perUserDailyCapUsd == null ? 0 : perUserDailyCapUsd.hashCode) +
        (perUserMonthlyCapUsd == null ? 0 : perUserMonthlyCapUsd.hashCode);

  factory LlmProviderInput.fromJson(Map<String, dynamic> json) => _$LlmProviderInputFromJson(json);

  Map<String, dynamic> toJson() => _$LlmProviderInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LlmProviderInputTypeEnum {
@JsonValue(r'anthropic')
anthropic(r'anthropic'),
@JsonValue(r'openai-compatible')
openaiCompatible(r'openai-compatible'),
@JsonValue(r'ollama')
ollama(r'ollama');

const LlmProviderInputTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


