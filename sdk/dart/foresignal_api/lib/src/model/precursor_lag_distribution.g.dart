// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_lag_distribution.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorLagDistributionCWProxy {
  PrecursorLagDistribution p25(String? p25);

  PrecursorLagDistribution median(String? median);

  PrecursorLagDistribution p75(String? p75);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorLagDistribution(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorLagDistribution(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorLagDistribution call({String? p25, String? median, String? p75});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorLagDistribution.copyWith(...)` or call `instanceOfPrecursorLagDistribution.copyWith.fieldName(value)` for a single field.
class _$PrecursorLagDistributionCWProxyImpl
    implements _$PrecursorLagDistributionCWProxy {
  const _$PrecursorLagDistributionCWProxyImpl(this._value);

  final PrecursorLagDistribution _value;

  @override
  PrecursorLagDistribution p25(String? p25) => call(p25: p25);

  @override
  PrecursorLagDistribution median(String? median) => call(median: median);

  @override
  PrecursorLagDistribution p75(String? p75) => call(p75: p75);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorLagDistribution(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorLagDistribution(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorLagDistribution call({
    Object? p25 = const $CopyWithPlaceholder(),
    Object? median = const $CopyWithPlaceholder(),
    Object? p75 = const $CopyWithPlaceholder(),
  }) {
    return PrecursorLagDistribution(
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
    );
  }
}

extension $PrecursorLagDistributionCopyWith on PrecursorLagDistribution {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorLagDistribution.copyWith(...)` or `instanceOfPrecursorLagDistribution.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorLagDistributionCWProxy get copyWith =>
      _$PrecursorLagDistributionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorLagDistribution _$PrecursorLagDistributionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorLagDistribution', json, ($checkedConvert) {
  final val = PrecursorLagDistribution(
    p25: $checkedConvert('p25', (v) => v as String?),
    median: $checkedConvert('median', (v) => v as String?),
    p75: $checkedConvert('p75', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$PrecursorLagDistributionToJson(
  PrecursorLagDistribution instance,
) => <String, dynamic>{
  'p25': ?instance.p25,
  'median': ?instance.median,
  'p75': ?instance.p75,
};
