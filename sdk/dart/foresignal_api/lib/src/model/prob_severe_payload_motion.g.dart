// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'prob_severe_payload_motion.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProbSeverePayloadMotionCWProxy {
  ProbSeverePayloadMotion speedKmh(num speedKmh);

  ProbSeverePayloadMotion bearingDeg(int bearingDeg);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProbSeverePayloadMotion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProbSeverePayloadMotion(...).copyWith(id: 12, name: "My name")
  /// ```
  ProbSeverePayloadMotion call({num speedKmh, int bearingDeg});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProbSeverePayloadMotion.copyWith(...)` or call `instanceOfProbSeverePayloadMotion.copyWith.fieldName(value)` for a single field.
class _$ProbSeverePayloadMotionCWProxyImpl
    implements _$ProbSeverePayloadMotionCWProxy {
  const _$ProbSeverePayloadMotionCWProxyImpl(this._value);

  final ProbSeverePayloadMotion _value;

  @override
  ProbSeverePayloadMotion speedKmh(num speedKmh) => call(speedKmh: speedKmh);

  @override
  ProbSeverePayloadMotion bearingDeg(int bearingDeg) =>
      call(bearingDeg: bearingDeg);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProbSeverePayloadMotion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProbSeverePayloadMotion(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProbSeverePayloadMotion call({
    Object? speedKmh = const $CopyWithPlaceholder(),
    Object? bearingDeg = const $CopyWithPlaceholder(),
  }) {
    return ProbSeverePayloadMotion(
      speedKmh: speedKmh == const $CopyWithPlaceholder() || speedKmh == null
          ? _value.speedKmh
          // ignore: cast_nullable_to_non_nullable
          : speedKmh as num,
      bearingDeg:
          bearingDeg == const $CopyWithPlaceholder() || bearingDeg == null
          ? _value.bearingDeg
          // ignore: cast_nullable_to_non_nullable
          : bearingDeg as int,
    );
  }
}

extension $ProbSeverePayloadMotionCopyWith on ProbSeverePayloadMotion {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProbSeverePayloadMotion.copyWith(...)` or `instanceOfProbSeverePayloadMotion.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProbSeverePayloadMotionCWProxy get copyWith =>
      _$ProbSeverePayloadMotionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProbSeverePayloadMotion _$ProbSeverePayloadMotionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProbSeverePayloadMotion', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['speedKmh', 'bearingDeg']);
  final val = ProbSeverePayloadMotion(
    speedKmh: $checkedConvert('speedKmh', (v) => v as num),
    bearingDeg: $checkedConvert('bearingDeg', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$ProbSeverePayloadMotionToJson(
  ProbSeverePayloadMotion instance,
) => <String, dynamic>{
  'speedKmh': instance.speedKmh,
  'bearingDeg': instance.bearingDeg,
};
