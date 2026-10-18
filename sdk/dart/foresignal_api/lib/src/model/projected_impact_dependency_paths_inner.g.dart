// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'projected_impact_dependency_paths_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProjectedImpactDependencyPathsInnerCWProxy {
  ProjectedImpactDependencyPathsInner from(String from);

  ProjectedImpactDependencyPathsInner to(String to);

  ProjectedImpactDependencyPathsInner hops(List<String>? hops);

  ProjectedImpactDependencyPathsInner weakestEdgeLabel(
    ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnum weakestEdgeLabel,
  );

  ProjectedImpactDependencyPathsInner depth(int? depth);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactDependencyPathsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactDependencyPathsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ProjectedImpactDependencyPathsInner call({
    String from,
    String to,
    List<String>? hops,
    ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnum weakestEdgeLabel,
    int? depth,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProjectedImpactDependencyPathsInner.copyWith(...)` or call `instanceOfProjectedImpactDependencyPathsInner.copyWith.fieldName(value)` for a single field.
class _$ProjectedImpactDependencyPathsInnerCWProxyImpl
    implements _$ProjectedImpactDependencyPathsInnerCWProxy {
  const _$ProjectedImpactDependencyPathsInnerCWProxyImpl(this._value);

  final ProjectedImpactDependencyPathsInner _value;

  @override
  ProjectedImpactDependencyPathsInner from(String from) => call(from: from);

  @override
  ProjectedImpactDependencyPathsInner to(String to) => call(to: to);

  @override
  ProjectedImpactDependencyPathsInner hops(List<String>? hops) =>
      call(hops: hops);

  @override
  ProjectedImpactDependencyPathsInner weakestEdgeLabel(
    ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnum weakestEdgeLabel,
  ) => call(weakestEdgeLabel: weakestEdgeLabel);

  @override
  ProjectedImpactDependencyPathsInner depth(int? depth) => call(depth: depth);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactDependencyPathsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactDependencyPathsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProjectedImpactDependencyPathsInner call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? hops = const $CopyWithPlaceholder(),
    Object? weakestEdgeLabel = const $CopyWithPlaceholder(),
    Object? depth = const $CopyWithPlaceholder(),
  }) {
    return ProjectedImpactDependencyPathsInner(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as String,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as String,
      hops: hops == const $CopyWithPlaceholder()
          ? _value.hops
          // ignore: cast_nullable_to_non_nullable
          : hops as List<String>?,
      weakestEdgeLabel:
          weakestEdgeLabel == const $CopyWithPlaceholder() ||
              weakestEdgeLabel == null
          ? _value.weakestEdgeLabel
          // ignore: cast_nullable_to_non_nullable
          : weakestEdgeLabel
                as ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnum,
      depth: depth == const $CopyWithPlaceholder()
          ? _value.depth
          // ignore: cast_nullable_to_non_nullable
          : depth as int?,
    );
  }
}

extension $ProjectedImpactDependencyPathsInnerCopyWith
    on ProjectedImpactDependencyPathsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProjectedImpactDependencyPathsInner.copyWith(...)` or `instanceOfProjectedImpactDependencyPathsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProjectedImpactDependencyPathsInnerCWProxy get copyWith =>
      _$ProjectedImpactDependencyPathsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProjectedImpactDependencyPathsInner
_$ProjectedImpactDependencyPathsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProjectedImpactDependencyPathsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['from', 'to', 'weakestEdgeLabel']);
      final val = ProjectedImpactDependencyPathsInner(
        from: $checkedConvert('from', (v) => v as String),
        to: $checkedConvert('to', (v) => v as String),
        hops: $checkedConvert(
          'hops',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        weakestEdgeLabel: $checkedConvert(
          'weakestEdgeLabel',
          (v) => $enumDecode(
            _$ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnumEnumMap,
            v,
          ),
        ),
        depth: $checkedConvert('depth', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ProjectedImpactDependencyPathsInnerToJson(
  ProjectedImpactDependencyPathsInner instance,
) => <String, dynamic>{
  'from': instance.from,
  'to': instance.to,
  'hops': ?instance.hops,
  'weakestEdgeLabel':
      _$ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnumEnumMap[instance
          .weakestEdgeLabel]!,
  'depth': ?instance.depth,
};

const _$ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnumEnumMap = {
  ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnum.confirmed:
      'confirmed',
  ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnum.likely: 'likely',
  ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnum.possible: 'possible',
  ProjectedImpactDependencyPathsInnerWeakestEdgeLabelEnum.speculative:
      'speculative',
};
