// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'raster_frame.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RasterFrameCWProxy {
  RasterFrame productId(String productId);

  RasterFrame time(DateTime time);

  RasterFrame timeKey(String timeKey);

  RasterFrame frameKey(String frameKey);

  RasterFrame status(RasterFrameStatusEnum status);

  RasterFrame expiresAt(DateTime? expiresAt);

  RasterFrame run(String? run);

  RasterFrame step(int? step);

  RasterFrame obsStart(DateTime? obsStart);

  RasterFrame obsEnd(DateTime? obsEnd);

  RasterFrame tiles(String tiles);

  RasterFrame tiles2x(String? tiles2x);

  RasterFrame image(String? image);

  RasterFrame bounds(List<num>? bounds);

  RasterFrame stats(FrameStats? stats);

  RasterFrame thinned(bool? thinned);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RasterFrame(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RasterFrame(...).copyWith(id: 12, name: "My name")
  /// ```
  RasterFrame call({
    String productId,
    DateTime time,
    String timeKey,
    String frameKey,
    RasterFrameStatusEnum status,
    DateTime? expiresAt,
    String? run,
    int? step,
    DateTime? obsStart,
    DateTime? obsEnd,
    String tiles,
    String? tiles2x,
    String? image,
    List<num>? bounds,
    FrameStats? stats,
    bool? thinned,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRasterFrame.copyWith(...)` or call `instanceOfRasterFrame.copyWith.fieldName(value)` for a single field.
class _$RasterFrameCWProxyImpl implements _$RasterFrameCWProxy {
  const _$RasterFrameCWProxyImpl(this._value);

  final RasterFrame _value;

  @override
  RasterFrame productId(String productId) => call(productId: productId);

  @override
  RasterFrame time(DateTime time) => call(time: time);

  @override
  RasterFrame timeKey(String timeKey) => call(timeKey: timeKey);

  @override
  RasterFrame frameKey(String frameKey) => call(frameKey: frameKey);

  @override
  RasterFrame status(RasterFrameStatusEnum status) => call(status: status);

  @override
  RasterFrame expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  @override
  RasterFrame run(String? run) => call(run: run);

  @override
  RasterFrame step(int? step) => call(step: step);

  @override
  RasterFrame obsStart(DateTime? obsStart) => call(obsStart: obsStart);

  @override
  RasterFrame obsEnd(DateTime? obsEnd) => call(obsEnd: obsEnd);

  @override
  RasterFrame tiles(String tiles) => call(tiles: tiles);

  @override
  RasterFrame tiles2x(String? tiles2x) => call(tiles2x: tiles2x);

  @override
  RasterFrame image(String? image) => call(image: image);

  @override
  RasterFrame bounds(List<num>? bounds) => call(bounds: bounds);

  @override
  RasterFrame stats(FrameStats? stats) => call(stats: stats);

  @override
  RasterFrame thinned(bool? thinned) => call(thinned: thinned);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RasterFrame(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RasterFrame(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RasterFrame call({
    Object? productId = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? timeKey = const $CopyWithPlaceholder(),
    Object? frameKey = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? obsStart = const $CopyWithPlaceholder(),
    Object? obsEnd = const $CopyWithPlaceholder(),
    Object? tiles = const $CopyWithPlaceholder(),
    Object? tiles2x = const $CopyWithPlaceholder(),
    Object? image = const $CopyWithPlaceholder(),
    Object? bounds = const $CopyWithPlaceholder(),
    Object? stats = const $CopyWithPlaceholder(),
    Object? thinned = const $CopyWithPlaceholder(),
  }) {
    return RasterFrame(
      productId: productId == const $CopyWithPlaceholder() || productId == null
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String,
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime,
      timeKey: timeKey == const $CopyWithPlaceholder() || timeKey == null
          ? _value.timeKey
          // ignore: cast_nullable_to_non_nullable
          : timeKey as String,
      frameKey: frameKey == const $CopyWithPlaceholder() || frameKey == null
          ? _value.frameKey
          // ignore: cast_nullable_to_non_nullable
          : frameKey as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as RasterFrameStatusEnum,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      step: step == const $CopyWithPlaceholder()
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as int?,
      obsStart: obsStart == const $CopyWithPlaceholder()
          ? _value.obsStart
          // ignore: cast_nullable_to_non_nullable
          : obsStart as DateTime?,
      obsEnd: obsEnd == const $CopyWithPlaceholder()
          ? _value.obsEnd
          // ignore: cast_nullable_to_non_nullable
          : obsEnd as DateTime?,
      tiles: tiles == const $CopyWithPlaceholder() || tiles == null
          ? _value.tiles
          // ignore: cast_nullable_to_non_nullable
          : tiles as String,
      tiles2x: tiles2x == const $CopyWithPlaceholder()
          ? _value.tiles2x
          // ignore: cast_nullable_to_non_nullable
          : tiles2x as String?,
      image: image == const $CopyWithPlaceholder()
          ? _value.image
          // ignore: cast_nullable_to_non_nullable
          : image as String?,
      bounds: bounds == const $CopyWithPlaceholder()
          ? _value.bounds
          // ignore: cast_nullable_to_non_nullable
          : bounds as List<num>?,
      stats: stats == const $CopyWithPlaceholder()
          ? _value.stats
          // ignore: cast_nullable_to_non_nullable
          : stats as FrameStats?,
      thinned: thinned == const $CopyWithPlaceholder()
          ? _value.thinned
          // ignore: cast_nullable_to_non_nullable
          : thinned as bool?,
    );
  }
}

extension $RasterFrameCopyWith on RasterFrame {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRasterFrame.copyWith(...)` or `instanceOfRasterFrame.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RasterFrameCWProxy get copyWith => _$RasterFrameCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RasterFrame _$RasterFrameFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RasterFrame', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'productId',
          'time',
          'timeKey',
          'frameKey',
          'status',
          'tiles',
        ],
      );
      final val = RasterFrame(
        productId: $checkedConvert('productId', (v) => v as String),
        time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
        timeKey: $checkedConvert('timeKey', (v) => v as String),
        frameKey: $checkedConvert('frameKey', (v) => v as String),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$RasterFrameStatusEnumEnumMap, v),
        ),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        run: $checkedConvert('run', (v) => v as String?),
        step: $checkedConvert('step', (v) => (v as num?)?.toInt()),
        obsStart: $checkedConvert(
          'obsStart',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        obsEnd: $checkedConvert(
          'obsEnd',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        tiles: $checkedConvert('tiles', (v) => v as String),
        tiles2x: $checkedConvert('tiles2x', (v) => v as String?),
        image: $checkedConvert('image', (v) => v as String?),
        bounds: $checkedConvert(
          'bounds',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        stats: $checkedConvert(
          'stats',
          (v) =>
              v == null ? null : FrameStats.fromJson(v as Map<String, dynamic>),
        ),
        thinned: $checkedConvert('thinned', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$RasterFrameToJson(RasterFrame instance) =>
    <String, dynamic>{
      'productId': instance.productId,
      'time': instance.time.toIso8601String(),
      'timeKey': instance.timeKey,
      'frameKey': instance.frameKey,
      'status': _$RasterFrameStatusEnumEnumMap[instance.status]!,
      'expiresAt': ?instance.expiresAt?.toIso8601String(),
      'run': ?instance.run,
      'step': ?instance.step,
      'obsStart': ?instance.obsStart?.toIso8601String(),
      'obsEnd': ?instance.obsEnd?.toIso8601String(),
      'tiles': instance.tiles,
      'tiles2x': ?instance.tiles2x,
      'image': ?instance.image,
      'bounds': ?instance.bounds,
      'stats': ?instance.stats?.toJson(),
      'thinned': ?instance.thinned,
    };

const _$RasterFrameStatusEnumEnumMap = {
  RasterFrameStatusEnum.partial: 'partial',
  RasterFrameStatusEnum.complete: 'complete',
};
