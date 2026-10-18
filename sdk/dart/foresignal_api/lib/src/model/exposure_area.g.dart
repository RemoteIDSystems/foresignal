// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'exposure_area.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExposureAreaCWProxy {
  ExposureArea source_(ExposureAreaSource_Enum source_);

  ExposureArea placeId(String? placeId);

  ExposureArea eventId(String? eventId);

  ExposureArea geometry(Geometry? geometry);

  ExposureArea areaKm2(num? areaKm2);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureArea(...).copyWith(id: 12, name: "My name")
  /// ```
  ExposureArea call({
    ExposureAreaSource_Enum source_,
    String? placeId,
    String? eventId,
    Geometry? geometry,
    num? areaKm2,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExposureArea.copyWith(...)` or call `instanceOfExposureArea.copyWith.fieldName(value)` for a single field.
class _$ExposureAreaCWProxyImpl implements _$ExposureAreaCWProxy {
  const _$ExposureAreaCWProxyImpl(this._value);

  final ExposureArea _value;

  @override
  ExposureArea source_(ExposureAreaSource_Enum source_) =>
      call(source_: source_);

  @override
  ExposureArea placeId(String? placeId) => call(placeId: placeId);

  @override
  ExposureArea eventId(String? eventId) => call(eventId: eventId);

  @override
  ExposureArea geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  ExposureArea areaKm2(num? areaKm2) => call(areaKm2: areaKm2);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExposureArea(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExposureArea(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExposureArea call({
    Object? source_ = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? eventId = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? areaKm2 = const $CopyWithPlaceholder(),
  }) {
    return ExposureArea(
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as ExposureAreaSource_Enum,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      eventId: eventId == const $CopyWithPlaceholder()
          ? _value.eventId
          // ignore: cast_nullable_to_non_nullable
          : eventId as String?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      areaKm2: areaKm2 == const $CopyWithPlaceholder()
          ? _value.areaKm2
          // ignore: cast_nullable_to_non_nullable
          : areaKm2 as num?,
    );
  }
}

extension $ExposureAreaCopyWith on ExposureArea {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExposureArea.copyWith(...)` or `instanceOfExposureArea.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExposureAreaCWProxy get copyWith => _$ExposureAreaCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExposureArea _$ExposureAreaFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExposureArea', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['source']);
      final val = ExposureArea(
        source_: $checkedConvert(
          'source',
          (v) => $enumDecode(_$ExposureAreaSource_EnumEnumMap, v),
        ),
        placeId: $checkedConvert('placeId', (v) => v as String?),
        eventId: $checkedConvert('eventId', (v) => v as String?),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        areaKm2: $checkedConvert('areaKm2', (v) => v as num?),
      );
      return val;
    }, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$ExposureAreaToJson(ExposureArea instance) =>
    <String, dynamic>{
      'source': _$ExposureAreaSource_EnumEnumMap[instance.source_]!,
      'placeId': ?instance.placeId,
      'eventId': ?instance.eventId,
      'geometry': ?instance.geometry?.toJson(),
      'areaKm2': ?instance.areaKm2,
    };

const _$ExposureAreaSource_EnumEnumMap = {
  ExposureAreaSource_Enum.geometry: 'geometry',
  ExposureAreaSource_Enum.place: 'place',
  ExposureAreaSource_Enum.event: 'event',
};
