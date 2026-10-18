//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmSettings {
  /// Returns a new [LlmSettings] instance.
  LlmSettings({

     this.chatProvider,

     this.embeddingProvider,

     this.askEnabled = true,

     this.enrichmentEnabled = true,

     this.logContent = false,

     this.askMaxTokens = 200000,

     this.allowPrivateLocal = false,

     this.ttsProvider,
  });

  @JsonKey(
    
    name: r'chatProvider',
    required: false,
    includeIfNull: false,
  )


  final String? chatProvider;



  @JsonKey(
    
    name: r'embeddingProvider',
    required: false,
    includeIfNull: false,
  )


  final String? embeddingProvider;



  @JsonKey(
    defaultValue: true,
    name: r'askEnabled',
    required: false,
    includeIfNull: false,
  )


  final bool? askEnabled;



  @JsonKey(
    defaultValue: true,
    name: r'enrichmentEnabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enrichmentEnabled;



  @JsonKey(
    defaultValue: false,
    name: r'logContent',
    required: false,
    includeIfNull: false,
  )


  final bool? logContent;



          // minimum: 1000
  @JsonKey(
    defaultValue: 200000,
    name: r'askMaxTokens',
    required: false,
    includeIfNull: false,
  )


  final int? askMaxTokens;



      /// Allow private-source records in prompts for providers classified as local (039): Ollama or `options.local`, whose base URL resolves only to loopback or private addresses. Never for remote providers. Default false.
  @JsonKey(
    defaultValue: false,
    name: r'allowPrivateLocal',
    required: false,
    includeIfNull: false,
  )


  final bool? allowPrivateLocal;



      /// Provider of the `tts` role (141): an enabled `openai-compatible` provider whose `/audio/speech` endpoint synthesizes audio briefings (OpenAI, Kokoro-FastAPI, openedai-speech). Budgeted by characters (`options.ttsCharactersPerDay`) and by the provider's requests per minute.
  @JsonKey(
    
    name: r'ttsProvider',
    required: false,
    includeIfNull: false,
  )


  final String? ttsProvider;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmSettings &&
      other.chatProvider == chatProvider &&
      other.embeddingProvider == embeddingProvider &&
      other.askEnabled == askEnabled &&
      other.enrichmentEnabled == enrichmentEnabled &&
      other.logContent == logContent &&
      other.askMaxTokens == askMaxTokens &&
      other.allowPrivateLocal == allowPrivateLocal &&
      other.ttsProvider == ttsProvider;

    @override
    int get hashCode =>
        (chatProvider == null ? 0 : chatProvider.hashCode) +
        (embeddingProvider == null ? 0 : embeddingProvider.hashCode) +
        askEnabled.hashCode +
        enrichmentEnabled.hashCode +
        logContent.hashCode +
        askMaxTokens.hashCode +
        allowPrivateLocal.hashCode +
        (ttsProvider == null ? 0 : ttsProvider.hashCode);

  factory LlmSettings.fromJson(Map<String, dynamic> json) => _$LlmSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$LlmSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

