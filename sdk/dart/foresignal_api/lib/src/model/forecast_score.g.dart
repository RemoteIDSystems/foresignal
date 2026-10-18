// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_score.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastScoreCWProxy {
  ForecastScore questionId(String questionId);

  ForecastScore forecasterId(String forecasterId);

  ForecastScore brier(num? brier);

  ForecastScore logScore(num? logScore);

  ForecastScore rps(num? rps);

  ForecastScore crps(num? crps);

  ForecastScore pinball(Map<String, num>? pinball);

  ForecastScore intervalHits(ForecastScoreIntervalHits? intervalHits);

  ForecastScore timeAveragedBrier(num? timeAveragedBrier);

  ForecastScore nDays(int nDays);

  ForecastScore finalProbability(num? finalProbability);

  ForecastScore scorerVersion(String scorerVersion);

  ForecastScore computedAt(DateTime computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastScore(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastScore(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastScore call({
    String questionId,
    String forecasterId,
    num? brier,
    num? logScore,
    num? rps,
    num? crps,
    Map<String, num>? pinball,
    ForecastScoreIntervalHits? intervalHits,
    num? timeAveragedBrier,
    int nDays,
    num? finalProbability,
    String scorerVersion,
    DateTime computedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastScore.copyWith(...)` or call `instanceOfForecastScore.copyWith.fieldName(value)` for a single field.
class _$ForecastScoreCWProxyImpl implements _$ForecastScoreCWProxy {
  const _$ForecastScoreCWProxyImpl(this._value);

  final ForecastScore _value;

  @override
  ForecastScore questionId(String questionId) => call(questionId: questionId);

  @override
  ForecastScore forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  ForecastScore brier(num? brier) => call(brier: brier);

  @override
  ForecastScore logScore(num? logScore) => call(logScore: logScore);

  @override
  ForecastScore rps(num? rps) => call(rps: rps);

  @override
  ForecastScore crps(num? crps) => call(crps: crps);

  @override
  ForecastScore pinball(Map<String, num>? pinball) => call(pinball: pinball);

  @override
  ForecastScore intervalHits(ForecastScoreIntervalHits? intervalHits) =>
      call(intervalHits: intervalHits);

  @override
  ForecastScore timeAveragedBrier(num? timeAveragedBrier) =>
      call(timeAveragedBrier: timeAveragedBrier);

  @override
  ForecastScore nDays(int nDays) => call(nDays: nDays);

  @override
  ForecastScore finalProbability(num? finalProbability) =>
      call(finalProbability: finalProbability);

  @override
  ForecastScore scorerVersion(String scorerVersion) =>
      call(scorerVersion: scorerVersion);

  @override
  ForecastScore computedAt(DateTime computedAt) => call(computedAt: computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastScore(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastScore(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastScore call({
    Object? questionId = const $CopyWithPlaceholder(),
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? brier = const $CopyWithPlaceholder(),
    Object? logScore = const $CopyWithPlaceholder(),
    Object? rps = const $CopyWithPlaceholder(),
    Object? crps = const $CopyWithPlaceholder(),
    Object? pinball = const $CopyWithPlaceholder(),
    Object? intervalHits = const $CopyWithPlaceholder(),
    Object? timeAveragedBrier = const $CopyWithPlaceholder(),
    Object? nDays = const $CopyWithPlaceholder(),
    Object? finalProbability = const $CopyWithPlaceholder(),
    Object? scorerVersion = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
  }) {
    return ForecastScore(
      questionId:
          questionId == const $CopyWithPlaceholder() || questionId == null
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String,
      forecasterId:
          forecasterId == const $CopyWithPlaceholder() || forecasterId == null
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String,
      brier: brier == const $CopyWithPlaceholder()
          ? _value.brier
          // ignore: cast_nullable_to_non_nullable
          : brier as num?,
      logScore: logScore == const $CopyWithPlaceholder()
          ? _value.logScore
          // ignore: cast_nullable_to_non_nullable
          : logScore as num?,
      rps: rps == const $CopyWithPlaceholder()
          ? _value.rps
          // ignore: cast_nullable_to_non_nullable
          : rps as num?,
      crps: crps == const $CopyWithPlaceholder()
          ? _value.crps
          // ignore: cast_nullable_to_non_nullable
          : crps as num?,
      pinball: pinball == const $CopyWithPlaceholder()
          ? _value.pinball
          // ignore: cast_nullable_to_non_nullable
          : pinball as Map<String, num>?,
      intervalHits: intervalHits == const $CopyWithPlaceholder()
          ? _value.intervalHits
          // ignore: cast_nullable_to_non_nullable
          : intervalHits as ForecastScoreIntervalHits?,
      timeAveragedBrier: timeAveragedBrier == const $CopyWithPlaceholder()
          ? _value.timeAveragedBrier
          // ignore: cast_nullable_to_non_nullable
          : timeAveragedBrier as num?,
      nDays: nDays == const $CopyWithPlaceholder() || nDays == null
          ? _value.nDays
          // ignore: cast_nullable_to_non_nullable
          : nDays as int,
      finalProbability: finalProbability == const $CopyWithPlaceholder()
          ? _value.finalProbability
          // ignore: cast_nullable_to_non_nullable
          : finalProbability as num?,
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

extension $ForecastScoreCopyWith on ForecastScore {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastScore.copyWith(...)` or `instanceOfForecastScore.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastScoreCWProxy get copyWith => _$ForecastScoreCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastScore _$ForecastScoreFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastScore', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'questionId',
      'forecasterId',
      'nDays',
      'scorerVersion',
      'computedAt',
    ],
  );
  final val = ForecastScore(
    questionId: $checkedConvert('questionId', (v) => v as String),
    forecasterId: $checkedConvert('forecasterId', (v) => v as String),
    brier: $checkedConvert('brier', (v) => v as num?),
    logScore: $checkedConvert('logScore', (v) => v as num?),
    rps: $checkedConvert('rps', (v) => v as num?),
    crps: $checkedConvert('crps', (v) => v as num?),
    pinball: $checkedConvert(
      'pinball',
      (v) => (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as num)),
    ),
    intervalHits: $checkedConvert(
      'intervalHits',
      (v) => v == null
          ? null
          : ForecastScoreIntervalHits.fromJson(v as Map<String, dynamic>),
    ),
    timeAveragedBrier: $checkedConvert('timeAveragedBrier', (v) => v as num?),
    nDays: $checkedConvert('nDays', (v) => (v as num).toInt()),
    finalProbability: $checkedConvert('finalProbability', (v) => v as num?),
    scorerVersion: $checkedConvert('scorerVersion', (v) => v as String),
    computedAt: $checkedConvert(
      'computedAt',
      (v) => DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$ForecastScoreToJson(ForecastScore instance) =>
    <String, dynamic>{
      'questionId': instance.questionId,
      'forecasterId': instance.forecasterId,
      'brier': ?instance.brier,
      'logScore': ?instance.logScore,
      'rps': ?instance.rps,
      'crps': ?instance.crps,
      'pinball': ?instance.pinball,
      'intervalHits': ?instance.intervalHits?.toJson(),
      'timeAveragedBrier': ?instance.timeAveragedBrier,
      'nDays': instance.nDays,
      'finalProbability': ?instance.finalProbability,
      'scorerVersion': instance.scorerVersion,
      'computedAt': instance.computedAt.toIso8601String(),
    };
