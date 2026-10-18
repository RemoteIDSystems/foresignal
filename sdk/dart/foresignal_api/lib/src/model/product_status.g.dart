// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductStatusCWProxy {
  ProductStatus health(HealthStatus health);

  ProductStatus stale(bool stale);

  ProductStatus lastFrameAt(DateTime? lastFrameAt);

  ProductStatus reason(String? reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductStatus call({
    HealthStatus health,
    bool stale,
    DateTime? lastFrameAt,
    String? reason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductStatus.copyWith(...)` or call `instanceOfProductStatus.copyWith.fieldName(value)` for a single field.
class _$ProductStatusCWProxyImpl implements _$ProductStatusCWProxy {
  const _$ProductStatusCWProxyImpl(this._value);

  final ProductStatus _value;

  @override
  ProductStatus health(HealthStatus health) => call(health: health);

  @override
  ProductStatus stale(bool stale) => call(stale: stale);

  @override
  ProductStatus lastFrameAt(DateTime? lastFrameAt) =>
      call(lastFrameAt: lastFrameAt);

  @override
  ProductStatus reason(String? reason) => call(reason: reason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductStatus call({
    Object? health = const $CopyWithPlaceholder(),
    Object? stale = const $CopyWithPlaceholder(),
    Object? lastFrameAt = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
  }) {
    return ProductStatus(
      health: health == const $CopyWithPlaceholder() || health == null
          ? _value.health
          // ignore: cast_nullable_to_non_nullable
          : health as HealthStatus,
      stale: stale == const $CopyWithPlaceholder() || stale == null
          ? _value.stale
          // ignore: cast_nullable_to_non_nullable
          : stale as bool,
      lastFrameAt: lastFrameAt == const $CopyWithPlaceholder()
          ? _value.lastFrameAt
          // ignore: cast_nullable_to_non_nullable
          : lastFrameAt as DateTime?,
      reason: reason == const $CopyWithPlaceholder()
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String?,
    );
  }
}

extension $ProductStatusCopyWith on ProductStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductStatus.copyWith(...)` or `instanceOfProductStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductStatusCWProxy get copyWith => _$ProductStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductStatus _$ProductStatusFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProductStatus', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['health', 'stale']);
      final val = ProductStatus(
        health: $checkedConvert(
          'health',
          (v) => $enumDecode(_$HealthStatusEnumMap, v),
        ),
        stale: $checkedConvert('stale', (v) => v as bool),
        lastFrameAt: $checkedConvert(
          'lastFrameAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        reason: $checkedConvert('reason', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ProductStatusToJson(ProductStatus instance) =>
    <String, dynamic>{
      'health': _$HealthStatusEnumMap[instance.health]!,
      'stale': instance.stale,
      'lastFrameAt': ?instance.lastFrameAt?.toIso8601String(),
      'reason': ?instance.reason,
    };

const _$HealthStatusEnumMap = {
  HealthStatus.healthy: 'healthy',
  HealthStatus.degraded: 'degraded',
  HealthStatus.failing: 'failing',
  HealthStatus.idle: 'idle',
  HealthStatus.disabled: 'disabled',
};
