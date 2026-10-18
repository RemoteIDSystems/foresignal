// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_temporal_runs_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductTemporalRunsInnerCWProxy {
  ProductTemporalRunsInner run(String run);

  ProductTemporalRunsInner status(ProductTemporalRunsInnerStatusEnum status);

  ProductTemporalRunsInner steps(List<int>? steps);

  ProductTemporalRunsInner validFrom(DateTime? validFrom);

  ProductTemporalRunsInner validTo(DateTime? validTo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductTemporalRunsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductTemporalRunsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductTemporalRunsInner call({
    String run,
    ProductTemporalRunsInnerStatusEnum status,
    List<int>? steps,
    DateTime? validFrom,
    DateTime? validTo,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductTemporalRunsInner.copyWith(...)` or call `instanceOfProductTemporalRunsInner.copyWith.fieldName(value)` for a single field.
class _$ProductTemporalRunsInnerCWProxyImpl
    implements _$ProductTemporalRunsInnerCWProxy {
  const _$ProductTemporalRunsInnerCWProxyImpl(this._value);

  final ProductTemporalRunsInner _value;

  @override
  ProductTemporalRunsInner run(String run) => call(run: run);

  @override
  ProductTemporalRunsInner status(ProductTemporalRunsInnerStatusEnum status) =>
      call(status: status);

  @override
  ProductTemporalRunsInner steps(List<int>? steps) => call(steps: steps);

  @override
  ProductTemporalRunsInner validFrom(DateTime? validFrom) =>
      call(validFrom: validFrom);

  @override
  ProductTemporalRunsInner validTo(DateTime? validTo) => call(validTo: validTo);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductTemporalRunsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductTemporalRunsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductTemporalRunsInner call({
    Object? run = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? steps = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? validTo = const $CopyWithPlaceholder(),
  }) {
    return ProductTemporalRunsInner(
      run: run == const $CopyWithPlaceholder() || run == null
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ProductTemporalRunsInnerStatusEnum,
      steps: steps == const $CopyWithPlaceholder()
          ? _value.steps
          // ignore: cast_nullable_to_non_nullable
          : steps as List<int>?,
      validFrom: validFrom == const $CopyWithPlaceholder()
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime?,
      validTo: validTo == const $CopyWithPlaceholder()
          ? _value.validTo
          // ignore: cast_nullable_to_non_nullable
          : validTo as DateTime?,
    );
  }
}

extension $ProductTemporalRunsInnerCopyWith on ProductTemporalRunsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductTemporalRunsInner.copyWith(...)` or `instanceOfProductTemporalRunsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductTemporalRunsInnerCWProxy get copyWith =>
      _$ProductTemporalRunsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductTemporalRunsInner _$ProductTemporalRunsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ProductTemporalRunsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['run', 'status']);
  final val = ProductTemporalRunsInner(
    run: $checkedConvert('run', (v) => v as String),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ProductTemporalRunsInnerStatusEnumEnumMap, v),
    ),
    steps: $checkedConvert(
      'steps',
      (v) => (v as List<dynamic>?)?.map((e) => (e as num).toInt()).toList(),
    ),
    validFrom: $checkedConvert(
      'validFrom',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    validTo: $checkedConvert(
      'validTo',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ProductTemporalRunsInnerToJson(
  ProductTemporalRunsInner instance,
) => <String, dynamic>{
  'run': instance.run,
  'status': _$ProductTemporalRunsInnerStatusEnumEnumMap[instance.status]!,
  'steps': ?instance.steps,
  'validFrom': ?instance.validFrom?.toIso8601String(),
  'validTo': ?instance.validTo?.toIso8601String(),
};

const _$ProductTemporalRunsInnerStatusEnumEnumMap = {
  ProductTemporalRunsInnerStatusEnum.complete: 'complete',
  ProductTemporalRunsInnerStatusEnum.partial: 'partial',
};
