// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hazard_forecast_probability_field_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HazardForecastProbabilityFieldInnerCWProxy {
  HazardForecastProbabilityFieldInner band(String? band);

  HazardForecastProbabilityFieldInner pMin(num pMin);

  HazardForecastProbabilityFieldInner pMax(num? pMax);

  HazardForecastProbabilityFieldInner geometry(Geometry? geometry);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HazardForecastProbabilityFieldInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HazardForecastProbabilityFieldInner(...).copyWith(id: 12, name: "My name")
  /// ```
  HazardForecastProbabilityFieldInner call({
    String? band,
    num pMin,
    num? pMax,
    Geometry? geometry,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHazardForecastProbabilityFieldInner.copyWith(...)` or call `instanceOfHazardForecastProbabilityFieldInner.copyWith.fieldName(value)` for a single field.
class _$HazardForecastProbabilityFieldInnerCWProxyImpl
    implements _$HazardForecastProbabilityFieldInnerCWProxy {
  const _$HazardForecastProbabilityFieldInnerCWProxyImpl(this._value);

  final HazardForecastProbabilityFieldInner _value;

  @override
  HazardForecastProbabilityFieldInner band(String? band) => call(band: band);

  @override
  HazardForecastProbabilityFieldInner pMin(num pMin) => call(pMin: pMin);

  @override
  HazardForecastProbabilityFieldInner pMax(num? pMax) => call(pMax: pMax);

  @override
  HazardForecastProbabilityFieldInner geometry(Geometry? geometry) =>
      call(geometry: geometry);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HazardForecastProbabilityFieldInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HazardForecastProbabilityFieldInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HazardForecastProbabilityFieldInner call({
    Object? band = const $CopyWithPlaceholder(),
    Object? pMin = const $CopyWithPlaceholder(),
    Object? pMax = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
  }) {
    return HazardForecastProbabilityFieldInner(
      band: band == const $CopyWithPlaceholder()
          ? _value.band
          // ignore: cast_nullable_to_non_nullable
          : band as String?,
      pMin: pMin == const $CopyWithPlaceholder() || pMin == null
          ? _value.pMin
          // ignore: cast_nullable_to_non_nullable
          : pMin as num,
      pMax: pMax == const $CopyWithPlaceholder()
          ? _value.pMax
          // ignore: cast_nullable_to_non_nullable
          : pMax as num?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
    );
  }
}

extension $HazardForecastProbabilityFieldInnerCopyWith
    on HazardForecastProbabilityFieldInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHazardForecastProbabilityFieldInner.copyWith(...)` or `instanceOfHazardForecastProbabilityFieldInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HazardForecastProbabilityFieldInnerCWProxy get copyWith =>
      _$HazardForecastProbabilityFieldInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HazardForecastProbabilityFieldInner
_$HazardForecastProbabilityFieldInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('HazardForecastProbabilityFieldInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['pMin']);
      final val = HazardForecastProbabilityFieldInner(
        band: $checkedConvert('band', (v) => v as String?),
        pMin: $checkedConvert('pMin', (v) => v as num),
        pMax: $checkedConvert('pMax', (v) => v as num?),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$HazardForecastProbabilityFieldInnerToJson(
  HazardForecastProbabilityFieldInner instance,
) => <String, dynamic>{
  'band': ?instance.band,
  'pMin': instance.pMin,
  'pMax': ?instance.pMax,
  'geometry': ?instance.geometry?.toJson(),
};
