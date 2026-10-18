//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/provenance_chain_entry.dart';
import 'package:foresignal_api/src/model/propagation_node.dart';
import 'package:foresignal_api/src/model/propagation_graph_earliest_by_platform_inner.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:foresignal_api/src/model/propagation_graph_cross_platform_order_inner.dart';
import 'package:foresignal_api/src/model/propagation_edge.dart';
import 'package:foresignal_api/src/model/propagation_graph_first_news_pickup.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'propagation_graph.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PropagationGraph {
  /// Returns a new [PropagationGraph] instance.
  PropagationGraph({

    required  this.narrativeId,

    required  this.summarised,

    required  this.totalNodes,

    required  this.totalEdges,

    required  this.nodes,

    required  this.edges,

    required  this.earliestByPlatform,

     this.firstNewsPickup,

    required  this.descentShare,

    required  this.crossPlatformOrder,

     this.medianRepostDelaySeconds,

     this.provenanceChain,

    required  this.tracer,

    required  this.computedAt,

     this.manifestId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'narrativeId',
    required: true,
    includeIfNull: false,
  )


  final String narrativeId;



  @JsonKey(
    
    name: r'summarised',
    required: true,
    includeIfNull: false,
  )


  final bool summarised;



  @JsonKey(
    
    name: r'totalNodes',
    required: true,
    includeIfNull: false,
  )


  final int totalNodes;



  @JsonKey(
    
    name: r'totalEdges',
    required: true,
    includeIfNull: false,
  )


  final int totalEdges;



  @JsonKey(
    
    name: r'nodes',
    required: true,
    includeIfNull: false,
  )


  final List<PropagationNode> nodes;



  @JsonKey(
    
    name: r'edges',
    required: true,
    includeIfNull: false,
  )


  final List<PropagationEdge> edges;



  @JsonKey(
    
    name: r'earliestByPlatform',
    required: true,
    includeIfNull: false,
  )


  final List<PropagationGraphEarliestByPlatformInner> earliestByPlatform;



  @JsonKey(
    
    name: r'firstNewsPickup',
    required: false,
    includeIfNull: false,
  )


  final PropagationGraphFirstNewsPickup? firstNewsPickup;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'descentShare',
    required: true,
    includeIfNull: false,
  )


  final num descentShare;



  @JsonKey(
    
    name: r'crossPlatformOrder',
    required: true,
    includeIfNull: false,
  )


  final List<PropagationGraphCrossPlatformOrderInner> crossPlatformOrder;



  @JsonKey(
    
    name: r'medianRepostDelaySeconds',
    required: false,
    includeIfNull: false,
  )


  final num? medianRepostDelaySeconds;



      /// 134 chain so circular reporting is recognised
  @JsonKey(
    
    name: r'provenanceChain',
    required: false,
    includeIfNull: false,
  )


  final List<ProvenanceChainEntry>? provenanceChain;



  @JsonKey(
    
    name: r'tracer',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef tracer;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PropagationGraph &&
      other.narrativeId == narrativeId &&
      other.summarised == summarised &&
      other.totalNodes == totalNodes &&
      other.totalEdges == totalEdges &&
      other.nodes == nodes &&
      other.edges == edges &&
      other.earliestByPlatform == earliestByPlatform &&
      other.firstNewsPickup == firstNewsPickup &&
      other.descentShare == descentShare &&
      other.crossPlatformOrder == crossPlatformOrder &&
      other.medianRepostDelaySeconds == medianRepostDelaySeconds &&
      other.provenanceChain == provenanceChain &&
      other.tracer == tracer &&
      other.computedAt == computedAt &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        narrativeId.hashCode +
        summarised.hashCode +
        totalNodes.hashCode +
        totalEdges.hashCode +
        nodes.hashCode +
        edges.hashCode +
        earliestByPlatform.hashCode +
        (firstNewsPickup == null ? 0 : firstNewsPickup.hashCode) +
        descentShare.hashCode +
        crossPlatformOrder.hashCode +
        (medianRepostDelaySeconds == null ? 0 : medianRepostDelaySeconds.hashCode) +
        provenanceChain.hashCode +
        tracer.hashCode +
        computedAt.hashCode +
        manifestId.hashCode;

  factory PropagationGraph.fromJson(Map<String, dynamic> json) => _$PropagationGraphFromJson(json);

  Map<String, dynamic> toJson() => _$PropagationGraphToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

