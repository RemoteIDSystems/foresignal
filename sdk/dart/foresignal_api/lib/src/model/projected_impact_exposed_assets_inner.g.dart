// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'projected_impact_exposed_assets_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProjectedImpactExposedAssetsInnerCWProxy {
  ProjectedImpactExposedAssetsInner ref(String ref);

  ProjectedImpactExposedAssetsInner title(String? title);

  ProjectedImpactExposedAssetsInner kind(String? kind);

  ProjectedImpactExposedAssetsInner pHazard(num pHazard);

  ProjectedImpactExposedAssetsInner distanceKm(num? distanceKm);

  ProjectedImpactExposedAssetsInner population(int? population);

  ProjectedImpactExposedAssetsInner band(String? band);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactExposedAssetsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactExposedAssetsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ProjectedImpactExposedAssetsInner call({
    String ref,
    String? title,
    String? kind,
    num pHazard,
    num? distanceKm,
    int? population,
    String? band,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProjectedImpactExposedAssetsInner.copyWith(...)` or call `instanceOfProjectedImpactExposedAssetsInner.copyWith.fieldName(value)` for a single field.
class _$ProjectedImpactExposedAssetsInnerCWProxyImpl
    implements _$ProjectedImpactExposedAssetsInnerCWProxy {
  const _$ProjectedImpactExposedAssetsInnerCWProxyImpl(this._value);

  final ProjectedImpactExposedAssetsInner _value;

  @override
  ProjectedImpactExposedAssetsInner ref(String ref) => call(ref: ref);

  @override
  ProjectedImpactExposedAssetsInner title(String? title) => call(title: title);

  @override
  ProjectedImpactExposedAssetsInner kind(String? kind) => call(kind: kind);

  @override
  ProjectedImpactExposedAssetsInner pHazard(num pHazard) =>
      call(pHazard: pHazard);

  @override
  ProjectedImpactExposedAssetsInner distanceKm(num? distanceKm) =>
      call(distanceKm: distanceKm);

  @override
  ProjectedImpactExposedAssetsInner population(int? population) =>
      call(population: population);

  @override
  ProjectedImpactExposedAssetsInner band(String? band) => call(band: band);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProjectedImpactExposedAssetsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProjectedImpactExposedAssetsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProjectedImpactExposedAssetsInner call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? pHazard = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
    Object? population = const $CopyWithPlaceholder(),
    Object? band = const $CopyWithPlaceholder(),
  }) {
    return ProjectedImpactExposedAssetsInner(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String?,
      pHazard: pHazard == const $CopyWithPlaceholder() || pHazard == null
          ? _value.pHazard
          // ignore: cast_nullable_to_non_nullable
          : pHazard as num,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
      population: population == const $CopyWithPlaceholder()
          ? _value.population
          // ignore: cast_nullable_to_non_nullable
          : population as int?,
      band: band == const $CopyWithPlaceholder()
          ? _value.band
          // ignore: cast_nullable_to_non_nullable
          : band as String?,
    );
  }
}

extension $ProjectedImpactExposedAssetsInnerCopyWith
    on ProjectedImpactExposedAssetsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProjectedImpactExposedAssetsInner.copyWith(...)` or `instanceOfProjectedImpactExposedAssetsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProjectedImpactExposedAssetsInnerCWProxy get copyWith =>
      _$ProjectedImpactExposedAssetsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProjectedImpactExposedAssetsInner _$ProjectedImpactExposedAssetsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProjectedImpactExposedAssetsInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['ref', 'pHazard']);
  final val = ProjectedImpactExposedAssetsInner(
    ref: $checkedConvert('ref', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String?),
    kind: $checkedConvert('kind', (v) => v as String?),
    pHazard: $checkedConvert('pHazard', (v) => v as num),
    distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
    population: $checkedConvert('population', (v) => (v as num?)?.toInt()),
    band: $checkedConvert('band', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ProjectedImpactExposedAssetsInnerToJson(
  ProjectedImpactExposedAssetsInner instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'title': ?instance.title,
  'kind': ?instance.kind,
  'pHazard': instance.pHazard,
  'distanceKm': ?instance.distanceKm,
  'population': ?instance.population,
  'band': ?instance.band,
};
