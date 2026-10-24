// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_numeric_calibration.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastNumericCalibrationCWProxy {
  ForecastNumericCalibration forecasterId(String? forecasterId);

  ForecastNumericCalibration category(String? category);

  ForecastNumericCalibration type(ForecastNumericCalibrationTypeEnum type);

  ForecastNumericCalibration n(int n);

  ForecastNumericCalibration label(ForecastNumericCalibrationLabelEnum label);

  ForecastNumericCalibration quantileReliability(
    List<ForecastNumericCalibrationQuantileReliabilityInner>
    quantileReliability,
  );

  ForecastNumericCalibration pit(ForecastNumericCalibrationPit pit);

  ForecastNumericCalibration summary(
    ForecastNumericCalibrationSummary? summary,
  );

  ForecastNumericCalibration scorerVersion(String scorerVersion);

  ForecastNumericCalibration computedAt(DateTime computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastNumericCalibration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastNumericCalibration(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastNumericCalibration call({
    String? forecasterId,
    String? category,
    ForecastNumericCalibrationTypeEnum type,
    int n,
    ForecastNumericCalibrationLabelEnum label,
    List<ForecastNumericCalibrationQuantileReliabilityInner>
    quantileReliability,
    ForecastNumericCalibrationPit pit,
    ForecastNumericCalibrationSummary? summary,
    String scorerVersion,
    DateTime computedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastNumericCalibration.copyWith(...)` or call `instanceOfForecastNumericCalibration.copyWith.fieldName(value)` for a single field.
class _$ForecastNumericCalibrationCWProxyImpl
    implements _$ForecastNumericCalibrationCWProxy {
  const _$ForecastNumericCalibrationCWProxyImpl(this._value);

  final ForecastNumericCalibration _value;

  @override
  ForecastNumericCalibration forecasterId(String? forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  ForecastNumericCalibration category(String? category) =>
      call(category: category);

  @override
  ForecastNumericCalibration type(ForecastNumericCalibrationTypeEnum type) =>
      call(type: type);

  @override
  ForecastNumericCalibration n(int n) => call(n: n);

  @override
  ForecastNumericCalibration label(ForecastNumericCalibrationLabelEnum label) =>
      call(label: label);

  @override
  ForecastNumericCalibration quantileReliability(
    List<ForecastNumericCalibrationQuantileReliabilityInner>
    quantileReliability,
  ) => call(quantileReliability: quantileReliability);

  @override
  ForecastNumericCalibration pit(ForecastNumericCalibrationPit pit) =>
      call(pit: pit);

  @override
  ForecastNumericCalibration summary(
    ForecastNumericCalibrationSummary? summary,
  ) => call(summary: summary);

  @override
  ForecastNumericCalibration scorerVersion(String scorerVersion) =>
      call(scorerVersion: scorerVersion);

  @override
  ForecastNumericCalibration computedAt(DateTime computedAt) =>
      call(computedAt: computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastNumericCalibration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastNumericCalibration(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastNumericCalibration call({
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? quantileReliability = const $CopyWithPlaceholder(),
    Object? pit = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? scorerVersion = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
  }) {
    return ForecastNumericCalibration(
      forecasterId: forecasterId == const $CopyWithPlaceholder()
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String?,
      category: category == const $CopyWithPlaceholder()
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String?,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ForecastNumericCalibrationTypeEnum,
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as ForecastNumericCalibrationLabelEnum,
      quantileReliability:
          quantileReliability == const $CopyWithPlaceholder() ||
              quantileReliability == null
          ? _value.quantileReliability
          // ignore: cast_nullable_to_non_nullable
          : quantileReliability
                as List<ForecastNumericCalibrationQuantileReliabilityInner>,
      pit: pit == const $CopyWithPlaceholder() || pit == null
          ? _value.pit
          // ignore: cast_nullable_to_non_nullable
          : pit as ForecastNumericCalibrationPit,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as ForecastNumericCalibrationSummary?,
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

extension $ForecastNumericCalibrationCopyWith on ForecastNumericCalibration {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastNumericCalibration.copyWith(...)` or `instanceOfForecastNumericCalibration.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastNumericCalibrationCWProxy get copyWith =>
      _$ForecastNumericCalibrationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastNumericCalibration _$ForecastNumericCalibrationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastNumericCalibration', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'type',
      'n',
      'label',
      'quantileReliability',
      'pit',
      'scorerVersion',
      'computedAt',
    ],
  );
  final val = ForecastNumericCalibration(
    forecasterId: $checkedConvert('forecasterId', (v) => v as String?),
    category: $checkedConvert('category', (v) => v as String?),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ForecastNumericCalibrationTypeEnumEnumMap, v),
    ),
    n: $checkedConvert('n', (v) => (v as num).toInt()),
    label: $checkedConvert(
      'label',
      (v) => $enumDecode(_$ForecastNumericCalibrationLabelEnumEnumMap, v),
    ),
    quantileReliability: $checkedConvert(
      'quantileReliability',
      (v) => (v as List<dynamic>)
          .map(
            (e) => ForecastNumericCalibrationQuantileReliabilityInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    pit: $checkedConvert(
      'pit',
      (v) => ForecastNumericCalibrationPit.fromJson(v as Map<String, dynamic>),
    ),
    summary: $checkedConvert(
      'summary',
      (v) => v == null
          ? null
          : ForecastNumericCalibrationSummary.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
    scorerVersion: $checkedConvert('scorerVersion', (v) => v as String),
    computedAt: $checkedConvert(
      'computedAt',
      (v) => DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ForecastNumericCalibrationToJson(
  ForecastNumericCalibration instance,
) => <String, dynamic>{
  'forecasterId': ?instance.forecasterId,
  'category': ?instance.category,
  'type': _$ForecastNumericCalibrationTypeEnumEnumMap[instance.type]!,
  'n': instance.n,
  'label': _$ForecastNumericCalibrationLabelEnumEnumMap[instance.label]!,
  'quantileReliability': instance.quantileReliability
      .map((e) => e.toJson())
      .toList(),
  'pit': instance.pit.toJson(),
  'summary': ?instance.summary?.toJson(),
  'scorerVersion': instance.scorerVersion,
  'computedAt': instance.computedAt.toIso8601String(),
};

const _$ForecastNumericCalibrationTypeEnumEnumMap = {
  ForecastNumericCalibrationTypeEnum.numeric: 'numeric',
};

const _$ForecastNumericCalibrationLabelEnumEnumMap = {
  ForecastNumericCalibrationLabelEnum.ok: 'ok',
  ForecastNumericCalibrationLabelEnum.insufficient: 'insufficient',
};
