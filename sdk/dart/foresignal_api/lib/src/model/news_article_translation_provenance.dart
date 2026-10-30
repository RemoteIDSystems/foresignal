//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'news_article_translation_provenance.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NewsArticleTranslationProvenance {
  /// Returns a new [NewsArticleTranslationProvenance] instance.
  NewsArticleTranslationProvenance({

    required  this.engine,

     this.model,

    required  this.at,
  });

  @JsonKey(
    
    name: r'engine',
    required: true,
    includeIfNull: false,
  )


  final String engine;



  @JsonKey(
    
    name: r'model',
    required: false,
    includeIfNull: false,
  )


  final String? model;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NewsArticleTranslationProvenance &&
      other.engine == engine &&
      other.model == model &&
      other.at == at;

    @override
    int get hashCode =>
        engine.hashCode +
        (model == null ? 0 : model.hashCode) +
        at.hashCode;

  factory NewsArticleTranslationProvenance.fromJson(Map<String, dynamic> json) => _$NewsArticleTranslationProvenanceFromJson(json);

  Map<String, dynamic> toJson() => _$NewsArticleTranslationProvenanceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

