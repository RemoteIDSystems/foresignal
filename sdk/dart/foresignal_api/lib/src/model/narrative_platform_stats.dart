//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'narrative_platform_stats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NarrativePlatformStats {
  /// Returns a new [NarrativePlatformStats] instance.
  NarrativePlatformStats({

    required  this.platform,

    required  this.posts,

    required  this.uniqueAuthors,

    required  this.firstObservedAt,

    required  this.earliestPostRef,
  });

      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final String platform;



          // minimum: 0
  @JsonKey(
    
    name: r'posts',
    required: true,
    includeIfNull: false,
  )


  final int posts;



          // minimum: 0
  @JsonKey(
    
    name: r'uniqueAuthors',
    required: true,
    includeIfNull: false,
  )


  final int uniqueAuthors;



  @JsonKey(
    
    name: r'firstObservedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime firstObservedAt;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'earliestPostRef',
    required: true,
    includeIfNull: false,
  )


  final String earliestPostRef;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NarrativePlatformStats &&
      other.platform == platform &&
      other.posts == posts &&
      other.uniqueAuthors == uniqueAuthors &&
      other.firstObservedAt == firstObservedAt &&
      other.earliestPostRef == earliestPostRef;

    @override
    int get hashCode =>
        platform.hashCode +
        posts.hashCode +
        uniqueAuthors.hashCode +
        firstObservedAt.hashCode +
        earliestPostRef.hashCode;

  factory NarrativePlatformStats.fromJson(Map<String, dynamic> json) => _$NarrativePlatformStatsFromJson(json);

  Map<String, dynamic> toJson() => _$NarrativePlatformStatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

