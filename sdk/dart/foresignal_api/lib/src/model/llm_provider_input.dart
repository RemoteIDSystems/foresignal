//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/llm_provider_input_options.dart';
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
      other.knowledgeCutoff == knowledgeCutoff;

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
        (knowledgeCutoff == null ? 0 : knowledgeCutoff.hashCode);

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


