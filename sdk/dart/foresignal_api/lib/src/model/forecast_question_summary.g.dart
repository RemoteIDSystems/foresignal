// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_question_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastQuestionSummaryCWProxy {
  ForecastQuestionSummary id(String id);

  ForecastQuestionSummary title(String title);

  ForecastQuestionSummary type(ForecastQuestionSummaryTypeEnum type);

  ForecastQuestionSummary status(ForecastQuestionSummaryStatusEnum status);

  ForecastQuestionSummary category(String category);

  ForecastQuestionSummary tags(List<String>? tags);

  ForecastQuestionSummary resolverType(
    ForecastQuestionSummaryResolverTypeEnum resolverType,
  );

  ForecastQuestionSummary opensAt(DateTime opensAt);

  ForecastQuestionSummary closesAt(DateTime closesAt);

  ForecastQuestionSummary resolveBy(DateTime resolveBy);

  ForecastQuestionSummary origin(ForecastQuestionSummaryOriginEnum origin);

  ForecastQuestionSummary links(QuestionLinks? links);

  ForecastQuestionSummary private(bool private);

  ForecastQuestionSummary forecastCount(int? forecastCount);

  ForecastQuestionSummary ensembleProbability(num? ensembleProbability);

  ForecastQuestionSummary estimativeTerm(IcdTerm? estimativeTerm);

  ForecastQuestionSummary resolvedOutcome(String? resolvedOutcome);

  ForecastQuestionSummary resolvedValue(num? resolvedValue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastQuestionSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastQuestionSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastQuestionSummary call({
    String id,
    String title,
    ForecastQuestionSummaryTypeEnum type,
    ForecastQuestionSummaryStatusEnum status,
    String category,
    List<String>? tags,
    ForecastQuestionSummaryResolverTypeEnum resolverType,
    DateTime opensAt,
    DateTime closesAt,
    DateTime resolveBy,
    ForecastQuestionSummaryOriginEnum origin,
    QuestionLinks? links,
    bool private,
    int? forecastCount,
    num? ensembleProbability,
    IcdTerm? estimativeTerm,
    String? resolvedOutcome,
    num? resolvedValue,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastQuestionSummary.copyWith(...)` or call `instanceOfForecastQuestionSummary.copyWith.fieldName(value)` for a single field.
class _$ForecastQuestionSummaryCWProxyImpl
    implements _$ForecastQuestionSummaryCWProxy {
  const _$ForecastQuestionSummaryCWProxyImpl(this._value);

  final ForecastQuestionSummary _value;

  @override
  ForecastQuestionSummary id(String id) => call(id: id);

  @override
  ForecastQuestionSummary title(String title) => call(title: title);

  @override
  ForecastQuestionSummary type(ForecastQuestionSummaryTypeEnum type) =>
      call(type: type);

  @override
  ForecastQuestionSummary status(ForecastQuestionSummaryStatusEnum status) =>
      call(status: status);

  @override
  ForecastQuestionSummary category(String category) => call(category: category);

  @override
  ForecastQuestionSummary tags(List<String>? tags) => call(tags: tags);

  @override
  ForecastQuestionSummary resolverType(
    ForecastQuestionSummaryResolverTypeEnum resolverType,
  ) => call(resolverType: resolverType);

  @override
  ForecastQuestionSummary opensAt(DateTime opensAt) => call(opensAt: opensAt);

  @override
  ForecastQuestionSummary closesAt(DateTime closesAt) =>
      call(closesAt: closesAt);

  @override
  ForecastQuestionSummary resolveBy(DateTime resolveBy) =>
      call(resolveBy: resolveBy);

  @override
  ForecastQuestionSummary origin(ForecastQuestionSummaryOriginEnum origin) =>
      call(origin: origin);

  @override
  ForecastQuestionSummary links(QuestionLinks? links) => call(links: links);

  @override
  ForecastQuestionSummary private(bool private) => call(private: private);

  @override
  ForecastQuestionSummary forecastCount(int? forecastCount) =>
      call(forecastCount: forecastCount);

  @override
  ForecastQuestionSummary ensembleProbability(num? ensembleProbability) =>
      call(ensembleProbability: ensembleProbability);

  @override
  ForecastQuestionSummary estimativeTerm(IcdTerm? estimativeTerm) =>
      call(estimativeTerm: estimativeTerm);

  @override
  ForecastQuestionSummary resolvedOutcome(String? resolvedOutcome) =>
      call(resolvedOutcome: resolvedOutcome);

  @override
  ForecastQuestionSummary resolvedValue(num? resolvedValue) =>
      call(resolvedValue: resolvedValue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastQuestionSummary(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastQuestionSummary(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastQuestionSummary call({
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
  }) {
    return ForecastQuestionSummary(
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
          : type as ForecastQuestionSummaryTypeEnum,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as ForecastQuestionSummaryStatusEnum,
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
          : resolverType as ForecastQuestionSummaryResolverTypeEnum,
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
          : origin as ForecastQuestionSummaryOriginEnum,
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
    );
  }
}

extension $ForecastQuestionSummaryCopyWith on ForecastQuestionSummary {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastQuestionSummary.copyWith(...)` or `instanceOfForecastQuestionSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastQuestionSummaryCWProxy get copyWith =>
      _$ForecastQuestionSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastQuestionSummary _$ForecastQuestionSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastQuestionSummary', json, ($checkedConvert) {
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
    ],
  );
  final val = ForecastQuestionSummary(
    id: $checkedConvert('id', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ForecastQuestionSummaryTypeEnumEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ForecastQuestionSummaryStatusEnumEnumMap, v),
    ),
    category: $checkedConvert('category', (v) => v as String),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    resolverType: $checkedConvert(
      'resolverType',
      (v) => $enumDecode(_$ForecastQuestionSummaryResolverTypeEnumEnumMap, v),
    ),
    opensAt: $checkedConvert('opensAt', (v) => DateTime.parse(v as String)),
    closesAt: $checkedConvert('closesAt', (v) => DateTime.parse(v as String)),
    resolveBy: $checkedConvert('resolveBy', (v) => DateTime.parse(v as String)),
    origin: $checkedConvert(
      'origin',
      (v) => $enumDecode(_$ForecastQuestionSummaryOriginEnumEnumMap, v),
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
  );
  return val;
});

Map<String, dynamic> _$ForecastQuestionSummaryToJson(
  ForecastQuestionSummary instance,
) => <String, dynamic>{
  'id': instance.id,
  'title': instance.title,
  'type': _$ForecastQuestionSummaryTypeEnumEnumMap[instance.type]!,
  'status': _$ForecastQuestionSummaryStatusEnumEnumMap[instance.status]!,
  'category': instance.category,
  'tags': ?instance.tags,
  'resolverType':
      _$ForecastQuestionSummaryResolverTypeEnumEnumMap[instance.resolverType]!,
  'opensAt': instance.opensAt.toIso8601String(),
  'closesAt': instance.closesAt.toIso8601String(),
  'resolveBy': instance.resolveBy.toIso8601String(),
  'origin': _$ForecastQuestionSummaryOriginEnumEnumMap[instance.origin]!,
  'links': ?instance.links?.toJson(),
  'private': instance.private,
  'forecastCount': ?instance.forecastCount,
  'ensembleProbability': ?instance.ensembleProbability,
  'estimativeTerm': ?_$IcdTermEnumMap[instance.estimativeTerm],
  'resolvedOutcome': ?instance.resolvedOutcome,
  'resolvedValue': ?instance.resolvedValue,
};

const _$ForecastQuestionSummaryTypeEnumEnumMap = {
  ForecastQuestionSummaryTypeEnum.binary: 'binary',
  ForecastQuestionSummaryTypeEnum.multiple: 'multiple',
  ForecastQuestionSummaryTypeEnum.ordered: 'ordered',
  ForecastQuestionSummaryTypeEnum.numeric: 'numeric',
};

const _$ForecastQuestionSummaryStatusEnumEnumMap = {
  ForecastQuestionSummaryStatusEnum.open: 'open',
  ForecastQuestionSummaryStatusEnum.closed: 'closed',
  ForecastQuestionSummaryStatusEnum.resolved: 'resolved',
  ForecastQuestionSummaryStatusEnum.annulled: 'annulled',
};

const _$ForecastQuestionSummaryResolverTypeEnumEnumMap = {
  ForecastQuestionSummaryResolverTypeEnum.eventExists: 'event-exists',
  ForecastQuestionSummaryResolverTypeEnum.countThreshold: 'count-threshold',
  ForecastQuestionSummaryResolverTypeEnum.seriesValue: 'series-value',
  ForecastQuestionSummaryResolverTypeEnum.alertFired: 'alert-fired',
  ForecastQuestionSummaryResolverTypeEnum.situationStatus: 'situation-status',
  ForecastQuestionSummaryResolverTypeEnum.hypothesisOutcome:
      'hypothesis-outcome',
  ForecastQuestionSummaryResolverTypeEnum.manual: 'manual',
};

const _$ForecastQuestionSummaryOriginEnumEnumMap = {
  ForecastQuestionSummaryOriginEnum.operator_: 'operator',
  ForecastQuestionSummaryOriginEnum.agent: 'agent',
  ForecastQuestionSummaryOriginEnum.system: 'system',
  ForecastQuestionSummaryOriginEnum.analyst: 'analyst',
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
