// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exposure_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExposureRequestCWProxy {
  ExposureRequest geometry(Geometry? geometry);

  ExposureRequest placeId(String? placeId);

  ExposureRequest eventId(String? eventId);

  ExposureRequest population(ExposureRequestPopulationEnum? population);

  ExposureRequest dependents(bool? dependents);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  ExposureRequest call({
    Geometry? geometry,
    String? placeId,
    String? eventId,
    ExposureRequestPopulationEnum? population,
    bool? dependents,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExposureRequest.copyWith(...)` or call `instanceOfExposureRequest.copyWith.fieldName(value)` for a single field.
class _$ExposureRequestCWProxyImpl implements _$ExposureRequestCWProxy {
  const _$ExposureRequestCWProxyImpl(this._value);

  final ExposureRequest _value;

  @override
  ExposureRequest geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  ExposureRequest placeId(String? placeId) => call(placeId: placeId);

  @override
  ExposureRequest eventId(String? eventId) => call(eventId: eventId);

  @override
  ExposureRequest population(ExposureRequestPopulationEnum? population) =>
      call(population: population);

  @override
  ExposureRequest dependents(bool? dependents) => call(dependents: dependents);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExposureRequest call({
    Object? geometry = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? eventId = const $CopyWithPlaceholder(),
    Object? population = const $CopyWithPlaceholder(),
    Object? dependents = const $CopyWithPlaceholder(),
  }) {
    return ExposureRequest(
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      eventId: eventId == const $CopyWithPlaceholder()
          ? _value.eventId
          // ignore: cast_nullable_to_non_nullable
          : eventId as String?,
      population: population == const $CopyWithPlaceholder()
          ? _value.population
          // ignore: cast_nullable_to_non_nullable
          : population as ExposureRequestPopulationEnum?,
      dependents: dependents == const $CopyWithPlaceholder()
          ? _value.dependents
          // ignore: cast_nullable_to_non_nullable
          : dependents as bool?,
    );
  }
}

extension $ExposureRequestCopyWith on ExposureRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExposureRequest.copyWith(...)` or `instanceOfExposureRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExposureRequestCWProxy get copyWith => _$ExposureRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExposureRequest _$ExposureRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExposureRequest', json, ($checkedConvert) {
      final val = ExposureRequest(
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        placeId: $checkedConvert('placeId', (v) => v as String?),
        eventId: $checkedConvert('eventId', (v) => v as String?),
        population: $checkedConvert(
          'population',
          (v) => $enumDecodeNullable(_$ExposureRequestPopulationEnumEnumMap, v),
        ),
        dependents: $checkedConvert('dependents', (v) => v as bool? ?? true),
      );
      return val;
    });

Map<String, dynamic> _$ExposureRequestToJson(
  ExposureRequest instance,
) => <String, dynamic>{
  'geometry': ?instance.geometry?.toJson(),
  'placeId': ?instance.placeId,
  'eventId': ?instance.eventId,
  'population': ?_$ExposureRequestPopulationEnumEnumMap[instance.population],
  'dependents': ?instance.dependents,
};

const _$ExposureRequestPopulationEnumEnumMap = {
  ExposureRequestPopulationEnum.auto: 'auto',
  ExposureRequestPopulationEnum.off: 'off',
  ExposureRequestPopulationEnum.required_: 'required',
};
