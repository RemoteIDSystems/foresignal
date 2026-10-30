//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/translation_settings_input_budget.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'translation_settings_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TranslationSettingsInput {
  /// Returns a new [TranslationSettingsInput] instance.
  TranslationSettingsInput({

     this.enabled = false,

     this.targetLanguages,

     this.fields,

     this.engine = 'llm',

     this.budget,

     this.minSourceLanguageConfidence = 0.8,
  });

      /// Master switch: off, no article or story is translated and reads carry the originals only
  @JsonKey(
    defaultValue: false,
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// Languages foreign news is translated into; an article already in a target language is not translated into it
  @JsonKey(
    
    name: r'targetLanguages',
    required: false,
    includeIfNull: false,
  )


  final Set<String>? targetLanguages;



      /// Which fields are translated: `title` (the article or story title) and `excerpt` (the article's feed summary). Expected to grow (`x-extensible-enum`).
  @JsonKey(
    
    name: r'fields',
    required: false,
    includeIfNull: false,
  )


  final Set<String>? fields;



      /// The translation engine: `llm` uses the 026 LLM layer under the `translation` feature, budget-governed. Expected to grow (`x-extensible-enum`).
  @JsonKey(
    defaultValue: 'llm',
    name: r'engine',
    required: false,
    includeIfNull: false,
  )


  final String? engine;



  @JsonKey(
    
    name: r'budget',
    required: false,
    includeIfNull: false,
  )


  final TranslationSettingsInputBudget? budget;



      /// The least confidence in the detected source language at which an article is translated; below it the article is left as is
          // minimum: 0
          // maximum: 1
  @JsonKey(
    defaultValue: 0.8,
    name: r'minSourceLanguageConfidence',
    required: false,
    includeIfNull: false,
  )


  final num? minSourceLanguageConfidence;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TranslationSettingsInput &&
      other.enabled == enabled &&
      other.targetLanguages == targetLanguages &&
      other.fields == fields &&
      other.engine == engine &&
      other.budget == budget &&
      other.minSourceLanguageConfidence == minSourceLanguageConfidence;

    @override
    int get hashCode =>
        enabled.hashCode +
        targetLanguages.hashCode +
        fields.hashCode +
        engine.hashCode +
        budget.hashCode +
        minSourceLanguageConfidence.hashCode;

  factory TranslationSettingsInput.fromJson(Map<String, dynamic> json) => _$TranslationSettingsInputFromJson(json);

  Map<String, dynamic> toJson() => _$TranslationSettingsInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

