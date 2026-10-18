// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'radar_cell_payload_motion.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RadarCellPayloadMotionCWProxy {
  RadarCellPayloadMotion speedKmh(num? speedKmh);

  RadarCellPayloadMotion bearingDeg(int? bearingDeg);

  RadarCellPayloadMotion method(RadarCellPayloadMotionMethodEnum method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RadarCellPayloadMotion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RadarCellPayloadMotion(...).copyWith(id: 12, name: "My name")
  /// ```
  RadarCellPayloadMotion call({
    num? speedKmh,
    int? bearingDeg,
    RadarCellPayloadMotionMethodEnum method,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRadarCellPayloadMotion.copyWith(...)` or call `instanceOfRadarCellPayloadMotion.copyWith.fieldName(value)` for a single field.
class _$RadarCellPayloadMotionCWProxyImpl
    implements _$RadarCellPayloadMotionCWProxy {
  const _$RadarCellPayloadMotionCWProxyImpl(this._value);

  final RadarCellPayloadMotion _value;

  @override
  RadarCellPayloadMotion speedKmh(num? speedKmh) => call(speedKmh: speedKmh);

  @override
  RadarCellPayloadMotion bearingDeg(int? bearingDeg) =>
      call(bearingDeg: bearingDeg);

  @override
  RadarCellPayloadMotion method(RadarCellPayloadMotionMethodEnum method) =>
      call(method: method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RadarCellPayloadMotion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RadarCellPayloadMotion(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RadarCellPayloadMotion call({
    Object? speedKmh = const $CopyWithPlaceholder(),
    Object? bearingDeg = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
  }) {
    return RadarCellPayloadMotion(
      speedKmh: speedKmh == const $CopyWithPlaceholder()
          ? _value.speedKmh
          // ignore: cast_nullable_to_non_nullable
          : speedKmh as num?,
      bearingDeg: bearingDeg == const $CopyWithPlaceholder()
          ? _value.bearingDeg
          // ignore: cast_nullable_to_non_nullable
          : bearingDeg as int?,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as RadarCellPayloadMotionMethodEnum,
    );
  }
}

extension $RadarCellPayloadMotionCopyWith on RadarCellPayloadMotion {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRadarCellPayloadMotion.copyWith(...)` or `instanceOfRadarCellPayloadMotion.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RadarCellPayloadMotionCWProxy get copyWith =>
      _$RadarCellPayloadMotionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RadarCellPayloadMotion _$RadarCellPayloadMotionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RadarCellPayloadMotion', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['method']);
  final val = RadarCellPayloadMotion(
    speedKmh: $checkedConvert('speedKmh', (v) => v as num?),
    bearingDeg: $checkedConvert('bearingDeg', (v) => (v as num?)?.toInt()),
    method: $checkedConvert(
      'method',
      (v) => $enumDecode(_$RadarCellPayloadMotionMethodEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$RadarCellPayloadMotionToJson(
  RadarCellPayloadMotion instance,
) => <String, dynamic>{
  'speedKmh': ?instance.speedKmh,
  'bearingDeg': ?instance.bearingDeg,
  'method': _$RadarCellPayloadMotionMethodEnumEnumMap[instance.method]!,
};

const _$RadarCellPayloadMotionMethodEnumEnumMap = {
  RadarCellPayloadMotionMethodEnum.centroidV1: 'centroid-v1',
};
