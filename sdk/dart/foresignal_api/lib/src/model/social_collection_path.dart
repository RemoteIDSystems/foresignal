//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_collection_type.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_collection_path.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialCollectionPath {
  /// Returns a new [SocialCollectionPath] instance.
  SocialCollectionPath({

    required  this.path,

    required  this.collectionType,

    required  this.enabled,

    required  this.feedsBaselines,
  });

  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final SocialCollectionPathPathEnum path;



  @JsonKey(
    
    name: r'collectionType',
    required: true,
    includeIfNull: false,
  )


  final SocialCollectionType collectionType;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// True only for complete and paged paths
  @JsonKey(
    
    name: r'feedsBaselines',
    required: true,
    includeIfNull: false,
  )


  final bool feedsBaselines;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialCollectionPath &&
      other.path == path &&
      other.collectionType == collectionType &&
      other.enabled == enabled &&
      other.feedsBaselines == feedsBaselines;

    @override
    int get hashCode =>
        path.hashCode +
        collectionType.hashCode +
        enabled.hashCode +
        feedsBaselines.hashCode;

  factory SocialCollectionPath.fromJson(Map<String, dynamic> json) => _$SocialCollectionPathFromJson(json);

  Map<String, dynamic> toJson() => _$SocialCollectionPathToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialCollectionPathPathEnum {
@JsonValue(r'stream')
stream(r'stream'),
@JsonValue(r'replay')
replay(r'replay'),
@JsonValue(r'search')
search(r'search'),
@JsonValue(r'counts')
counts(r'counts'),
@JsonValue(r'trends')
trends(r'trends'),
@JsonValue(r'channel-poll')
channelPoll(r'channel-poll'),
@JsonValue(r'account-poll')
accountPoll(r'account-poll'),
@JsonValue(r'hashtag-search')
hashtagSearch(r'hashtag-search'),
@JsonValue(r'cue')
cue(r'cue');

const SocialCollectionPathPathEnum(this.value);

final String value;

@override
String toString() => value;
}


