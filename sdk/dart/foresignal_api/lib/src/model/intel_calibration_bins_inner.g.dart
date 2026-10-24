// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_calibration_bins_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelCalibrationBinsInnerCWProxy {
  IntelCalibrationBinsInner lower(num? lower);

  IntelCalibrationBinsInner upper(num? upper);

  IntelCalibrationBinsInner n(int? n);

  IntelCalibrationBinsInner meanForecast(num? meanForecast);

  IntelCalibrationBinsInner observedFrequency(num? observedFrequency);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCalibrationBinsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCalibrationBinsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelCalibrationBinsInner call({
    num? lower,
    num? upper,
    int? n,
    num? meanForecast,
    num? observedFrequency,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelCalibrationBinsInner.copyWith(...)` or call `instanceOfIntelCalibrationBinsInner.copyWith.fieldName(value)` for a single field.
class _$IntelCalibrationBinsInnerCWProxyImpl
    implements _$IntelCalibrationBinsInnerCWProxy {
  const _$IntelCalibrationBinsInnerCWProxyImpl(this._value);

  final IntelCalibrationBinsInner _value;

  @override
  IntelCalibrationBinsInner lower(num? lower) => call(lower: lower);

  @override
  IntelCalibrationBinsInner upper(num? upper) => call(upper: upper);

  @override
  IntelCalibrationBinsInner n(int? n) => call(n: n);

  @override
  IntelCalibrationBinsInner meanForecast(num? meanForecast) =>
      call(meanForecast: meanForecast);

  @override
  IntelCalibrationBinsInner observedFrequency(num? observedFrequency) =>
      call(observedFrequency: observedFrequency);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCalibrationBinsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCalibrationBinsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelCalibrationBinsInner call({
    Object? lower = const $CopyWithPlaceholder(),
    Object? upper = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? meanForecast = const $CopyWithPlaceholder(),
    Object? observedFrequency = const $CopyWithPlaceholder(),
  }) {
    return IntelCalibrationBinsInner(
      lower: lower == const $CopyWithPlaceholder()
          ? _value.lower
          // ignore: cast_nullable_to_non_nullable
          : lower as num?,
      upper: upper == const $CopyWithPlaceholder()
          ? _value.upper
          // ignore: cast_nullable_to_non_nullable
          : upper as num?,
      n: n == const $CopyWithPlaceholder()
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int?,
      meanForecast: meanForecast == const $CopyWithPlaceholder()
          ? _value.meanForecast
          // ignore: cast_nullable_to_non_nullable
          : meanForecast as num?,
      observedFrequency: observedFrequency == const $CopyWithPlaceholder()
          ? _value.observedFrequency
          // ignore: cast_nullable_to_non_nullable
          : observedFrequency as num?,
    );
  }
}

extension $IntelCalibrationBinsInnerCopyWith on IntelCalibrationBinsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelCalibrationBinsInner.copyWith(...)` or `instanceOfIntelCalibrationBinsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelCalibrationBinsInnerCWProxy get copyWith =>
      _$IntelCalibrationBinsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelCalibrationBinsInner _$IntelCalibrationBinsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelCalibrationBinsInner', json, ($checkedConvert) {
  final val = IntelCalibrationBinsInner(
    lower: $checkedConvert('lower', (v) => v as num?),
    upper: $checkedConvert('upper', (v) => v as num?),
    n: $checkedConvert('n', (v) => (v as num?)?.toInt()),
    meanForecast: $checkedConvert('meanForecast', (v) => v as num?),
    observedFrequency: $checkedConvert('observedFrequency', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$IntelCalibrationBinsInnerToJson(
  IntelCalibrationBinsInner instance,
) => <String, dynamic>{
  'lower': ?instance.lower,
  'upper': ?instance.upper,
  'n': ?instance.n,
  'meanForecast': ?instance.meanForecast,
  'observedFrequency': ?instance.observedFrequency,
};
