// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_numeric_calibration_pit_bins_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastNumericCalibrationPitBinsInnerCWProxy {
  ForecastNumericCalibrationPitBinsInner lower(num? lower);

  ForecastNumericCalibrationPitBinsInner upper(num? upper);

  ForecastNumericCalibrationPitBinsInner count(int? count);

  ForecastNumericCalibrationPitBinsInner frequency(num? frequency);

  ForecastNumericCalibrationPitBinsInner expected(num? expected);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastNumericCalibrationPitBinsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastNumericCalibrationPitBinsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastNumericCalibrationPitBinsInner call({
    num? lower,
    num? upper,
    int? count,
    num? frequency,
    num? expected,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastNumericCalibrationPitBinsInner.copyWith(...)` or call `instanceOfForecastNumericCalibrationPitBinsInner.copyWith.fieldName(value)` for a single field.
class _$ForecastNumericCalibrationPitBinsInnerCWProxyImpl
    implements _$ForecastNumericCalibrationPitBinsInnerCWProxy {
  const _$ForecastNumericCalibrationPitBinsInnerCWProxyImpl(this._value);

  final ForecastNumericCalibrationPitBinsInner _value;

  @override
  ForecastNumericCalibrationPitBinsInner lower(num? lower) =>
      call(lower: lower);

  @override
  ForecastNumericCalibrationPitBinsInner upper(num? upper) =>
      call(upper: upper);

  @override
  ForecastNumericCalibrationPitBinsInner count(int? count) =>
      call(count: count);

  @override
  ForecastNumericCalibrationPitBinsInner frequency(num? frequency) =>
      call(frequency: frequency);

  @override
  ForecastNumericCalibrationPitBinsInner expected(num? expected) =>
      call(expected: expected);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastNumericCalibrationPitBinsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastNumericCalibrationPitBinsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastNumericCalibrationPitBinsInner call({
    Object? lower = const $CopyWithPlaceholder(),
    Object? upper = const $CopyWithPlaceholder(),
    Object? count = const $CopyWithPlaceholder(),
    Object? frequency = const $CopyWithPlaceholder(),
    Object? expected = const $CopyWithPlaceholder(),
  }) {
    return ForecastNumericCalibrationPitBinsInner(
      lower: lower == const $CopyWithPlaceholder()
          ? _value.lower
          // ignore: cast_nullable_to_non_nullable
          : lower as num?,
      upper: upper == const $CopyWithPlaceholder()
          ? _value.upper
          // ignore: cast_nullable_to_non_nullable
          : upper as num?,
      count: count == const $CopyWithPlaceholder()
          ? _value.count
          // ignore: cast_nullable_to_non_nullable
          : count as int?,
      frequency: frequency == const $CopyWithPlaceholder()
          ? _value.frequency
          // ignore: cast_nullable_to_non_nullable
          : frequency as num?,
      expected: expected == const $CopyWithPlaceholder()
          ? _value.expected
          // ignore: cast_nullable_to_non_nullable
          : expected as num?,
    );
  }
}

extension $ForecastNumericCalibrationPitBinsInnerCopyWith
    on ForecastNumericCalibrationPitBinsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastNumericCalibrationPitBinsInner.copyWith(...)` or `instanceOfForecastNumericCalibrationPitBinsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastNumericCalibrationPitBinsInnerCWProxy get copyWith =>
      _$ForecastNumericCalibrationPitBinsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastNumericCalibrationPitBinsInner
_$ForecastNumericCalibrationPitBinsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ForecastNumericCalibrationPitBinsInner', json, (
      $checkedConvert,
    ) {
      final val = ForecastNumericCalibrationPitBinsInner(
        lower: $checkedConvert('lower', (v) => v as num?),
        upper: $checkedConvert('upper', (v) => v as num?),
        count: $checkedConvert('count', (v) => (v as num?)?.toInt()),
        frequency: $checkedConvert('frequency', (v) => v as num?),
        expected: $checkedConvert('expected', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$ForecastNumericCalibrationPitBinsInnerToJson(
  ForecastNumericCalibrationPitBinsInner instance,
) => <String, dynamic>{
  'lower': ?instance.lower,
  'upper': ?instance.upper,
  'count': ?instance.count,
  'frequency': ?instance.frequency,
  'expected': ?instance.expected,
};
