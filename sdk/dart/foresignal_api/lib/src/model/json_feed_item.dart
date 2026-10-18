//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/json_feed_item_authors_inner.dart';
import 'package:foresignal_api/src/model/json_feed_item_foresignal.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'json_feed_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class JsonFeedItem {
  /// Returns a new [JsonFeedItem] instance.
  JsonFeedItem({

    required  this.id,

     this.url,

    required  this.title,

     this.contentHtml,

     this.contentText,

     this.datePublished,

     this.dateModified,

     this.tags,

     this.authors,

     this.foresignal,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Client deep link, else `publicBaseUrl/v1/alerts/{id}`
  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



      /// Markdown rendered with commonmark-java and sanitised by the allowlist policy
  @JsonKey(
    
    name: r'content_html',
    required: false,
    includeIfNull: false,
  )


  final String? contentHtml;



  @JsonKey(
    
    name: r'content_text',
    required: false,
    includeIfNull: false,
  )


  final String? contentText;



      /// `firedAt` (alerts) or run start
  @JsonKey(
    
    name: r'date_published',
    required: false,
    includeIfNull: false,
  )


  final DateTime? datePublished;



      /// Last lifecycle change
  @JsonKey(
    
    name: r'date_modified',
    required: false,
    includeIfNull: false,
  )


  final DateTime? dateModified;



      /// Severity and rule tags
  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



  @JsonKey(
    
    name: r'authors',
    required: false,
    includeIfNull: false,
  )


  final List<JsonFeedItemAuthorsInner>? authors;



  @JsonKey(
    
    name: r'_foresignal',
    required: false,
    includeIfNull: false,
  )


  final JsonFeedItemForesignal? foresignal;





    @override
    bool operator ==(Object other) => identical(this, other) || other is JsonFeedItem &&
      other.id == id &&
      other.url == url &&
      other.title == title &&
      other.contentHtml == contentHtml &&
      other.contentText == contentText &&
      other.datePublished == datePublished &&
      other.dateModified == dateModified &&
      other.tags == tags &&
      other.authors == authors &&
      other.foresignal == foresignal;

    @override
    int get hashCode =>
        id.hashCode +
        url.hashCode +
        title.hashCode +
        contentHtml.hashCode +
        contentText.hashCode +
        datePublished.hashCode +
        dateModified.hashCode +
        tags.hashCode +
        authors.hashCode +
        foresignal.hashCode;

  factory JsonFeedItem.fromJson(Map<String, dynamic> json) => _$JsonFeedItemFromJson(json);

  Map<String, dynamic> toJson() => _$JsonFeedItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

