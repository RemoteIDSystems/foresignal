//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'propagation_graph_first_news_pickup.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PropagationGraphFirstNewsPickup {
  /// Returns a new [PropagationGraphFirstNewsPickup] instance.
  PropagationGraphFirstNewsPickup({

    required  this.storyId,

     this.articleRef,

    required  this.observedAt,

     this.afterFirstPostSeconds,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'storyId',
    required: true,
    includeIfNull: false,
  )


  final String storyId;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'articleRef',
    required: false,
    includeIfNull: false,
  )


  final String? articleRef;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



  @JsonKey(
    
    name: r'afterFirstPostSeconds',
    required: false,
    includeIfNull: false,
  )


  final num? afterFirstPostSeconds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PropagationGraphFirstNewsPickup &&
      other.storyId == storyId &&
      other.articleRef == articleRef &&
      other.observedAt == observedAt &&
      other.afterFirstPostSeconds == afterFirstPostSeconds;

    @override
    int get hashCode =>
        storyId.hashCode +
        (articleRef == null ? 0 : articleRef.hashCode) +
        observedAt.hashCode +
        afterFirstPostSeconds.hashCode;

  factory PropagationGraphFirstNewsPickup.fromJson(Map<String, dynamic> json) => _$PropagationGraphFirstNewsPickupFromJson(json);

  Map<String, dynamic> toJson() => _$PropagationGraphFirstNewsPickupToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

