//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/json_feed_authors_inner.dart';
import 'package:foresignal_api/src/model/json_feed_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'json_feed.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class JsonFeed {
  /// Returns a new [JsonFeed] instance.
  JsonFeed({

    required  this.version,

    required  this.title,

     this.homePageUrl,

     this.feedUrl,

     this.description,

     this.language,

     this.authors,

    required  this.items,
  });

  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final JsonFeedVersionEnum version;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'home_page_url',
    required: false,
    includeIfNull: false,
  )


  final String? homePageUrl;



  @JsonKey(
    
    name: r'feed_url',
    required: false,
    includeIfNull: false,
  )


  final String? feedUrl;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'language',
    required: false,
    includeIfNull: false,
  )


  final String? language;



  @JsonKey(
    
    name: r'authors',
    required: false,
    includeIfNull: false,
  )


  final List<JsonFeedAuthorsInner>? authors;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<JsonFeedItem> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is JsonFeed &&
      other.version == version &&
      other.title == title &&
      other.homePageUrl == homePageUrl &&
      other.feedUrl == feedUrl &&
      other.description == description &&
      other.language == language &&
      other.authors == authors &&
      other.items == items;

    @override
    int get hashCode =>
        version.hashCode +
        title.hashCode +
        homePageUrl.hashCode +
        feedUrl.hashCode +
        description.hashCode +
        language.hashCode +
        authors.hashCode +
        items.hashCode;

  factory JsonFeed.fromJson(Map<String, dynamic> json) => _$JsonFeedFromJson(json);

  Map<String, dynamic> toJson() => _$JsonFeedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum JsonFeedVersionEnum {
@JsonValue(r'https://jsonfeed.org/version/1.1')
httpsColonSlashSlashJsonfeedPeriodOrgSlashVersionSlash1Period1(r'https://jsonfeed.org/version/1.1');

const JsonFeedVersionEnum(this.value);

final String value;

@override
String toString() => value;
}


