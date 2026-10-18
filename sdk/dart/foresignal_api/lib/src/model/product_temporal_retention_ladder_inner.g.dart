// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_temporal_retention_ladder_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductTemporalRetentionLadderInnerCWProxy {
  ProductTemporalRetentionLadderInner olderThan(String olderThan);

  ProductTemporalRetentionLadderInner keepEvery(String keepEvery);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductTemporalRetentionLadderInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductTemporalRetentionLadderInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductTemporalRetentionLadderInner call({
    String olderThan,
    String keepEvery,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductTemporalRetentionLadderInner.copyWith(...)` or call `instanceOfProductTemporalRetentionLadderInner.copyWith.fieldName(value)` for a single field.
class _$ProductTemporalRetentionLadderInnerCWProxyImpl
    implements _$ProductTemporalRetentionLadderInnerCWProxy {
  const _$ProductTemporalRetentionLadderInnerCWProxyImpl(this._value);

  final ProductTemporalRetentionLadderInner _value;

  @override
  ProductTemporalRetentionLadderInner olderThan(String olderThan) =>
      call(olderThan: olderThan);

  @override
  ProductTemporalRetentionLadderInner keepEvery(String keepEvery) =>
      call(keepEvery: keepEvery);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductTemporalRetentionLadderInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductTemporalRetentionLadderInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductTemporalRetentionLadderInner call({
    Object? olderThan = const $CopyWithPlaceholder(),
    Object? keepEvery = const $CopyWithPlaceholder(),
  }) {
    return ProductTemporalRetentionLadderInner(
      olderThan: olderThan == const $CopyWithPlaceholder() || olderThan == null
          ? _value.olderThan
          // ignore: cast_nullable_to_non_nullable
          : olderThan as String,
      keepEvery: keepEvery == const $CopyWithPlaceholder() || keepEvery == null
          ? _value.keepEvery
          // ignore: cast_nullable_to_non_nullable
          : keepEvery as String,
    );
  }
}

extension $ProductTemporalRetentionLadderInnerCopyWith
    on ProductTemporalRetentionLadderInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductTemporalRetentionLadderInner.copyWith(...)` or `instanceOfProductTemporalRetentionLadderInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductTemporalRetentionLadderInnerCWProxy get copyWith =>
      _$ProductTemporalRetentionLadderInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductTemporalRetentionLadderInner
_$ProductTemporalRetentionLadderInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProductTemporalRetentionLadderInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['olderThan', 'keepEvery']);
      final val = ProductTemporalRetentionLadderInner(
        olderThan: $checkedConvert('olderThan', (v) => v as String),
        keepEvery: $checkedConvert('keepEvery', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$ProductTemporalRetentionLadderInnerToJson(
  ProductTemporalRetentionLadderInner instance,
) => <String, dynamic>{
  'olderThan': instance.olderThan,
  'keepEvery': instance.keepEvery,
};
