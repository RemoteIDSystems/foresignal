//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/ownership_chain_node_paths_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ownership_chain_node.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OwnershipChainNode {
  /// Returns a new [OwnershipChainNode] instance.
  OwnershipChainNode({

    required  this.node,

    required  this.reachedFrom,

    required  this.weakestEdgeLabel,

    required  this.paths,
  });

  @JsonKey(
    
    name: r'node',
    required: true,
    includeIfNull: false,
  )


  final ObjectEnvelope node;



      /// The seeds whose chain reaches this entity
  @JsonKey(
    
    name: r'reachedFrom',
    required: true,
    includeIfNull: false,
  )


  final List<String> reachedFrom;



      /// Weakest edge across the reaching chains (104 vocabulary)
  @JsonKey(
    
    name: r'weakestEdgeLabel',
    required: true,
    includeIfNull: false,
  )


  final OwnershipChainNodeWeakestEdgeLabelEnum weakestEdgeLabel;



  @JsonKey(
    
    name: r'paths',
    required: true,
    includeIfNull: false,
  )


  final List<OwnershipChainNodePathsInner> paths;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OwnershipChainNode &&
      other.node == node &&
      other.reachedFrom == reachedFrom &&
      other.weakestEdgeLabel == weakestEdgeLabel &&
      other.paths == paths;

    @override
    int get hashCode =>
        node.hashCode +
        reachedFrom.hashCode +
        weakestEdgeLabel.hashCode +
        paths.hashCode;

  factory OwnershipChainNode.fromJson(Map<String, dynamic> json) => _$OwnershipChainNodeFromJson(json);

  Map<String, dynamic> toJson() => _$OwnershipChainNodeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Weakest edge across the reaching chains (104 vocabulary)
enum OwnershipChainNodeWeakestEdgeLabelEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'likely')
likely(r'likely'),
@JsonValue(r'possible')
possible(r'possible'),
@JsonValue(r'speculative')
speculative(r'speculative');

const OwnershipChainNodeWeakestEdgeLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


