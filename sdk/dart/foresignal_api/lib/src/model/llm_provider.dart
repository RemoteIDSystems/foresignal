//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_provider.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmProvider {
  /// Returns a new [LlmProvider] instance.
  LlmProvider({

    required  this.id,

    required  this.type,

     this.baseUrl,

     this.chatModel,

     this.embeddingModel,

     this.embeddingDimensions,

     this.apiKeySet,

     this.options,

     this.requestsPerMinute,

     this.tokensPerDay,

    required  this.enabled,

     this.roles,

    required  this.status,

     this.tokensToday,

     this.lastError,

     this.updatedAt,

     this.supportsTools,

     this.knowledgeCutoff,

     this.lastSuccessAt,

     this.lastErrorAt,

     this.consecutiveFailures,

     this.local,
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


  final LlmProviderTypeEnum type;



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



  @JsonKey(
    
    name: r'embeddingDimensions',
    required: false,
    includeIfNull: false,
  )


  final int? embeddingDimensions;



  @JsonKey(
    
    name: r'apiKeySet',
    required: false,
    includeIfNull: false,
  )


  final bool? apiKeySet;



  @JsonKey(
    
    name: r'options',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? options;



  @JsonKey(
    
    name: r'requestsPerMinute',
    required: false,
    includeIfNull: false,
  )


  final int? requestsPerMinute;



  @JsonKey(
    
    name: r'tokensPerDay',
    required: false,
    includeIfNull: false,
  )


  final int? tokensPerDay;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'roles',
    required: false,
    includeIfNull: false,
  )


  final List<LlmProviderRolesEnum>? roles;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final LlmProviderStatusEnum status;



  @JsonKey(
    
    name: r'tokensToday',
    required: false,
    includeIfNull: false,
  )


  final int? tokensToday;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



      /// False when the chat model rejected tools; Ask then runs retrieval-only
  @JsonKey(
    
    name: r'supportsTools',
    required: false,
    includeIfNull: false,
  )


  final bool? supportsTools;



      /// Training-data cutoff of the chat model; required for leak-free retrospective evaluation of LLM forecasters
  @JsonKey(
    
    name: r'knowledgeCutoff',
    required: false,
    includeIfNull: false,
  )


  final DateTime? knowledgeCutoff;



  @JsonKey(
    
    name: r'lastSuccessAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastSuccessAt;



  @JsonKey(
    
    name: r'lastErrorAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastErrorAt;



  @JsonKey(
    
    name: r'consecutiveFailures',
    required: false,
    includeIfNull: false,
  )


  final int? consecutiveFailures;



      /// Treated as local for LLM egress (ollama, or `options.local=true`)
  @JsonKey(
    
    name: r'local',
    required: false,
    includeIfNull: false,
  )


  final bool? local;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmProvider &&
      other.id == id &&
      other.type == type &&
      other.baseUrl == baseUrl &&
      other.chatModel == chatModel &&
      other.embeddingModel == embeddingModel &&
      other.embeddingDimensions == embeddingDimensions &&
      other.apiKeySet == apiKeySet &&
      other.options == options &&
      other.requestsPerMinute == requestsPerMinute &&
      other.tokensPerDay == tokensPerDay &&
      other.enabled == enabled &&
      other.roles == roles &&
      other.status == status &&
      other.tokensToday == tokensToday &&
      other.lastError == lastError &&
      other.updatedAt == updatedAt &&
      other.supportsTools == supportsTools &&
      other.knowledgeCutoff == knowledgeCutoff &&
      other.lastSuccessAt == lastSuccessAt &&
      other.lastErrorAt == lastErrorAt &&
      other.consecutiveFailures == consecutiveFailures &&
      other.local == local;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        (baseUrl == null ? 0 : baseUrl.hashCode) +
        (chatModel == null ? 0 : chatModel.hashCode) +
        (embeddingModel == null ? 0 : embeddingModel.hashCode) +
        (embeddingDimensions == null ? 0 : embeddingDimensions.hashCode) +
        apiKeySet.hashCode +
        options.hashCode +
        requestsPerMinute.hashCode +
        tokensPerDay.hashCode +
        enabled.hashCode +
        roles.hashCode +
        status.hashCode +
        tokensToday.hashCode +
        (lastError == null ? 0 : lastError.hashCode) +
        updatedAt.hashCode +
        (supportsTools == null ? 0 : supportsTools.hashCode) +
        (knowledgeCutoff == null ? 0 : knowledgeCutoff.hashCode) +
        (lastSuccessAt == null ? 0 : lastSuccessAt.hashCode) +
        (lastErrorAt == null ? 0 : lastErrorAt.hashCode) +
        consecutiveFailures.hashCode +
        local.hashCode;

  factory LlmProvider.fromJson(Map<String, dynamic> json) => _$LlmProviderFromJson(json);

  Map<String, dynamic> toJson() => _$LlmProviderToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LlmProviderTypeEnum {
@JsonValue(r'anthropic')
anthropic(r'anthropic'),
@JsonValue(r'openai-compatible')
openaiCompatible(r'openai-compatible'),
@JsonValue(r'ollama')
ollama(r'ollama');

const LlmProviderTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum LlmProviderRolesEnum {
@JsonValue(r'chat')
chat(r'chat'),
@JsonValue(r'embedding')
embedding(r'embedding'),
@JsonValue(r'tts')
tts(r'tts');

const LlmProviderRolesEnum(this.value);

final String value;

@override
String toString() => value;
}


enum LlmProviderStatusEnum {
@JsonValue(r'healthy')
healthy(r'healthy'),
@JsonValue(r'degraded')
degraded(r'degraded'),
@JsonValue(r'failing')
failing(r'failing'),
@JsonValue(r'idle')
idle(r'idle'),
@JsonValue(r'disabled')
disabled(r'disabled');

const LlmProviderStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


