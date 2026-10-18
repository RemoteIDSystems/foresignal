// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_impact_result_dependents_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraImpactResultDependentsInnerCWProxy {
  InfraImpactResultDependentsInner node(InfraNode node);

  InfraImpactResultDependentsInner depth(int depth);

  InfraImpactResultDependentsInner pathLabel(
    InfraImpactResultDependentsInnerPathLabelEnum pathLabel,
  );

  InfraImpactResultDependentsInner pathScore(num pathScore);

  InfraImpactResultDependentsInner path(
    List<InfraImpactResultDependentsInnerPathInner> path,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraImpactResultDependentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraImpactResultDependentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraImpactResultDependentsInner call({
    InfraNode node,
    int depth,
    InfraImpactResultDependentsInnerPathLabelEnum pathLabel,
    num pathScore,
    List<InfraImpactResultDependentsInnerPathInner> path,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraImpactResultDependentsInner.copyWith(...)` or call `instanceOfInfraImpactResultDependentsInner.copyWith.fieldName(value)` for a single field.
class _$InfraImpactResultDependentsInnerCWProxyImpl
    implements _$InfraImpactResultDependentsInnerCWProxy {
  const _$InfraImpactResultDependentsInnerCWProxyImpl(this._value);

  final InfraImpactResultDependentsInner _value;

  @override
  InfraImpactResultDependentsInner node(InfraNode node) => call(node: node);

  @override
  InfraImpactResultDependentsInner depth(int depth) => call(depth: depth);

  @override
  InfraImpactResultDependentsInner pathLabel(
    InfraImpactResultDependentsInnerPathLabelEnum pathLabel,
  ) => call(pathLabel: pathLabel);

  @override
  InfraImpactResultDependentsInner pathScore(num pathScore) =>
      call(pathScore: pathScore);

  @override
  InfraImpactResultDependentsInner path(
    List<InfraImpactResultDependentsInnerPathInner> path,
  ) => call(path: path);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraImpactResultDependentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraImpactResultDependentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraImpactResultDependentsInner call({
    Object? node = const $CopyWithPlaceholder(),
    Object? depth = const $CopyWithPlaceholder(),
    Object? pathLabel = const $CopyWithPlaceholder(),
    Object? pathScore = const $CopyWithPlaceholder(),
    Object? path = const $CopyWithPlaceholder(),
  }) {
    return InfraImpactResultDependentsInner(
      node: node == const $CopyWithPlaceholder() || node == null
          ? _value.node
          // ignore: cast_nullable_to_non_nullable
          : node as InfraNode,
      depth: depth == const $CopyWithPlaceholder() || depth == null
          ? _value.depth
          // ignore: cast_nullable_to_non_nullable
          : depth as int,
      pathLabel: pathLabel == const $CopyWithPlaceholder() || pathLabel == null
          ? _value.pathLabel
          // ignore: cast_nullable_to_non_nullable
          : pathLabel as InfraImpactResultDependentsInnerPathLabelEnum,
      pathScore: pathScore == const $CopyWithPlaceholder() || pathScore == null
          ? _value.pathScore
          // ignore: cast_nullable_to_non_nullable
          : pathScore as num,
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as List<InfraImpactResultDependentsInnerPathInner>,
    );
  }
}

extension $InfraImpactResultDependentsInnerCopyWith
    on InfraImpactResultDependentsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraImpactResultDependentsInner.copyWith(...)` or `instanceOfInfraImpactResultDependentsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraImpactResultDependentsInnerCWProxy get copyWith =>
      _$InfraImpactResultDependentsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraImpactResultDependentsInner _$InfraImpactResultDependentsInnerFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('InfraImpactResultDependentsInner', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['node', 'depth', 'pathLabel', 'pathScore', 'path'],
      );
      final val = InfraImpactResultDependentsInner(
        node: $checkedConvert(
          'node',
          (v) => InfraNode.fromJson(v as Map<String, dynamic>),
        ),
        depth: $checkedConvert('depth', (v) => (v as num).toInt()),
        pathLabel: $checkedConvert(
          'pathLabel',
          (v) => $enumDecode(
            _$InfraImpactResultDependentsInnerPathLabelEnumEnumMap,
            v,
          ),
        ),
        pathScore: $checkedConvert('pathScore', (v) => v as num),
        path: $checkedConvert(
          'path',
          (v) => (v as List<dynamic>)
              .map(
                (e) => InfraImpactResultDependentsInnerPathInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$InfraImpactResultDependentsInnerToJson(
  InfraImpactResultDependentsInner instance,
) => <String, dynamic>{
  'node': instance.node.toJson(),
  'depth': instance.depth,
  'pathLabel':
      _$InfraImpactResultDependentsInnerPathLabelEnumEnumMap[instance
          .pathLabel]!,
  'pathScore': instance.pathScore,
  'path': instance.path.map((e) => e.toJson()).toList(),
};

const _$InfraImpactResultDependentsInnerPathLabelEnumEnumMap = {
  InfraImpactResultDependentsInnerPathLabelEnum.confirmed: 'confirmed',
  InfraImpactResultDependentsInnerPathLabelEnum.likely: 'likely',
  InfraImpactResultDependentsInnerPathLabelEnum.possible: 'possible',
  InfraImpactResultDependentsInnerPathLabelEnum.speculative: 'speculative',
};
