// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hazard_forecast_probability_range.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HazardForecastProbabilityRangeCWProxy {
  HazardForecastProbabilityRange low(num low);

  HazardForecastProbabilityRange high(num? high);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HazardForecastProbabilityRange(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HazardForecastProbabilityRange(...).copyWith(id: 12, name: "My name")
  /// ```
  HazardForecastProbabilityRange call({num low, num? high});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHazardForecastProbabilityRange.copyWith(...)` or call `instanceOfHazardForecastProbabilityRange.copyWith.fieldName(value)` for a single field.
class _$HazardForecastProbabilityRangeCWProxyImpl
    implements _$HazardForecastProbabilityRangeCWProxy {
  const _$HazardForecastProbabilityRangeCWProxyImpl(this._value);

  final HazardForecastProbabilityRange _value;

  @override
  HazardForecastProbabilityRange low(num low) => call(low: low);

  @override
  HazardForecastProbabilityRange high(num? high) => call(high: high);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HazardForecastProbabilityRange(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HazardForecastProbabilityRange(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HazardForecastProbabilityRange call({
    Object? low = const $CopyWithPlaceholder(),
    Object? high = const $CopyWithPlaceholder(),
  }) {
    return HazardForecastProbabilityRange(
      low: low == const $CopyWithPlaceholder() || low == null
          ? _value.low
          // ignore: cast_nullable_to_non_nullable
          : low as num,
      high: high == const $CopyWithPlaceholder()
          ? _value.high
          // ignore: cast_nullable_to_non_nullable
          : high as num?,
    );
  }
}

extension $HazardForecastProbabilityRangeCopyWith
    on HazardForecastProbabilityRange {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHazardForecastProbabilityRange.copyWith(...)` or `instanceOfHazardForecastProbabilityRange.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HazardForecastProbabilityRangeCWProxy get copyWith =>
      _$HazardForecastProbabilityRangeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HazardForecastProbabilityRange _$HazardForecastProbabilityRangeFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HazardForecastProbabilityRange', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['low']);
  final val = HazardForecastProbabilityRange(
    low: $checkedConvert('low', (v) => v as num),
    high: $checkedConvert('high', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$HazardForecastProbabilityRangeToJson(
  HazardForecastProbabilityRange instance,
) => <String, dynamic>{'low': instance.low, 'high': ?instance.high};
