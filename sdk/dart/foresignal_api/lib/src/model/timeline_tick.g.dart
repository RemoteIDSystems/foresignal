// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'timeline_tick.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TimelineTickCWProxy {
  TimelineTick time(DateTime time);

  TimelineTick kind(TimelineTickKindEnum kind);

  TimelineTick frames(List<TimelineTickFramesInner> frames);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TimelineTick(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TimelineTick(...).copyWith(id: 12, name: "My name")
  /// ```
  TimelineTick call({
    DateTime time,
    TimelineTickKindEnum kind,
    List<TimelineTickFramesInner> frames,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTimelineTick.copyWith(...)` or call `instanceOfTimelineTick.copyWith.fieldName(value)` for a single field.
class _$TimelineTickCWProxyImpl implements _$TimelineTickCWProxy {
  const _$TimelineTickCWProxyImpl(this._value);

  final TimelineTick _value;

  @override
  TimelineTick time(DateTime time) => call(time: time);

  @override
  TimelineTick kind(TimelineTickKindEnum kind) => call(kind: kind);

  @override
  TimelineTick frames(List<TimelineTickFramesInner> frames) =>
      call(frames: frames);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TimelineTick(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TimelineTick(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TimelineTick call({
    Object? time = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? frames = const $CopyWithPlaceholder(),
  }) {
    return TimelineTick(
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as TimelineTickKindEnum,
      frames: frames == const $CopyWithPlaceholder() || frames == null
          ? _value.frames
          // ignore: cast_nullable_to_non_nullable
          : frames as List<TimelineTickFramesInner>,
    );
  }
}

extension $TimelineTickCopyWith on TimelineTick {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTimelineTick.copyWith(...)` or `instanceOfTimelineTick.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TimelineTickCWProxy get copyWith => _$TimelineTickCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TimelineTick _$TimelineTickFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TimelineTick', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['time', 'kind', 'frames']);
      final val = TimelineTick(
        time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
        kind: $checkedConvert(
          'kind',
          (v) => $enumDecode(_$TimelineTickKindEnumEnumMap, v),
        ),
        frames: $checkedConvert(
          'frames',
          (v) => (v as List<dynamic>)
              .map(
                (e) =>
                    TimelineTickFramesInner.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TimelineTickToJson(TimelineTick instance) =>
    <String, dynamic>{
      'time': instance.time.toIso8601String(),
      'kind': _$TimelineTickKindEnumEnumMap[instance.kind]!,
      'frames': instance.frames.map((e) => e.toJson()).toList(),
    };

const _$TimelineTickKindEnumEnumMap = {
  TimelineTickKindEnum.observed: 'observed',
  TimelineTickKindEnum.forecast: 'forecast',
};
