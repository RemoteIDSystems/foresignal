//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/forecast_scorecard.dart';
import 'package:foresignal_api/src/model/forecast_evaluation_skipped.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_evaluation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastEvaluation {
  /// Returns a new [ForecastEvaluation] instance.
  ForecastEvaluation({

    required  this.id,

    required  this.forecasterId,

    required  this.from,

    required  this.to,

     this.categories,

     this.maxQuestions,

    required  this.status,

     this.progress,

    required  this.questionsConsidered,

    required  this.questionsEvaluated,

    required  this.skipped,

     this.scorecard,

    required  this.caveats,

     this.error,

    required  this.createdBy,

    required  this.createdAt,

     this.startedAt,

     this.finishedAt,

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
    
    name: r'forecasterId',
    required: true,
    includeIfNull: false,
  )


  final String forecasterId;



  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



  @JsonKey(
    
    name: r'categories',
    required: false,
    includeIfNull: false,
  )


  final List<String>? categories;



  @JsonKey(
    
    name: r'maxQuestions',
    required: false,
    includeIfNull: false,
  )


  final int? maxQuestions;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ForecastEvaluationStatusEnum status;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'progress',
    required: false,
    includeIfNull: false,
  )


  final num? progress;



  @JsonKey(
    
    name: r'questionsConsidered',
    required: true,
    includeIfNull: false,
  )


  final int questionsConsidered;



  @JsonKey(
    
    name: r'questionsEvaluated',
    required: true,
    includeIfNull: false,
  )


  final int questionsEvaluated;



  @JsonKey(
    
    name: r'skipped',
    required: true,
    includeIfNull: false,
  )


  final ForecastEvaluationSkipped skipped;



  @JsonKey(
    
    name: r'scorecard',
    required: false,
    includeIfNull: false,
  )


  final ForecastScorecard? scorecard;



  @JsonKey(
    
    name: r'caveats',
    required: true,
    includeIfNull: false,
  )


  final List<Caveat> caveats;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



  @JsonKey(
    
    name: r'createdBy',
    required: true,
    includeIfNull: false,
  )


  final String createdBy;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'startedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startedAt;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastEvaluation &&
      other.id == id &&
      other.forecasterId == forecasterId &&
      other.from == from &&
      other.to == to &&
      other.categories == categories &&
      other.maxQuestions == maxQuestions &&
      other.status == status &&
      other.progress == progress &&
      other.questionsConsidered == questionsConsidered &&
      other.questionsEvaluated == questionsEvaluated &&
      other.skipped == skipped &&
      other.scorecard == scorecard &&
      other.caveats == caveats &&
      other.error == error &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        id.hashCode +
        forecasterId.hashCode +
        from.hashCode +
        to.hashCode +
        categories.hashCode +
        (maxQuestions == null ? 0 : maxQuestions.hashCode) +
        status.hashCode +
        (progress == null ? 0 : progress.hashCode) +
        questionsConsidered.hashCode +
        questionsEvaluated.hashCode +
        skipped.hashCode +
        (scorecard == null ? 0 : scorecard.hashCode) +
        caveats.hashCode +
        (error == null ? 0 : error.hashCode) +
        createdBy.hashCode +
        createdAt.hashCode +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory ForecastEvaluation.fromJson(Map<String, dynamic> json) => _$ForecastEvaluationFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastEvaluationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastEvaluationStatusEnum {
@JsonValue(r'queued')
queued(r'queued'),
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'done')
done(r'done'),
@JsonValue(r'failed')
failed(r'failed'),
@JsonValue(r'cancelled')
cancelled(r'cancelled');

const ForecastEvaluationStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


