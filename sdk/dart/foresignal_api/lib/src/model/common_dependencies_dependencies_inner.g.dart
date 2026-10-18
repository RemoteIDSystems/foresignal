// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'common_dependencies_dependencies_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CommonDependenciesDependenciesInnerCWProxy {
  CommonDependenciesDependenciesInner node(ObjectEnvelope node);

  CommonDependenciesDependenciesInner sharedBy(List<String> sharedBy);

  CommonDependenciesDependenciesInner weakestEdgeLabel(
    CommonDependenciesDependenciesInnerWeakestEdgeLabelEnum weakestEdgeLabel,
  );

  CommonDependenciesDependenciesInner paths(
    List<CommonDependenciesDependenciesInnerPathsInner> paths,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CommonDependenciesDependenciesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CommonDependenciesDependenciesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  CommonDependenciesDependenciesInner call({
    ObjectEnvelope node,
    List<String> sharedBy,
    CommonDependenciesDependenciesInnerWeakestEdgeLabelEnum weakestEdgeLabel,
    List<CommonDependenciesDependenciesInnerPathsInner> paths,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCommonDependenciesDependenciesInner.copyWith(...)` or call `instanceOfCommonDependenciesDependenciesInner.copyWith.fieldName(value)` for a single field.
class _$CommonDependenciesDependenciesInnerCWProxyImpl
    implements _$CommonDependenciesDependenciesInnerCWProxy {
  const _$CommonDependenciesDependenciesInnerCWProxyImpl(this._value);

  final CommonDependenciesDependenciesInner _value;

  @override
  CommonDependenciesDependenciesInner node(ObjectEnvelope node) =>
      call(node: node);

  @override
  CommonDependenciesDependenciesInner sharedBy(List<String> sharedBy) =>
      call(sharedBy: sharedBy);

  @override
  CommonDependenciesDependenciesInner weakestEdgeLabel(
    CommonDependenciesDependenciesInnerWeakestEdgeLabelEnum weakestEdgeLabel,
  ) => call(weakestEdgeLabel: weakestEdgeLabel);

  @override
  CommonDependenciesDependenciesInner paths(
    List<CommonDependenciesDependenciesInnerPathsInner> paths,
  ) => call(paths: paths);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CommonDependenciesDependenciesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CommonDependenciesDependenciesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CommonDependenciesDependenciesInner call({
    Object? node = const $CopyWithPlaceholder(),
    Object? sharedBy = const $CopyWithPlaceholder(),
    Object? weakestEdgeLabel = const $CopyWithPlaceholder(),
    Object? paths = const $CopyWithPlaceholder(),
  }) {
    return CommonDependenciesDependenciesInner(
      node: node == const $CopyWithPlaceholder() || node == null
          ? _value.node
          // ignore: cast_nullable_to_non_nullable
          : node as ObjectEnvelope,
      sharedBy: sharedBy == const $CopyWithPlaceholder() || sharedBy == null
          ? _value.sharedBy
          // ignore: cast_nullable_to_non_nullable
          : sharedBy as List<String>,
      weakestEdgeLabel:
          weakestEdgeLabel == const $CopyWithPlaceholder() ||
              weakestEdgeLabel == null
          ? _value.weakestEdgeLabel
          // ignore: cast_nullable_to_non_nullable
          : weakestEdgeLabel
                as CommonDependenciesDependenciesInnerWeakestEdgeLabelEnum,
      paths: paths == const $CopyWithPlaceholder() || paths == null
          ? _value.paths
          // ignore: cast_nullable_to_non_nullable
          : paths as List<CommonDependenciesDependenciesInnerPathsInner>,
    );
  }
}

extension $CommonDependenciesDependenciesInnerCopyWith
    on CommonDependenciesDependenciesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCommonDependenciesDependenciesInner.copyWith(...)` or `instanceOfCommonDependenciesDependenciesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CommonDependenciesDependenciesInnerCWProxy get copyWith =>
      _$CommonDependenciesDependenciesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CommonDependenciesDependenciesInner
_$CommonDependenciesDependenciesInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CommonDependenciesDependenciesInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(
        json,
        requiredKeys: const ['node', 'sharedBy', 'weakestEdgeLabel', 'paths'],
      );
      final val = CommonDependenciesDependenciesInner(
        node: $checkedConvert(
          'node',
          (v) => ObjectEnvelope.fromJson(v as Map<String, dynamic>),
        ),
        sharedBy: $checkedConvert(
          'sharedBy',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        weakestEdgeLabel: $checkedConvert(
          'weakestEdgeLabel',
          (v) => $enumDecode(
            _$CommonDependenciesDependenciesInnerWeakestEdgeLabelEnumEnumMap,
            v,
          ),
        ),
        paths: $checkedConvert(
          'paths',
          (v) => (v as List<dynamic>)
              .map(
                (e) => CommonDependenciesDependenciesInnerPathsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$CommonDependenciesDependenciesInnerToJson(
  CommonDependenciesDependenciesInner instance,
) => <String, dynamic>{
  'node': instance.node.toJson(),
  'sharedBy': instance.sharedBy,
  'weakestEdgeLabel':
      _$CommonDependenciesDependenciesInnerWeakestEdgeLabelEnumEnumMap[instance
          .weakestEdgeLabel]!,
  'paths': instance.paths.map((e) => e.toJson()).toList(),
};

const _$CommonDependenciesDependenciesInnerWeakestEdgeLabelEnumEnumMap = {
  CommonDependenciesDependenciesInnerWeakestEdgeLabelEnum.confirmed:
      'confirmed',
  CommonDependenciesDependenciesInnerWeakestEdgeLabelEnum.likely: 'likely',
  CommonDependenciesDependenciesInnerWeakestEdgeLabelEnum.possible: 'possible',
  CommonDependenciesDependenciesInnerWeakestEdgeLabelEnum.speculative:
      'speculative',
};
