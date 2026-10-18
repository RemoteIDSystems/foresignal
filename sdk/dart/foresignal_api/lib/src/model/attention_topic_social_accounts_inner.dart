//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'attention_topic_social_accounts_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AttentionTopicSocialAccountsInner {
  /// Returns a new [AttentionTopicSocialAccountsInner] instance.
  AttentionTopicSocialAccountsInner({

    required  this.platform,

    required  this.handle,
  });

      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final String platform;



  @JsonKey(
    
    name: r'handle',
    required: true,
    includeIfNull: false,
  )


  final String handle;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AttentionTopicSocialAccountsInner &&
      other.platform == platform &&
      other.handle == handle;

    @override
    int get hashCode =>
        platform.hashCode +
        handle.hashCode;

  factory AttentionTopicSocialAccountsInner.fromJson(Map<String, dynamic> json) => _$AttentionTopicSocialAccountsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AttentionTopicSocialAccountsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

