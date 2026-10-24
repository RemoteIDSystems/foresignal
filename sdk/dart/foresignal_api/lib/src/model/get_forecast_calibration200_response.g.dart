// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'get_forecast_calibration200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GetForecastCalibration200ResponseCWProxy {
  GetForecastCalibration200Response forecasterId(String? forecasterId);

  GetForecastCalibration200Response category(String? category);

  GetForecastCalibration200Response n(int n);

  GetForecastCalibration200Response label(
    GetForecastCalibration200ResponseLabelEnum label,
  );

  GetForecastCalibration200Response method(
    GetForecastCalibration200ResponseMethodEnum method,
  );

  GetForecastCalibration200Response curve(
    List<ForecastCalibrationCurveInner> curve,
  );

  GetForecastCalibration200Response bins(
    List<ForecastCalibrationBinsInner> bins,
  );

  GetForecastCalibration200Response decomposition(
    ForecastCalibrationDecomposition? decomposition,
  );

  GetForecastCalibration200Response sharpness(num? sharpness);

  GetForecastCalibration200Response scorerVersion(String scorerVersion);

  GetForecastCalibration200Response computedAt(DateTime computedAt);

  GetForecastCalibration200Response type(
    GetForecastCalibration200ResponseTypeEnum type,
  );

  GetForecastCalibration200Response quantileReliability(
    List<ForecastNumericCalibrationQuantileReliabilityInner>
    quantileReliability,
  );

  GetForecastCalibration200Response pit(ForecastNumericCalibrationPit pit);

  GetForecastCalibration200Response summary(
    ForecastNumericCalibrationSummary? summary,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GetForecastCalibration200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GetForecastCalibration200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  GetForecastCalibration200Response call({
    String? forecasterId,
    String? category,
    int n,
    GetForecastCalibration200ResponseLabelEnum label,
    GetForecastCalibration200ResponseMethodEnum method,
    List<ForecastCalibrationCurveInner> curve,
    List<ForecastCalibrationBinsInner> bins,
    ForecastCalibrationDecomposition? decomposition,
    num? sharpness,
    String scorerVersion,
    DateTime computedAt,
    GetForecastCalibration200ResponseTypeEnum type,
    List<ForecastNumericCalibrationQuantileReliabilityInner>
    quantileReliability,
    ForecastNumericCalibrationPit pit,
    ForecastNumericCalibrationSummary? summary,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGetForecastCalibration200Response.copyWith(...)` or call `instanceOfGetForecastCalibration200Response.copyWith.fieldName(value)` for a single field.
class _$GetForecastCalibration200ResponseCWProxyImpl
    implements _$GetForecastCalibration200ResponseCWProxy {
  const _$GetForecastCalibration200ResponseCWProxyImpl(this._value);

  final GetForecastCalibration200Response _value;

  @override
  GetForecastCalibration200Response forecasterId(String? forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  GetForecastCalibration200Response category(String? category) =>
      call(category: category);

  @override
  GetForecastCalibration200Response n(int n) => call(n: n);

  @override
  GetForecastCalibration200Response label(
    GetForecastCalibration200ResponseLabelEnum label,
  ) => call(label: label);

  @override
  GetForecastCalibration200Response method(
    GetForecastCalibration200ResponseMethodEnum method,
  ) => call(method: method);

  @override
  GetForecastCalibration200Response curve(
    List<ForecastCalibrationCurveInner> curve,
  ) => call(curve: curve);

  @override
  GetForecastCalibration200Response bins(
    List<ForecastCalibrationBinsInner> bins,
  ) => call(bins: bins);

  @override
  GetForecastCalibration200Response decomposition(
    ForecastCalibrationDecomposition? decomposition,
  ) => call(decomposition: decomposition);

  @override
  GetForecastCalibration200Response sharpness(num? sharpness) =>
      call(sharpness: sharpness);

  @override
  GetForecastCalibration200Response scorerVersion(String scorerVersion) =>
      call(scorerVersion: scorerVersion);

  @override
  GetForecastCalibration200Response computedAt(DateTime computedAt) =>
      call(computedAt: computedAt);

  @override
  GetForecastCalibration200Response type(
    GetForecastCalibration200ResponseTypeEnum type,
  ) => call(type: type);

  @override
  GetForecastCalibration200Response quantileReliability(
    List<ForecastNumericCalibrationQuantileReliabilityInner>
    quantileReliability,
  ) => call(quantileReliability: quantileReliability);

  @override
  GetForecastCalibration200Response pit(ForecastNumericCalibrationPit pit) =>
      call(pit: pit);

  @override
  GetForecastCalibration200Response summary(
    ForecastNumericCalibrationSummary? summary,
  ) => call(summary: summary);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GetForecastCalibration200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GetForecastCalibration200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GetForecastCalibration200Response call({
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
    Object? type = const $CopyWithPlaceholder(),
    Object? quantileReliability = const $CopyWithPlaceholder(),
    Object? pit = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
  }) {
    return GetForecastCalibration200Response(
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
          : label as GetForecastCalibration200ResponseLabelEnum,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as GetForecastCalibration200ResponseMethodEnum,
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
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as GetForecastCalibration200ResponseTypeEnum,
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
    );
  }
}

extension $GetForecastCalibration200ResponseCopyWith
    on GetForecastCalibration200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGetForecastCalibration200Response.copyWith(...)` or `instanceOfGetForecastCalibration200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GetForecastCalibration200ResponseCWProxy get copyWith =>
      _$GetForecastCalibration200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GetForecastCalibration200Response _$GetForecastCalibration200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('GetForecastCalibration200Response', json, (
  $checkedConvert,
) {
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
      'type',
      'quantileReliability',
      'pit',
    ],
  );
  final val = GetForecastCalibration200Response(
    forecasterId: $checkedConvert('forecasterId', (v) => v as String?),
    category: $checkedConvert('category', (v) => v as String?),
    n: $checkedConvert('n', (v) => (v as num).toInt()),
    label: $checkedConvert(
      'label',
      (v) =>
          $enumDecode(_$GetForecastCalibration200ResponseLabelEnumEnumMap, v),
    ),
    method: $checkedConvert(
      'method',
      (v) =>
          $enumDecode(_$GetForecastCalibration200ResponseMethodEnumEnumMap, v),
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
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$GetForecastCalibration200ResponseTypeEnumEnumMap, v),
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
  );
  return val;
});

Map<String, dynamic> _$GetForecastCalibration200ResponseToJson(
  GetForecastCalibration200Response instance,
) => <String, dynamic>{
  'forecasterId': ?instance.forecasterId,
  'category': ?instance.category,
  'n': instance.n,
  'label': _$GetForecastCalibration200ResponseLabelEnumEnumMap[instance.label]!,
  'method':
      _$GetForecastCalibration200ResponseMethodEnumEnumMap[instance.method]!,
  'curve': instance.curve.map((e) => e.toJson()).toList(),
  'bins': instance.bins.map((e) => e.toJson()).toList(),
  'decomposition': ?instance.decomposition?.toJson(),
  'sharpness': ?instance.sharpness,
  'scorerVersion': instance.scorerVersion,
  'computedAt': instance.computedAt.toIso8601String(),
  'type': _$GetForecastCalibration200ResponseTypeEnumEnumMap[instance.type]!,
  'quantileReliability': instance.quantileReliability
      .map((e) => e.toJson())
      .toList(),
  'pit': instance.pit.toJson(),
  'summary': ?instance.summary?.toJson(),
};

const _$GetForecastCalibration200ResponseLabelEnumEnumMap = {
  GetForecastCalibration200ResponseLabelEnum.ok: 'ok',
  GetForecastCalibration200ResponseLabelEnum.insufficient: 'insufficient',
};

const _$GetForecastCalibration200ResponseMethodEnumEnumMap = {
  GetForecastCalibration200ResponseMethodEnum.corp: 'corp',
  GetForecastCalibration200ResponseMethodEnum.bins: 'bins',
};

const _$GetForecastCalibration200ResponseTypeEnumEnumMap = {
  GetForecastCalibration200ResponseTypeEnum.numeric: 'numeric',
};
