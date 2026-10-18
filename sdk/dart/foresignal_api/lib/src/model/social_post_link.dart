//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_post_link.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPostLink {
  /// Returns a new [SocialPostLink] instance.
  SocialPostLink({

    required  this.platform,

    required  this.postId,

     this.ref,

     this.url,
  });

      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final String platform;



  @JsonKey(
    
    name: r'postId',
    required: true,
    includeIfNull: false,
  )


  final String postId;



      /// Record ref when the target post is stored
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPostLink &&
      other.platform == platform &&
      other.postId == postId &&
      other.ref == ref &&
      other.url == url;

    @override
    int get hashCode =>
        platform.hashCode +
        postId.hashCode +
        (ref == null ? 0 : ref.hashCode) +
        (url == null ? 0 : url.hashCode);

  factory SocialPostLink.fromJson(Map<String, dynamic> json) => _$SocialPostLinkFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPostLinkToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

