//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/llm_model_info.dart';
import 'package:foresignal_api/src/model/llm_test_result.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_probe_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmProbeResult {
  /// Returns a new [LlmProbeResult] instance.
  LlmProbeResult({

    required  this.test,

    required  this.models,

     this.modelsError,
  });

      /// With a chatModel (or an embedding model and dimensions) the testLlmProvider probes; without one, success reports whether the model list could be read, with its error
  @JsonKey(
    
    name: r'test',
    required: true,
    includeIfNull: false,
  )


  final LlmTestResult test;



      /// The provider's models (Anthropic Models API, OpenAI-compatible /models, Ollama /api/tags); empty when it could not list them
  @JsonKey(
    
    name: r'models',
    required: true,
    includeIfNull: false,
  )


  final List<LlmModelInfo> models;



      /// Why the models could not be listed, or null; a provider that cannot enumerate its models answers an empty list with this set
  @JsonKey(
    
    name: r'modelsError',
    required: false,
    includeIfNull: false,
  )


  final String? modelsError;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmProbeResult &&
      other.test == test &&
      other.models == models &&
      other.modelsError == modelsError;

    @override
    int get hashCode =>
        test.hashCode +
        models.hashCode +
        (modelsError == null ? 0 : modelsError.hashCode);

  factory LlmProbeResult.fromJson(Map<String, dynamic> json) => _$LlmProbeResultFromJson(json);

  Map<String, dynamic> toJson() => _$LlmProbeResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

