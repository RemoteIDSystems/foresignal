//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_post.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'narrative_post.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NarrativePost {
  /// Returns a new [NarrativePost] instance.
  NarrativePost({

    required  this.post,

    required  this.linkReason,

    required  this.evidenceGroupId,

    required  this.joinedAt,

     this.centrality,
  });

  @JsonKey(
    
    name: r'post',
    required: true,
    includeIfNull: false,
  )


  final SocialPost post;



      /// `seed` for the first member
  @JsonKey(
    
    name: r'linkReason',
    required: true,
    includeIfNull: false,
  )


  final NarrativePostLinkReasonEnum linkReason;



      /// ULID identifier
  @JsonKey(
    
    name: r'evidenceGroupId',
    required: true,
    includeIfNull: false,
  )


  final String evidenceGroupId;



  @JsonKey(
    
    name: r'joinedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime joinedAt;



      /// Similarity to the narrative centroid
  @JsonKey(
    
    name: r'centrality',
    required: false,
    includeIfNull: false,
  )


  final num? centrality;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NarrativePost &&
      other.post == post &&
      other.linkReason == linkReason &&
      other.evidenceGroupId == evidenceGroupId &&
      other.joinedAt == joinedAt &&
      other.centrality == centrality;

    @override
    int get hashCode =>
        post.hashCode +
        linkReason.hashCode +
        evidenceGroupId.hashCode +
        joinedAt.hashCode +
        (centrality == null ? 0 : centrality.hashCode);

  factory NarrativePost.fromJson(Map<String, dynamic> json) => _$NarrativePostFromJson(json);

  Map<String, dynamic> toJson() => _$NarrativePostToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `seed` for the first member
enum NarrativePostLinkReasonEnum {
@JsonValue(r'seed')
seed(r'seed'),
@JsonValue(r'repost')
repost(r'repost'),
@JsonValue(r'quote')
quote(r'quote'),
@JsonValue(r'text')
text(r'text'),
@JsonValue(r'link')
link(r'link'),
@JsonValue(r'media')
media(r'media');

const NarrativePostLinkReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


