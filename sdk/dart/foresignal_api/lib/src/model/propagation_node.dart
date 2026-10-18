//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'propagation_node.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PropagationNode {
  /// Returns a new [PropagationNode] instance.
  PropagationNode({

    required  this.id,

    required  this.type,

     this.ref,

     this.platform,

    required  this.observedAt,

     this.evidenceGroupId,

     this.memberCount,

     this.label,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final PropagationNodeTypeEnum type;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



  @JsonKey(
    
    name: r'platform',
    required: false,
    includeIfNull: false,
  )


  final String? platform;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



      /// ULID identifier
  @JsonKey(
    
    name: r'evidenceGroupId',
    required: false,
    includeIfNull: false,
  )


  final String? evidenceGroupId;



      /// Posts collapsed into an `evidence-group` node
          // minimum: 1
  @JsonKey(
    
    name: r'memberCount',
    required: false,
    includeIfNull: false,
  )


  final int? memberCount;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PropagationNode &&
      other.id == id &&
      other.type == type &&
      other.ref == ref &&
      other.platform == platform &&
      other.observedAt == observedAt &&
      other.evidenceGroupId == evidenceGroupId &&
      other.memberCount == memberCount &&
      other.label == label;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        (ref == null ? 0 : ref.hashCode) +
        (platform == null ? 0 : platform.hashCode) +
        observedAt.hashCode +
        (evidenceGroupId == null ? 0 : evidenceGroupId.hashCode) +
        memberCount.hashCode +
        (label == null ? 0 : label.hashCode);

  factory PropagationNode.fromJson(Map<String, dynamic> json) => _$PropagationNodeFromJson(json);

  Map<String, dynamic> toJson() => _$PropagationNodeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PropagationNodeTypeEnum {
@JsonValue(r'post')
post(r'post'),
@JsonValue(r'evidence-group')
evidenceGroup(r'evidence-group'),
@JsonValue(r'news-article')
newsArticle(r'news-article'),
@JsonValue(r'official-record')
officialRecord(r'official-record');

const PropagationNodeTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


