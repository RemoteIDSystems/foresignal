// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'resource_thresholds_collector_backlog.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ResourceThresholdsCollectorBacklogCWProxy {
  ResourceThresholdsCollectorBacklog triggerLagSeq(int? triggerLagSeq);

  ResourceThresholdsCollectorBacklog waitingPulls(int? waitingPulls);

  ResourceThresholdsCollectorBacklog sustainedSeconds(int? sustainedSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceThresholdsCollectorBacklog(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceThresholdsCollectorBacklog(...).copyWith(id: 12, name: "My name")
  /// ```
  ResourceThresholdsCollectorBacklog call({
    int? triggerLagSeq,
    int? waitingPulls,
    int? sustainedSeconds,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfResourceThresholdsCollectorBacklog.copyWith(...)` or call `instanceOfResourceThresholdsCollectorBacklog.copyWith.fieldName(value)` for a single field.
class _$ResourceThresholdsCollectorBacklogCWProxyImpl
    implements _$ResourceThresholdsCollectorBacklogCWProxy {
  const _$ResourceThresholdsCollectorBacklogCWProxyImpl(this._value);

  final ResourceThresholdsCollectorBacklog _value;

  @override
  ResourceThresholdsCollectorBacklog triggerLagSeq(int? triggerLagSeq) =>
      call(triggerLagSeq: triggerLagSeq);

  @override
  ResourceThresholdsCollectorBacklog waitingPulls(int? waitingPulls) =>
      call(waitingPulls: waitingPulls);

  @override
  ResourceThresholdsCollectorBacklog sustainedSeconds(int? sustainedSeconds) =>
      call(sustainedSeconds: sustainedSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ResourceThresholdsCollectorBacklog(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ResourceThresholdsCollectorBacklog(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ResourceThresholdsCollectorBacklog call({
    Object? triggerLagSeq = const $CopyWithPlaceholder(),
    Object? waitingPulls = const $CopyWithPlaceholder(),
    Object? sustainedSeconds = const $CopyWithPlaceholder(),
  }) {
    return ResourceThresholdsCollectorBacklog(
      triggerLagSeq: triggerLagSeq == const $CopyWithPlaceholder()
          ? _value.triggerLagSeq
          // ignore: cast_nullable_to_non_nullable
          : triggerLagSeq as int?,
      waitingPulls: waitingPulls == const $CopyWithPlaceholder()
          ? _value.waitingPulls
          // ignore: cast_nullable_to_non_nullable
          : waitingPulls as int?,
      sustainedSeconds: sustainedSeconds == const $CopyWithPlaceholder()
          ? _value.sustainedSeconds
          // ignore: cast_nullable_to_non_nullable
          : sustainedSeconds as int?,
    );
  }
}

extension $ResourceThresholdsCollectorBacklogCopyWith
    on ResourceThresholdsCollectorBacklog {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfResourceThresholdsCollectorBacklog.copyWith(...)` or `instanceOfResourceThresholdsCollectorBacklog.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ResourceThresholdsCollectorBacklogCWProxy get copyWith =>
      _$ResourceThresholdsCollectorBacklogCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ResourceThresholdsCollectorBacklog _$ResourceThresholdsCollectorBacklogFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ResourceThresholdsCollectorBacklog', json, (
  $checkedConvert,
) {
  final val = ResourceThresholdsCollectorBacklog(
    triggerLagSeq: $checkedConvert(
      'triggerLagSeq',
      (v) => (v as num?)?.toInt(),
    ),
    waitingPulls: $checkedConvert('waitingPulls', (v) => (v as num?)?.toInt()),
    sustainedSeconds: $checkedConvert(
      'sustainedSeconds',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ResourceThresholdsCollectorBacklogToJson(
  ResourceThresholdsCollectorBacklog instance,
) => <String, dynamic>{
  'triggerLagSeq': ?instance.triggerLagSeq,
  'waitingPulls': ?instance.waitingPulls,
  'sustainedSeconds': ?instance.sustainedSeconds,
};
