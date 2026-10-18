// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'storm_track_payload_motion.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StormTrackPayloadMotionCWProxy {
  StormTrackPayloadMotion speedKmh(num speedKmh);

  StormTrackPayloadMotion bearingDeg(num bearingDeg);

  StormTrackPayloadMotion method(StormTrackPayloadMotionMethodEnum method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayloadMotion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayloadMotion(...).copyWith(id: 12, name: "My name")
  /// ```
  StormTrackPayloadMotion call({
    num speedKmh,
    num bearingDeg,
    StormTrackPayloadMotionMethodEnum method,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStormTrackPayloadMotion.copyWith(...)` or call `instanceOfStormTrackPayloadMotion.copyWith.fieldName(value)` for a single field.
class _$StormTrackPayloadMotionCWProxyImpl
    implements _$StormTrackPayloadMotionCWProxy {
  const _$StormTrackPayloadMotionCWProxyImpl(this._value);

  final StormTrackPayloadMotion _value;

  @override
  StormTrackPayloadMotion speedKmh(num speedKmh) => call(speedKmh: speedKmh);

  @override
  StormTrackPayloadMotion bearingDeg(num bearingDeg) =>
      call(bearingDeg: bearingDeg);

  @override
  StormTrackPayloadMotion method(StormTrackPayloadMotionMethodEnum method) =>
      call(method: method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StormTrackPayloadMotion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StormTrackPayloadMotion(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StormTrackPayloadMotion call({
    Object? speedKmh = const $CopyWithPlaceholder(),
    Object? bearingDeg = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
  }) {
    return StormTrackPayloadMotion(
      speedKmh: speedKmh == const $CopyWithPlaceholder() || speedKmh == null
          ? _value.speedKmh
          // ignore: cast_nullable_to_non_nullable
          : speedKmh as num,
      bearingDeg:
          bearingDeg == const $CopyWithPlaceholder() || bearingDeg == null
          ? _value.bearingDeg
          // ignore: cast_nullable_to_non_nullable
          : bearingDeg as num,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as StormTrackPayloadMotionMethodEnum,
    );
  }
}

extension $StormTrackPayloadMotionCopyWith on StormTrackPayloadMotion {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStormTrackPayloadMotion.copyWith(...)` or `instanceOfStormTrackPayloadMotion.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StormTrackPayloadMotionCWProxy get copyWith =>
      _$StormTrackPayloadMotionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StormTrackPayloadMotion _$StormTrackPayloadMotionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StormTrackPayloadMotion', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['speedKmh', 'bearingDeg', 'method']);
  final val = StormTrackPayloadMotion(
    speedKmh: $checkedConvert('speedKmh', (v) => v as num),
    bearingDeg: $checkedConvert('bearingDeg', (v) => v as num),
    method: $checkedConvert(
      'method',
      (v) => $enumDecode(_$StormTrackPayloadMotionMethodEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$StormTrackPayloadMotionToJson(
  StormTrackPayloadMotion instance,
) => <String, dynamic>{
  'speedKmh': instance.speedKmh,
  'bearingDeg': instance.bearingDeg,
  'method': _$StormTrackPayloadMotionMethodEnumEnumMap[instance.method]!,
};

const _$StormTrackPayloadMotionMethodEnumEnumMap = {
  StormTrackPayloadMotionMethodEnum.titanLiteV1: 'titan-lite-v1',
  StormTrackPayloadMotionMethodEnum.centroidV1: 'centroid-v1',
};
