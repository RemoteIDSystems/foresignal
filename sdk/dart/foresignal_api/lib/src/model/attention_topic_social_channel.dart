//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'attention_topic_social_channel.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AttentionTopicSocialChannel {
  /// Returns a new [AttentionTopicSocialChannel] instance.
  AttentionTopicSocialChannel({

    required  this.platform,

    required  this.type,

    required  this.id,
  });

      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final String platform;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final AttentionTopicSocialChannelTypeEnum type;



      /// Subreddit name, channel username, instance host, hashtag or YouTube channel id
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AttentionTopicSocialChannel &&
      other.platform == platform &&
      other.type == type &&
      other.id == id;

    @override
    int get hashCode =>
        platform.hashCode +
        type.hashCode +
        id.hashCode;

  factory AttentionTopicSocialChannel.fromJson(Map<String, dynamic> json) => _$AttentionTopicSocialChannelFromJson(json);

  Map<String, dynamic> toJson() => _$AttentionTopicSocialChannelToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AttentionTopicSocialChannelTypeEnum {
@JsonValue(r'subreddit')
subreddit(r'subreddit'),
@JsonValue(r'mastodon-instance')
mastodonInstance(r'mastodon-instance'),
@JsonValue(r'mastodon-hashtag')
mastodonHashtag(r'mastodon-hashtag'),
@JsonValue(r'youtube-channel')
youtubeChannel(r'youtube-channel');

const AttentionTopicSocialChannelTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


