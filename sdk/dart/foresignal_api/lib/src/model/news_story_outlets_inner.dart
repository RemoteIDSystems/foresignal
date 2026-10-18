//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/media_outlet.dart';
import 'package:foresignal_api/src/model/news_article.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'news_story_outlets_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NewsStoryOutletsInner {
  /// Returns a new [NewsStoryOutletsInner] instance.
  NewsStoryOutletsInner({

    required  this.outlet,

    required  this.articles,

     this.evidenceGroupId,
  });

  @JsonKey(
    
    name: r'outlet',
    required: true,
    includeIfNull: false,
  )


  final MediaOutlet outlet;



  @JsonKey(
    
    name: r'articles',
    required: true,
    includeIfNull: false,
  )


  final List<NewsArticle> articles;



  @JsonKey(
    
    name: r'evidenceGroupId',
    required: false,
    includeIfNull: false,
  )


  final String? evidenceGroupId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NewsStoryOutletsInner &&
      other.outlet == outlet &&
      other.articles == articles &&
      other.evidenceGroupId == evidenceGroupId;

    @override
    int get hashCode =>
        outlet.hashCode +
        articles.hashCode +
        (evidenceGroupId == null ? 0 : evidenceGroupId.hashCode);

  factory NewsStoryOutletsInner.fromJson(Map<String, dynamic> json) => _$NewsStoryOutletsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$NewsStoryOutletsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

