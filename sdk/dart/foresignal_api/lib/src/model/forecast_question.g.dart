// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_question.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastQuestionCWProxy {
  ForecastQuestion id(String id);

  ForecastQuestion title(String title);

  ForecastQuestion type(ForecastQuestionTypeEnum type);

  ForecastQuestion status(ForecastQuestionStatusEnum status);

  ForecastQuestion category(String category);

  ForecastQuestion tags(List<String>? tags);

  ForecastQuestion resolverType(ForecastQuestionResolverTypeEnum resolverType);

  ForecastQuestion opensAt(DateTime opensAt);

  ForecastQuestion closesAt(DateTime closesAt);

  ForecastQuestion resolveBy(DateTime resolveBy);

  ForecastQuestion origin(ForecastQuestionOriginEnum origin);

  ForecastQuestion links(QuestionLinks? links);

  ForecastQuestion private(bool private);

  ForecastQuestion forecastCount(int? forecastCount);

  ForecastQuestion ensembleProbability(num? ensembleProbability);

  ForecastQuestion estimativeTerm(IcdTerm? estimativeTerm);

  ForecastQuestion resolvedOutcome(String? resolvedOutcome);

  ForecastQuestion resolvedValue(num? resolvedValue);

  ForecastQuestion outcomes(QuestionOutcomes? outcomes);

  ForecastQuestion resolutionCriteria(String resolutionCriteria);

  ForecastQuestion resolver(ForecastResolver resolver);

  ForecastQuestion resolution(ForecastResolution? resolution);

  ForecastQuestion annulment(QuestionAnnulment? annulment);

  ForecastQuestion dedupKey(String? dedupKey);

  ForecastQuestion latestForecasts(List<Forecast> latestForecasts);

  ForecastQuestion ensemble(Forecast? ensemble);

  ForecastQuestion scores(List<ForecastScore>? scores);

  ForecastQuestion createdAt(DateTime? createdAt);

  ForecastQuestion updatedAt(DateTime? updatedAt);

  ForecastQuestion baselineNotes(Map<String, String>? baselineNotes);

  ForecastQuestion manifestId(String? manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastQuestion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastQuestion(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastQuestion call({
    String id,
    String title,
    ForecastQuestionTypeEnum type,
    ForecastQuestionStatusEnum status,
    String category,
    List<String>? tags,
    ForecastQuestionResolverTypeEnum resolverType,
    DateTime opensAt,
    DateTime closesAt,
    DateTime resolveBy,
    ForecastQuestionOriginEnum origin,
    QuestionLinks? links,
    bool private,
    int? forecastCount,
    num? ensembleProbability,
    IcdTerm? estimativeTerm,
    String? resolvedOutcome,
    num? resolvedValue,
    QuestionOutcomes? outcomes,
    String resolutionCriteria,
    ForecastResolver resolver,
    ForecastResolution? resolution,
    QuestionAnnulment? annulment,
    String? dedupKey,
    List<Forecast> latestForecasts,
    Forecast? ensemble,
    List<ForecastScore>? scores,
    DateTime? createdAt,
    DateTime? updatedAt,
    Map<String, String>? baselineNotes,
    String? manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastQuestion.copyWith(...)` or call `instanceOfForecastQuestion.copyWith.fieldName(value)` for a single field.
class _$ForecastQuestionCWProxyImpl implements _$ForecastQuestionCWProxy {
  const _$ForecastQuestionCWProxyImpl(this._value);

  final ForecastQuestion _value;

  @override
  ForecastQuestion id(String id) => call(id: id);

  @override
  ForecastQuestion title(String title) => call(title: title);

  @override
  ForecastQuestion type(ForecastQuestionTypeEnum type) => call(type: type);

  @override
  ForecastQuestion status(ForecastQuestionStatusEnum status) =>
      call(status: status);

  @override
  ForecastQuestion category(String category) => call(category: category);

  @override
  ForecastQuestion tags(List<String>? tags) => call(tags: tags);

  @override
  ForecastQuestion resolverType(
    ForecastQuestionResolverTypeEnum resolverType,
  ) => call(resolverType: resolverType);

  @override
  ForecastQuestion opensAt(DateTime opensAt) => call(opensAt: opensAt);

  @override
  ForecastQuestion closesAt(DateTime closesAt) => call(closesAt: closesAt);

  @override
  ForecastQuestion resolveBy(DateTime resolveBy) => call(resolveBy: resolveBy);

  @override
  ForecastQuestion origin(ForecastQuestionOriginEnum origin) =>
      call(origin: origin);

  @override
  ForecastQuestion links(QuestionLinks? links) => call(links: links);

  @override
  ForecastQuestion private(bool private) => call(private: private);

  @override
  ForecastQuestion forecastCount(int? forecastCount) =>
      call(forecastCount: forecastCount);

  @override
  ForecastQuestion ensembleProbability(num? ensembleProbability) =>
      call(ensembleProbability: ensembleProbability);

  @override
  ForecastQuestion estimativeTerm(IcdTerm? estimativeTerm) =>
      call(estimativeTerm: estimativeTerm);

  @override
  ForecastQuestion resolvedOutcome(String? resolvedOutcome) =>
      call(resolvedOutcome: resolvedOutcome);

  @override
  ForecastQuestion resolvedValue(num? resolvedValue) =>
      call(resolvedValue: resolvedValue);

  @override
  ForecastQuestion outcomes(QuestionOutcomes? outcomes) =>
      call(outcomes: outcomes);

  @override
  ForecastQuestion resolutionCriteria(String resolutionCriteria) =>
      call(resolutionCriteria: resolutionCriteria);

  @override
  ForecastQuestion resolver(ForecastResolver resolver) =>
      call(resolver: resolver);

  @override
  ForecastQuestion resolution(ForecastResolution? resolution) =>
      call(resolution: resolution);

  @override
  ForecastQuestion annulment(QuestionAnnulment? annulment) =>
      call(annulment: annulment);

  @override
  ForecastQuestion dedupKey(String? dedupKey) => call(dedupKey: dedupKey);

  @override
  ForecastQuestion latestForecasts(List<Forecast> latestForecasts) =>
      call(latestForecasts: latestForecasts);

  @override
  ForecastQuestion ensemble(Forecast? ensemble) => call(ensemble: ensemble);

  @override
  ForecastQuestion scores(List<ForecastScore>? scores) => call(scores: scores);

  @override
  ForecastQuestion createdAt(DateTime? createdAt) => call(createdAt: createdAt);

  @override
  ForecastQuestion updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  @override
  ForecastQuestion baselineNotes(Map<String, String>? baselineNotes) =>
      call(baselineNotes: baselineNotes);

  @override
  ForecastQuestion manifestId(String? manifestId) =>
      call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastQuestion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastQuestion(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastQuestion call({
    Object? id = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? resolverType = const $CopyWithPlaceholder(),
    Object? opensAt = const $CopyWithPlaceholder(),
    Object? closesAt = const $CopyWithPlaceholder(),
    Object? resolveBy = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? forecastCount = const $CopyWithPlaceholder(),
    Object? ensembleProbability = const $CopyWithPlaceholder(),
    Object? estimativeTerm = const $CopyWithPlaceholder(),
    Object? resolvedOutcome = const $CopyWithPlaceholder(),
    Object? resolvedValue = const $CopyWithPlaceholder(),
    Object? outcomes = const $CopyWithPlaceholder(),
    Object? resolutionCriteria = const $CopyWithPlaceholder(),
    Object? resolver = const $CopyWithPlaceholder(),
    Object? resolution = const $CopyWithPlaceholder(),
    Object? annulment = const $CopyWithPlaceholder(),
    Object? dedupKey = const $CopyWithPlaceholder(),
    Object? latestForecasts = const $CopyWithPlaceholder(),
    Object? ensemble = const $CopyWithPlaceholder(),
    Object? scores = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? baselineNotes = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return ForecastQuestion(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ForecastQuestionTypeEnum,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ForecastQuestionStatusEnum,
      category: category == const $CopyWithPlaceholder() || category == null
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      resolverType:
          resolverType == const $CopyWithPlaceholder() || resolverType == null
          ? _value.resolverType
          // ignore: cast_nullable_to_non_nullable
          : resolverType as ForecastQuestionResolverTypeEnum,
      opensAt: opensAt == const $CopyWithPlaceholder() || opensAt == null
          ? _value.opensAt
          // ignore: cast_nullable_to_non_nullable
          : opensAt as DateTime,
      closesAt: closesAt == const $CopyWithPlaceholder() || closesAt == null
          ? _value.closesAt
          // ignore: cast_nullable_to_non_nullable
          : closesAt as DateTime,
      resolveBy: resolveBy == const $CopyWithPlaceholder() || resolveBy == null
          ? _value.resolveBy
          // ignore: cast_nullable_to_non_nullable
          : resolveBy as DateTime,
      origin: origin == const $CopyWithPlaceholder() || origin == null
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as ForecastQuestionOriginEnum,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as QuestionLinks?,
      private: private == const $CopyWithPlaceholder() || private == null
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool,
      forecastCount: forecastCount == const $CopyWithPlaceholder()
          ? _value.forecastCount
          // ignore: cast_nullable_to_non_nullable
          : forecastCount as int?,
      ensembleProbability: ensembleProbability == const $CopyWithPlaceholder()
          ? _value.ensembleProbability
          // ignore: cast_nullable_to_non_nullable
          : ensembleProbability as num?,
      estimativeTerm: estimativeTerm == const $CopyWithPlaceholder()
          ? _value.estimativeTerm
          // ignore: cast_nullable_to_non_nullable
          : estimativeTerm as IcdTerm?,
      resolvedOutcome: resolvedOutcome == const $CopyWithPlaceholder()
          ? _value.resolvedOutcome
          // ignore: cast_nullable_to_non_nullable
          : resolvedOutcome as String?,
      resolvedValue: resolvedValue == const $CopyWithPlaceholder()
          ? _value.resolvedValue
          // ignore: cast_nullable_to_non_nullable
          : resolvedValue as num?,
      outcomes: outcomes == const $CopyWithPlaceholder()
          ? _value.outcomes
          // ignore: cast_nullable_to_non_nullable
          : outcomes as QuestionOutcomes?,
      resolutionCriteria:
          resolutionCriteria == const $CopyWithPlaceholder() ||
              resolutionCriteria == null
          ? _value.resolutionCriteria
          // ignore: cast_nullable_to_non_nullable
          : resolutionCriteria as String,
      resolver: resolver == const $CopyWithPlaceholder() || resolver == null
          ? _value.resolver
          // ignore: cast_nullable_to_non_nullable
          : resolver as ForecastResolver,
      resolution: resolution == const $CopyWithPlaceholder()
          ? _value.resolution
          // ignore: cast_nullable_to_non_nullable
          : resolution as ForecastResolution?,
      annulment: annulment == const $CopyWithPlaceholder()
          ? _value.annulment
          // ignore: cast_nullable_to_non_nullable
          : annulment as QuestionAnnulment?,
      dedupKey: dedupKey == const $CopyWithPlaceholder()
          ? _value.dedupKey
          // ignore: cast_nullable_to_non_nullable
          : dedupKey as String?,
      latestForecasts:
          latestForecasts == const $CopyWithPlaceholder() ||
              latestForecasts == null
          ? _value.latestForecasts
          // ignore: cast_nullable_to_non_nullable
          : latestForecasts as List<Forecast>,
      ensemble: ensemble == const $CopyWithPlaceholder()
          ? _value.ensemble
          // ignore: cast_nullable_to_non_nullable
          : ensemble as Forecast?,
      scores: scores == const $CopyWithPlaceholder()
          ? _value.scores
          // ignore: cast_nullable_to_non_nullable
          : scores as List<ForecastScore>?,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
      baselineNotes: baselineNotes == const $CopyWithPlaceholder()
          ? _value.baselineNotes
          // ignore: cast_nullable_to_non_nullable
          : baselineNotes as Map<String, String>?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
    );
  }
}

extension $ForecastQuestionCopyWith on ForecastQuestion {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastQuestion.copyWith(...)` or `instanceOfForecastQuestion.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastQuestionCWProxy get copyWith => _$ForecastQuestionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastQuestion _$ForecastQuestionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastQuestion', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'title',
      'type',
      'status',
      'category',
      'resolverType',
      'opensAt',
      'closesAt',
      'resolveBy',
      'origin',
      'private',
      'resolutionCriteria',
      'resolver',
      'latestForecasts',
    ],
  );
  final val = ForecastQuestion(
    id: $checkedConvert('id', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ForecastQuestionTypeEnumEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ForecastQuestionStatusEnumEnumMap, v),
    ),
    category: $checkedConvert('category', (v) => v as String),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    resolverType: $checkedConvert(
      'resolverType',
      (v) => $enumDecode(_$ForecastQuestionResolverTypeEnumEnumMap, v),
    ),
    opensAt: $checkedConvert('opensAt', (v) => DateTime.parse(v as String)),
    closesAt: $checkedConvert('closesAt', (v) => DateTime.parse(v as String)),
    resolveBy: $checkedConvert('resolveBy', (v) => DateTime.parse(v as String)),
    origin: $checkedConvert(
      'origin',
      (v) => $enumDecode(_$ForecastQuestionOriginEnumEnumMap, v),
    ),
    links: $checkedConvert(
      'links',
      (v) =>
          v == null ? null : QuestionLinks.fromJson(v as Map<String, dynamic>),
    ),
    private: $checkedConvert('private', (v) => v as bool),
    forecastCount: $checkedConvert(
      'forecastCount',
      (v) => (v as num?)?.toInt(),
    ),
    ensembleProbability: $checkedConvert(
      'ensembleProbability',
      (v) => v as num?,
    ),
    estimativeTerm: $checkedConvert(
      'estimativeTerm',
      (v) => $enumDecodeNullable(_$IcdTermEnumMap, v),
    ),
    resolvedOutcome: $checkedConvert('resolvedOutcome', (v) => v as String?),
    resolvedValue: $checkedConvert('resolvedValue', (v) => v as num?),
    outcomes: $checkedConvert(
      'outcomes',
      (v) => v == null
          ? null
          : QuestionOutcomes.fromJson(v as Map<String, dynamic>),
    ),
    resolutionCriteria: $checkedConvert(
      'resolutionCriteria',
      (v) => v as String,
    ),
    resolver: $checkedConvert(
      'resolver',
      (v) => ForecastResolver.fromJson(v as Map<String, dynamic>),
    ),
    resolution: $checkedConvert(
      'resolution',
      (v) => v == null
          ? null
          : ForecastResolution.fromJson(v as Map<String, dynamic>),
    ),
    annulment: $checkedConvert(
      'annulment',
      (v) => v == null
          ? null
          : QuestionAnnulment.fromJson(v as Map<String, dynamic>),
    ),
    dedupKey: $checkedConvert('dedupKey', (v) => v as String?),
    latestForecasts: $checkedConvert(
      'latestForecasts',
      (v) => (v as List<dynamic>)
          .map((e) => Forecast.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    ensemble: $checkedConvert(
      'ensemble',
      (v) => v == null ? null : Forecast.fromJson(v as Map<String, dynamic>),
    ),
    scores: $checkedConvert(
      'scores',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ForecastScore.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    createdAt: $checkedConvert(
      'createdAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    baselineNotes: $checkedConvert(
      'baselineNotes',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ForecastQuestionToJson(
  ForecastQuestion instance,
) => <String, dynamic>{
  'id': instance.id,
  'title': instance.title,
  'type': _$ForecastQuestionTypeEnumEnumMap[instance.type]!,
  'status': _$ForecastQuestionStatusEnumEnumMap[instance.status]!,
  'category': instance.category,
  'tags': ?instance.tags,
  'resolverType':
      _$ForecastQuestionResolverTypeEnumEnumMap[instance.resolverType]!,
  'opensAt': instance.opensAt.toIso8601String(),
  'closesAt': instance.closesAt.toIso8601String(),
  'resolveBy': instance.resolveBy.toIso8601String(),
  'origin': _$ForecastQuestionOriginEnumEnumMap[instance.origin]!,
  'links': ?instance.links?.toJson(),
  'private': instance.private,
  'forecastCount': ?instance.forecastCount,
  'ensembleProbability': ?instance.ensembleProbability,
  'estimativeTerm': ?_$IcdTermEnumMap[instance.estimativeTerm],
  'resolvedOutcome': ?instance.resolvedOutcome,
  'resolvedValue': ?instance.resolvedValue,
  'outcomes': ?instance.outcomes?.toJson(),
  'resolutionCriteria': instance.resolutionCriteria,
  'resolver': instance.resolver.toJson(),
  'resolution': ?instance.resolution?.toJson(),
  'annulment': ?instance.annulment?.toJson(),
  'dedupKey': ?instance.dedupKey,
  'latestForecasts': instance.latestForecasts.map((e) => e.toJson()).toList(),
  'ensemble': ?instance.ensemble?.toJson(),
  'scores': ?instance.scores?.map((e) => e.toJson()).toList(),
  'createdAt': ?instance.createdAt?.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
  'baselineNotes': ?instance.baselineNotes,
  'manifestId': ?instance.manifestId,
};

const _$ForecastQuestionTypeEnumEnumMap = {
  ForecastQuestionTypeEnum.binary: 'binary',
  ForecastQuestionTypeEnum.multiple: 'multiple',
  ForecastQuestionTypeEnum.ordered: 'ordered',
  ForecastQuestionTypeEnum.numeric: 'numeric',
};

const _$ForecastQuestionStatusEnumEnumMap = {
  ForecastQuestionStatusEnum.open: 'open',
  ForecastQuestionStatusEnum.closed: 'closed',
  ForecastQuestionStatusEnum.resolved: 'resolved',
  ForecastQuestionStatusEnum.annulled: 'annulled',
};

const _$ForecastQuestionResolverTypeEnumEnumMap = {
  ForecastQuestionResolverTypeEnum.eventExists: 'event-exists',
  ForecastQuestionResolverTypeEnum.countThreshold: 'count-threshold',
  ForecastQuestionResolverTypeEnum.seriesValue: 'series-value',
  ForecastQuestionResolverTypeEnum.alertFired: 'alert-fired',
  ForecastQuestionResolverTypeEnum.situationStatus: 'situation-status',
  ForecastQuestionResolverTypeEnum.hypothesisOutcome: 'hypothesis-outcome',
  ForecastQuestionResolverTypeEnum.manual: 'manual',
};

const _$ForecastQuestionOriginEnumEnumMap = {
  ForecastQuestionOriginEnum.operator_: 'operator',
  ForecastQuestionOriginEnum.agent: 'agent',
  ForecastQuestionOriginEnum.system: 'system',
  ForecastQuestionOriginEnum.analyst: 'analyst',
};

const _$IcdTermEnumMap = {
  IcdTerm.almostNoChance: 'almost no chance',
  IcdTerm.veryUnlikely: 'very unlikely',
  IcdTerm.unlikely: 'unlikely',
  IcdTerm.roughlyEvenChance: 'roughly even chance',
  IcdTerm.likely: 'likely',
  IcdTerm.veryLikely: 'very likely',
  IcdTerm.almostCertain: 'almost certain',
};
