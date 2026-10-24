// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ownership_chain_node_paths_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OwnershipChainNodePathsInnerCWProxy {
  OwnershipChainNodePathsInner from(String from);

  OwnershipChainNodePathsInner hops(List<String> hops);

  OwnershipChainNodePathsInner relationTypes(
    List<OwnershipChainNodePathsInnerRelationTypesEnum>? relationTypes,
  );

  OwnershipChainNodePathsInner weakestEdgeLabel(
    OwnershipChainNodePathsInnerWeakestEdgeLabelEnum weakestEdgeLabel,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OwnershipChainNodePathsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OwnershipChainNodePathsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  OwnershipChainNodePathsInner call({
    String from,
    List<String> hops,
    List<OwnershipChainNodePathsInnerRelationTypesEnum>? relationTypes,
    OwnershipChainNodePathsInnerWeakestEdgeLabelEnum weakestEdgeLabel,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOwnershipChainNodePathsInner.copyWith(...)` or call `instanceOfOwnershipChainNodePathsInner.copyWith.fieldName(value)` for a single field.
class _$OwnershipChainNodePathsInnerCWProxyImpl
    implements _$OwnershipChainNodePathsInnerCWProxy {
  const _$OwnershipChainNodePathsInnerCWProxyImpl(this._value);

  final OwnershipChainNodePathsInner _value;

  @override
  OwnershipChainNodePathsInner from(String from) => call(from: from);

  @override
  OwnershipChainNodePathsInner hops(List<String> hops) => call(hops: hops);

  @override
  OwnershipChainNodePathsInner relationTypes(
    List<OwnershipChainNodePathsInnerRelationTypesEnum>? relationTypes,
  ) => call(relationTypes: relationTypes);

  @override
  OwnershipChainNodePathsInner weakestEdgeLabel(
    OwnershipChainNodePathsInnerWeakestEdgeLabelEnum weakestEdgeLabel,
  ) => call(weakestEdgeLabel: weakestEdgeLabel);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OwnershipChainNodePathsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OwnershipChainNodePathsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OwnershipChainNodePathsInner call({
    Object? from = const $CopyWithPlaceholder(),
    Object? hops = const $CopyWithPlaceholder(),
    Object? relationTypes = const $CopyWithPlaceholder(),
    Object? weakestEdgeLabel = const $CopyWithPlaceholder(),
  }) {
    return OwnershipChainNodePathsInner(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as String,
      hops: hops == const $CopyWithPlaceholder() || hops == null
          ? _value.hops
          // ignore: cast_nullable_to_non_nullable
          : hops as List<String>,
      relationTypes: relationTypes == const $CopyWithPlaceholder()
          ? _value.relationTypes
          // ignore: cast_nullable_to_non_nullable
          : relationTypes
                as List<OwnershipChainNodePathsInnerRelationTypesEnum>?,
      weakestEdgeLabel:
          weakestEdgeLabel == const $CopyWithPlaceholder() ||
              weakestEdgeLabel == null
          ? _value.weakestEdgeLabel
          // ignore: cast_nullable_to_non_nullable
          : weakestEdgeLabel
                as OwnershipChainNodePathsInnerWeakestEdgeLabelEnum,
    );
  }
}

extension $OwnershipChainNodePathsInnerCopyWith
    on OwnershipChainNodePathsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOwnershipChainNodePathsInner.copyWith(...)` or `instanceOfOwnershipChainNodePathsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OwnershipChainNodePathsInnerCWProxy get copyWith =>
      _$OwnershipChainNodePathsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OwnershipChainNodePathsInner _$OwnershipChainNodePathsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OwnershipChainNodePathsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['from', 'hops', 'weakestEdgeLabel']);
  final val = OwnershipChainNodePathsInner(
    from: $checkedConvert('from', (v) => v as String),
    hops: $checkedConvert(
      'hops',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    relationTypes: $checkedConvert(
      'relationTypes',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => $enumDecode(
              _$OwnershipChainNodePathsInnerRelationTypesEnumEnumMap,
              e,
            ),
          )
          .toList(),
    ),
    weakestEdgeLabel: $checkedConvert(
      'weakestEdgeLabel',
      (v) => $enumDecode(
        _$OwnershipChainNodePathsInnerWeakestEdgeLabelEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$OwnershipChainNodePathsInnerToJson(
  OwnershipChainNodePathsInner instance,
) => <String, dynamic>{
  'from': instance.from,
  'hops': instance.hops,
  'relationTypes': ?instance.relationTypes
      ?.map((e) => _$OwnershipChainNodePathsInnerRelationTypesEnumEnumMap[e]!)
      .toList(),
  'weakestEdgeLabel':
      _$OwnershipChainNodePathsInnerWeakestEdgeLabelEnumEnumMap[instance
          .weakestEdgeLabel]!,
};

const _$OwnershipChainNodePathsInnerRelationTypesEnumEnumMap = {
  OwnershipChainNodePathsInnerRelationTypesEnum.owns: 'owns',
  OwnershipChainNodePathsInnerRelationTypesEnum.controls: 'controls',
  OwnershipChainNodePathsInnerRelationTypesEnum.officerOf: 'officer-of',
  OwnershipChainNodePathsInnerRelationTypesEnum.beneficialOwner:
      'beneficial-owner',
  OwnershipChainNodePathsInnerRelationTypesEnum.affiliatedWith:
      'affiliated-with',
};

const _$OwnershipChainNodePathsInnerWeakestEdgeLabelEnumEnumMap = {
  OwnershipChainNodePathsInnerWeakestEdgeLabelEnum.confirmed: 'confirmed',
  OwnershipChainNodePathsInnerWeakestEdgeLabelEnum.likely: 'likely',
  OwnershipChainNodePathsInnerWeakestEdgeLabelEnum.possible: 'possible',
  OwnershipChainNodePathsInnerWeakestEdgeLabelEnum.speculative: 'speculative',
};
