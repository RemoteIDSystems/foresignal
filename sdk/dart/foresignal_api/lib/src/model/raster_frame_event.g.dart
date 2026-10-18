// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'raster_frame_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RasterFrameEventCWProxy {
  RasterFrameEvent productId(String productId);

  RasterFrameEvent frameKey(String frameKey);

  RasterFrameEvent time(DateTime time);

  RasterFrameEvent timeKey(String timeKey);

  RasterFrameEvent tiles(String tiles);

  RasterFrameEvent bounds(List<num>? bounds);

  RasterFrameEvent stats(FrameStats? stats);

  RasterFrameEvent expiresAt(DateTime? expiresAt);

  RasterFrameEvent run(String run);

  RasterFrameEvent steps(List<int> steps);

  RasterFrameEvent validTimes(List<DateTime> validTimes);

  RasterFrameEvent tilesTemplate(String tilesTemplate);

  RasterFrameEvent partial(bool? partial);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RasterFrameEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RasterFrameEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  RasterFrameEvent call({
    String productId,
    String frameKey,
    DateTime time,
    String timeKey,
    String tiles,
    List<num>? bounds,
    FrameStats? stats,
    DateTime? expiresAt,
    String run,
    List<int> steps,
    List<DateTime> validTimes,
    String tilesTemplate,
    bool? partial,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRasterFrameEvent.copyWith(...)` or call `instanceOfRasterFrameEvent.copyWith.fieldName(value)` for a single field.
class _$RasterFrameEventCWProxyImpl implements _$RasterFrameEventCWProxy {
  const _$RasterFrameEventCWProxyImpl(this._value);

  final RasterFrameEvent _value;

  @override
  RasterFrameEvent productId(String productId) => call(productId: productId);

  @override
  RasterFrameEvent frameKey(String frameKey) => call(frameKey: frameKey);

  @override
  RasterFrameEvent time(DateTime time) => call(time: time);

  @override
  RasterFrameEvent timeKey(String timeKey) => call(timeKey: timeKey);

  @override
  RasterFrameEvent tiles(String tiles) => call(tiles: tiles);

  @override
  RasterFrameEvent bounds(List<num>? bounds) => call(bounds: bounds);

  @override
  RasterFrameEvent stats(FrameStats? stats) => call(stats: stats);

  @override
  RasterFrameEvent expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  @override
  RasterFrameEvent run(String run) => call(run: run);

  @override
  RasterFrameEvent steps(List<int> steps) => call(steps: steps);

  @override
  RasterFrameEvent validTimes(List<DateTime> validTimes) =>
      call(validTimes: validTimes);

  @override
  RasterFrameEvent tilesTemplate(String tilesTemplate) =>
      call(tilesTemplate: tilesTemplate);

  @override
  RasterFrameEvent partial(bool? partial) => call(partial: partial);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RasterFrameEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RasterFrameEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RasterFrameEvent call({
    Object? productId = const $CopyWithPlaceholder(),
    Object? frameKey = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? timeKey = const $CopyWithPlaceholder(),
    Object? tiles = const $CopyWithPlaceholder(),
    Object? bounds = const $CopyWithPlaceholder(),
    Object? stats = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? steps = const $CopyWithPlaceholder(),
    Object? validTimes = const $CopyWithPlaceholder(),
    Object? tilesTemplate = const $CopyWithPlaceholder(),
    Object? partial = const $CopyWithPlaceholder(),
  }) {
    return RasterFrameEvent(
      productId: productId == const $CopyWithPlaceholder() || productId == null
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String,
      frameKey: frameKey == const $CopyWithPlaceholder() || frameKey == null
          ? _value.frameKey
          // ignore: cast_nullable_to_non_nullable
          : frameKey as String,
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime,
      timeKey: timeKey == const $CopyWithPlaceholder() || timeKey == null
          ? _value.timeKey
          // ignore: cast_nullable_to_non_nullable
          : timeKey as String,
      tiles: tiles == const $CopyWithPlaceholder() || tiles == null
          ? _value.tiles
          // ignore: cast_nullable_to_non_nullable
          : tiles as String,
      bounds: bounds == const $CopyWithPlaceholder()
          ? _value.bounds
          // ignore: cast_nullable_to_non_nullable
          : bounds as List<num>?,
      stats: stats == const $CopyWithPlaceholder()
          ? _value.stats
          // ignore: cast_nullable_to_non_nullable
          : stats as FrameStats?,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      run: run == const $CopyWithPlaceholder() || run == null
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String,
      steps: steps == const $CopyWithPlaceholder() || steps == null
          ? _value.steps
          // ignore: cast_nullable_to_non_nullable
          : steps as List<int>,
      validTimes:
          validTimes == const $CopyWithPlaceholder() || validTimes == null
          ? _value.validTimes
          // ignore: cast_nullable_to_non_nullable
          : validTimes as List<DateTime>,
      tilesTemplate:
          tilesTemplate == const $CopyWithPlaceholder() || tilesTemplate == null
          ? _value.tilesTemplate
          // ignore: cast_nullable_to_non_nullable
          : tilesTemplate as String,
      partial: partial == const $CopyWithPlaceholder()
          ? _value.partial
          // ignore: cast_nullable_to_non_nullable
          : partial as bool?,
    );
  }
}

extension $RasterFrameEventCopyWith on RasterFrameEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRasterFrameEvent.copyWith(...)` or `instanceOfRasterFrameEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RasterFrameEventCWProxy get copyWith => _$RasterFrameEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RasterFrameEvent _$RasterFrameEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RasterFrameEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'productId',
          'frameKey',
          'time',
          'timeKey',
          'tiles',
          'run',
          'steps',
          'validTimes',
          'tilesTemplate',
        ],
      );
      final val = RasterFrameEvent(
        productId: $checkedConvert('productId', (v) => v as String),
        frameKey: $checkedConvert('frameKey', (v) => v as String),
        time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
        timeKey: $checkedConvert('timeKey', (v) => v as String),
        tiles: $checkedConvert('tiles', (v) => v as String),
        bounds: $checkedConvert(
          'bounds',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        stats: $checkedConvert(
          'stats',
          (v) =>
              v == null ? null : FrameStats.fromJson(v as Map<String, dynamic>),
        ),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        run: $checkedConvert('run', (v) => v as String),
        steps: $checkedConvert(
          'steps',
          (v) => (v as List<dynamic>).map((e) => (e as num).toInt()).toList(),
        ),
        validTimes: $checkedConvert(
          'validTimes',
          (v) => (v as List<dynamic>)
              .map((e) => DateTime.parse(e as String))
              .toList(),
        ),
        tilesTemplate: $checkedConvert('tilesTemplate', (v) => v as String),
        partial: $checkedConvert('partial', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$RasterFrameEventToJson(
  RasterFrameEvent instance,
) => <String, dynamic>{
  'productId': instance.productId,
  'frameKey': instance.frameKey,
  'time': instance.time.toIso8601String(),
  'timeKey': instance.timeKey,
  'tiles': instance.tiles,
  'bounds': ?instance.bounds,
  'stats': ?instance.stats?.toJson(),
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
  'run': instance.run,
  'steps': instance.steps,
  'validTimes': instance.validTimes.map((e) => e.toIso8601String()).toList(),
  'tilesTemplate': instance.tilesTemplate,
  'partial': ?instance.partial,
};
