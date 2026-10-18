// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'channel_airtime.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChannelAirtimeCWProxy {
  ChannelAirtime windowSeconds(int windowSeconds);

  ChannelAirtime usedSeconds(num usedSeconds);

  ChannelAirtime limitSeconds(num limitSeconds);

  ChannelAirtime dutyCyclePercent(num dutyCyclePercent);

  ChannelAirtime queued(int queued);

  ChannelAirtime foldedLastHour(int foldedLastHour);

  ChannelAirtime droppedLastHour(int droppedLastHour);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelAirtime(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelAirtime(...).copyWith(id: 12, name: "My name")
  /// ```
  ChannelAirtime call({
    int windowSeconds,
    num usedSeconds,
    num limitSeconds,
    num dutyCyclePercent,
    int queued,
    int foldedLastHour,
    int droppedLastHour,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChannelAirtime.copyWith(...)` or call `instanceOfChannelAirtime.copyWith.fieldName(value)` for a single field.
class _$ChannelAirtimeCWProxyImpl implements _$ChannelAirtimeCWProxy {
  const _$ChannelAirtimeCWProxyImpl(this._value);

  final ChannelAirtime _value;

  @override
  ChannelAirtime windowSeconds(int windowSeconds) =>
      call(windowSeconds: windowSeconds);

  @override
  ChannelAirtime usedSeconds(num usedSeconds) => call(usedSeconds: usedSeconds);

  @override
  ChannelAirtime limitSeconds(num limitSeconds) =>
      call(limitSeconds: limitSeconds);

  @override
  ChannelAirtime dutyCyclePercent(num dutyCyclePercent) =>
      call(dutyCyclePercent: dutyCyclePercent);

  @override
  ChannelAirtime queued(int queued) => call(queued: queued);

  @override
  ChannelAirtime foldedLastHour(int foldedLastHour) =>
      call(foldedLastHour: foldedLastHour);

  @override
  ChannelAirtime droppedLastHour(int droppedLastHour) =>
      call(droppedLastHour: droppedLastHour);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelAirtime(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelAirtime(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChannelAirtime call({
    Object? windowSeconds = const $CopyWithPlaceholder(),
    Object? usedSeconds = const $CopyWithPlaceholder(),
    Object? limitSeconds = const $CopyWithPlaceholder(),
    Object? dutyCyclePercent = const $CopyWithPlaceholder(),
    Object? queued = const $CopyWithPlaceholder(),
    Object? foldedLastHour = const $CopyWithPlaceholder(),
    Object? droppedLastHour = const $CopyWithPlaceholder(),
  }) {
    return ChannelAirtime(
      windowSeconds:
          windowSeconds == const $CopyWithPlaceholder() || windowSeconds == null
          ? _value.windowSeconds
          // ignore: cast_nullable_to_non_nullable
          : windowSeconds as int,
      usedSeconds:
          usedSeconds == const $CopyWithPlaceholder() || usedSeconds == null
          ? _value.usedSeconds
          // ignore: cast_nullable_to_non_nullable
          : usedSeconds as num,
      limitSeconds:
          limitSeconds == const $CopyWithPlaceholder() || limitSeconds == null
          ? _value.limitSeconds
          // ignore: cast_nullable_to_non_nullable
          : limitSeconds as num,
      dutyCyclePercent:
          dutyCyclePercent == const $CopyWithPlaceholder() ||
              dutyCyclePercent == null
          ? _value.dutyCyclePercent
          // ignore: cast_nullable_to_non_nullable
          : dutyCyclePercent as num,
      queued: queued == const $CopyWithPlaceholder() || queued == null
          ? _value.queued
          // ignore: cast_nullable_to_non_nullable
          : queued as int,
      foldedLastHour:
          foldedLastHour == const $CopyWithPlaceholder() ||
              foldedLastHour == null
          ? _value.foldedLastHour
          // ignore: cast_nullable_to_non_nullable
          : foldedLastHour as int,
      droppedLastHour:
          droppedLastHour == const $CopyWithPlaceholder() ||
              droppedLastHour == null
          ? _value.droppedLastHour
          // ignore: cast_nullable_to_non_nullable
          : droppedLastHour as int,
    );
  }
}

extension $ChannelAirtimeCopyWith on ChannelAirtime {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChannelAirtime.copyWith(...)` or `instanceOfChannelAirtime.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChannelAirtimeCWProxy get copyWith => _$ChannelAirtimeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChannelAirtime _$ChannelAirtimeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChannelAirtime', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'windowSeconds',
          'usedSeconds',
          'limitSeconds',
          'dutyCyclePercent',
          'queued',
          'foldedLastHour',
          'droppedLastHour',
        ],
      );
      final val = ChannelAirtime(
        windowSeconds: $checkedConvert(
          'windowSeconds',
          (v) => (v as num).toInt(),
        ),
        usedSeconds: $checkedConvert('usedSeconds', (v) => v as num),
        limitSeconds: $checkedConvert('limitSeconds', (v) => v as num),
        dutyCyclePercent: $checkedConvert('dutyCyclePercent', (v) => v as num),
        queued: $checkedConvert('queued', (v) => (v as num).toInt()),
        foldedLastHour: $checkedConvert(
          'foldedLastHour',
          (v) => (v as num).toInt(),
        ),
        droppedLastHour: $checkedConvert(
          'droppedLastHour',
          (v) => (v as num).toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ChannelAirtimeToJson(ChannelAirtime instance) =>
    <String, dynamic>{
      'windowSeconds': instance.windowSeconds,
      'usedSeconds': instance.usedSeconds,
      'limitSeconds': instance.limitSeconds,
      'dutyCyclePercent': instance.dutyCyclePercent,
      'queued': instance.queued,
      'foldedLastHour': instance.foldedLastHour,
      'droppedLastHour': instance.droppedLastHour,
    };
