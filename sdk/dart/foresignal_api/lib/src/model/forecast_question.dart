//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:foresignal_api/src/model/question_links.dart';
import 'package:foresignal_api/src/model/forecast_score.dart';
import 'package:foresignal_api/src/model/forecast_resolution.dart';
import 'package:foresignal_api/src/model/question_outcomes.dart';
import 'package:foresignal_api/src/model/forecast_resolver.dart';
import 'package:foresignal_api/src/model/forecast.dart';
import 'package:foresignal_api/src/model/question_annulment.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_question.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastQuestion {
  /// Returns a new [ForecastQuestion] instance.
  ForecastQuestion({

    required  this.id,

    required  this.title,

    required  this.type,

    required  this.status,

    required  this.category,

     this.tags,

    required  this.resolverType,

    required  this.opensAt,

    required  this.closesAt,

    required  this.resolveBy,

    required  this.origin,

     this.links,

    required  this.private,

     this.forecastCount,

     this.ensembleProbability,

     this.estimativeTerm,

     this.resolvedOutcome,

     this.resolvedValue,

     this.outcomes,

    required  this.resolutionCriteria,

    required  this.resolver,

     this.resolution,

     this.annulment,

     this.dedupKey,

    required  this.latestForecasts,

     this.ensemble,

     this.scores,

     this.createdAt,

     this.updatedAt,

     this.baselineNotes,

     this.manifestId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ForecastQuestionTypeEnum type;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ForecastQuestionStatusEnum status;



      /// e.g. `conflict`, `maritime`, `markets`, `weather-impact`, `activity`
  @JsonKey(
    
    name: r'category',
    required: true,
    includeIfNull: false,
  )


  final String category;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



  @JsonKey(
    
    name: r'resolverType',
    required: true,
    includeIfNull: false,
  )


  final ForecastQuestionResolverTypeEnum resolverType;



  @JsonKey(
    
    name: r'opensAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime opensAt;



  @JsonKey(
    
    name: r'closesAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime closesAt;



  @JsonKey(
    
    name: r'resolveBy',
    required: true,
    includeIfNull: false,
  )


  final DateTime resolveBy;



  @JsonKey(
    
    name: r'origin',
    required: true,
    includeIfNull: false,
  )


  final ForecastQuestionOriginEnum origin;



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final QuestionLinks? links;



  @JsonKey(
    
    name: r'private',
    required: true,
    includeIfNull: false,
  )


  final bool private;



  @JsonKey(
    
    name: r'forecastCount',
    required: false,
    includeIfNull: false,
  )


  final int? forecastCount;



      /// Latest ensemble probability (binary questions)
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'ensembleProbability',
    required: false,
    includeIfNull: false,
  )


  final num? ensembleProbability;



  @JsonKey(
    
    name: r'estimativeTerm',
    required: false,
    includeIfNull: false,
  )


  final IcdTerm? estimativeTerm;



  @JsonKey(
    
    name: r'resolvedOutcome',
    required: false,
    includeIfNull: false,
  )


  final String? resolvedOutcome;



  @JsonKey(
    
    name: r'resolvedValue',
    required: false,
    includeIfNull: false,
  )


  final num? resolvedValue;



  @JsonKey(
    
    name: r'outcomes',
    required: false,
    includeIfNull: false,
  )


  final QuestionOutcomes? outcomes;



  @JsonKey(
    
    name: r'resolutionCriteria',
    required: true,
    includeIfNull: false,
  )


  final String resolutionCriteria;



  @JsonKey(
    
    name: r'resolver',
    required: true,
    includeIfNull: false,
  )


  final ForecastResolver resolver;



  @JsonKey(
    
    name: r'resolution',
    required: false,
    includeIfNull: false,
  )


  final ForecastResolution? resolution;



  @JsonKey(
    
    name: r'annulment',
    required: false,
    includeIfNull: false,
  )


  final QuestionAnnulment? annulment;



  @JsonKey(
    
    name: r'dedupKey',
    required: false,
    includeIfNull: false,
  )


  final String? dedupKey;



      /// Latest forecast per forecaster, newest first
  @JsonKey(
    
    name: r'latestForecasts',
    required: true,
    includeIfNull: false,
  )


  final List<Forecast> latestForecasts;



  @JsonKey(
    
    name: r'ensemble',
    required: false,
    includeIfNull: false,
  )


  final Forecast? ensemble;



      /// Present once resolved
  @JsonKey(
    
    name: r'scores',
    required: false,
    includeIfNull: false,
  )


  final List<ForecastScore>? scores;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



      /// Why a baseline forecaster that supports this kind of question forecast nothing, by forecaster id (e.g. no reference family, no covered history)
  @JsonKey(
    
    name: r'baselineNotes',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? baselineNotes;



      /// 116 manifest of the question's opening
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastQuestion &&
      other.id == id &&
      other.title == title &&
      other.type == type &&
      other.status == status &&
      other.category == category &&
      other.tags == tags &&
      other.resolverType == resolverType &&
      other.opensAt == opensAt &&
      other.closesAt == closesAt &&
      other.resolveBy == resolveBy &&
      other.origin == origin &&
      other.links == links &&
      other.private == private &&
      other.forecastCount == forecastCount &&
      other.ensembleProbability == ensembleProbability &&
      other.estimativeTerm == estimativeTerm &&
      other.resolvedOutcome == resolvedOutcome &&
      other.resolvedValue == resolvedValue &&
      other.outcomes == outcomes &&
      other.resolutionCriteria == resolutionCriteria &&
      other.resolver == resolver &&
      other.resolution == resolution &&
      other.annulment == annulment &&
      other.dedupKey == dedupKey &&
      other.latestForecasts == latestForecasts &&
      other.ensemble == ensemble &&
      other.scores == scores &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.baselineNotes == baselineNotes &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        type.hashCode +
        status.hashCode +
        category.hashCode +
        tags.hashCode +
        resolverType.hashCode +
        opensAt.hashCode +
        closesAt.hashCode +
        resolveBy.hashCode +
        origin.hashCode +
        links.hashCode +
        private.hashCode +
        forecastCount.hashCode +
        (ensembleProbability == null ? 0 : ensembleProbability.hashCode) +
        (estimativeTerm == null ? 0 : estimativeTerm.hashCode) +
        (resolvedOutcome == null ? 0 : resolvedOutcome.hashCode) +
        (resolvedValue == null ? 0 : resolvedValue.hashCode) +
        outcomes.hashCode +
        resolutionCriteria.hashCode +
        resolver.hashCode +
        (resolution == null ? 0 : resolution.hashCode) +
        (annulment == null ? 0 : annulment.hashCode) +
        (dedupKey == null ? 0 : dedupKey.hashCode) +
        latestForecasts.hashCode +
        (ensemble == null ? 0 : ensemble.hashCode) +
        scores.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        baselineNotes.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory ForecastQuestion.fromJson(Map<String, dynamic> json) => _$ForecastQuestionFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastQuestionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastQuestionTypeEnum {
@JsonValue(r'binary')
binary(r'binary'),
@JsonValue(r'multiple')
multiple(r'multiple'),
@JsonValue(r'ordered')
ordered(r'ordered'),
@JsonValue(r'numeric')
numeric(r'numeric');

const ForecastQuestionTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ForecastQuestionStatusEnum {
@JsonValue(r'open')
open(r'open'),
@JsonValue(r'closed')
closed(r'closed'),
@JsonValue(r'resolved')
resolved(r'resolved'),
@JsonValue(r'annulled')
annulled(r'annulled');

const ForecastQuestionStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ForecastQuestionResolverTypeEnum {
@JsonValue(r'event-exists')
eventExists(r'event-exists'),
@JsonValue(r'count-threshold')
countThreshold(r'count-threshold'),
@JsonValue(r'series-value')
seriesValue(r'series-value'),
@JsonValue(r'alert-fired')
alertFired(r'alert-fired'),
@JsonValue(r'situation-status')
situationStatus(r'situation-status'),
@JsonValue(r'hypothesis-outcome')
hypothesisOutcome(r'hypothesis-outcome'),
@JsonValue(r'manual')
manual(r'manual');

const ForecastQuestionResolverTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ForecastQuestionOriginEnum {
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'system')
system(r'system'),
@JsonValue(r'analyst')
analyst(r'analyst');

const ForecastQuestionOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


