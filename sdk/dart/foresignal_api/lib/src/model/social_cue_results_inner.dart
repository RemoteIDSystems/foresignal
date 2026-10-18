//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_platform_id.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_cue_results_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialCueResultsInner {
  /// Returns a new [SocialCueResultsInner] instance.
  SocialCueResultsInner({

    required  this.platform,

    required  this.posts,

     this.newPosts,

     this.costUsd,

     this.error,
  });

  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final SocialPlatformId platform;



          // minimum: 0
  @JsonKey(
    
    name: r'posts',
    required: true,
    includeIfNull: false,
  )


  final int posts;



          // minimum: 0
  @JsonKey(
    
    name: r'newPosts',
    required: false,
    includeIfNull: false,
  )


  final int? newPosts;



          // minimum: 0
  @JsonKey(
    
    name: r'costUsd',
    required: false,
    includeIfNull: false,
  )


  final num? costUsd;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialCueResultsInner &&
      other.platform == platform &&
      other.posts == posts &&
      other.newPosts == newPosts &&
      other.costUsd == costUsd &&
      other.error == error;

    @override
    int get hashCode =>
        platform.hashCode +
        posts.hashCode +
        newPosts.hashCode +
        costUsd.hashCode +
        (error == null ? 0 : error.hashCode);

  factory SocialCueResultsInner.fromJson(Map<String, dynamic> json) => _$SocialCueResultsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SocialCueResultsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

