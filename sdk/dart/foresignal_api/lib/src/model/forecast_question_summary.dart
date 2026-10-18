//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:foresignal_api/src/model/question_links.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_question_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastQuestionSummary {
  /// Returns a new [ForecastQuestionSummary] instance.
  ForecastQuestionSummary({

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


  final ForecastQuestionSummaryTypeEnum type;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ForecastQuestionSummaryStatusEnum status;



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


  final ForecastQuestionSummaryResolverTypeEnum resolverType;



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


  final ForecastQuestionSummaryOriginEnum origin;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastQuestionSummary &&
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
      other.resolvedValue == resolvedValue;

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
        (resolvedValue == null ? 0 : resolvedValue.hashCode);

  factory ForecastQuestionSummary.fromJson(Map<String, dynamic> json) => _$ForecastQuestionSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastQuestionSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastQuestionSummaryTypeEnum {
@JsonValue(r'binary')
binary(r'binary'),
@JsonValue(r'multiple')
multiple(r'multiple'),
@JsonValue(r'ordered')
ordered(r'ordered'),
@JsonValue(r'numeric')
numeric(r'numeric');

const ForecastQuestionSummaryTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ForecastQuestionSummaryStatusEnum {
@JsonValue(r'open')
open(r'open'),
@JsonValue(r'closed')
closed(r'closed'),
@JsonValue(r'resolved')
resolved(r'resolved'),
@JsonValue(r'annulled')
annulled(r'annulled');

const ForecastQuestionSummaryStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ForecastQuestionSummaryResolverTypeEnum {
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

const ForecastQuestionSummaryResolverTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ForecastQuestionSummaryOriginEnum {
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'system')
system(r'system'),
@JsonValue(r'analyst')
analyst(r'analyst');

const ForecastQuestionSummaryOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


