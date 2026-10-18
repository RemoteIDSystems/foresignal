// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_scheduler.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoSchedulerCWProxy {
  SystemInfoScheduler running(bool? running);

  SystemInfoScheduler scheduledSources(int? scheduledSources);

  SystemInfoScheduler activeFetches(int? activeFetches);

  SystemInfoScheduler activeStreams(int? activeStreams);

  SystemInfoScheduler ingestQueueDepth(int? ingestQueueDepth);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoScheduler(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoScheduler(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoScheduler call({
    bool? running,
    int? scheduledSources,
    int? activeFetches,
    int? activeStreams,
    int? ingestQueueDepth,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoScheduler.copyWith(...)` or call `instanceOfSystemInfoScheduler.copyWith.fieldName(value)` for a single field.
class _$SystemInfoSchedulerCWProxyImpl implements _$SystemInfoSchedulerCWProxy {
  const _$SystemInfoSchedulerCWProxyImpl(this._value);

  final SystemInfoScheduler _value;

  @override
  SystemInfoScheduler running(bool? running) => call(running: running);

  @override
  SystemInfoScheduler scheduledSources(int? scheduledSources) =>
      call(scheduledSources: scheduledSources);

  @override
  SystemInfoScheduler activeFetches(int? activeFetches) =>
      call(activeFetches: activeFetches);

  @override
  SystemInfoScheduler activeStreams(int? activeStreams) =>
      call(activeStreams: activeStreams);

  @override
  SystemInfoScheduler ingestQueueDepth(int? ingestQueueDepth) =>
      call(ingestQueueDepth: ingestQueueDepth);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoScheduler(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoScheduler(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoScheduler call({
    Object? running = const $CopyWithPlaceholder(),
    Object? scheduledSources = const $CopyWithPlaceholder(),
    Object? activeFetches = const $CopyWithPlaceholder(),
    Object? activeStreams = const $CopyWithPlaceholder(),
    Object? ingestQueueDepth = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoScheduler(
      running: running == const $CopyWithPlaceholder()
          ? _value.running
          // ignore: cast_nullable_to_non_nullable
          : running as bool?,
      scheduledSources: scheduledSources == const $CopyWithPlaceholder()
          ? _value.scheduledSources
          // ignore: cast_nullable_to_non_nullable
          : scheduledSources as int?,
      activeFetches: activeFetches == const $CopyWithPlaceholder()
          ? _value.activeFetches
          // ignore: cast_nullable_to_non_nullable
          : activeFetches as int?,
      activeStreams: activeStreams == const $CopyWithPlaceholder()
          ? _value.activeStreams
          // ignore: cast_nullable_to_non_nullable
          : activeStreams as int?,
      ingestQueueDepth: ingestQueueDepth == const $CopyWithPlaceholder()
          ? _value.ingestQueueDepth
          // ignore: cast_nullable_to_non_nullable
          : ingestQueueDepth as int?,
    );
  }
}

extension $SystemInfoSchedulerCopyWith on SystemInfoScheduler {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoScheduler.copyWith(...)` or `instanceOfSystemInfoScheduler.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoSchedulerCWProxy get copyWith =>
      _$SystemInfoSchedulerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoScheduler _$SystemInfoSchedulerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SystemInfoScheduler', json, ($checkedConvert) {
      final val = SystemInfoScheduler(
        running: $checkedConvert('running', (v) => v as bool?),
        scheduledSources: $checkedConvert(
          'scheduledSources',
          (v) => (v as num?)?.toInt(),
        ),
        activeFetches: $checkedConvert(
          'activeFetches',
          (v) => (v as num?)?.toInt(),
        ),
        activeStreams: $checkedConvert(
          'activeStreams',
          (v) => (v as num?)?.toInt(),
        ),
        ingestQueueDepth: $checkedConvert(
          'ingestQueueDepth',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SystemInfoSchedulerToJson(
  SystemInfoScheduler instance,
) => <String, dynamic>{
  'running': ?instance.running,
  'scheduledSources': ?instance.scheduledSources,
  'activeFetches': ?instance.activeFetches,
  'activeStreams': ?instance.activeStreams,
  'ingestQueueDepth': ?instance.ingestQueueDepth,
};
