//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/forecast_score_interval_hits.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_score.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastScore {
  /// Returns a new [ForecastScore] instance.
  ForecastScore({

    required  this.questionId,

    required  this.forecasterId,

     this.brier,

     this.logScore,

     this.rps,

     this.crps,

     this.pinball,

     this.intervalHits,

     this.timeAveragedBrier,

    required  this.nDays,

     this.finalProbability,

    required  this.scorerVersion,

    required  this.computedAt,
  });

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
    
    name: r'brier',
    required: false,
    includeIfNull: false,
  )


  final num? brier;



  @JsonKey(
    
    name: r'logScore',
    required: false,
    includeIfNull: false,
  )


  final num? logScore;



  @JsonKey(
    
    name: r'rps',
    required: false,
    includeIfNull: false,
  )


  final num? rps;



  @JsonKey(
    
    name: r'crps',
    required: false,
    includeIfNull: false,
  )


  final num? crps;



      /// Pinball loss per quantile level
  @JsonKey(
    
    name: r'pinball',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num>? pinball;



  @JsonKey(
    
    name: r'intervalHits',
    required: false,
    includeIfNull: false,
  )


  final ForecastScoreIntervalHits? intervalHits;



  @JsonKey(
    
    name: r'timeAveragedBrier',
    required: false,
    includeIfNull: false,
  )


  final num? timeAveragedBrier;



  @JsonKey(
    
    name: r'nDays',
    required: true,
    includeIfNull: false,
  )


  final int nDays;



  @JsonKey(
    
    name: r'finalProbability',
    required: false,
    includeIfNull: false,
  )


  final num? finalProbability;



  @JsonKey(
    
    name: r'scorerVersion',
    required: true,
    includeIfNull: false,
  )


  final String scorerVersion;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastScore &&
      other.questionId == questionId &&
      other.forecasterId == forecasterId &&
      other.brier == brier &&
      other.logScore == logScore &&
      other.rps == rps &&
      other.crps == crps &&
      other.pinball == pinball &&
      other.intervalHits == intervalHits &&
      other.timeAveragedBrier == timeAveragedBrier &&
      other.nDays == nDays &&
      other.finalProbability == finalProbability &&
      other.scorerVersion == scorerVersion &&
      other.computedAt == computedAt;

    @override
    int get hashCode =>
        questionId.hashCode +
        forecasterId.hashCode +
        (brier == null ? 0 : brier.hashCode) +
        (logScore == null ? 0 : logScore.hashCode) +
        (rps == null ? 0 : rps.hashCode) +
        (crps == null ? 0 : crps.hashCode) +
        (pinball == null ? 0 : pinball.hashCode) +
        (intervalHits == null ? 0 : intervalHits.hashCode) +
        (timeAveragedBrier == null ? 0 : timeAveragedBrier.hashCode) +
        nDays.hashCode +
        (finalProbability == null ? 0 : finalProbability.hashCode) +
        scorerVersion.hashCode +
        computedAt.hashCode;

  factory ForecastScore.fromJson(Map<String, dynamic> json) => _$ForecastScoreFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastScoreToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

