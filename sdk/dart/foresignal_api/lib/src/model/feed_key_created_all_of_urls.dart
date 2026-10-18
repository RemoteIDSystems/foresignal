//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'feed_key_created_all_of_urls.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FeedKeyCreatedAllOfUrls {
  /// Returns a new [FeedKeyCreatedAllOfUrls] instance.
  FeedKeyCreatedAllOfUrls({

    required  this.prefix,

    required  this.url,
  });

  @JsonKey(
    
    name: r'prefix',
    required: true,
    includeIfNull: false,
  )


  final String prefix;



  @JsonKey(
    
    name: r'url',
    required: true,
    includeIfNull: false,
  )


  final String url;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FeedKeyCreatedAllOfUrls &&
      other.prefix == prefix &&
      other.url == url;

    @override
    int get hashCode =>
        prefix.hashCode +
        url.hashCode;

  factory FeedKeyCreatedAllOfUrls.fromJson(Map<String, dynamic> json) => _$FeedKeyCreatedAllOfUrlsFromJson(json);

  Map<String, dynamic> toJson() => _$FeedKeyCreatedAllOfUrlsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

