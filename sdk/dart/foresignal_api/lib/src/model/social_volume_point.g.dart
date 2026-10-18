// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_volume_point.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialVolumePointCWProxy {
  SocialVolumePoint t(DateTime t);

  SocialVolumePoint observed(num observed);

  SocialVolumePoint baseline(num? baseline);

  SocialVolumePoint mad(num? mad);

  SocialVolumePoint velocityRatio(num? velocityRatio);

  SocialVolumePoint zRobust(num? zRobust);

  SocialVolumePoint complete(bool complete);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVolumePoint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVolumePoint(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialVolumePoint call({
    DateTime t,
    num observed,
    num? baseline,
    num? mad,
    num? velocityRatio,
    num? zRobust,
    bool complete,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialVolumePoint.copyWith(...)` or call `instanceOfSocialVolumePoint.copyWith.fieldName(value)` for a single field.
class _$SocialVolumePointCWProxyImpl implements _$SocialVolumePointCWProxy {
  const _$SocialVolumePointCWProxyImpl(this._value);

  final SocialVolumePoint _value;

  @override
  SocialVolumePoint t(DateTime t) => call(t: t);

  @override
  SocialVolumePoint observed(num observed) => call(observed: observed);

  @override
  SocialVolumePoint baseline(num? baseline) => call(baseline: baseline);

  @override
  SocialVolumePoint mad(num? mad) => call(mad: mad);

  @override
  SocialVolumePoint velocityRatio(num? velocityRatio) =>
      call(velocityRatio: velocityRatio);

  @override
  SocialVolumePoint zRobust(num? zRobust) => call(zRobust: zRobust);

  @override
  SocialVolumePoint complete(bool complete) => call(complete: complete);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialVolumePoint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialVolumePoint(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialVolumePoint call({
    Object? t = const $CopyWithPlaceholder(),
    Object? observed = const $CopyWithPlaceholder(),
    Object? baseline = const $CopyWithPlaceholder(),
    Object? mad = const $CopyWithPlaceholder(),
    Object? velocityRatio = const $CopyWithPlaceholder(),
    Object? zRobust = const $CopyWithPlaceholder(),
    Object? complete = const $CopyWithPlaceholder(),
  }) {
    return SocialVolumePoint(
      t: t == const $CopyWithPlaceholder() || t == null
          ? _value.t
          // ignore: cast_nullable_to_non_nullable
          : t as DateTime,
      observed: observed == const $CopyWithPlaceholder() || observed == null
          ? _value.observed
          // ignore: cast_nullable_to_non_nullable
          : observed as num,
      baseline: baseline == const $CopyWithPlaceholder()
          ? _value.baseline
          // ignore: cast_nullable_to_non_nullable
          : baseline as num?,
      mad: mad == const $CopyWithPlaceholder()
          ? _value.mad
          // ignore: cast_nullable_to_non_nullable
          : mad as num?,
      velocityRatio: velocityRatio == const $CopyWithPlaceholder()
          ? _value.velocityRatio
          // ignore: cast_nullable_to_non_nullable
          : velocityRatio as num?,
      zRobust: zRobust == const $CopyWithPlaceholder()
          ? _value.zRobust
          // ignore: cast_nullable_to_non_nullable
          : zRobust as num?,
      complete: complete == const $CopyWithPlaceholder() || complete == null
          ? _value.complete
          // ignore: cast_nullable_to_non_nullable
          : complete as bool,
    );
  }
}

extension $SocialVolumePointCopyWith on SocialVolumePoint {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialVolumePoint.copyWith(...)` or `instanceOfSocialVolumePoint.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialVolumePointCWProxy get copyWith =>
      _$SocialVolumePointCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialVolumePoint _$SocialVolumePointFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialVolumePoint', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['t', 'observed', 'complete']);
      final val = SocialVolumePoint(
        t: $checkedConvert('t', (v) => DateTime.parse(v as String)),
        observed: $checkedConvert('observed', (v) => v as num),
        baseline: $checkedConvert('baseline', (v) => v as num?),
        mad: $checkedConvert('mad', (v) => v as num?),
        velocityRatio: $checkedConvert('velocityRatio', (v) => v as num?),
        zRobust: $checkedConvert('zRobust', (v) => v as num?),
        complete: $checkedConvert('complete', (v) => v as bool),
      );
      return val;
    });

Map<String, dynamic> _$SocialVolumePointToJson(SocialVolumePoint instance) =>
    <String, dynamic>{
      't': instance.t.toIso8601String(),
      'observed': instance.observed,
      'baseline': ?instance.baseline,
      'mad': ?instance.mad,
      'velocityRatio': ?instance.velocityRatio,
      'zRobust': ?instance.zRobust,
      'complete': instance.complete,
    };
