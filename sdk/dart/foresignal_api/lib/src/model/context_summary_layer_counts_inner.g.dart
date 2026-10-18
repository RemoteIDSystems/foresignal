// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'context_summary_layer_counts_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ContextSummaryLayerCountsInnerCWProxy {
  ContextSummaryLayerCountsInner layer(String layer);

  ContextSummaryLayerCountsInner count(int count);

  ContextSummaryLayerCountsInner liveCount(int? liveCount);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextSummaryLayerCountsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextSummaryLayerCountsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ContextSummaryLayerCountsInner call({
    String layer,
    int count,
    int? liveCount,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfContextSummaryLayerCountsInner.copyWith(...)` or call `instanceOfContextSummaryLayerCountsInner.copyWith.fieldName(value)` for a single field.
class _$ContextSummaryLayerCountsInnerCWProxyImpl
    implements _$ContextSummaryLayerCountsInnerCWProxy {
  const _$ContextSummaryLayerCountsInnerCWProxyImpl(this._value);

  final ContextSummaryLayerCountsInner _value;

  @override
  ContextSummaryLayerCountsInner layer(String layer) => call(layer: layer);

  @override
  ContextSummaryLayerCountsInner count(int count) => call(count: count);

  @override
  ContextSummaryLayerCountsInner liveCount(int? liveCount) =>
      call(liveCount: liveCount);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextSummaryLayerCountsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextSummaryLayerCountsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ContextSummaryLayerCountsInner call({
    Object? layer = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
    Object? liveCount = const $CopyWithPlaceholder(),
  }) {
    return ContextSummaryLayerCountsInner(
      layer: layer == const $CopyWithPlaceholder() || layer == null
          ? _value.layer
          // ignore: cast_nullable_to_non_nullable
          : layer as String,
      count: count == const $CopyWithPlaceholder() || count == null
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int,
      liveCount: liveCount == const $CopyWithPlaceholder()
          ? _value.liveCount
          // ignore: cast_nullable_to_non_nullable
          : liveCount as int?,
    );
  }
}

extension $ContextSummaryLayerCountsInnerCopyWith
    on ContextSummaryLayerCountsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfContextSummaryLayerCountsInner.copyWith(...)` or `instanceOfContextSummaryLayerCountsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ContextSummaryLayerCountsInnerCWProxy get copyWith =>
      _$ContextSummaryLayerCountsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ContextSummaryLayerCountsInner _$ContextSummaryLayerCountsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ContextSummaryLayerCountsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['layer', 'count']);
  final val = ContextSummaryLayerCountsInner(
    layer: $checkedConvert('layer', (v) => v as String),
    count: $checkedConvert('count', (v) => (v as num).toInt()),
    liveCount: $checkedConvert('liveCount', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$ContextSummaryLayerCountsInnerToJson(
  ContextSummaryLayerCountsInner instance,
) => <String, dynamic>{
  'layer': instance.layer,
  'count': instance.count,
  'liveCount': ?instance.liveCount,
};
