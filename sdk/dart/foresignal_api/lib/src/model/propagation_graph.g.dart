// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'propagation_graph.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PropagationGraphCWProxy {
  PropagationGraph narrativeId(String narrativeId);

  PropagationGraph summarised(bool summarised);

  PropagationGraph totalNodes(int totalNodes);

  PropagationGraph totalEdges(int totalEdges);

  PropagationGraph nodes(List<PropagationNode> nodes);

  PropagationGraph edges(List<PropagationEdge> edges);

  PropagationGraph earliestByPlatform(
    List<PropagationGraphEarliestByPlatformInner> earliestByPlatform,
  );

  PropagationGraph firstNewsPickup(
    PropagationGraphFirstNewsPickup? firstNewsPickup,
  );

  PropagationGraph descentShare(num descentShare);

  PropagationGraph crossPlatformOrder(
    List<PropagationGraphCrossPlatformOrderInner> crossPlatformOrder,
  );

  PropagationGraph medianRepostDelaySeconds(num? medianRepostDelaySeconds);

  PropagationGraph provenanceChain(List<ProvenanceChainEntry>? provenanceChain);

  PropagationGraph tracer(DetectorRef tracer);

  PropagationGraph computedAt(DateTime computedAt);

  PropagationGraph manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PropagationGraph(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PropagationGraph(...).copyWith(id: 12, name: "My name")
  /// ```
  PropagationGraph call({
    String narrativeId,
    bool summarised,
    int totalNodes,
    int totalEdges,
    List<PropagationNode> nodes,
    List<PropagationEdge> edges,
    List<PropagationGraphEarliestByPlatformInner> earliestByPlatform,
    PropagationGraphFirstNewsPickup? firstNewsPickup,
    num descentShare,
    List<PropagationGraphCrossPlatformOrderInner> crossPlatformOrder,
    num? medianRepostDelaySeconds,
    List<ProvenanceChainEntry>? provenanceChain,
    DetectorRef tracer,
    DateTime computedAt,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPropagationGraph.copyWith(...)` or call `instanceOfPropagationGraph.copyWith.fieldName(value)` for a single field.
class _$PropagationGraphCWProxyImpl implements _$PropagationGraphCWProxy {
  const _$PropagationGraphCWProxyImpl(this._value);

  final PropagationGraph _value;

  @override
  PropagationGraph narrativeId(String narrativeId) =>
      call(narrativeId: narrativeId);

  @override
  PropagationGraph summarised(bool summarised) => call(summarised: summarised);

  @override
  PropagationGraph totalNodes(int totalNodes) => call(totalNodes: totalNodes);

  @override
  PropagationGraph totalEdges(int totalEdges) => call(totalEdges: totalEdges);

  @override
  PropagationGraph nodes(List<PropagationNode> nodes) => call(nodes: nodes);

  @override
  PropagationGraph edges(List<PropagationEdge> edges) => call(edges: edges);

  @override
  PropagationGraph earliestByPlatform(
    List<PropagationGraphEarliestByPlatformInner> earliestByPlatform,
  ) => call(earliestByPlatform: earliestByPlatform);

  @override
  PropagationGraph firstNewsPickup(
    PropagationGraphFirstNewsPickup? firstNewsPickup,
  ) => call(firstNewsPickup: firstNewsPickup);

  @override
  PropagationGraph descentShare(num descentShare) =>
      call(descentShare: descentShare);

  @override
  PropagationGraph crossPlatformOrder(
    List<PropagationGraphCrossPlatformOrderInner> crossPlatformOrder,
  ) => call(crossPlatformOrder: crossPlatformOrder);

  @override
  PropagationGraph medianRepostDelaySeconds(num? medianRepostDelaySeconds) =>
      call(medianRepostDelaySeconds: medianRepostDelaySeconds);

  @override
  PropagationGraph provenanceChain(
    List<ProvenanceChainEntry>? provenanceChain,
  ) => call(provenanceChain: provenanceChain);

  @override
  PropagationGraph tracer(DetectorRef tracer) => call(tracer: tracer);

  @override
  PropagationGraph computedAt(DateTime computedAt) =>
      call(computedAt: computedAt);

  @override
  PropagationGraph manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PropagationGraph(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PropagationGraph(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PropagationGraph call({
    Object? narrativeId = const $CopyWithPlaceholder(),
    Object? summarised = const $CopyWithPlaceholder(),
    Object? totalNodes = const $CopyWithPlaceholder(),
    Object? totalEdges = const $CopyWithPlaceholder(),
    Object? nodes = const $CopyWithPlaceholder(),
    Object? edges = const $CopyWithPlaceholder(),
    Object? earliestByPlatform = const $CopyWithPlaceholder(),
    Object? firstNewsPickup = const $CopyWithPlaceholder(),
    Object? descentShare = const $CopyWithPlaceholder(),
    Object? crossPlatformOrder = const $CopyWithPlaceholder(),
    Object? medianRepostDelaySeconds = const $CopyWithPlaceholder(),
    Object? provenanceChain = const $CopyWithPlaceholder(),
    Object? tracer = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return PropagationGraph(
      narrativeId:
          narrativeId == const $CopyWithPlaceholder() || narrativeId == null
          ? _value.narrativeId
          // ignore: cast_nullable_to_non_nullable
          : narrativeId as String,
      summarised:
          summarised == const $CopyWithPlaceholder() || summarised == null
          ? _value.summarised
          // ignore: cast_nullable_to_non_nullable
          : summarised as bool,
      totalNodes:
          totalNodes == const $CopyWithPlaceholder() || totalNodes == null
          ? _value.totalNodes
          // ignore: cast_nullable_to_non_nullable
          : totalNodes as int,
      totalEdges:
          totalEdges == const $CopyWithPlaceholder() || totalEdges == null
          ? _value.totalEdges
          // ignore: cast_nullable_to_non_nullable
          : totalEdges as int,
      nodes: nodes == const $CopyWithPlaceholder() || nodes == null
          ? _value.nodes
          // ignore: cast_nullable_to_non_nullable
          : nodes as List<PropagationNode>,
      edges: edges == const $CopyWithPlaceholder() || edges == null
          ? _value.edges
          // ignore: cast_nullable_to_non_nullable
          : edges as List<PropagationEdge>,
      earliestByPlatform:
          earliestByPlatform == const $CopyWithPlaceholder() ||
              earliestByPlatform == null
          ? _value.earliestByPlatform
          // ignore: cast_nullable_to_non_nullable
          : earliestByPlatform as List<PropagationGraphEarliestByPlatformInner>,
      firstNewsPickup: firstNewsPickup == const $CopyWithPlaceholder()
          ? _value.firstNewsPickup
          // ignore: cast_nullable_to_non_nullable
          : firstNewsPickup as PropagationGraphFirstNewsPickup?,
      descentShare:
          descentShare == const $CopyWithPlaceholder() || descentShare == null
          ? _value.descentShare
          // ignore: cast_nullable_to_non_nullable
          : descentShare as num,
      crossPlatformOrder:
          crossPlatformOrder == const $CopyWithPlaceholder() ||
              crossPlatformOrder == null
          ? _value.crossPlatformOrder
          // ignore: cast_nullable_to_non_nullable
          : crossPlatformOrder as List<PropagationGraphCrossPlatformOrderInner>,
      medianRepostDelaySeconds:
          medianRepostDelaySeconds == const $CopyWithPlaceholder()
          ? _value.medianRepostDelaySeconds
          // ignore: cast_nullable_to_non_nullable
          : medianRepostDelaySeconds as num?,
      provenanceChain: provenanceChain == const $CopyWithPlaceholder()
          ? _value.provenanceChain
          // ignore: cast_nullable_to_non_nullable
          : provenanceChain as List<ProvenanceChainEntry>?,
      tracer: tracer == const $CopyWithPlaceholder() || tracer == null
          ? _value.tracer
          // ignore: cast_nullable_to_non_nullable
          : tracer as DetectorRef,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $PropagationGraphCopyWith on PropagationGraph {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPropagationGraph.copyWith(...)` or `instanceOfPropagationGraph.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PropagationGraphCWProxy get copyWith => _$PropagationGraphCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PropagationGraph _$PropagationGraphFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PropagationGraph', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'narrativeId',
      'summarised',
      'totalNodes',
      'totalEdges',
      'nodes',
      'edges',
      'earliestByPlatform',
      'descentShare',
      'crossPlatformOrder',
      'tracer',
      'computedAt',
    ],
  );
  final val = PropagationGraph(
    narrativeId: $checkedConvert('narrativeId', (v) => v as String),
    summarised: $checkedConvert('summarised', (v) => v as bool),
    totalNodes: $checkedConvert('totalNodes', (v) => (v as num).toInt()),
    totalEdges: $checkedConvert('totalEdges', (v) => (v as num).toInt()),
    nodes: $checkedConvert(
      'nodes',
      (v) => (v as List<dynamic>)
          .map((e) => PropagationNode.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    edges: $checkedConvert(
      'edges',
      (v) => (v as List<dynamic>)
          .map((e) => PropagationEdge.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    earliestByPlatform: $checkedConvert(
      'earliestByPlatform',
      (v) => (v as List<dynamic>)
          .map(
            (e) => PropagationGraphEarliestByPlatformInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    firstNewsPickup: $checkedConvert(
      'firstNewsPickup',
      (v) => v == null
          ? null
          : PropagationGraphFirstNewsPickup.fromJson(v as Map<String, dynamic>),
    ),
    descentShare: $checkedConvert('descentShare', (v) => v as num),
    crossPlatformOrder: $checkedConvert(
      'crossPlatformOrder',
      (v) => (v as List<dynamic>)
          .map(
            (e) => PropagationGraphCrossPlatformOrderInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    medianRepostDelaySeconds: $checkedConvert(
      'medianRepostDelaySeconds',
      (v) => v as num?,
    ),
    provenanceChain: $checkedConvert(
      'provenanceChain',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ProvenanceChainEntry.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    tracer: $checkedConvert(
      'tracer',
      (v) => DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
    computedAt: $checkedConvert(
      'computedAt',
      (v) => DateTime.parse(v as String),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PropagationGraphToJson(
  PropagationGraph instance,
) => <String, dynamic>{
  'narrativeId': instance.narrativeId,
  'summarised': instance.summarised,
  'totalNodes': instance.totalNodes,
  'totalEdges': instance.totalEdges,
  'nodes': instance.nodes.map((e) => e.toJson()).toList(),
  'edges': instance.edges.map((e) => e.toJson()).toList(),
  'earliestByPlatform': instance.earliestByPlatform
      .map((e) => e.toJson())
      .toList(),
  'firstNewsPickup': ?instance.firstNewsPickup?.toJson(),
  'descentShare': instance.descentShare,
  'crossPlatformOrder': instance.crossPlatformOrder
      .map((e) => e.toJson())
      .toList(),
  'medianRepostDelaySeconds': ?instance.medianRepostDelaySeconds,
  'provenanceChain': ?instance.provenanceChain?.map((e) => e.toJson()).toList(),
  'tracer': instance.tracer.toJson(),
  'computedAt': instance.computedAt.toIso8601String(),
  'manifestId': ?instance.manifestId,
};
