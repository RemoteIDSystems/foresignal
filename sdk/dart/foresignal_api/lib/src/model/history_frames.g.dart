// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'history_frames.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HistoryFramesCWProxy {
  HistoryFrames asOf(DateTime asOf);

  HistoryFrames step(String step);

  HistoryFrames watermarkSeq(int watermarkSeq);

  HistoryFrames horizon(DateTime? horizon);

  HistoryFrames approximate(bool? approximate);

  HistoryFrames keyframe(List<FrameItem> keyframe);

  HistoryFrames frames(List<HistoryFrame> frames);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HistoryFrames(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HistoryFrames(...).copyWith(id: 12, name: "My name")
  /// ```
  HistoryFrames call({
    DateTime asOf,
    String step,
    int watermarkSeq,
    DateTime? horizon,
    bool? approximate,
    List<FrameItem> keyframe,
    List<HistoryFrame> frames,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHistoryFrames.copyWith(...)` or call `instanceOfHistoryFrames.copyWith.fieldName(value)` for a single field.
class _$HistoryFramesCWProxyImpl implements _$HistoryFramesCWProxy {
  const _$HistoryFramesCWProxyImpl(this._value);

  final HistoryFrames _value;

  @override
  HistoryFrames asOf(DateTime asOf) => call(asOf: asOf);

  @override
  HistoryFrames step(String step) => call(step: step);

  @override
  HistoryFrames watermarkSeq(int watermarkSeq) =>
      call(watermarkSeq: watermarkSeq);

  @override
  HistoryFrames horizon(DateTime? horizon) => call(horizon: horizon);

  @override
  HistoryFrames approximate(bool? approximate) =>
      call(approximate: approximate);

  @override
  HistoryFrames keyframe(List<FrameItem> keyframe) => call(keyframe: keyframe);

  @override
  HistoryFrames frames(List<HistoryFrame> frames) => call(frames: frames);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HistoryFrames(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HistoryFrames(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HistoryFrames call({
    Object? asOf = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? watermarkSeq = const $CopyWithPlaceholder(),
    Object? horizon = const $CopyWithPlaceholder(),
    Object? approximate = const $CopyWithPlaceholder(),
    Object? keyframe = const $CopyWithPlaceholder(),
    Object? frames = const $CopyWithPlaceholder(),
  }) {
    return HistoryFrames(
      asOf: asOf == const $CopyWithPlaceholder() || asOf == null
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime,
      step: step == const $CopyWithPlaceholder() || step == null
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as String,
      watermarkSeq:
          watermarkSeq == const $CopyWithPlaceholder() || watermarkSeq == null
          ? _value.watermarkSeq
          // ignore: cast_nullable_to_non_nullable
          : watermarkSeq as int,
      horizon: horizon == const $CopyWithPlaceholder()
          ? _value.horizon
          // ignore: cast_nullable_to_non_nullable
          : horizon as DateTime?,
      approximate: approximate == const $CopyWithPlaceholder()
          ? _value.approximate
          // ignore: cast_nullable_to_non_nullable
          : approximate as bool?,
      keyframe: keyframe == const $CopyWithPlaceholder() || keyframe == null
          ? _value.keyframe
          // ignore: cast_nullable_to_non_nullable
          : keyframe as List<FrameItem>,
      frames: frames == const $CopyWithPlaceholder() || frames == null
          ? _value.frames
          // ignore: cast_nullable_to_non_nullable
          : frames as List<HistoryFrame>,
    );
  }
}

extension $HistoryFramesCopyWith on HistoryFrames {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHistoryFrames.copyWith(...)` or `instanceOfHistoryFrames.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HistoryFramesCWProxy get copyWith => _$HistoryFramesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HistoryFrames _$HistoryFramesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HistoryFrames', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['asOf', 'step', 'watermarkSeq', 'keyframe', 'frames'],
  );
  final val = HistoryFrames(
    asOf: $checkedConvert('asOf', (v) => DateTime.parse(v as String)),
    step: $checkedConvert('step', (v) => v as String),
    watermarkSeq: $checkedConvert('watermarkSeq', (v) => (v as num).toInt()),
    horizon: $checkedConvert(
      'horizon',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    approximate: $checkedConvert('approximate', (v) => v as bool?),
    keyframe: $checkedConvert(
      'keyframe',
      (v) => (v as List<dynamic>)
          .map((e) => FrameItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    frames: $checkedConvert(
      'frames',
      (v) => (v as List<dynamic>)
          .map((e) => HistoryFrame.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$HistoryFramesToJson(HistoryFrames instance) =>
    <String, dynamic>{
      'asOf': instance.asOf.toIso8601String(),
      'step': instance.step,
      'watermarkSeq': instance.watermarkSeq,
      'horizon': ?instance.horizon?.toIso8601String(),
      'approximate': ?instance.approximate,
      'keyframe': instance.keyframe.map((e) => e.toJson()).toList(),
      'frames': instance.frames.map((e) => e.toJson()).toList(),
    };
