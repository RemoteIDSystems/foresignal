// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'product_temporal.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProductTemporalCWProxy {
  ProductTemporal type(ProductTemporalTypeEnum type);

  ProductTemporal cadence(String? cadence);

  ProductTemporal maxGap(String? maxGap);

  ProductTemporal earliest(DateTime? earliest);

  ProductTemporal latest(DateTime? latest);

  ProductTemporal retention(ProductTemporalRetention? retention);

  ProductTemporal runs(List<ProductTemporalRunsInner>? runs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductTemporal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductTemporal(...).copyWith(id: 12, name: "My name")
  /// ```
  ProductTemporal call({
    ProductTemporalTypeEnum type,
    String? cadence,
    String? maxGap,
    DateTime? earliest,
    DateTime? latest,
    ProductTemporalRetention? retention,
    List<ProductTemporalRunsInner>? runs,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfProductTemporal.copyWith(...)` or call `instanceOfProductTemporal.copyWith.fieldName(value)` for a single field.
class _$ProductTemporalCWProxyImpl implements _$ProductTemporalCWProxy {
  const _$ProductTemporalCWProxyImpl(this._value);

  final ProductTemporal _value;

  @override
  ProductTemporal type(ProductTemporalTypeEnum type) => call(type: type);

  @override
  ProductTemporal cadence(String? cadence) => call(cadence: cadence);

  @override
  ProductTemporal maxGap(String? maxGap) => call(maxGap: maxGap);

  @override
  ProductTemporal earliest(DateTime? earliest) => call(earliest: earliest);

  @override
  ProductTemporal latest(DateTime? latest) => call(latest: latest);

  @override
  ProductTemporal retention(ProductTemporalRetention? retention) =>
      call(retention: retention);

  @override
  ProductTemporal runs(List<ProductTemporalRunsInner>? runs) =>
      call(runs: runs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ProductTemporal(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ProductTemporal(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ProductTemporal call({
    Object? type = const $CopyWithPlaceholder(),
    Object? cadence = const $CopyWithPlaceholder(),
    Object? maxGap = const $CopyWithPlaceholder(),
    Object? earliest = const $CopyWithPlaceholder(),
    Object? latest = const $CopyWithPlaceholder(),
    Object? retention = const $CopyWithPlaceholder(),
    Object? runs = const $CopyWithPlaceholder(),
  }) {
    return ProductTemporal(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ProductTemporalTypeEnum,
      cadence: cadence == const $CopyWithPlaceholder()
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as String?,
      maxGap: maxGap == const $CopyWithPlaceholder()
          ? _value.maxGap
          // ignore: cast_nullable_to_non_nullable
          : maxGap as String?,
      earliest: earliest == const $CopyWithPlaceholder()
          ? _value.earliest
          // ignore: cast_nullable_to_non_nullable
          : earliest as DateTime?,
      latest: latest == const $CopyWithPlaceholder()
          ? _value.latest
          // ignore: cast_nullable_to_non_nullable
          : latest as DateTime?,
      retention: retention == const $CopyWithPlaceholder()
          ? _value.retention
          // ignore: cast_nullable_to_non_nullable
          : retention as ProductTemporalRetention?,
      runs: runs == const $CopyWithPlaceholder()
          ? _value.runs
          // ignore: cast_nullable_to_non_nullable
          : runs as List<ProductTemporalRunsInner>?,
    );
  }
}

extension $ProductTemporalCopyWith on ProductTemporal {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfProductTemporal.copyWith(...)` or `instanceOfProductTemporal.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProductTemporalCWProxy get copyWith => _$ProductTemporalCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ProductTemporal _$ProductTemporalFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ProductTemporal', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type']);
      final val = ProductTemporal(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ProductTemporalTypeEnumEnumMap, v),
        ),
        cadence: $checkedConvert('cadence', (v) => v as String?),
        maxGap: $checkedConvert('maxGap', (v) => v as String?),
        earliest: $checkedConvert(
          'earliest',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        latest: $checkedConvert(
          'latest',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        retention: $checkedConvert(
          'retention',
          (v) => v == null
              ? null
              : ProductTemporalRetention.fromJson(v as Map<String, dynamic>),
        ),
        runs: $checkedConvert(
          'runs',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => ProductTemporalRunsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ProductTemporalToJson(ProductTemporal instance) =>
    <String, dynamic>{
      'type': _$ProductTemporalTypeEnumEnumMap[instance.type]!,
      'cadence': ?instance.cadence,
      'maxGap': ?instance.maxGap,
      'earliest': ?instance.earliest?.toIso8601String(),
      'latest': ?instance.latest?.toIso8601String(),
      'retention': ?instance.retention?.toJson(),
      'runs': ?instance.runs?.map((e) => e.toJson()).toList(),
    };

const _$ProductTemporalTypeEnumEnumMap = {
  ProductTemporalTypeEnum.observation: 'observation',
  ProductTemporalTypeEnum.forecast: 'forecast',
  ProductTemporalTypeEnum.pass: 'pass',
};
