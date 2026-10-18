// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'raster_frame_observation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RasterFrameObservationCWProxy {
  RasterFrameObservation productId(String productId);

  RasterFrameObservation frameKey(String frameKey);

  RasterFrameObservation time(DateTime time);

  RasterFrameObservation timeKey(String timeKey);

  RasterFrameObservation tiles(String tiles);

  RasterFrameObservation bounds(List<num>? bounds);

  RasterFrameObservation stats(FrameStats? stats);

  RasterFrameObservation expiresAt(DateTime? expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RasterFrameObservation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RasterFrameObservation(...).copyWith(id: 12, name: "My name")
  /// ```
  RasterFrameObservation call({
    String productId,
    String frameKey,
    DateTime time,
    String timeKey,
    String tiles,
    List<num>? bounds,
    FrameStats? stats,
    DateTime? expiresAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRasterFrameObservation.copyWith(...)` or call `instanceOfRasterFrameObservation.copyWith.fieldName(value)` for a single field.
class _$RasterFrameObservationCWProxyImpl
    implements _$RasterFrameObservationCWProxy {
  const _$RasterFrameObservationCWProxyImpl(this._value);

  final RasterFrameObservation _value;

  @override
  RasterFrameObservation productId(String productId) =>
      call(productId: productId);

  @override
  RasterFrameObservation frameKey(String frameKey) => call(frameKey: frameKey);

  @override
  RasterFrameObservation time(DateTime time) => call(time: time);

  @override
  RasterFrameObservation timeKey(String timeKey) => call(timeKey: timeKey);

  @override
  RasterFrameObservation tiles(String tiles) => call(tiles: tiles);

  @override
  RasterFrameObservation bounds(List<num>? bounds) => call(bounds: bounds);

  @override
  RasterFrameObservation stats(FrameStats? stats) => call(stats: stats);

  @override
  RasterFrameObservation expiresAt(DateTime? expiresAt) =>
      call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RasterFrameObservation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RasterFrameObservation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RasterFrameObservation call({
    Object? productId = const $CopyWithPlaceholder(),
    Object? frameKey = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? timeKey = const $CopyWithPlaceholder(),
    Object? tiles = const $CopyWithPlaceholder(),
    Object? bounds = const $CopyWithPlaceholder(),
    Object? stats = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return RasterFrameObservation(
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
    );
  }
}

extension $RasterFrameObservationCopyWith on RasterFrameObservation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRasterFrameObservation.copyWith(...)` or `instanceOfRasterFrameObservation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RasterFrameObservationCWProxy get copyWith =>
      _$RasterFrameObservationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RasterFrameObservation _$RasterFrameObservationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RasterFrameObservation', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['productId', 'frameKey', 'time', 'timeKey', 'tiles'],
  );
  final val = RasterFrameObservation(
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
      (v) => v == null ? null : FrameStats.fromJson(v as Map<String, dynamic>),
    ),
    expiresAt: $checkedConvert(
      'expiresAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$RasterFrameObservationToJson(
  RasterFrameObservation instance,
) => <String, dynamic>{
  'productId': instance.productId,
  'frameKey': instance.frameKey,
  'time': instance.time.toIso8601String(),
  'timeKey': instance.timeKey,
  'tiles': instance.tiles,
  'bounds': ?instance.bounds,
  'stats': ?instance.stats?.toJson(),
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
};
