//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attention_topic_social.dart';
import 'package:foresignal_api/src/model/attention_topic_input_wiki_articles_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'attention_topic_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AttentionTopicInput {
  /// Returns a new [AttentionTopicInput] instance.
  AttentionTopicInput({

    required  this.id,

    required  this.name,

     this.description,

     this.gdeltQuery,

     this.wikiArticles,

     this.keywords,

     this.regionCodes,

     this.themes,

     this.enabled,

     this.social,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



      /// GDELT DOC 2.0 query
  @JsonKey(
    
    name: r'gdeltQuery',
    required: false,
    includeIfNull: false,
  )


  final String? gdeltQuery;



  @JsonKey(
    
    name: r'wikiArticles',
    required: false,
    includeIfNull: false,
  )


  final List<AttentionTopicInputWikiArticlesInner>? wikiArticles;



      /// Used by 118's linker
  @JsonKey(
    
    name: r'keywords',
    required: false,
    includeIfNull: false,
  )


  final List<String>? keywords;



      /// 040 region codes
  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



  @JsonKey(
    
    name: r'themes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? themes;



  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// Social tracking block (135): terms, hashtags, accounts, channels, excludes, platforms, places, regions, entities, mode
  @JsonKey(
    
    name: r'social',
    required: false,
    includeIfNull: false,
  )


  final AttentionTopicSocial? social;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AttentionTopicInput &&
      other.id == id &&
      other.name == name &&
      other.description == description &&
      other.gdeltQuery == gdeltQuery &&
      other.wikiArticles == wikiArticles &&
      other.keywords == keywords &&
      other.regionCodes == regionCodes &&
      other.themes == themes &&
      other.enabled == enabled &&
      other.social == social;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        (description == null ? 0 : description.hashCode) +
        (gdeltQuery == null ? 0 : gdeltQuery.hashCode) +
        wikiArticles.hashCode +
        keywords.hashCode +
        regionCodes.hashCode +
        themes.hashCode +
        enabled.hashCode +
        (social == null ? 0 : social.hashCode);

  factory AttentionTopicInput.fromJson(Map<String, dynamic> json) => _$AttentionTopicInputFromJson(json);

  Map<String, dynamic> toJson() => _$AttentionTopicInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

