// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_calibration.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelCalibrationCWProxy {
  IntelCalibration scope(IntelCalibrationScopeEnum scope);

  IntelCalibration calibrated(bool calibrated);

  IntelCalibration sampleN(int sampleN);

  IntelCalibration minSamples(int minSamples);

  IntelCalibration brier(num? brier);

  IntelCalibration bins(List<IntelCalibrationBinsInner>? bins);

  IntelCalibration curve(List<IntelCalibrationCurveInner>? curve);

  IntelCalibration fittedAt(DateTime? fittedAt);

  IntelCalibration updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCalibration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCalibration(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelCalibration call({
    IntelCalibrationScopeEnum scope,
    bool calibrated,
    int sampleN,
    int minSamples,
    num? brier,
    List<IntelCalibrationBinsInner>? bins,
    List<IntelCalibrationCurveInner>? curve,
    DateTime? fittedAt,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelCalibration.copyWith(...)` or call `instanceOfIntelCalibration.copyWith.fieldName(value)` for a single field.
class _$IntelCalibrationCWProxyImpl implements _$IntelCalibrationCWProxy {
  const _$IntelCalibrationCWProxyImpl(this._value);

  final IntelCalibration _value;

  @override
  IntelCalibration scope(IntelCalibrationScopeEnum scope) => call(scope: scope);

  @override
  IntelCalibration calibrated(bool calibrated) => call(calibrated: calibrated);

  @override
  IntelCalibration sampleN(int sampleN) => call(sampleN: sampleN);

  @override
  IntelCalibration minSamples(int minSamples) => call(minSamples: minSamples);

  @override
  IntelCalibration brier(num? brier) => call(brier: brier);

  @override
  IntelCalibration bins(List<IntelCalibrationBinsInner>? bins) =>
      call(bins: bins);

  @override
  IntelCalibration curve(List<IntelCalibrationCurveInner>? curve) =>
      call(curve: curve);

  @override
  IntelCalibration fittedAt(DateTime? fittedAt) => call(fittedAt: fittedAt);

  @override
  IntelCalibration updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelCalibration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelCalibration(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelCalibration call({
    Object? scope = const $CopyWithPlaceholder(),
    Object? calibrated = const $CopyWithPlaceholder(),
    Object? sampleN = const $CopyWithPlaceholder(),
    Object? minSamples = const $CopyWithPlaceholder(),
    Object? brier = const $CopyWithPlaceholder(),
    Object? bins = const $CopyWithPlaceholder(),
    Object? curve = const $CopyWithPlaceholder(),
    Object? fittedAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return IntelCalibration(
      scope: scope == const $CopyWithPlaceholder() || scope == null
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as IntelCalibrationScopeEnum,
      calibrated:
          calibrated == const $CopyWithPlaceholder() || calibrated == null
          ? _value.calibrated
          // ignore: cast_nullable_to_non_nullable
          : calibrated as bool,
      sampleN: sampleN == const $CopyWithPlaceholder() || sampleN == null
          ? _value.sampleN
          // ignore: cast_nullable_to_non_nullable
          : sampleN as int,
      minSamples:
          minSamples == const $CopyWithPlaceholder() || minSamples == null
          ? _value.minSamples
          // ignore: cast_nullable_to_non_nullable
          : minSamples as int,
      brier: brier == const $CopyWithPlaceholder()
          ? _value.brier
          // ignore: cast_nullable_to_non_nullable
          : brier as num?,
      bins: bins == const $CopyWithPlaceholder()
          ? _value.bins
          // ignore: cast_nullable_to_non_nullable
          : bins as List<IntelCalibrationBinsInner>?,
      curve: curve == const $CopyWithPlaceholder()
          ? _value.curve
          // ignore: cast_nullable_to_non_nullable
          : curve as List<IntelCalibrationCurveInner>?,
      fittedAt: fittedAt == const $CopyWithPlaceholder()
          ? _value.fittedAt
          // ignore: cast_nullable_to_non_nullable
          : fittedAt as DateTime?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $IntelCalibrationCopyWith on IntelCalibration {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelCalibration.copyWith(...)` or `instanceOfIntelCalibration.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelCalibrationCWProxy get copyWith => _$IntelCalibrationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelCalibration _$IntelCalibrationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelCalibration', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['scope', 'calibrated', 'sampleN', 'minSamples'],
  );
  final val = IntelCalibration(
    scope: $checkedConvert(
      'scope',
      (v) => $enumDecode(_$IntelCalibrationScopeEnumEnumMap, v),
    ),
    calibrated: $checkedConvert('calibrated', (v) => v as bool),
    sampleN: $checkedConvert('sampleN', (v) => (v as num).toInt()),
    minSamples: $checkedConvert('minSamples', (v) => (v as num).toInt()),
    brier: $checkedConvert('brier', (v) => v as num?),
    bins: $checkedConvert(
      'bins',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                IntelCalibrationBinsInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    curve: $checkedConvert(
      'curve',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                IntelCalibrationCurveInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    fittedAt: $checkedConvert(
      'fittedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$IntelCalibrationToJson(IntelCalibration instance) =>
    <String, dynamic>{
      'scope': _$IntelCalibrationScopeEnumEnumMap[instance.scope]!,
      'calibrated': instance.calibrated,
      'sampleN': instance.sampleN,
      'minSamples': instance.minSamples,
      'brier': ?instance.brier,
      'bins': ?instance.bins?.map((e) => e.toJson()).toList(),
      'curve': ?instance.curve?.map((e) => e.toJson()).toList(),
      'fittedAt': ?instance.fittedAt?.toIso8601String(),
      'updatedAt': ?instance.updatedAt?.toIso8601String(),
    };

const _$IntelCalibrationScopeEnumEnumMap = {
  IntelCalibrationScopeEnum.intelEvent: 'intel-event',
  IntelCalibrationScopeEnum.precursorActive: 'precursor-active',
};
