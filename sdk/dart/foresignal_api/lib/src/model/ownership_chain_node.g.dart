// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ownership_chain_node.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OwnershipChainNodeCWProxy {
  OwnershipChainNode node(ObjectEnvelope node);

  OwnershipChainNode reachedFrom(List<String> reachedFrom);

  OwnershipChainNode weakestEdgeLabel(
    OwnershipChainNodeWeakestEdgeLabelEnum weakestEdgeLabel,
  );

  OwnershipChainNode paths(List<OwnershipChainNodePathsInner> paths);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OwnershipChainNode(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OwnershipChainNode(...).copyWith(id: 12, name: "My name")
  /// ```
  OwnershipChainNode call({
    ObjectEnvelope node,
    List<String> reachedFrom,
    OwnershipChainNodeWeakestEdgeLabelEnum weakestEdgeLabel,
    List<OwnershipChainNodePathsInner> paths,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOwnershipChainNode.copyWith(...)` or call `instanceOfOwnershipChainNode.copyWith.fieldName(value)` for a single field.
class _$OwnershipChainNodeCWProxyImpl implements _$OwnershipChainNodeCWProxy {
  const _$OwnershipChainNodeCWProxyImpl(this._value);

  final OwnershipChainNode _value;

  @override
  OwnershipChainNode node(ObjectEnvelope node) => call(node: node);

  @override
  OwnershipChainNode reachedFrom(List<String> reachedFrom) =>
      call(reachedFrom: reachedFrom);

  @override
  OwnershipChainNode weakestEdgeLabel(
    OwnershipChainNodeWeakestEdgeLabelEnum weakestEdgeLabel,
  ) => call(weakestEdgeLabel: weakestEdgeLabel);

  @override
  OwnershipChainNode paths(List<OwnershipChainNodePathsInner> paths) =>
      call(paths: paths);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OwnershipChainNode(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OwnershipChainNode(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OwnershipChainNode call({
    Object? node = const $CopyWithPlaceholder(),
    Object? reachedFrom = const $CopyWithPlaceholder(),
    Object? weakestEdgeLabel = const $CopyWithPlaceholder(),
    Object? paths = const $CopyWithPlaceholder(),
  }) {
    return OwnershipChainNode(
      node: node == const $CopyWithPlaceholder() || node == null
          ? _value.node
          // ignore: cast_nullable_to_non_nullable
          : node as ObjectEnvelope,
      reachedFrom:
          reachedFrom == const $CopyWithPlaceholder() || reachedFrom == null
          ? _value.reachedFrom
          // ignore: cast_nullable_to_non_nullable
          : reachedFrom as List<String>,
      weakestEdgeLabel:
          weakestEdgeLabel == const $CopyWithPlaceholder() ||
              weakestEdgeLabel == null
          ? _value.weakestEdgeLabel
          // ignore: cast_nullable_to_non_nullable
          : weakestEdgeLabel as OwnershipChainNodeWeakestEdgeLabelEnum,
      paths: paths == const $CopyWithPlaceholder() || paths == null
          ? _value.paths
          // ignore: cast_nullable_to_non_nullable
          : paths as List<OwnershipChainNodePathsInner>,
    );
  }
}

extension $OwnershipChainNodeCopyWith on OwnershipChainNode {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOwnershipChainNode.copyWith(...)` or `instanceOfOwnershipChainNode.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OwnershipChainNodeCWProxy get copyWith =>
      _$OwnershipChainNodeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OwnershipChainNode _$OwnershipChainNodeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OwnershipChainNode', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'node',
          'reachedFrom',
          'weakestEdgeLabel',
          'paths',
        ],
      );
      final val = OwnershipChainNode(
        node: $checkedConvert(
          'node',
          (v) => ObjectEnvelope.fromJson(v as Map<String, dynamic>),
        ),
        reachedFrom: $checkedConvert(
          'reachedFrom',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        weakestEdgeLabel: $checkedConvert(
          'weakestEdgeLabel',
          (v) =>
              $enumDecode(_$OwnershipChainNodeWeakestEdgeLabelEnumEnumMap, v),
        ),
        paths: $checkedConvert(
          'paths',
          (v) => (v as List<dynamic>)
              .map(
                (e) => OwnershipChainNodePathsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$OwnershipChainNodeToJson(OwnershipChainNode instance) =>
    <String, dynamic>{
      'node': instance.node.toJson(),
      'reachedFrom': instance.reachedFrom,
      'weakestEdgeLabel':
          _$OwnershipChainNodeWeakestEdgeLabelEnumEnumMap[instance
              .weakestEdgeLabel]!,
      'paths': instance.paths.map((e) => e.toJson()).toList(),
    };

const _$OwnershipChainNodeWeakestEdgeLabelEnumEnumMap = {
  OwnershipChainNodeWeakestEdgeLabelEnum.confirmed: 'confirmed',
  OwnershipChainNodeWeakestEdgeLabelEnum.likely: 'likely',
  OwnershipChainNodeWeakestEdgeLabelEnum.possible: 'possible',
  OwnershipChainNodeWeakestEdgeLabelEnum.speculative: 'speculative',
};
