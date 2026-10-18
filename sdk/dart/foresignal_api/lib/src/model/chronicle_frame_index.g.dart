// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_frame_index.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleFrameIndexCWProxy {
  ChronicleFrameIndex chronicleId(String chronicleId);

  ChronicleFrameIndex from(DateTime from);

  ChronicleFrameIndex to(DateTime to);

  ChronicleFrameIndex step(String step);

  ChronicleFrameIndex layers(List<String>? layers);

  ChronicleFrameIndex frames(List<ChronicleFrameIndexFramesInner> frames);

  ChronicleFrameIndex truncated(bool? truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleFrameIndex(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleFrameIndex(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleFrameIndex call({
    String chronicleId,
    DateTime from,
    DateTime to,
    String step,
    List<String>? layers,
    List<ChronicleFrameIndexFramesInner> frames,
    bool? truncated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleFrameIndex.copyWith(...)` or call `instanceOfChronicleFrameIndex.copyWith.fieldName(value)` for a single field.
class _$ChronicleFrameIndexCWProxyImpl implements _$ChronicleFrameIndexCWProxy {
  const _$ChronicleFrameIndexCWProxyImpl(this._value);

  final ChronicleFrameIndex _value;

  @override
  ChronicleFrameIndex chronicleId(String chronicleId) =>
      call(chronicleId: chronicleId);

  @override
  ChronicleFrameIndex from(DateTime from) => call(from: from);

  @override
  ChronicleFrameIndex to(DateTime to) => call(to: to);

  @override
  ChronicleFrameIndex step(String step) => call(step: step);

  @override
  ChronicleFrameIndex layers(List<String>? layers) => call(layers: layers);

  @override
  ChronicleFrameIndex frames(List<ChronicleFrameIndexFramesInner> frames) =>
      call(frames: frames);

  @override
  ChronicleFrameIndex truncated(bool? truncated) => call(truncated: truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleFrameIndex(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleFrameIndex(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleFrameIndex call({
    Object? chronicleId = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? layers = const $CopyWithPlaceholder(),
    Object? frames = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
  }) {
    return ChronicleFrameIndex(
      chronicleId:
          chronicleId == const $CopyWithPlaceholder() || chronicleId == null
          ? _value.chronicleId
          // ignore: cast_nullable_to_non_nullable
          : chronicleId as String,
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      step: step == const $CopyWithPlaceholder() || step == null
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as String,
      layers: layers == const $CopyWithPlaceholder()
          ? _value.layers
          // ignore: cast_nullable_to_non_nullable
          : layers as List<String>?,
      frames: frames == const $CopyWithPlaceholder() || frames == null
          ? _value.frames
          // ignore: cast_nullable_to_non_nullable
          : frames as List<ChronicleFrameIndexFramesInner>,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
    );
  }
}

extension $ChronicleFrameIndexCopyWith on ChronicleFrameIndex {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleFrameIndex.copyWith(...)` or `instanceOfChronicleFrameIndex.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleFrameIndexCWProxy get copyWith =>
      _$ChronicleFrameIndexCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleFrameIndex _$ChronicleFrameIndexFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChronicleFrameIndex', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['chronicleId', 'from', 'to', 'step', 'frames'],
      );
      final val = ChronicleFrameIndex(
        chronicleId: $checkedConvert('chronicleId', (v) => v as String),
        from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
        to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
        step: $checkedConvert('step', (v) => v as String),
        layers: $checkedConvert(
          'layers',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        frames: $checkedConvert(
          'frames',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ChronicleFrameIndexFramesInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        truncated: $checkedConvert('truncated', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleFrameIndexToJson(
  ChronicleFrameIndex instance,
) => <String, dynamic>{
  'chronicleId': instance.chronicleId,
  'from': instance.from.toIso8601String(),
  'to': instance.to.toIso8601String(),
  'step': instance.step,
  'layers': ?instance.layers,
  'frames': instance.frames.map((e) => e.toJson()).toList(),
  'truncated': ?instance.truncated,
};
