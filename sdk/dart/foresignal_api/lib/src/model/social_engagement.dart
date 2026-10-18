//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_engagement.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialEngagement {
  /// Returns a new [SocialEngagement] instance.
  SocialEngagement({

     this.replies,

     this.reposts,

     this.quotes,

     this.likes,

     this.views,
  });

          // minimum: 0
  @JsonKey(
    
    name: r'replies',
    required: false,
    includeIfNull: false,
  )


  final int? replies;



          // minimum: 0
  @JsonKey(
    
    name: r'reposts',
    required: false,
    includeIfNull: false,
  )


  final int? reposts;



          // minimum: 0
  @JsonKey(
    
    name: r'quotes',
    required: false,
    includeIfNull: false,
  )


  final int? quotes;



          // minimum: 0
  @JsonKey(
    
    name: r'likes',
    required: false,
    includeIfNull: false,
  )


  final int? likes;



          // minimum: 0
  @JsonKey(
    
    name: r'views',
    required: false,
    includeIfNull: false,
  )


  final int? views;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialEngagement &&
      other.replies == replies &&
      other.reposts == reposts &&
      other.quotes == quotes &&
      other.likes == likes &&
      other.views == views;

    @override
    int get hashCode =>
        (replies == null ? 0 : replies.hashCode) +
        (reposts == null ? 0 : reposts.hashCode) +
        (quotes == null ? 0 : quotes.hashCode) +
        (likes == null ? 0 : likes.hashCode) +
        (views == null ? 0 : views.hashCode);

  factory SocialEngagement.fromJson(Map<String, dynamic> json) => _$SocialEngagementFromJson(json);

  Map<String, dynamic> toJson() => _$SocialEngagementToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

