// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastCWProxy {
  Forecast id(String id);

  Forecast questionId(String questionId);

  Forecast forecasterId(String forecasterId);

  Forecast forecasterType(String forecasterType);

  Forecast madeAt(DateTime madeAt);

  Forecast asOfSeq(int asOfSeq);

  Forecast knowledgeCutoff(DateTime? knowledgeCutoff);

  Forecast probability(num? probability);

  Forecast distribution(Map<String, num>? distribution);

  Forecast quantiles(Quantiles? quantiles);

  Forecast estimativeTerm(IcdTerm? estimativeTerm);

  Forecast rationale(String? rationale);

  Forecast claims(List<Map<String, Object>>? claims);

  Forecast citations(List<ForecastCitationsInner>? citations);

  Forecast inputs(Map<String, Object>? inputs);

  Forecast supersedesId(String? supersedesId);

  Forecast recalibrated(bool recalibrated);

  Forecast manifestId(String manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Forecast(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Forecast(...).copyWith(id: 12, name: "My name")
  /// ```
  Forecast call({
    String id,
    String questionId,
    String forecasterId,
    String forecasterType,
    DateTime madeAt,
    int asOfSeq,
    DateTime? knowledgeCutoff,
    num? probability,
    Map<String, num>? distribution,
    Quantiles? quantiles,
    IcdTerm? estimativeTerm,
    String? rationale,
    List<Map<String, Object>>? claims,
    List<ForecastCitationsInner>? citations,
    Map<String, Object>? inputs,
    String? supersedesId,
    bool recalibrated,
    String manifestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecast.copyWith(...)` or call `instanceOfForecast.copyWith.fieldName(value)` for a single field.
class _$ForecastCWProxyImpl implements _$ForecastCWProxy {
  const _$ForecastCWProxyImpl(this._value);

  final Forecast _value;

  @override
  Forecast id(String id) => call(id: id);

  @override
  Forecast questionId(String questionId) => call(questionId: questionId);

  @override
  Forecast forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  Forecast forecasterType(String forecasterType) =>
      call(forecasterType: forecasterType);

  @override
  Forecast madeAt(DateTime madeAt) => call(madeAt: madeAt);

  @override
  Forecast asOfSeq(int asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  Forecast knowledgeCutoff(DateTime? knowledgeCutoff) =>
      call(knowledgeCutoff: knowledgeCutoff);

  @override
  Forecast probability(num? probability) => call(probability: probability);

  @override
  Forecast distribution(Map<String, num>? distribution) =>
      call(distribution: distribution);

  @override
  Forecast quantiles(Quantiles? quantiles) => call(quantiles: quantiles);

  @override
  Forecast estimativeTerm(IcdTerm? estimativeTerm) =>
      call(estimativeTerm: estimativeTerm);

  @override
  Forecast rationale(String? rationale) => call(rationale: rationale);

  @override
  Forecast claims(List<Map<String, Object>>? claims) => call(claims: claims);

  @override
  Forecast citations(List<ForecastCitationsInner>? citations) =>
      call(citations: citations);

  @override
  Forecast inputs(Map<String, Object>? inputs) => call(inputs: inputs);

  @override
  Forecast supersedesId(String? supersedesId) =>
      call(supersedesId: supersedesId);

  @override
  Forecast recalibrated(bool recalibrated) => call(recalibrated: recalibrated);

  @override
  Forecast manifestId(String manifestId) => call(manifestId: manifestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Forecast(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Forecast(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Forecast call({
    Object? id = const $CopyWithPlaceholder(),
    Object? questionId = const $CopyWithPlaceholder(),
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? forecasterType = const $CopyWithPlaceholder(),
    Object? madeAt = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? knowledgeCutoff = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? distribution = const $CopyWithPlaceholder(),
    Object? quantiles = const $CopyWithPlaceholder(),
    Object? estimativeTerm = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? claims = const $CopyWithPlaceholder(),
    Object? citations = const $CopyWithPlaceholder(),
    Object? inputs = const $CopyWithPlaceholder(),
    Object? supersedesId = const $CopyWithPlaceholder(),
    Object? recalibrated = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
  }) {
    return Forecast(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
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
      forecasterType:
          forecasterType == const $CopyWithPlaceholder() ||
              forecasterType == null
          ? _value.forecasterType
          // ignore: cast_nullable_to_non_nullable
          : forecasterType as String,
      madeAt: madeAt == const $CopyWithPlaceholder() || madeAt == null
          ? _value.madeAt
          // ignore: cast_nullable_to_non_nullable
          : madeAt as DateTime,
      asOfSeq: asOfSeq == const $CopyWithPlaceholder() || asOfSeq == null
          ? _value.asOfSeq
          // ignore: cast_nullable_to_non_nullable
          : asOfSeq as int,
      knowledgeCutoff: knowledgeCutoff == const $CopyWithPlaceholder()
          ? _value.knowledgeCutoff
          // ignore: cast_nullable_to_non_nullable
          : knowledgeCutoff as DateTime?,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      distribution: distribution == const $CopyWithPlaceholder()
          ? _value.distribution
          // ignore: cast_nullable_to_non_nullable
          : distribution as Map<String, num>?,
      quantiles: quantiles == const $CopyWithPlaceholder()
          ? _value.quantiles
          // ignore: cast_nullable_to_non_nullable
          : quantiles as Quantiles?,
      estimativeTerm: estimativeTerm == const $CopyWithPlaceholder()
          ? _value.estimativeTerm
          // ignore: cast_nullable_to_non_nullable
          : estimativeTerm as IcdTerm?,
      rationale: rationale == const $CopyWithPlaceholder()
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String?,
      claims: claims == const $CopyWithPlaceholder()
          ? _value.claims
          // ignore: cast_nullable_to_non_nullable
          : claims as List<Map<String, Object>>?,
      citations: citations == const $CopyWithPlaceholder()
          ? _value.citations
          // ignore: cast_nullable_to_non_nullable
          : citations as List<ForecastCitationsInner>?,
      inputs: inputs == const $CopyWithPlaceholder()
          ? _value.inputs
          // ignore: cast_nullable_to_non_nullable
          : inputs as Map<String, Object>?,
      supersedesId: supersedesId == const $CopyWithPlaceholder()
          ? _value.supersedesId
          // ignore: cast_nullable_to_non_nullable
          : supersedesId as String?,
      recalibrated:
          recalibrated == const $CopyWithPlaceholder() || recalibrated == null
          ? _value.recalibrated
          // ignore: cast_nullable_to_non_nullable
          : recalibrated as bool,
      manifestId:
          manifestId == const $CopyWithPlaceholder() || manifestId == null
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String,
    );
  }
}

extension $ForecastCopyWith on Forecast {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecast.copyWith(...)` or `instanceOfForecast.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastCWProxy get copyWith => _$ForecastCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Forecast _$ForecastFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Forecast', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'questionId',
      'forecasterId',
      'forecasterType',
      'madeAt',
      'asOfSeq',
      'recalibrated',
      'manifestId',
    ],
  );
  final val = Forecast(
    id: $checkedConvert('id', (v) => v as String),
    questionId: $checkedConvert('questionId', (v) => v as String),
    forecasterId: $checkedConvert('forecasterId', (v) => v as String),
    forecasterType: $checkedConvert('forecasterType', (v) => v as String),
    madeAt: $checkedConvert('madeAt', (v) => DateTime.parse(v as String)),
    asOfSeq: $checkedConvert('asOfSeq', (v) => (v as num).toInt()),
    knowledgeCutoff: $checkedConvert(
      'knowledgeCutoff',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    probability: $checkedConvert('probability', (v) => v as num?),
    distribution: $checkedConvert(
      'distribution',
      (v) => (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as num)),
    ),
    quantiles: $checkedConvert(
      'quantiles',
      (v) => v == null ? null : Quantiles.fromJson(v as Map<String, dynamic>),
    ),
    estimativeTerm: $checkedConvert(
      'estimativeTerm',
      (v) => $enumDecodeNullable(_$IcdTermEnumMap, v),
    ),
    rationale: $checkedConvert('rationale', (v) => v as String?),
    claims: $checkedConvert(
      'claims',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => (e as Map<String, dynamic>).map(
              (k, e) => MapEntry(k, e as Object),
            ),
          )
          .toList(),
    ),
    citations: $checkedConvert(
      'citations',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => ForecastCitationsInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    inputs: $checkedConvert(
      'inputs',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    supersedesId: $checkedConvert('supersedesId', (v) => v as String?),
    recalibrated: $checkedConvert('recalibrated', (v) => v as bool),
    manifestId: $checkedConvert('manifestId', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$ForecastToJson(Forecast instance) => <String, dynamic>{
  'id': instance.id,
  'questionId': instance.questionId,
  'forecasterId': instance.forecasterId,
  'forecasterType': instance.forecasterType,
  'madeAt': instance.madeAt.toIso8601String(),
  'asOfSeq': instance.asOfSeq,
  'knowledgeCutoff': ?instance.knowledgeCutoff?.toIso8601String(),
  'probability': ?instance.probability,
  'distribution': ?instance.distribution,
  'quantiles': ?instance.quantiles?.toJson(),
  'estimativeTerm': ?_$IcdTermEnumMap[instance.estimativeTerm],
  'rationale': ?instance.rationale,
  'claims': ?instance.claims,
  'citations': ?instance.citations?.map((e) => e.toJson()).toList(),
  'inputs': ?instance.inputs,
  'supersedesId': ?instance.supersedesId,
  'recalibrated': instance.recalibrated,
  'manifestId': instance.manifestId,
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
