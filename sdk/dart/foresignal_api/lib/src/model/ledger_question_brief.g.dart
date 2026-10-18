// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ledger_question_brief.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LedgerQuestionBriefCWProxy {
  LedgerQuestionBrief id(String id);

  LedgerQuestionBrief ref(String? ref);

  LedgerQuestionBrief title(String title);

  LedgerQuestionBrief type(LedgerQuestionBriefTypeEnum type);

  LedgerQuestionBrief status(LedgerQuestionBriefStatusEnum status);

  LedgerQuestionBrief closesAt(DateTime? closesAt);

  LedgerQuestionBrief resolveBy(DateTime? resolveBy);

  LedgerQuestionBrief resolutionCriteria(String? resolutionCriteria);

  LedgerQuestionBrief probability(num? probability);

  LedgerQuestionBrief estimativeTerm(IcdTerm? estimativeTerm);

  LedgerQuestionBrief distribution(Object? distribution);

  LedgerQuestionBrief quantiles(Quantiles? quantiles);

  LedgerQuestionBrief issuer(String issuer);

  LedgerQuestionBrief forecasters(
    List<LedgerQuestionBriefForecastersInner>? forecasters,
  );

  LedgerQuestionBrief knowledgeCutoff(DateTime? knowledgeCutoff);

  LedgerQuestionBrief baseRate(num? baseRate);

  LedgerQuestionBrief updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LedgerQuestionBrief(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LedgerQuestionBrief(...).copyWith(id: 12, name: "My name")
  /// ```
  LedgerQuestionBrief call({
    String id,
    String? ref,
    String title,
    LedgerQuestionBriefTypeEnum type,
    LedgerQuestionBriefStatusEnum status,
    DateTime? closesAt,
    DateTime? resolveBy,
    String? resolutionCriteria,
    num? probability,
    IcdTerm? estimativeTerm,
    Object? distribution,
    Quantiles? quantiles,
    String issuer,
    List<LedgerQuestionBriefForecastersInner>? forecasters,
    DateTime? knowledgeCutoff,
    num? baseRate,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLedgerQuestionBrief.copyWith(...)` or call `instanceOfLedgerQuestionBrief.copyWith.fieldName(value)` for a single field.
class _$LedgerQuestionBriefCWProxyImpl implements _$LedgerQuestionBriefCWProxy {
  const _$LedgerQuestionBriefCWProxyImpl(this._value);

  final LedgerQuestionBrief _value;

  @override
  LedgerQuestionBrief id(String id) => call(id: id);

  @override
  LedgerQuestionBrief ref(String? ref) => call(ref: ref);

  @override
  LedgerQuestionBrief title(String title) => call(title: title);

  @override
  LedgerQuestionBrief type(LedgerQuestionBriefTypeEnum type) =>
      call(type: type);

  @override
  LedgerQuestionBrief status(LedgerQuestionBriefStatusEnum status) =>
      call(status: status);

  @override
  LedgerQuestionBrief closesAt(DateTime? closesAt) => call(closesAt: closesAt);

  @override
  LedgerQuestionBrief resolveBy(DateTime? resolveBy) =>
      call(resolveBy: resolveBy);

  @override
  LedgerQuestionBrief resolutionCriteria(String? resolutionCriteria) =>
      call(resolutionCriteria: resolutionCriteria);

  @override
  LedgerQuestionBrief probability(num? probability) =>
      call(probability: probability);

  @override
  LedgerQuestionBrief estimativeTerm(IcdTerm? estimativeTerm) =>
      call(estimativeTerm: estimativeTerm);

  @override
  LedgerQuestionBrief distribution(Object? distribution) =>
      call(distribution: distribution);

  @override
  LedgerQuestionBrief quantiles(Quantiles? quantiles) =>
      call(quantiles: quantiles);

  @override
  LedgerQuestionBrief issuer(String issuer) => call(issuer: issuer);

  @override
  LedgerQuestionBrief forecasters(
    List<LedgerQuestionBriefForecastersInner>? forecasters,
  ) => call(forecasters: forecasters);

  @override
  LedgerQuestionBrief knowledgeCutoff(DateTime? knowledgeCutoff) =>
      call(knowledgeCutoff: knowledgeCutoff);

  @override
  LedgerQuestionBrief baseRate(num? baseRate) => call(baseRate: baseRate);

  @override
  LedgerQuestionBrief updatedAt(DateTime? updatedAt) =>
      call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LedgerQuestionBrief(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LedgerQuestionBrief(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LedgerQuestionBrief call({
    Object? id = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? closesAt = const $CopyWithPlaceholder(),
    Object? resolveBy = const $CopyWithPlaceholder(),
    Object? resolutionCriteria = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? estimativeTerm = const $CopyWithPlaceholder(),
    Object? distribution = const $CopyWithPlaceholder(),
    Object? quantiles = const $CopyWithPlaceholder(),
    Object? issuer = const $CopyWithPlaceholder(),
    Object? forecasters = const $CopyWithPlaceholder(),
    Object? knowledgeCutoff = const $CopyWithPlaceholder(),
    Object? baseRate = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return LedgerQuestionBrief(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as LedgerQuestionBriefTypeEnum,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as LedgerQuestionBriefStatusEnum,
      closesAt: closesAt == const $CopyWithPlaceholder()
          ? _value.closesAt
          // ignore: cast_nullable_to_non_nullable
          : closesAt as DateTime?,
      resolveBy: resolveBy == const $CopyWithPlaceholder()
          ? _value.resolveBy
          // ignore: cast_nullable_to_non_nullable
          : resolveBy as DateTime?,
      resolutionCriteria: resolutionCriteria == const $CopyWithPlaceholder()
          ? _value.resolutionCriteria
          // ignore: cast_nullable_to_non_nullable
          : resolutionCriteria as String?,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      estimativeTerm: estimativeTerm == const $CopyWithPlaceholder()
          ? _value.estimativeTerm
          // ignore: cast_nullable_to_non_nullable
          : estimativeTerm as IcdTerm?,
      distribution: distribution == const $CopyWithPlaceholder()
          ? _value.distribution
          // ignore: cast_nullable_to_non_nullable
          : distribution as Object?,
      quantiles: quantiles == const $CopyWithPlaceholder()
          ? _value.quantiles
          // ignore: cast_nullable_to_non_nullable
          : quantiles as Quantiles?,
      issuer: issuer == const $CopyWithPlaceholder() || issuer == null
          ? _value.issuer
          // ignore: cast_nullable_to_non_nullable
          : issuer as String,
      forecasters: forecasters == const $CopyWithPlaceholder()
          ? _value.forecasters
          // ignore: cast_nullable_to_non_nullable
          : forecasters as List<LedgerQuestionBriefForecastersInner>?,
      knowledgeCutoff: knowledgeCutoff == const $CopyWithPlaceholder()
          ? _value.knowledgeCutoff
          // ignore: cast_nullable_to_non_nullable
          : knowledgeCutoff as DateTime?,
      baseRate: baseRate == const $CopyWithPlaceholder()
          ? _value.baseRate
          // ignore: cast_nullable_to_non_nullable
          : baseRate as num?,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $LedgerQuestionBriefCopyWith on LedgerQuestionBrief {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLedgerQuestionBrief.copyWith(...)` or `instanceOfLedgerQuestionBrief.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LedgerQuestionBriefCWProxy get copyWith =>
      _$LedgerQuestionBriefCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LedgerQuestionBrief _$LedgerQuestionBriefFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LedgerQuestionBrief', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'title', 'type', 'status', 'issuer'],
      );
      final val = LedgerQuestionBrief(
        id: $checkedConvert('id', (v) => v as String),
        ref: $checkedConvert('ref', (v) => v as String?),
        title: $checkedConvert('title', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$LedgerQuestionBriefTypeEnumEnumMap, v),
        ),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$LedgerQuestionBriefStatusEnumEnumMap, v),
        ),
        closesAt: $checkedConvert(
          'closesAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        resolveBy: $checkedConvert(
          'resolveBy',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        resolutionCriteria: $checkedConvert(
          'resolutionCriteria',
          (v) => v as String?,
        ),
        probability: $checkedConvert('probability', (v) => v as num?),
        estimativeTerm: $checkedConvert(
          'estimativeTerm',
          (v) => $enumDecodeNullable(_$IcdTermEnumMap, v),
        ),
        distribution: $checkedConvert('distribution', (v) => v),
        quantiles: $checkedConvert(
          'quantiles',
          (v) =>
              v == null ? null : Quantiles.fromJson(v as Map<String, dynamic>),
        ),
        issuer: $checkedConvert('issuer', (v) => v as String),
        forecasters: $checkedConvert(
          'forecasters',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => LedgerQuestionBriefForecastersInner.fromJson(
                  e as Map<String, dynamic>,
                ),
              )
              .toList(),
        ),
        knowledgeCutoff: $checkedConvert(
          'knowledgeCutoff',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        baseRate: $checkedConvert('baseRate', (v) => v as num?),
        updatedAt: $checkedConvert(
          'updatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$LedgerQuestionBriefToJson(
  LedgerQuestionBrief instance,
) => <String, dynamic>{
  'id': instance.id,
  'ref': ?instance.ref,
  'title': instance.title,
  'type': _$LedgerQuestionBriefTypeEnumEnumMap[instance.type]!,
  'status': _$LedgerQuestionBriefStatusEnumEnumMap[instance.status]!,
  'closesAt': ?instance.closesAt?.toIso8601String(),
  'resolveBy': ?instance.resolveBy?.toIso8601String(),
  'resolutionCriteria': ?instance.resolutionCriteria,
  'probability': ?instance.probability,
  'estimativeTerm': ?_$IcdTermEnumMap[instance.estimativeTerm],
  'distribution': ?instance.distribution,
  'quantiles': ?instance.quantiles?.toJson(),
  'issuer': instance.issuer,
  'forecasters': ?instance.forecasters?.map((e) => e.toJson()).toList(),
  'knowledgeCutoff': ?instance.knowledgeCutoff?.toIso8601String(),
  'baseRate': ?instance.baseRate,
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};

const _$LedgerQuestionBriefTypeEnumEnumMap = {
  LedgerQuestionBriefTypeEnum.binary: 'binary',
  LedgerQuestionBriefTypeEnum.multiple: 'multiple',
  LedgerQuestionBriefTypeEnum.ordered: 'ordered',
  LedgerQuestionBriefTypeEnum.numeric: 'numeric',
};

const _$LedgerQuestionBriefStatusEnumEnumMap = {
  LedgerQuestionBriefStatusEnum.open: 'open',
  LedgerQuestionBriefStatusEnum.closed: 'closed',
  LedgerQuestionBriefStatusEnum.resolved: 'resolved',
  LedgerQuestionBriefStatusEnum.annulled: 'annulled',
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
