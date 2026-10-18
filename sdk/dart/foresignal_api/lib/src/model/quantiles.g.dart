// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'quantiles.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$QuantilesCWProxy {
  Quantiles p05(num p05);

  Quantiles p10(num? p10);

  Quantiles p25(num p25);

  Quantiles p50(num p50);

  Quantiles p75(num p75);

  Quantiles p90(num? p90);

  Quantiles p95(num p95);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Quantiles(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Quantiles(...).copyWith(id: 12, name: "My name")
  /// ```
  Quantiles call({
    num p05,
    num? p10,
    num p25,
    num p50,
    num p75,
    num? p90,
    num p95,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfQuantiles.copyWith(...)` or call `instanceOfQuantiles.copyWith.fieldName(value)` for a single field.
class _$QuantilesCWProxyImpl implements _$QuantilesCWProxy {
  const _$QuantilesCWProxyImpl(this._value);

  final Quantiles _value;

  @override
  Quantiles p05(num p05) => call(p05: p05);

  @override
  Quantiles p10(num? p10) => call(p10: p10);

  @override
  Quantiles p25(num p25) => call(p25: p25);

  @override
  Quantiles p50(num p50) => call(p50: p50);

  @override
  Quantiles p75(num p75) => call(p75: p75);

  @override
  Quantiles p90(num? p90) => call(p90: p90);

  @override
  Quantiles p95(num p95) => call(p95: p95);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Quantiles(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Quantiles(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Quantiles call({
    Object? p05 = const $CopyWithPlaceholder(),
    Object? p10 = const $CopyWithPlaceholder(),
    Object? p25 = const $CopyWithPlaceholder(),
    Object? p50 = const $CopyWithPlaceholder(),
    Object? p75 = const $CopyWithPlaceholder(),
    Object? p90 = const $CopyWithPlaceholder(),
    Object? p95 = const $CopyWithPlaceholder(),
  }) {
    return Quantiles(
      p05: p05 == const $CopyWithPlaceholder() || p05 == null
          ? _value.p05
          // ignore: cast_nullable_to_non_nullable
          : p05 as num,
      p10: p10 == const $CopyWithPlaceholder()
          ? _value.p10
          // ignore: cast_nullable_to_non_nullable
          : p10 as num?,
      p25: p25 == const $CopyWithPlaceholder() || p25 == null
          ? _value.p25
          // ignore: cast_nullable_to_non_nullable
          : p25 as num,
      p50: p50 == const $CopyWithPlaceholder() || p50 == null
          ? _value.p50
          // ignore: cast_nullable_to_non_nullable
          : p50 as num,
      p75: p75 == const $CopyWithPlaceholder() || p75 == null
          ? _value.p75
          // ignore: cast_nullable_to_non_nullable
          : p75 as num,
      p90: p90 == const $CopyWithPlaceholder()
          ? _value.p90
          // ignore: cast_nullable_to_non_nullable
          : p90 as num?,
      p95: p95 == const $CopyWithPlaceholder() || p95 == null
          ? _value.p95
          // ignore: cast_nullable_to_non_nullable
          : p95 as num,
    );
  }
}

extension $QuantilesCopyWith on Quantiles {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfQuantiles.copyWith(...)` or `instanceOfQuantiles.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$QuantilesCWProxy get copyWith => _$QuantilesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Quantiles _$QuantilesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Quantiles', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['p05', 'p25', 'p50', 'p75', 'p95']);
      final val = Quantiles(
        p05: $checkedConvert('p05', (v) => v as num),
        p10: $checkedConvert('p10', (v) => v as num?),
        p25: $checkedConvert('p25', (v) => v as num),
        p50: $checkedConvert('p50', (v) => v as num),
        p75: $checkedConvert('p75', (v) => v as num),
        p90: $checkedConvert('p90', (v) => v as num?),
        p95: $checkedConvert('p95', (v) => v as num),
      );
      return val;
    });

Map<String, dynamic> _$QuantilesToJson(Quantiles instance) => <String, dynamic>{
  'p05': instance.p05,
  'p10': ?instance.p10,
  'p25': instance.p25,
  'p50': instance.p50,
  'p75': instance.p75,
  'p90': ?instance.p90,
  'p95': instance.p95,
};
