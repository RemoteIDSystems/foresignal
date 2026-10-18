// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'timeline_tick_frames_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TimelineTickFramesInnerCWProxy {
  TimelineTickFramesInner productId(String productId);

  TimelineTickFramesInner frameKey(String? frameKey);

  TimelineTickFramesInner time(DateTime time);

  TimelineTickFramesInner run(String? run);

  TimelineTickFramesInner step(int? step);

  TimelineTickFramesInner tiles(String tiles);

  TimelineTickFramesInner availability(
    TimelineTickFramesInnerAvailabilityEnum availability,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TimelineTickFramesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TimelineTickFramesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  TimelineTickFramesInner call({
    String productId,
    String? frameKey,
    DateTime time,
    String? run,
    int? step,
    String tiles,
    TimelineTickFramesInnerAvailabilityEnum availability,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTimelineTickFramesInner.copyWith(...)` or call `instanceOfTimelineTickFramesInner.copyWith.fieldName(value)` for a single field.
class _$TimelineTickFramesInnerCWProxyImpl
    implements _$TimelineTickFramesInnerCWProxy {
  const _$TimelineTickFramesInnerCWProxyImpl(this._value);

  final TimelineTickFramesInner _value;

  @override
  TimelineTickFramesInner productId(String productId) =>
      call(productId: productId);

  @override
  TimelineTickFramesInner frameKey(String? frameKey) =>
      call(frameKey: frameKey);

  @override
  TimelineTickFramesInner time(DateTime time) => call(time: time);

  @override
  TimelineTickFramesInner run(String? run) => call(run: run);

  @override
  TimelineTickFramesInner step(int? step) => call(step: step);

  @override
  TimelineTickFramesInner tiles(String tiles) => call(tiles: tiles);

  @override
  TimelineTickFramesInner availability(
    TimelineTickFramesInnerAvailabilityEnum availability,
  ) => call(availability: availability);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TimelineTickFramesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TimelineTickFramesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TimelineTickFramesInner call({
    Object? productId = const $CopyWithPlaceholder(),
    Object? frameKey = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
    Object? tiles = const $CopyWithPlaceholder(),
    Object? availability = const $CopyWithPlaceholder(),
  }) {
    return TimelineTickFramesInner(
      productId: productId == const $CopyWithPlaceholder() || productId == null
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String,
      frameKey: frameKey == const $CopyWithPlaceholder()
          ? _value.frameKey
          // ignore: cast_nullable_to_non_nullable
          : frameKey as String?,
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime,
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      step: step == const $CopyWithPlaceholder()
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as int?,
      tiles: tiles == const $CopyWithPlaceholder() || tiles == null
          ? _value.tiles
          // ignore: cast_nullable_to_non_nullable
          : tiles as String,
      availability:
          availability == const $CopyWithPlaceholder() || availability == null
          ? _value.availability
          // ignore: cast_nullable_to_non_nullable
          : availability as TimelineTickFramesInnerAvailabilityEnum,
    );
  }
}

extension $TimelineTickFramesInnerCopyWith on TimelineTickFramesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTimelineTickFramesInner.copyWith(...)` or `instanceOfTimelineTickFramesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TimelineTickFramesInnerCWProxy get copyWith =>
      _$TimelineTickFramesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TimelineTickFramesInner _$TimelineTickFramesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TimelineTickFramesInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['productId', 'time', 'tiles', 'availability'],
  );
  final val = TimelineTickFramesInner(
    productId: $checkedConvert('productId', (v) => v as String),
    frameKey: $checkedConvert('frameKey', (v) => v as String?),
    time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
    run: $checkedConvert('run', (v) => v as String?),
    step: $checkedConvert('step', (v) => (v as num?)?.toInt()),
    tiles: $checkedConvert('tiles', (v) => v as String),
    availability: $checkedConvert(
      'availability',
      (v) => $enumDecode(_$TimelineTickFramesInnerAvailabilityEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$TimelineTickFramesInnerToJson(
  TimelineTickFramesInner instance,
) => <String, dynamic>{
  'productId': instance.productId,
  'frameKey': ?instance.frameKey,
  'time': instance.time.toIso8601String(),
  'run': ?instance.run,
  'step': ?instance.step,
  'tiles': instance.tiles,
  'availability':
      _$TimelineTickFramesInnerAvailabilityEnumEnumMap[instance.availability]!,
};

const _$TimelineTickFramesInnerAvailabilityEnumEnumMap = {
  TimelineTickFramesInnerAvailabilityEnum.available: 'available',
  TimelineTickFramesInnerAvailabilityEnum.unverified: 'unverified',
};
