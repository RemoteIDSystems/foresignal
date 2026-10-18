// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'context_summary_insights_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ContextSummaryInsightsInnerCWProxy {
  ContextSummaryInsightsInner ref(String ref);

  ContextSummaryInsightsInner kind(ContextSummaryInsightsInnerKindEnum kind);

  ContextSummaryInsightsInner title(String title);

  ContextSummaryInsightsInner probability(num? probability);

  ContextSummaryInsightsInner estimativeTerm(IcdTerm? estimativeTerm);

  ContextSummaryInsightsInner issuer(String? issuer);

  ContextSummaryInsightsInner baseRate(num? baseRate);

  ContextSummaryInsightsInner knowledgeCutoff(DateTime? knowledgeCutoff);

  ContextSummaryInsightsInner at(DateTime? at);

  ContextSummaryInsightsInner textOrigin(
    ContextSummaryInsightsInnerTextOriginEnum? textOrigin,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextSummaryInsightsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextSummaryInsightsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ContextSummaryInsightsInner call({
    String ref,
    ContextSummaryInsightsInnerKindEnum kind,
    String title,
    num? probability,
    IcdTerm? estimativeTerm,
    String? issuer,
    num? baseRate,
    DateTime? knowledgeCutoff,
    DateTime? at,
    ContextSummaryInsightsInnerTextOriginEnum? textOrigin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfContextSummaryInsightsInner.copyWith(...)` or call `instanceOfContextSummaryInsightsInner.copyWith.fieldName(value)` for a single field.
class _$ContextSummaryInsightsInnerCWProxyImpl
    implements _$ContextSummaryInsightsInnerCWProxy {
  const _$ContextSummaryInsightsInnerCWProxyImpl(this._value);

  final ContextSummaryInsightsInner _value;

  @override
  ContextSummaryInsightsInner ref(String ref) => call(ref: ref);

  @override
  ContextSummaryInsightsInner kind(ContextSummaryInsightsInnerKindEnum kind) =>
      call(kind: kind);

  @override
  ContextSummaryInsightsInner title(String title) => call(title: title);

  @override
  ContextSummaryInsightsInner probability(num? probability) =>
      call(probability: probability);

  @override
  ContextSummaryInsightsInner estimativeTerm(IcdTerm? estimativeTerm) =>
      call(estimativeTerm: estimativeTerm);

  @override
  ContextSummaryInsightsInner issuer(String? issuer) => call(issuer: issuer);

  @override
  ContextSummaryInsightsInner baseRate(num? baseRate) =>
      call(baseRate: baseRate);

  @override
  ContextSummaryInsightsInner knowledgeCutoff(DateTime? knowledgeCutoff) =>
      call(knowledgeCutoff: knowledgeCutoff);

  @override
  ContextSummaryInsightsInner at(DateTime? at) => call(at: at);

  @override
  ContextSummaryInsightsInner textOrigin(
    ContextSummaryInsightsInnerTextOriginEnum? textOrigin,
  ) => call(textOrigin: textOrigin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ContextSummaryInsightsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ContextSummaryInsightsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ContextSummaryInsightsInner call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? estimativeTerm = const $CopyWithPlaceholder(),
    Object? issuer = const $CopyWithPlaceholder(),
    Object? baseRate = const $CopyWithPlaceholder(),
    Object? knowledgeCutoff = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? textOrigin = const $CopyWithPlaceholder(),
  }) {
    return ContextSummaryInsightsInner(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as ContextSummaryInsightsInnerKindEnum,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      estimativeTerm: estimativeTerm == const $CopyWithPlaceholder()
          ? _value.estimativeTerm
          // ignore: cast_nullable_to_non_nullable
          : estimativeTerm as IcdTerm?,
      issuer: issuer == const $CopyWithPlaceholder()
          ? _value.issuer
          // ignore: cast_nullable_to_non_nullable
          : issuer as String?,
      baseRate: baseRate == const $CopyWithPlaceholder()
          ? _value.baseRate
          // ignore: cast_nullable_to_non_nullable
          : baseRate as num?,
      knowledgeCutoff: knowledgeCutoff == const $CopyWithPlaceholder()
          ? _value.knowledgeCutoff
          // ignore: cast_nullable_to_non_nullable
          : knowledgeCutoff as DateTime?,
      at: at == const $CopyWithPlaceholder()
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime?,
      textOrigin: textOrigin == const $CopyWithPlaceholder()
          ? _value.textOrigin
          // ignore: cast_nullable_to_non_nullable
          : textOrigin as ContextSummaryInsightsInnerTextOriginEnum?,
    );
  }
}

extension $ContextSummaryInsightsInnerCopyWith on ContextSummaryInsightsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfContextSummaryInsightsInner.copyWith(...)` or `instanceOfContextSummaryInsightsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ContextSummaryInsightsInnerCWProxy get copyWith =>
      _$ContextSummaryInsightsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ContextSummaryInsightsInner _$ContextSummaryInsightsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ContextSummaryInsightsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['ref', 'kind', 'title']);
  final val = ContextSummaryInsightsInner(
    ref: $checkedConvert('ref', (v) => v as String),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$ContextSummaryInsightsInnerKindEnumEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String),
    probability: $checkedConvert('probability', (v) => v as num?),
    estimativeTerm: $checkedConvert(
      'estimativeTerm',
      (v) => $enumDecodeNullable(_$IcdTermEnumMap, v),
    ),
    issuer: $checkedConvert('issuer', (v) => v as String?),
    baseRate: $checkedConvert('baseRate', (v) => v as num?),
    knowledgeCutoff: $checkedConvert(
      'knowledgeCutoff',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    at: $checkedConvert(
      'at',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    textOrigin: $checkedConvert(
      'textOrigin',
      (v) => $enumDecodeNullable(
        _$ContextSummaryInsightsInnerTextOriginEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$ContextSummaryInsightsInnerToJson(
  ContextSummaryInsightsInner instance,
) => <String, dynamic>{
  'ref': instance.ref,
  'kind': _$ContextSummaryInsightsInnerKindEnumEnumMap[instance.kind]!,
  'title': instance.title,
  'probability': ?instance.probability,
  'estimativeTerm': ?_$IcdTermEnumMap[instance.estimativeTerm],
  'issuer': ?instance.issuer,
  'baseRate': ?instance.baseRate,
  'knowledgeCutoff': ?instance.knowledgeCutoff?.toIso8601String(),
  'at': ?instance.at?.toIso8601String(),
  'textOrigin':
      ?_$ContextSummaryInsightsInnerTextOriginEnumEnumMap[instance.textOrigin],
};

const _$ContextSummaryInsightsInnerKindEnumEnumMap = {
  ContextSummaryInsightsInnerKindEnum.finding: 'finding',
  ContextSummaryInsightsInnerKindEnum.forecast: 'forecast',
  ContextSummaryInsightsInnerKindEnum.bandChange: 'band-change',
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

const _$ContextSummaryInsightsInnerTextOriginEnumEnumMap = {
  ContextSummaryInsightsInnerTextOriginEnum.deterministic: 'deterministic',
  ContextSummaryInsightsInnerTextOriginEnum.llm: 'llm',
};
