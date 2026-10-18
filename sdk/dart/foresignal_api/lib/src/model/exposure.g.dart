// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exposure.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExposureCWProxy {
  Exposure area(ExposureArea area);

  Exposure computedAt(DateTime computedAt);

  Exposure facilities(ExposureFacilities facilities);

  Exposure places(ExposurePlaces places);

  Exposure watchlistEntities(ExposureWatchlistEntities watchlistEntities);

  Exposure traffic(ExposureTraffic traffic);

  Exposure population(ExposurePopulation population);

  Exposure dependents(ExposureDependents? dependents);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Exposure(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Exposure(...).copyWith(id: 12, name: "My name")
  /// ```
  Exposure call({
    ExposureArea area,
    DateTime computedAt,
    ExposureFacilities facilities,
    ExposurePlaces places,
    ExposureWatchlistEntities watchlistEntities,
    ExposureTraffic traffic,
    ExposurePopulation population,
    ExposureDependents? dependents,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExposure.copyWith(...)` or call `instanceOfExposure.copyWith.fieldName(value)` for a single field.
class _$ExposureCWProxyImpl implements _$ExposureCWProxy {
  const _$ExposureCWProxyImpl(this._value);

  final Exposure _value;

  @override
  Exposure area(ExposureArea area) => call(area: area);

  @override
  Exposure computedAt(DateTime computedAt) => call(computedAt: computedAt);

  @override
  Exposure facilities(ExposureFacilities facilities) =>
      call(facilities: facilities);

  @override
  Exposure places(ExposurePlaces places) => call(places: places);

  @override
  Exposure watchlistEntities(ExposureWatchlistEntities watchlistEntities) =>
      call(watchlistEntities: watchlistEntities);

  @override
  Exposure traffic(ExposureTraffic traffic) => call(traffic: traffic);

  @override
  Exposure population(ExposurePopulation population) =>
      call(population: population);

  @override
  Exposure dependents(ExposureDependents? dependents) =>
      call(dependents: dependents);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Exposure(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Exposure(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Exposure call({
    Object? area = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
    Object? facilities = const $CopyWithPlaceholder(),
    Object? places = const $CopyWithPlaceholder(),
    Object? watchlistEntities = const $CopyWithPlaceholder(),
    Object? traffic = const $CopyWithPlaceholder(),
    Object? population = const $CopyWithPlaceholder(),
    Object? dependents = const $CopyWithPlaceholder(),
  }) {
    return Exposure(
      area: area == const $CopyWithPlaceholder() || area == null
          ? _value.area
          // ignore: cast_nullable_to_non_nullable
          : area as ExposureArea,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
      facilities:
          facilities == const $CopyWithPlaceholder() || facilities == null
          ? _value.facilities
          // ignore: cast_nullable_to_non_nullable
          : facilities as ExposureFacilities,
      places: places == const $CopyWithPlaceholder() || places == null
          ? _value.places
          // ignore: cast_nullable_to_non_nullable
          : places as ExposurePlaces,
      watchlistEntities:
          watchlistEntities == const $CopyWithPlaceholder() ||
              watchlistEntities == null
          ? _value.watchlistEntities
          // ignore: cast_nullable_to_non_nullable
          : watchlistEntities as ExposureWatchlistEntities,
      traffic: traffic == const $CopyWithPlaceholder() || traffic == null
          ? _value.traffic
          // ignore: cast_nullable_to_non_nullable
          : traffic as ExposureTraffic,
      population:
          population == const $CopyWithPlaceholder() || population == null
          ? _value.population
          // ignore: cast_nullable_to_non_nullable
          : population as ExposurePopulation,
      dependents: dependents == const $CopyWithPlaceholder()
          ? _value.dependents
          // ignore: cast_nullable_to_non_nullable
          : dependents as ExposureDependents?,
    );
  }
}

extension $ExposureCopyWith on Exposure {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExposure.copyWith(...)` or `instanceOfExposure.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExposureCWProxy get copyWith => _$ExposureCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Exposure _$ExposureFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Exposure', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'area',
          'computedAt',
          'facilities',
          'places',
          'watchlistEntities',
          'traffic',
          'population',
        ],
      );
      final val = Exposure(
        area: $checkedConvert(
          'area',
          (v) => ExposureArea.fromJson(v as Map<String, dynamic>),
        ),
        computedAt: $checkedConvert(
          'computedAt',
          (v) => DateTime.parse(v as String),
        ),
        facilities: $checkedConvert(
          'facilities',
          (v) => ExposureFacilities.fromJson(v as Map<String, dynamic>),
        ),
        places: $checkedConvert(
          'places',
          (v) => ExposurePlaces.fromJson(v as Map<String, dynamic>),
        ),
        watchlistEntities: $checkedConvert(
          'watchlistEntities',
          (v) => ExposureWatchlistEntities.fromJson(v as Map<String, dynamic>),
        ),
        traffic: $checkedConvert(
          'traffic',
          (v) => ExposureTraffic.fromJson(v as Map<String, dynamic>),
        ),
        population: $checkedConvert(
          'population',
          (v) => ExposurePopulation.fromJson(v as Map<String, dynamic>),
        ),
        dependents: $checkedConvert(
          'dependents',
          (v) => v == null
              ? null
              : ExposureDependents.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ExposureToJson(Exposure instance) => <String, dynamic>{
  'area': instance.area.toJson(),
  'computedAt': instance.computedAt.toIso8601String(),
  'facilities': instance.facilities.toJson(),
  'places': instance.places.toJson(),
  'watchlistEntities': instance.watchlistEntities.toJson(),
  'traffic': instance.traffic.toJson(),
  'population': instance.population.toJson(),
  'dependents': ?instance.dependents?.toJson(),
};
