//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_provider_input_options.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmProviderInputOptions {
  /// Returns a new [LlmProviderInputOptions] instance.
  LlmProviderInputOptions({

     this.effort,

     this.maxTokens,

     this.temperature,

     this.extraHeaders,

     this.local = false,

     this.ttsModel = 'tts-1',

     this.ttsCharactersPerDay = 100000,
  });

  @JsonKey(
    
    name: r'effort',
    required: false,
    includeIfNull: false,
  )


  final LlmProviderInputOptionsEffortEnum? effort;



          // minimum: 256
          // maximum: 128000
  @JsonKey(
    
    name: r'maxTokens',
    required: false,
    includeIfNull: false,
  )


  final int? maxTokens;



      /// Sampling temperature 0–2 for openai-compatible and ollama providers; refused for anthropic (current Claude models control depth with effort)
          // minimum: 0
          // maximum: 2
  @JsonKey(
    
    name: r'temperature',
    required: false,
    includeIfNull: false,
  )


  final num? temperature;



      /// Extra request headers (for example OpenRouter `HTTP-Referer`); Authorization, x-api-key, Content-Type and the anthropic-* headers are reserved
  @JsonKey(
    
    name: r'extraHeaders',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? extraHeaders;



      /// Treat this provider as local for LLM egress (private-source records may be sent to it, 026/039); `ollama` is always local
  @JsonKey(
    defaultValue: false,
    name: r'local',
    required: false,
    includeIfNull: false,
  )


  final bool? local;



      /// Speech model for the `tts` role (141), sent as `model` to `{baseUrl}/audio/speech`; openai-compatible only (for example `tts-1`, `gpt-4o-mini-tts`, `kokoro`)
  @JsonKey(
    defaultValue: 'tts-1',
    name: r'ttsModel',
    required: false,
    includeIfNull: false,
  )


  final String? ttsModel;



      /// Daily character budget of the `tts` role (141), per UTC day
          // minimum: 1000
          // maximum: 100000000
  @JsonKey(
    defaultValue: 100000,
    name: r'ttsCharactersPerDay',
    required: false,
    includeIfNull: false,
  )


  final int? ttsCharactersPerDay;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmProviderInputOptions &&
      other.effort == effort &&
      other.maxTokens == maxTokens &&
      other.temperature == temperature &&
      other.extraHeaders == extraHeaders &&
      other.local == local &&
      other.ttsModel == ttsModel &&
      other.ttsCharactersPerDay == ttsCharactersPerDay;

    @override
    int get hashCode =>
        effort.hashCode +
        maxTokens.hashCode +
        (temperature == null ? 0 : temperature.hashCode) +
        extraHeaders.hashCode +
        local.hashCode +
        ttsModel.hashCode +
        ttsCharactersPerDay.hashCode;

  factory LlmProviderInputOptions.fromJson(Map<String, dynamic> json) => _$LlmProviderInputOptionsFromJson(json);

  Map<String, dynamic> toJson() => _$LlmProviderInputOptionsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LlmProviderInputOptionsEffortEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'high')
high(r'high'),
@JsonValue(r'xhigh')
xhigh(r'xhigh'),
@JsonValue(r'max')
max(r'max');

const LlmProviderInputOptionsEffortEnum(this.value);

final String value;

@override
String toString() => value;
}


