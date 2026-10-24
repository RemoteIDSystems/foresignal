// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_numeric_calibration_pit.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastNumericCalibrationPitCWProxy {
  ForecastNumericCalibrationPit n(int? n);

  ForecastNumericCalibrationPit bins(
    List<ForecastNumericCalibrationPitBinsInner>? bins,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastNumericCalibrationPit(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastNumericCalibrationPit(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastNumericCalibrationPit call({
    int? n,
    List<ForecastNumericCalibrationPitBinsInner>? bins,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastNumericCalibrationPit.copyWith(...)` or call `instanceOfForecastNumericCalibrationPit.copyWith.fieldName(value)` for a single field.
class _$ForecastNumericCalibrationPitCWProxyImpl
    implements _$ForecastNumericCalibrationPitCWProxy {
  const _$ForecastNumericCalibrationPitCWProxyImpl(this._value);

  final ForecastNumericCalibrationPit _value;

  @override
  ForecastNumericCalibrationPit n(int? n) => call(n: n);

  @override
  ForecastNumericCalibrationPit bins(
    List<ForecastNumericCalibrationPitBinsInner>? bins,
  ) => call(bins: bins);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastNumericCalibrationPit(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastNumericCalibrationPit(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastNumericCalibrationPit call({
    Object? n = const $CopyWithPlaceholder(),
    Object? bins = const $CopyWithPlaceholder(),
  }) {
    return ForecastNumericCalibrationPit(
      n: n == const $CopyWithPlaceholder()
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int?,
      bins: bins == const $CopyWithPlaceholder()
          ? _value.bins
          // ignore: cast_nullable_to_non_nullable
          : bins as List<ForecastNumericCalibrationPitBinsInner>?,
    );
  }
}

extension $ForecastNumericCalibrationPitCopyWith
    on ForecastNumericCalibrationPit {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastNumericCalibrationPit.copyWith(...)` or `instanceOfForecastNumericCalibrationPit.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastNumericCalibrationPitCWProxy get copyWith =>
      _$ForecastNumericCalibrationPitCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastNumericCalibrationPit _$ForecastNumericCalibrationPitFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastNumericCalibrationPit', json, ($checkedConvert) {
  final val = ForecastNumericCalibrationPit(
    n: $checkedConvert('n', (v) => (v as num?)?.toInt()),
    bins: $checkedConvert(
      'bins',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ForecastNumericCalibrationPitBinsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$ForecastNumericCalibrationPitToJson(
  ForecastNumericCalibrationPit instance,
) => <String, dynamic>{
  'n': ?instance.n,
  'bins': ?instance.bins?.map((e) => e.toJson()).toList(),
};
