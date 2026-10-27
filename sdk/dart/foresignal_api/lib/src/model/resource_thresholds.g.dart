// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'resource_thresholds.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ResourceThresholdsCWProxy {
  ResourceThresholds disk(ResourceThreshold? disk);

  ResourceThresholds memory(ResourceThreshold? memory);

  ResourceThresholds alertChannels(List<String>? alertChannels);

  ResourceThresholds sourceStorm(ResourceThresholdsSourceStorm? sourceStorm);

  ResourceThresholds collectorBacklog(
    ResourceThresholdsCollectorBacklog? collectorBacklog,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceThresholds(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceThresholds(...).copyWith(id: 12, name: "My name")
  /// ```
  ResourceThresholds call({
    ResourceThreshold? disk,
    ResourceThreshold? memory,
    List<String>? alertChannels,
    ResourceThresholdsSourceStorm? sourceStorm,
    ResourceThresholdsCollectorBacklog? collectorBacklog,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfResourceThresholds.copyWith(...)` or call `instanceOfResourceThresholds.copyWith.fieldName(value)` for a single field.
class _$ResourceThresholdsCWProxyImpl implements _$ResourceThresholdsCWProxy {
  const _$ResourceThresholdsCWProxyImpl(this._value);

  final ResourceThresholds _value;

  @override
  ResourceThresholds disk(ResourceThreshold? disk) => call(disk: disk);

  @override
  ResourceThresholds memory(ResourceThreshold? memory) => call(memory: memory);

  @override
  ResourceThresholds alertChannels(List<String>? alertChannels) =>
      call(alertChannels: alertChannels);

  @override
  ResourceThresholds sourceStorm(ResourceThresholdsSourceStorm? sourceStorm) =>
      call(sourceStorm: sourceStorm);

  @override
  ResourceThresholds collectorBacklog(
    ResourceThresholdsCollectorBacklog? collectorBacklog,
  ) => call(collectorBacklog: collectorBacklog);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceThresholds(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceThresholds(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ResourceThresholds call({
    Object? disk = const $CopyWithPlaceholder(),
    Object? memory = const $CopyWithPlaceholder(),
    Object? alertChannels = const $CopyWithPlaceholder(),
    Object? sourceStorm = const $CopyWithPlaceholder(),
    Object? collectorBacklog = const $CopyWithPlaceholder(),
  }) {
    return ResourceThresholds(
      disk: disk == const $CopyWithPlaceholder()
          ? _value.disk
          // ignore: cast_nullable_to_non_nullable
          : disk as ResourceThreshold?,
      memory: memory == const $CopyWithPlaceholder()
          ? _value.memory
          // ignore: cast_nullable_to_non_nullable
          : memory as ResourceThreshold?,
      alertChannels: alertChannels == const $CopyWithPlaceholder()
          ? _value.alertChannels
          // ignore: cast_nullable_to_non_nullable
          : alertChannels as List<String>?,
      sourceStorm: sourceStorm == const $CopyWithPlaceholder()
          ? _value.sourceStorm
          // ignore: cast_nullable_to_non_nullable
          : sourceStorm as ResourceThresholdsSourceStorm?,
      collectorBacklog: collectorBacklog == const $CopyWithPlaceholder()
          ? _value.collectorBacklog
          // ignore: cast_nullable_to_non_nullable
          : collectorBacklog as ResourceThresholdsCollectorBacklog?,
    );
  }
}

extension $ResourceThresholdsCopyWith on ResourceThresholds {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfResourceThresholds.copyWith(...)` or `instanceOfResourceThresholds.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ResourceThresholdsCWProxy get copyWith =>
      _$ResourceThresholdsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ResourceThresholds _$ResourceThresholdsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ResourceThresholds', json, ($checkedConvert) {
      final val = ResourceThresholds(
        disk: $checkedConvert(
          'disk',
          (v) => v == null
              ? null
              : ResourceThreshold.fromJson(v as Map<String, dynamic>),
        ),
        memory: $checkedConvert(
          'memory',
          (v) => v == null
              ? null
              : ResourceThreshold.fromJson(v as Map<String, dynamic>),
        ),
        alertChannels: $checkedConvert(
          'alertChannels',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        sourceStorm: $checkedConvert(
          'sourceStorm',
          (v) => v == null
              ? null
              : ResourceThresholdsSourceStorm.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
        collectorBacklog: $checkedConvert(
          'collectorBacklog',
          (v) => v == null
              ? null
              : ResourceThresholdsCollectorBacklog.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ResourceThresholdsToJson(ResourceThresholds instance) =>
    <String, dynamic>{
      'disk': ?instance.disk?.toJson(),
      'memory': ?instance.memory?.toJson(),
      'alertChannels': ?instance.alertChannels,
      'sourceStorm': ?instance.sourceStorm?.toJson(),
      'collectorBacklog': ?instance.collectorBacklog?.toJson(),
    };
