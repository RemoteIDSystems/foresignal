// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_rasters_render_queue.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemRastersRenderQueueCWProxy {
  SystemRastersRenderQueue depth(int? depth);

  SystemRastersRenderQueue active(int? active);

  SystemRastersRenderQueue poolSize(int? poolSize);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemRastersRenderQueue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemRastersRenderQueue(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemRastersRenderQueue call({int? depth, int? active, int? poolSize});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemRastersRenderQueue.copyWith(...)` or call `instanceOfSystemRastersRenderQueue.copyWith.fieldName(value)` for a single field.
class _$SystemRastersRenderQueueCWProxyImpl
    implements _$SystemRastersRenderQueueCWProxy {
  const _$SystemRastersRenderQueueCWProxyImpl(this._value);

  final SystemRastersRenderQueue _value;

  @override
  SystemRastersRenderQueue depth(int? depth) => call(depth: depth);

  @override
  SystemRastersRenderQueue active(int? active) => call(active: active);

  @override
  SystemRastersRenderQueue poolSize(int? poolSize) => call(poolSize: poolSize);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemRastersRenderQueue(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemRastersRenderQueue(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemRastersRenderQueue call({
    Object? depth = const $CopyWithPlaceholder(),
    Object? active = const $CopyWithPlaceholder(),
    Object? poolSize = const $CopyWithPlaceholder(),
  }) {
    return SystemRastersRenderQueue(
      depth: depth == const $CopyWithPlaceholder()
          ? _value.depth
          // ignore: cast_nullable_to_non_nullable
          : depth as int?,
      active: active == const $CopyWithPlaceholder()
          ? _value.active
          // ignore: cast_nullable_to_non_nullable
          : active as int?,
      poolSize: poolSize == const $CopyWithPlaceholder()
          ? _value.poolSize
          // ignore: cast_nullable_to_non_nullable
          : poolSize as int?,
    );
  }
}

extension $SystemRastersRenderQueueCopyWith on SystemRastersRenderQueue {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemRastersRenderQueue.copyWith(...)` or `instanceOfSystemRastersRenderQueue.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemRastersRenderQueueCWProxy get copyWith =>
      _$SystemRastersRenderQueueCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemRastersRenderQueue _$SystemRastersRenderQueueFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SystemRastersRenderQueue', json, ($checkedConvert) {
  final val = SystemRastersRenderQueue(
    depth: $checkedConvert('depth', (v) => (v as num?)?.toInt()),
    active: $checkedConvert('active', (v) => (v as num?)?.toInt()),
    poolSize: $checkedConvert('poolSize', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$SystemRastersRenderQueueToJson(
  SystemRastersRenderQueue instance,
) => <String, dynamic>{
  'depth': ?instance.depth,
  'active': ?instance.active,
  'poolSize': ?instance.poolSize,
};
