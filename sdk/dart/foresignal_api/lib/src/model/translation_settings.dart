//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/llm_spend_window.dart';
import 'package:foresignal_api/src/model/translation_settings_input_budget.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'translation_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TranslationSettings {
  /// Returns a new [TranslationSettings] instance.
  TranslationSettings({

     this.enabled = false,

    required  this.targetLanguages,

    required  this.fields,

     this.engine = 'llm',

     this.budget,

     this.minSourceLanguageConfidence = 0.8,

     this.spend,
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
    required: true,
    includeIfNull: false,
  )


  final Set<String> targetLanguages;



      /// Which fields are translated: `title` (the article or story title) and `excerpt` (the article's feed summary). Expected to grow (`x-extensible-enum`).
  @JsonKey(
    
    name: r'fields',
    required: true,
    includeIfNull: false,
  )


  final Set<String> fields;



      /// The translation engine: `llm` uses the 026 LLM layer under the `translation` feature, budget-governed. Expected to grow (`x-extensible-enum`).
  @JsonKey(
    defaultValue: 'llm',
    name: r'engine',
    required: true,
    includeIfNull: false,
  )


  final String engine;



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



      /// Translation spend in the current `budget.window`, or null when no translation has been priced yet
  @JsonKey(
    
    name: r'spend',
    required: false,
    includeIfNull: false,
  )


  final LlmSpendWindow? spend;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TranslationSettings &&
      other.enabled == enabled &&
      other.targetLanguages == targetLanguages &&
      other.fields == fields &&
      other.engine == engine &&
      other.budget == budget &&
      other.minSourceLanguageConfidence == minSourceLanguageConfidence &&
      other.spend == spend;

    @override
    int get hashCode =>
        enabled.hashCode +
        targetLanguages.hashCode +
        fields.hashCode +
        engine.hashCode +
        budget.hashCode +
        minSourceLanguageConfidence.hashCode +
        (spend == null ? 0 : spend.hashCode);

  factory TranslationSettings.fromJson(Map<String, dynamic> json) => _$TranslationSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$TranslationSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

