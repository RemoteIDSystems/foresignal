// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'satellite_pass_payload_rf_snr_by_elevation_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SatellitePassPayloadRfSnrByElevationInnerCWProxy {
  SatellitePassPayloadRfSnrByElevationInner elevationDeg(num elevationDeg);

  SatellitePassPayloadRfSnrByElevationInner snrDb(num snrDb);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SatellitePassPayloadRfSnrByElevationInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SatellitePassPayloadRfSnrByElevationInner(...).copyWith(id: 12, name: "My name")
  /// ```
  SatellitePassPayloadRfSnrByElevationInner call({num elevationDeg, num snrDb});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSatellitePassPayloadRfSnrByElevationInner.copyWith(...)` or call `instanceOfSatellitePassPayloadRfSnrByElevationInner.copyWith.fieldName(value)` for a single field.
class _$SatellitePassPayloadRfSnrByElevationInnerCWProxyImpl
    implements _$SatellitePassPayloadRfSnrByElevationInnerCWProxy {
  const _$SatellitePassPayloadRfSnrByElevationInnerCWProxyImpl(this._value);

  final SatellitePassPayloadRfSnrByElevationInner _value;

  @override
  SatellitePassPayloadRfSnrByElevationInner elevationDeg(num elevationDeg) =>
      call(elevationDeg: elevationDeg);

  @override
  SatellitePassPayloadRfSnrByElevationInner snrDb(num snrDb) =>
      call(snrDb: snrDb);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SatellitePassPayloadRfSnrByElevationInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SatellitePassPayloadRfSnrByElevationInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SatellitePassPayloadRfSnrByElevationInner call({
    Object? elevationDeg = const $CopyWithPlaceholder(),
    Object? snrDb = const $CopyWithPlaceholder(),
  }) {
    return SatellitePassPayloadRfSnrByElevationInner(
      elevationDeg:
          elevationDeg == const $CopyWithPlaceholder() || elevationDeg == null
          ? _value.elevationDeg
          // ignore: cast_nullable_to_non_nullable
          : elevationDeg as num,
      snrDb: snrDb == const $CopyWithPlaceholder() || snrDb == null
          ? _value.snrDb
          // ignore: cast_nullable_to_non_nullable
          : snrDb as num,
    );
  }
}

extension $SatellitePassPayloadRfSnrByElevationInnerCopyWith
    on SatellitePassPayloadRfSnrByElevationInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSatellitePassPayloadRfSnrByElevationInner.copyWith(...)` or `instanceOfSatellitePassPayloadRfSnrByElevationInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SatellitePassPayloadRfSnrByElevationInnerCWProxy get copyWith =>
      _$SatellitePassPayloadRfSnrByElevationInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SatellitePassPayloadRfSnrByElevationInner
_$SatellitePassPayloadRfSnrByElevationInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SatellitePassPayloadRfSnrByElevationInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['elevationDeg', 'snrDb']);
  final val = SatellitePassPayloadRfSnrByElevationInner(
    elevationDeg: $checkedConvert('elevationDeg', (v) => v as num),
    snrDb: $checkedConvert('snrDb', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$SatellitePassPayloadRfSnrByElevationInnerToJson(
  SatellitePassPayloadRfSnrByElevationInner instance,
) => <String, dynamic>{
  'elevationDeg': instance.elevationDeg,
  'snrDb': instance.snrDb,
};
