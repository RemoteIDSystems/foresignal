//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:foresignal_api/src/model/quantiles.dart';
import 'package:foresignal_api/src/model/forecast_citations_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Forecast {
  /// Returns a new [Forecast] instance.
  Forecast({

    required  this.id,

    required  this.questionId,

    required  this.forecasterId,

    required  this.forecasterType,

    required  this.madeAt,

    required  this.asOfSeq,

     this.knowledgeCutoff,

     this.probability,

     this.distribution,

     this.quantiles,

     this.estimativeTerm,

     this.rationale,

     this.claims,

     this.citations,

     this.inputs,

     this.supersedesId,

    required  this.recalibrated,

    required  this.manifestId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: true,
    includeIfNull: false,
  )


  final String questionId;



  @JsonKey(
    
    name: r'forecasterId',
    required: true,
    includeIfNull: false,
  )


  final String forecasterId;



  @JsonKey(
    
    name: r'forecasterType',
    required: true,
    includeIfNull: false,
  )


  final ForecastForecasterTypeEnum forecasterType;



      /// Server-set
  @JsonKey(
    
    name: r'madeAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime madeAt;



      /// Server-set knowledge cutoff in seq
  @JsonKey(
    
    name: r'asOfSeq',
    required: true,
    includeIfNull: false,
  )


  final int asOfSeq;



      /// Model knowledge cutoff for LLM forecasters
  @JsonKey(
    
    name: r'knowledgeCutoff',
    required: false,
    includeIfNull: false,
  )


  final DateTime? knowledgeCutoff;



      /// Binary questions
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: false,
    includeIfNull: false,
  )


  final num? probability;



      /// Multiple/ordered: outcome label → probability, summing to 1 ± 1e-6
  @JsonKey(
    
    name: r'distribution',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num>? distribution;



      /// Numeric: p05, p10, p25, p50, p75, p90, p95, monotone
  @JsonKey(
    
    name: r'quantiles',
    required: false,
    includeIfNull: false,
  )


  final Quantiles? quantiles;



  @JsonKey(
    
    name: r'estimativeTerm',
    required: false,
    includeIfNull: false,
  )


  final IcdTerm? estimativeTerm;



      /// Markdown
  @JsonKey(
    
    name: r'rationale',
    required: false,
    includeIfNull: false,
  )


  final String? rationale;



      /// Typed claims (LLM forecasters)
  @JsonKey(
    
    name: r'claims',
    required: false,
    includeIfNull: false,
  )


  final List<Map<String, Object>>? claims;



  @JsonKey(
    
    name: r'citations',
    required: false,
    includeIfNull: false,
  )


  final List<ForecastCitationsInner>? citations;



      /// Inputs: `baseRateIds`, `analogIds`, `precursorId`, `model`, `provider`, `effort`, `samples`, `promptHash`, `rawProbability` (before LLM caps), `weights` (ensemble)
  @JsonKey(
    
    name: r'inputs',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? inputs;



      /// ULID identifier
  @JsonKey(
    
    name: r'supersedesId',
    required: false,
    includeIfNull: false,
  )


  final String? supersedesId;



      /// True for Platt-recalibrated ensemble inputs
  @JsonKey(
    
    name: r'recalibrated',
    required: true,
    includeIfNull: false,
  )


  final bool recalibrated;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: true,
    includeIfNull: false,
  )


  final String manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Forecast &&
      other.id == id &&
      other.questionId == questionId &&
      other.forecasterId == forecasterId &&
      other.forecasterType == forecasterType &&
      other.madeAt == madeAt &&
      other.asOfSeq == asOfSeq &&
      other.knowledgeCutoff == knowledgeCutoff &&
      other.probability == probability &&
      other.distribution == distribution &&
      other.quantiles == quantiles &&
      other.estimativeTerm == estimativeTerm &&
      other.rationale == rationale &&
      other.claims == claims &&
      other.citations == citations &&
      other.inputs == inputs &&
      other.supersedesId == supersedesId &&
      other.recalibrated == recalibrated &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        id.hashCode +
        questionId.hashCode +
        forecasterId.hashCode +
        forecasterType.hashCode +
        madeAt.hashCode +
        asOfSeq.hashCode +
        (knowledgeCutoff == null ? 0 : knowledgeCutoff.hashCode) +
        (probability == null ? 0 : probability.hashCode) +
        (distribution == null ? 0 : distribution.hashCode) +
        (quantiles == null ? 0 : quantiles.hashCode) +
        (estimativeTerm == null ? 0 : estimativeTerm.hashCode) +
        (rationale == null ? 0 : rationale.hashCode) +
        claims.hashCode +
        citations.hashCode +
        inputs.hashCode +
        (supersedesId == null ? 0 : supersedesId.hashCode) +
        recalibrated.hashCode +
        manifestId.hashCode;

  factory Forecast.fromJson(Map<String, dynamic> json) => _$ForecastFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastForecasterTypeEnum {
@JsonValue(r'base-rate')
baseRate(r'base-rate'),
@JsonValue(r'climatology')
climatology(r'climatology'),
@JsonValue(r'persistence')
persistence(r'persistence'),
@JsonValue(r'statistical')
statistical(r'statistical'),
@JsonValue(r'precursor')
precursor(r'precursor'),
@JsonValue(r'analog')
analog(r'analog'),
@JsonValue(r'impact')
impact(r'impact'),
@JsonValue(r'indicator-bayes')
indicatorBayes(r'indicator-bayes'),
@JsonValue(r'llm')
llm(r'llm'),
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'external')
external_(r'external'),
@JsonValue(r'ensemble')
ensemble(r'ensemble');

const ForecastForecasterTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


