// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_temporal_retention.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductTemporalRetentionCWProxy {
  ProductTemporalRetention ladder(
    List<ProductTemporalRetentionLadderInner>? ladder,
  );

  ProductTemporalRetention maxAge(String? maxAge);

  ProductTemporalRetention runs(int? runs);

  ProductTemporalRetention summary(String? summary);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductTemporalRetention(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductTemporalRetention(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductTemporalRetention call({
    List<ProductTemporalRetentionLadderInner>? ladder,
    String? maxAge,
    int? runs,
    String? summary,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductTemporalRetention.copyWith(...)` or call `instanceOfProductTemporalRetention.copyWith.fieldName(value)` for a single field.
class _$ProductTemporalRetentionCWProxyImpl
    implements _$ProductTemporalRetentionCWProxy {
  const _$ProductTemporalRetentionCWProxyImpl(this._value);

  final ProductTemporalRetention _value;

  @override
  ProductTemporalRetention ladder(
    List<ProductTemporalRetentionLadderInner>? ladder,
  ) => call(ladder: ladder);

  @override
  ProductTemporalRetention maxAge(String? maxAge) => call(maxAge: maxAge);

  @override
  ProductTemporalRetention runs(int? runs) => call(runs: runs);

  @override
  ProductTemporalRetention summary(String? summary) => call(summary: summary);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductTemporalRetention(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductTemporalRetention(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductTemporalRetention call({
    Object? ladder = const $CopyWithPlaceholder(),
    Object? maxAge = const $CopyWithPlaceholder(),
    Object? runs = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
  }) {
    return ProductTemporalRetention(
      ladder: ladder == const $CopyWithPlaceholder()
          ? _value.ladder
          // ignore: cast_nullable_to_non_nullable
          : ladder as List<ProductTemporalRetentionLadderInner>?,
      maxAge: maxAge == const $CopyWithPlaceholder()
          ? _value.maxAge
          // ignore: cast_nullable_to_non_nullable
          : maxAge as String?,
      runs: runs == const $CopyWithPlaceholder()
          ? _value.runs
          // ignore: cast_nullable_to_non_nullable
          : runs as int?,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
    );
  }
}

extension $ProductTemporalRetentionCopyWith on ProductTemporalRetention {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductTemporalRetention.copyWith(...)` or `instanceOfProductTemporalRetention.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductTemporalRetentionCWProxy get copyWith =>
      _$ProductTemporalRetentionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductTemporalRetention _$ProductTemporalRetentionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProductTemporalRetention', json, ($checkedConvert) {
  final val = ProductTemporalRetention(
    ladder: $checkedConvert(
      'ladder',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ProductTemporalRetentionLadderInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    maxAge: $checkedConvert('maxAge', (v) => v as String?),
    runs: $checkedConvert('runs', (v) => (v as num?)?.toInt()),
    summary: $checkedConvert('summary', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ProductTemporalRetentionToJson(
  ProductTemporalRetention instance,
) => <String, dynamic>{
  'ladder': ?instance.ladder?.map((e) => e.toJson()).toList(),
  'maxAge': ?instance.maxAge,
  'runs': ?instance.runs,
  'summary': ?instance.summary,
};
