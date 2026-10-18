// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'storm_track_payload_cell.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StormTrackPayloadCellCWProxy {
  StormTrackPayloadCell maxDbz(num? maxDbz);

  StormTrackPayloadCell areaKm2(num? areaKm2);

  StormTrackPayloadCell meshMaxMm(num? meshMaxMm);

  StormTrackPayloadCell flashRate(num? flashRate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayloadCell(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayloadCell(...).copyWith(id: 12, name: "My name")
  /// ```
  StormTrackPayloadCell call({
    num? maxDbz,
    num? areaKm2,
    num? meshMaxMm,
    num? flashRate,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStormTrackPayloadCell.copyWith(...)` or call `instanceOfStormTrackPayloadCell.copyWith.fieldName(value)` for a single field.
class _$StormTrackPayloadCellCWProxyImpl
    implements _$StormTrackPayloadCellCWProxy {
  const _$StormTrackPayloadCellCWProxyImpl(this._value);

  final StormTrackPayloadCell _value;

  @override
  StormTrackPayloadCell maxDbz(num? maxDbz) => call(maxDbz: maxDbz);

  @override
  StormTrackPayloadCell areaKm2(num? areaKm2) => call(areaKm2: areaKm2);

  @override
  StormTrackPayloadCell meshMaxMm(num? meshMaxMm) => call(meshMaxMm: meshMaxMm);

  @override
  StormTrackPayloadCell flashRate(num? flashRate) => call(flashRate: flashRate);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayloadCell(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayloadCell(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StormTrackPayloadCell call({
    Object? maxDbz = const $CopyWithPlaceholder(),
    Object? areaKm2 = const $CopyWithPlaceholder(),
    Object? meshMaxMm = const $CopyWithPlaceholder(),
    Object? flashRate = const $CopyWithPlaceholder(),
  }) {
    return StormTrackPayloadCell(
      maxDbz: maxDbz == const $CopyWithPlaceholder()
          ? _value.maxDbz
          // ignore: cast_nullable_to_non_nullable
          : maxDbz as num?,
      areaKm2: areaKm2 == const $CopyWithPlaceholder()
          ? _value.areaKm2
          // ignore: cast_nullable_to_non_nullable
          : areaKm2 as num?,
      meshMaxMm: meshMaxMm == const $CopyWithPlaceholder()
          ? _value.meshMaxMm
          // ignore: cast_nullable_to_non_nullable
          : meshMaxMm as num?,
      flashRate: flashRate == const $CopyWithPlaceholder()
          ? _value.flashRate
          // ignore: cast_nullable_to_non_nullable
          : flashRate as num?,
    );
  }
}

extension $StormTrackPayloadCellCopyWith on StormTrackPayloadCell {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStormTrackPayloadCell.copyWith(...)` or `instanceOfStormTrackPayloadCell.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StormTrackPayloadCellCWProxy get copyWith =>
      _$StormTrackPayloadCellCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StormTrackPayloadCell _$StormTrackPayloadCellFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StormTrackPayloadCell', json, ($checkedConvert) {
  final val = StormTrackPayloadCell(
    maxDbz: $checkedConvert('maxDbz', (v) => v as num?),
    areaKm2: $checkedConvert('areaKm2', (v) => v as num?),
    meshMaxMm: $checkedConvert('meshMaxMm', (v) => v as num?),
    flashRate: $checkedConvert('flashRate', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$StormTrackPayloadCellToJson(
  StormTrackPayloadCell instance,
) => <String, dynamic>{
  'maxDbz': ?instance.maxDbz,
  'areaKm2': ?instance.areaKm2,
  'meshMaxMm': ?instance.meshMaxMm,
  'flashRate': ?instance.flashRate,
};
