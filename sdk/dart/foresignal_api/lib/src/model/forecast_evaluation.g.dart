// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_evaluation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastEvaluationCWProxy {
  ForecastEvaluation id(String id);

  ForecastEvaluation forecasterId(String forecasterId);

  ForecastEvaluation from(DateTime from);

  ForecastEvaluation to(DateTime to);

  ForecastEvaluation categories(List<String>? categories);

  ForecastEvaluation maxQuestions(int? maxQuestions);

  ForecastEvaluation status(ForecastEvaluationStatusEnum status);

  ForecastEvaluation progress(num? progress);

  ForecastEvaluation questionsConsidered(int questionsConsidered);

  ForecastEvaluation questionsEvaluated(int questionsEvaluated);

  ForecastEvaluation skipped(ForecastEvaluationSkipped skipped);

  ForecastEvaluation scorecard(ForecastScorecard? scorecard);

  ForecastEvaluation caveats(List<Caveat> caveats);

  ForecastEvaluation error(String? error);

  ForecastEvaluation createdBy(String createdBy);

  ForecastEvaluation createdAt(DateTime createdAt);

  ForecastEvaluation startedAt(DateTime? startedAt);

  ForecastEvaluation finishedAt(DateTime? finishedAt);

  ForecastEvaluation manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastEvaluation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastEvaluation(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastEvaluation call({
    String id,
    String forecasterId,
    DateTime from,
    DateTime to,
    List<String>? categories,
    int? maxQuestions,
    ForecastEvaluationStatusEnum status,
    num? progress,
    int questionsConsidered,
    int questionsEvaluated,
    ForecastEvaluationSkipped skipped,
    ForecastScorecard? scorecard,
    List<Caveat> caveats,
    String? error,
    String createdBy,
    DateTime createdAt,
    DateTime? startedAt,
    DateTime? finishedAt,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastEvaluation.copyWith(...)` or call `instanceOfForecastEvaluation.copyWith.fieldName(value)` for a single field.
class _$ForecastEvaluationCWProxyImpl implements _$ForecastEvaluationCWProxy {
  const _$ForecastEvaluationCWProxyImpl(this._value);

  final ForecastEvaluation _value;

  @override
  ForecastEvaluation id(String id) => call(id: id);

  @override
  ForecastEvaluation forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  ForecastEvaluation from(DateTime from) => call(from: from);

  @override
  ForecastEvaluation to(DateTime to) => call(to: to);

  @override
  ForecastEvaluation categories(List<String>? categories) =>
      call(categories: categories);

  @override
  ForecastEvaluation maxQuestions(int? maxQuestions) =>
      call(maxQuestions: maxQuestions);

  @override
  ForecastEvaluation status(ForecastEvaluationStatusEnum status) =>
      call(status: status);

  @override
  ForecastEvaluation progress(num? progress) => call(progress: progress);

  @override
  ForecastEvaluation questionsConsidered(int questionsConsidered) =>
      call(questionsConsidered: questionsConsidered);

  @override
  ForecastEvaluation questionsEvaluated(int questionsEvaluated) =>
      call(questionsEvaluated: questionsEvaluated);

  @override
  ForecastEvaluation skipped(ForecastEvaluationSkipped skipped) =>
      call(skipped: skipped);

  @override
  ForecastEvaluation scorecard(ForecastScorecard? scorecard) =>
      call(scorecard: scorecard);

  @override
  ForecastEvaluation caveats(List<Caveat> caveats) => call(caveats: caveats);

  @override
  ForecastEvaluation error(String? error) => call(error: error);

  @override
  ForecastEvaluation createdBy(String createdBy) => call(createdBy: createdBy);

  @override
  ForecastEvaluation createdAt(DateTime createdAt) =>
      call(createdAt: createdAt);

  @override
  ForecastEvaluation startedAt(DateTime? startedAt) =>
      call(startedAt: startedAt);

  @override
  ForecastEvaluation finishedAt(DateTime? finishedAt) =>
      call(finishedAt: finishedAt);

  @override
  ForecastEvaluation manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastEvaluation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastEvaluation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastEvaluation call({
    Object? id = const $CopyWithPlaceholder(),
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? categories = const $CopyWithPlaceholder(),
    Object? maxQuestions = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? progress = const $CopyWithPlaceholder(),
    Object? questionsConsidered = const $CopyWithPlaceholder(),
    Object? questionsEvaluated = const $CopyWithPlaceholder(),
    Object? skipped = const $CopyWithPlaceholder(),
    Object? scorecard = const $CopyWithPlaceholder(),
    Object? caveats = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return ForecastEvaluation(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      forecasterId:
          forecasterId == const $CopyWithPlaceholder() || forecasterId == null
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String,
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      categories: categories == const $CopyWithPlaceholder()
          ? _value.categories
          // ignore: cast_nullable_to_non_nullable
          : categories as List<String>?,
      maxQuestions: maxQuestions == const $CopyWithPlaceholder()
          ? _value.maxQuestions
          // ignore: cast_nullable_to_non_nullable
          : maxQuestions as int?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ForecastEvaluationStatusEnum,
      progress: progress == const $CopyWithPlaceholder()
          ? _value.progress
          // ignore: cast_nullable_to_non_nullable
          : progress as num?,
      questionsConsidered:
          questionsConsidered == const $CopyWithPlaceholder() ||
              questionsConsidered == null
          ? _value.questionsConsidered
          // ignore: cast_nullable_to_non_nullable
          : questionsConsidered as int,
      questionsEvaluated:
          questionsEvaluated == const $CopyWithPlaceholder() ||
              questionsEvaluated == null
          ? _value.questionsEvaluated
          // ignore: cast_nullable_to_non_nullable
          : questionsEvaluated as int,
      skipped: skipped == const $CopyWithPlaceholder() || skipped == null
          ? _value.skipped
          // ignore: cast_nullable_to_non_nullable
          : skipped as ForecastEvaluationSkipped,
      scorecard: scorecard == const $CopyWithPlaceholder()
          ? _value.scorecard
          // ignore: cast_nullable_to_non_nullable
          : scorecard as ForecastScorecard?,
      caveats: caveats == const $CopyWithPlaceholder() || caveats == null
          ? _value.caveats
          // ignore: cast_nullable_to_non_nullable
          : caveats as List<Caveat>,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
      createdBy: createdBy == const $CopyWithPlaceholder() || createdBy == null
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $ForecastEvaluationCopyWith on ForecastEvaluation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastEvaluation.copyWith(...)` or `instanceOfForecastEvaluation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastEvaluationCWProxy get copyWith =>
      _$ForecastEvaluationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastEvaluation _$ForecastEvaluationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastEvaluation', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'forecasterId',
      'from',
      'to',
      'status',
      'questionsConsidered',
      'questionsEvaluated',
      'skipped',
      'caveats',
      'createdBy',
      'createdAt',
    ],
  );
  final val = ForecastEvaluation(
    id: $checkedConvert('id', (v) => v as String),
    forecasterId: $checkedConvert('forecasterId', (v) => v as String),
    from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
    to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
    categories: $checkedConvert(
      'categories',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    maxQuestions: $checkedConvert('maxQuestions', (v) => (v as num?)?.toInt()),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ForecastEvaluationStatusEnumEnumMap, v),
    ),
    progress: $checkedConvert('progress', (v) => v as num?),
    questionsConsidered: $checkedConvert(
      'questionsConsidered',
      (v) => (v as num).toInt(),
    ),
    questionsEvaluated: $checkedConvert(
      'questionsEvaluated',
      (v) => (v as num).toInt(),
    ),
    skipped: $checkedConvert(
      'skipped',
      (v) => ForecastEvaluationSkipped.fromJson(v as Map<String, dynamic>),
    ),
    scorecard: $checkedConvert(
      'scorecard',
      (v) => v == null
          ? null
          : ForecastScorecard.fromJson(v as Map<String, dynamic>),
    ),
    caveats: $checkedConvert(
      'caveats',
      (v) => (v as List<dynamic>)
          .map((e) => Caveat.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    error: $checkedConvert('error', (v) => v as String?),
    createdBy: $checkedConvert('createdBy', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    startedAt: $checkedConvert(
      'startedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ForecastEvaluationToJson(ForecastEvaluation instance) =>
    <String, dynamic>{
      'id': instance.id,
      'forecasterId': instance.forecasterId,
      'from': instance.from.toIso8601String(),
      'to': instance.to.toIso8601String(),
      'categories': ?instance.categories,
      'maxQuestions': ?instance.maxQuestions,
      'status': _$ForecastEvaluationStatusEnumEnumMap[instance.status]!,
      'progress': ?instance.progress,
      'questionsConsidered': instance.questionsConsidered,
      'questionsEvaluated': instance.questionsEvaluated,
      'skipped': instance.skipped.toJson(),
      'scorecard': ?instance.scorecard?.toJson(),
      'caveats': instance.caveats.map((e) => e.toJson()).toList(),
      'error': ?instance.error,
      'createdBy': instance.createdBy,
      'createdAt': instance.createdAt.toIso8601String(),
      'startedAt': ?instance.startedAt?.toIso8601String(),
      'finishedAt': ?instance.finishedAt?.toIso8601String(),
      'manifestId': ?instance.manifestId,
    };

const _$ForecastEvaluationStatusEnumEnumMap = {
  ForecastEvaluationStatusEnum.queued: 'queued',
  ForecastEvaluationStatusEnum.running: 'running',
  ForecastEvaluationStatusEnum.done: 'done',
  ForecastEvaluationStatusEnum.failed: 'failed',
  ForecastEvaluationStatusEnum.cancelled: 'cancelled',
};
