// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_calibration.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastCalibrationCWProxy {
  ForecastCalibration forecasterId(String? forecasterId);

  ForecastCalibration category(String? category);

  ForecastCalibration n(int n);

  ForecastCalibration label(ForecastCalibrationLabelEnum label);

  ForecastCalibration method(ForecastCalibrationMethodEnum method);

  ForecastCalibration curve(List<ForecastCalibrationCurveInner> curve);

  ForecastCalibration bins(List<ForecastCalibrationBinsInner> bins);

  ForecastCalibration decomposition(
    ForecastCalibrationDecomposition? decomposition,
  );

  ForecastCalibration sharpness(num? sharpness);

  ForecastCalibration scorerVersion(String scorerVersion);

  ForecastCalibration computedAt(DateTime computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastCalibration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastCalibration(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastCalibration call({
    String? forecasterId,
    String? category,
    int n,
    ForecastCalibrationLabelEnum label,
    ForecastCalibrationMethodEnum method,
    List<ForecastCalibrationCurveInner> curve,
    List<ForecastCalibrationBinsInner> bins,
    ForecastCalibrationDecomposition? decomposition,
    num? sharpness,
    String scorerVersion,
    DateTime computedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastCalibration.copyWith(...)` or call `instanceOfForecastCalibration.copyWith.fieldName(value)` for a single field.
class _$ForecastCalibrationCWProxyImpl implements _$ForecastCalibrationCWProxy {
  const _$ForecastCalibrationCWProxyImpl(this._value);

  final ForecastCalibration _value;

  @override
  ForecastCalibration forecasterId(String? forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  ForecastCalibration category(String? category) => call(category: category);

  @override
  ForecastCalibration n(int n) => call(n: n);

  @override
  ForecastCalibration label(ForecastCalibrationLabelEnum label) =>
      call(label: label);

  @override
  ForecastCalibration method(ForecastCalibrationMethodEnum method) =>
      call(method: method);

  @override
  ForecastCalibration curve(List<ForecastCalibrationCurveInner> curve) =>
      call(curve: curve);

  @override
  ForecastCalibration bins(List<ForecastCalibrationBinsInner> bins) =>
      call(bins: bins);

  @override
  ForecastCalibration decomposition(
    ForecastCalibrationDecomposition? decomposition,
  ) => call(decomposition: decomposition);

  @override
  ForecastCalibration sharpness(num? sharpness) => call(sharpness: sharpness);

  @override
  ForecastCalibration scorerVersion(String scorerVersion) =>
      call(scorerVersion: scorerVersion);

  @override
  ForecastCalibration computedAt(DateTime computedAt) =>
      call(computedAt: computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastCalibration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastCalibration(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastCalibration call({
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? curve = const $CopyWithPlaceholder(),
    Object? bins = const $CopyWithPlaceholder(),
    Object? decomposition = const $CopyWithPlaceholder(),
    Object? sharpness = const $CopyWithPlaceholder(),
    Object? scorerVersion = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
  }) {
    return ForecastCalibration(
      forecasterId: forecasterId == const $CopyWithPlaceholder()
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String?,
      category: category == const $CopyWithPlaceholder()
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String?,
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as ForecastCalibrationLabelEnum,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as ForecastCalibrationMethodEnum,
      curve: curve == const $CopyWithPlaceholder() || curve == null
          ? _value.curve
          // ignore: cast_nullable_to_non_nullable
          : curve as List<ForecastCalibrationCurveInner>,
      bins: bins == const $CopyWithPlaceholder() || bins == null
          ? _value.bins
          // ignore: cast_nullable_to_non_nullable
          : bins as List<ForecastCalibrationBinsInner>,
      decomposition: decomposition == const $CopyWithPlaceholder()
          ? _value.decomposition
          // ignore: cast_nullable_to_non_nullable
          : decomposition as ForecastCalibrationDecomposition?,
      sharpness: sharpness == const $CopyWithPlaceholder()
          ? _value.sharpness
          // ignore: cast_nullable_to_non_nullable
          : sharpness as num?,
      scorerVersion:
          scorerVersion == const $CopyWithPlaceholder() || scorerVersion == null
          ? _value.scorerVersion
          // ignore: cast_nullable_to_non_nullable
          : scorerVersion as String,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
    );
  }
}

extension $ForecastCalibrationCopyWith on ForecastCalibration {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastCalibration.copyWith(...)` or `instanceOfForecastCalibration.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastCalibrationCWProxy get copyWith =>
      _$ForecastCalibrationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastCalibration _$ForecastCalibrationFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ForecastCalibration', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'n',
          'label',
          'method',
          'curve',
          'bins',
          'scorerVersion',
          'computedAt',
        ],
      );
      final val = ForecastCalibration(
        forecasterId: $checkedConvert('forecasterId', (v) => v as String?),
        category: $checkedConvert('category', (v) => v as String?),
        n: $checkedConvert('n', (v) => (v as num).toInt()),
        label: $checkedConvert(
          'label',
          (v) => $enumDecode(_$ForecastCalibrationLabelEnumEnumMap, v),
        ),
        method: $checkedConvert(
          'method',
          (v) => $enumDecode(_$ForecastCalibrationMethodEnumEnumMap, v),
        ),
        curve: $checkedConvert(
          'curve',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ForecastCalibrationCurveInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        bins: $checkedConvert(
          'bins',
          (v) => (v as List<dynamic>)
              .map(
                (e) => ForecastCalibrationBinsInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        decomposition: $checkedConvert(
          'decomposition',
          (v) => v == null
              ? null
              : ForecastCalibrationDecomposition.fromJson(
                  v as Map<String, dynamic>,
                ),
        ),
        sharpness: $checkedConvert('sharpness', (v) => v as num?),
        scorerVersion: $checkedConvert('scorerVersion', (v) => v as String),
        computedAt: $checkedConvert(
          'computedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ForecastCalibrationToJson(
  ForecastCalibration instance,
) => <String, dynamic>{
  'forecasterId': ?instance.forecasterId,
  'category': ?instance.category,
  'n': instance.n,
  'label': _$ForecastCalibrationLabelEnumEnumMap[instance.label]!,
  'method': _$ForecastCalibrationMethodEnumEnumMap[instance.method]!,
  'curve': instance.curve.map((e) => e.toJson()).toList(),
  'bins': instance.bins.map((e) => e.toJson()).toList(),
  'decomposition': ?instance.decomposition?.toJson(),
  'sharpness': ?instance.sharpness,
  'scorerVersion': instance.scorerVersion,
  'computedAt': instance.computedAt.toIso8601String(),
};

const _$ForecastCalibrationLabelEnumEnumMap = {
  ForecastCalibrationLabelEnum.ok: 'ok',
  ForecastCalibrationLabelEnum.insufficient: 'insufficient',
};

const _$ForecastCalibrationMethodEnumEnumMap = {
  ForecastCalibrationMethodEnum.corp: 'corp',
  ForecastCalibrationMethodEnum.bins: 'bins',
};
