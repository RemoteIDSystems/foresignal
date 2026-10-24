// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'common_dependencies_dependencies_inner_paths_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CommonDependenciesDependenciesInnerPathsInnerCWProxy {
  CommonDependenciesDependenciesInnerPathsInner from(String from);

  CommonDependenciesDependenciesInnerPathsInner hops(List<String> hops);

  CommonDependenciesDependenciesInnerPathsInner relationTypes(
    List<CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum>?
    relationTypes,
  );

  CommonDependenciesDependenciesInnerPathsInner weakestEdgeLabel(
    CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnum
    weakestEdgeLabel,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CommonDependenciesDependenciesInnerPathsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CommonDependenciesDependenciesInnerPathsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  CommonDependenciesDependenciesInnerPathsInner call({
    String from,
    List<String> hops,
    List<CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum>?
    relationTypes,
    CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnum
    weakestEdgeLabel,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCommonDependenciesDependenciesInnerPathsInner.copyWith(...)` or call `instanceOfCommonDependenciesDependenciesInnerPathsInner.copyWith.fieldName(value)` for a single field.
class _$CommonDependenciesDependenciesInnerPathsInnerCWProxyImpl
    implements _$CommonDependenciesDependenciesInnerPathsInnerCWProxy {
  const _$CommonDependenciesDependenciesInnerPathsInnerCWProxyImpl(this._value);

  final CommonDependenciesDependenciesInnerPathsInner _value;

  @override
  CommonDependenciesDependenciesInnerPathsInner from(String from) =>
      call(from: from);

  @override
  CommonDependenciesDependenciesInnerPathsInner hops(List<String> hops) =>
      call(hops: hops);

  @override
  CommonDependenciesDependenciesInnerPathsInner relationTypes(
    List<CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum>?
    relationTypes,
  ) => call(relationTypes: relationTypes);

  @override
  CommonDependenciesDependenciesInnerPathsInner weakestEdgeLabel(
    CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnum
    weakestEdgeLabel,
  ) => call(weakestEdgeLabel: weakestEdgeLabel);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CommonDependenciesDependenciesInnerPathsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CommonDependenciesDependenciesInnerPathsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CommonDependenciesDependenciesInnerPathsInner call({
    Object? from = const $CopyWithPlaceholder(),
    Object? hops = const $CopyWithPlaceholder(),
    Object? relationTypes = const $CopyWithPlaceholder(),
    Object? weakestEdgeLabel = const $CopyWithPlaceholder(),
  }) {
    return CommonDependenciesDependenciesInnerPathsInner(
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
                as List<
                  CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum
                >?,
      weakestEdgeLabel:
          weakestEdgeLabel == const $CopyWithPlaceholder() ||
              weakestEdgeLabel == null
          ? _value.weakestEdgeLabel
          // ignore: cast_nullable_to_non_nullable
          : weakestEdgeLabel
                as CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnum,
    );
  }
}

extension $CommonDependenciesDependenciesInnerPathsInnerCopyWith
    on CommonDependenciesDependenciesInnerPathsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCommonDependenciesDependenciesInnerPathsInner.copyWith(...)` or `instanceOfCommonDependenciesDependenciesInnerPathsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CommonDependenciesDependenciesInnerPathsInnerCWProxy get copyWith =>
      _$CommonDependenciesDependenciesInnerPathsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CommonDependenciesDependenciesInnerPathsInner
_$CommonDependenciesDependenciesInnerPathsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CommonDependenciesDependenciesInnerPathsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['from', 'hops', 'weakestEdgeLabel']);
  final val = CommonDependenciesDependenciesInnerPathsInner(
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
              _$CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnumEnumMap,
              e,
            ),
          )
          .toList(),
    ),
    weakestEdgeLabel: $checkedConvert(
      'weakestEdgeLabel',
      (v) => $enumDecode(
        _$CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$CommonDependenciesDependenciesInnerPathsInnerToJson(
  CommonDependenciesDependenciesInnerPathsInner instance,
) => <String, dynamic>{
  'from': instance.from,
  'hops': instance.hops,
  'relationTypes': ?instance.relationTypes
      ?.map(
        (e) =>
            _$CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnumEnumMap[e]!,
      )
      .toList(),
  'weakestEdgeLabel':
      _$CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnumEnumMap[instance
          .weakestEdgeLabel]!,
};

const _$CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnumEnumMap =
    {
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.memberOf:
          'member-of',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.observedBy:
          'observed-by',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.sameAs:
          'same-as',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.locatedIn:
          'located-in',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.near:
          'near',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.dependsOn:
          'depends-on',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.affects:
          'affects',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum
              .derivedFrom:
          'derived-from',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum
              .evidenceFor:
          'evidence-for',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum
              .evidenceAgainst:
          'evidence-against',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.about:
          'about',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.mentions:
          'mentions',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.precedes:
          'precedes',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.causes:
          'causes',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.relatedTo:
          'related-to',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.owns:
          'owns',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.controls:
          'controls',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum.officerOf:
          'officer-of',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum
              .beneficialOwner:
          'beneficial-owner',
      CommonDependenciesDependenciesInnerPathsInnerRelationTypesEnum
              .affiliatedWith:
          'affiliated-with',
    };

const _$CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnumEnumMap =
    {
      CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnum
              .confirmed:
          'confirmed',
      CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnum.likely:
          'likely',
      CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnum
              .possible:
          'possible',
      CommonDependenciesDependenciesInnerPathsInnerWeakestEdgeLabelEnum
              .speculative:
          'speculative',
    };
