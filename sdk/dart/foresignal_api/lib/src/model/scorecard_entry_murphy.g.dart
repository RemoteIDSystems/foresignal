// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scorecard_entry_murphy.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ScorecardEntryMurphyCWProxy {
  ScorecardEntryMurphy reliability(num reliability);

  ScorecardEntryMurphy resolution(num resolution);

  ScorecardEntryMurphy uncertainty(num uncertainty);

  ScorecardEntryMurphy withinBinVariance(num? withinBinVariance);

  ScorecardEntryMurphy withinBinCovariance(num? withinBinCovariance);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ScorecardEntryMurphy(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ScorecardEntryMurphy(...).copyWith(id: 12, name: "My name")
  /// ```
  ScorecardEntryMurphy call({
    num reliability,
    num resolution,
    num uncertainty,
    num? withinBinVariance,
    num? withinBinCovariance,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfScorecardEntryMurphy.copyWith(...)` or call `instanceOfScorecardEntryMurphy.copyWith.fieldName(value)` for a single field.
class _$ScorecardEntryMurphyCWProxyImpl
    implements _$ScorecardEntryMurphyCWProxy {
  const _$ScorecardEntryMurphyCWProxyImpl(this._value);

  final ScorecardEntryMurphy _value;

  @override
  ScorecardEntryMurphy reliability(num reliability) =>
      call(reliability: reliability);

  @override
  ScorecardEntryMurphy resolution(num resolution) =>
      call(resolution: resolution);

  @override
  ScorecardEntryMurphy uncertainty(num uncertainty) =>
      call(uncertainty: uncertainty);

  @override
  ScorecardEntryMurphy withinBinVariance(num? withinBinVariance) =>
      call(withinBinVariance: withinBinVariance);

  @override
  ScorecardEntryMurphy withinBinCovariance(num? withinBinCovariance) =>
      call(withinBinCovariance: withinBinCovariance);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ScorecardEntryMurphy(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ScorecardEntryMurphy(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ScorecardEntryMurphy call({
    Object? reliability = const $CopyWithPlaceholder(),
    Object? resolution = const $CopyWithPlaceholder(),
    Object? uncertainty = const $CopyWithPlaceholder(),
    Object? withinBinVariance = const $CopyWithPlaceholder(),
    Object? withinBinCovariance = const $CopyWithPlaceholder(),
  }) {
    return ScorecardEntryMurphy(
      reliability:
          reliability == const $CopyWithPlaceholder() || reliability == null
          ? _value.reliability
          // ignore: cast_nullable_to_non_nullable
          : reliability as num,
      resolution:
          resolution == const $CopyWithPlaceholder() || resolution == null
          ? _value.resolution
          // ignore: cast_nullable_to_non_nullable
          : resolution as num,
      uncertainty:
          uncertainty == const $CopyWithPlaceholder() || uncertainty == null
          ? _value.uncertainty
          // ignore: cast_nullable_to_non_nullable
          : uncertainty as num,
      withinBinVariance: withinBinVariance == const $CopyWithPlaceholder()
          ? _value.withinBinVariance
          // ignore: cast_nullable_to_non_nullable
          : withinBinVariance as num?,
      withinBinCovariance: withinBinCovariance == const $CopyWithPlaceholder()
          ? _value.withinBinCovariance
          // ignore: cast_nullable_to_non_nullable
          : withinBinCovariance as num?,
    );
  }
}

extension $ScorecardEntryMurphyCopyWith on ScorecardEntryMurphy {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfScorecardEntryMurphy.copyWith(...)` or `instanceOfScorecardEntryMurphy.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ScorecardEntryMurphyCWProxy get copyWith =>
      _$ScorecardEntryMurphyCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ScorecardEntryMurphy _$ScorecardEntryMurphyFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ScorecardEntryMurphy', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['reliability', 'resolution', 'uncertainty'],
  );
  final val = ScorecardEntryMurphy(
    reliability: $checkedConvert('reliability', (v) => v as num),
    resolution: $checkedConvert('resolution', (v) => v as num),
    uncertainty: $checkedConvert('uncertainty', (v) => v as num),
    withinBinVariance: $checkedConvert('withinBinVariance', (v) => v as num?),
    withinBinCovariance: $checkedConvert(
      'withinBinCovariance',
      (v) => v as num?,
    ),
  );
  return val;
});

Map<String, dynamic> _$ScorecardEntryMurphyToJson(
  ScorecardEntryMurphy instance,
) => <String, dynamic>{
  'reliability': instance.reliability,
  'resolution': instance.resolution,
  'uncertainty': instance.uncertainty,
  'withinBinVariance': ?instance.withinBinVariance,
  'withinBinCovariance': ?instance.withinBinCovariance,
};
