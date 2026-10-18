//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'attention_topic_input_wiki_articles_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AttentionTopicInputWikiArticlesInner {
  /// Returns a new [AttentionTopicInputWikiArticlesInner] instance.
  AttentionTopicInputWikiArticlesInner({

    required  this.project,

    required  this.article,
  });

  @JsonKey(
    
    name: r'project',
    required: true,
    includeIfNull: false,
  )


  final String project;



  @JsonKey(
    
    name: r'article',
    required: true,
    includeIfNull: false,
  )


  final String article;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AttentionTopicInputWikiArticlesInner &&
      other.project == project &&
      other.article == article;

    @override
    int get hashCode =>
        project.hashCode +
        article.hashCode;

  factory AttentionTopicInputWikiArticlesInner.fromJson(Map<String, dynamic> json) => _$AttentionTopicInputWikiArticlesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AttentionTopicInputWikiArticlesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

