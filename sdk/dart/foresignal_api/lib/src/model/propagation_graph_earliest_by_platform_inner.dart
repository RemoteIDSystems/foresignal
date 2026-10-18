//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'propagation_graph_earliest_by_platform_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PropagationGraphEarliestByPlatformInner {
  /// Returns a new [PropagationGraphEarliestByPlatformInner] instance.
  PropagationGraphEarliestByPlatformInner({

    required  this.platform,

    required  this.postRef,

    required  this.observedAt,
  });

      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final String platform;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'postRef',
    required: true,
    includeIfNull: false,
  )


  final String postRef;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PropagationGraphEarliestByPlatformInner &&
      other.platform == platform &&
      other.postRef == postRef &&
      other.observedAt == observedAt;

    @override
    int get hashCode =>
        platform.hashCode +
        postRef.hashCode +
        observedAt.hashCode;

  factory PropagationGraphEarliestByPlatformInner.fromJson(Map<String, dynamic> json) => _$PropagationGraphEarliestByPlatformInnerFromJson(json);

  Map<String, dynamic> toJson() => _$PropagationGraphEarliestByPlatformInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

