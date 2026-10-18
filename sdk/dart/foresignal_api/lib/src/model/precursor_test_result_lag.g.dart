// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_result_lag.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestResultLagCWProxy {
  PrecursorTestResultLag min(String? min);

  PrecursorTestResultLag p25(String? p25);

  PrecursorTestResultLag median(String? median);

  PrecursorTestResultLag p75(String? p75);

  PrecursorTestResultLag max(String? max);

  PrecursorTestResultLag mean(String? mean);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestResultLag(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestResultLag(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestResultLag call({
    String? min,
    String? p25,
    String? median,
    String? p75,
    String? max,
    String? mean,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestResultLag.copyWith(...)` or call `instanceOfPrecursorTestResultLag.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestResultLagCWProxyImpl
    implements _$PrecursorTestResultLagCWProxy {
  const _$PrecursorTestResultLagCWProxyImpl(this._value);

  final PrecursorTestResultLag _value;

  @override
  PrecursorTestResultLag min(String? min) => call(min: min);

  @override
  PrecursorTestResultLag p25(String? p25) => call(p25: p25);

  @override
  PrecursorTestResultLag median(String? median) => call(median: median);

  @override
  PrecursorTestResultLag p75(String? p75) => call(p75: p75);

  @override
  PrecursorTestResultLag max(String? max) => call(max: max);

  @override
  PrecursorTestResultLag mean(String? mean) => call(mean: mean);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestResultLag(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestResultLag(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestResultLag call({
    Object? min = const $CopyWithPlaceholder(),
    Object? p25 = const $CopyWithPlaceholder(),
    Object? median = const $CopyWithPlaceholder(),
    Object? p75 = const $CopyWithPlaceholder(),
    Object? max = const $CopyWithPlaceholder(),
    Object? mean = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestResultLag(
      min: min == const $CopyWithPlaceholder()
          ? _value.min
          // ignore: cast_nullable_to_non_nullable
          : min as String?,
      p25: p25 == const $CopyWithPlaceholder()
          ? _value.p25
          // ignore: cast_nullable_to_non_nullable
          : p25 as String?,
      median: median == const $CopyWithPlaceholder()
          ? _value.median
          // ignore: cast_nullable_to_non_nullable
          : median as String?,
      p75: p75 == const $CopyWithPlaceholder()
          ? _value.p75
          // ignore: cast_nullable_to_non_nullable
          : p75 as String?,
      max: max == const $CopyWithPlaceholder()
          ? _value.max
          // ignore: cast_nullable_to_non_nullable
          : max as String?,
      mean: mean == const $CopyWithPlaceholder()
          ? _value.mean
          // ignore: cast_nullable_to_non_nullable
          : mean as String?,
    );
  }
}

extension $PrecursorTestResultLagCopyWith on PrecursorTestResultLag {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestResultLag.copyWith(...)` or `instanceOfPrecursorTestResultLag.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestResultLagCWProxy get copyWith =>
      _$PrecursorTestResultLagCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestResultLag _$PrecursorTestResultLagFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestResultLag', json, ($checkedConvert) {
  final val = PrecursorTestResultLag(
    min: $checkedConvert('min', (v) => v as String?),
    p25: $checkedConvert('p25', (v) => v as String?),
    median: $checkedConvert('median', (v) => v as String?),
    p75: $checkedConvert('p75', (v) => v as String?),
    max: $checkedConvert('max', (v) => v as String?),
    mean: $checkedConvert('mean', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestResultLagToJson(
  PrecursorTestResultLag instance,
) => <String, dynamic>{
  'min': ?instance.min,
  'p25': ?instance.p25,
  'median': ?instance.median,
  'p75': ?instance.p75,
  'max': ?instance.max,
  'mean': ?instance.mean,
};
