//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/scorecard_entry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_scorecard.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastScorecard {
  /// Returns a new [ForecastScorecard] instance.
  ForecastScorecard({

     this.forecasterId,

     this.category,

     this.from,

     this.to,

    required  this.retrospective,

    required  this.entries,

    required  this.scorerVersion,

    required  this.computedAt,
  });

  @JsonKey(
    
    name: r'forecasterId',
    required: false,
    includeIfNull: false,
  )


  final String? forecasterId;



  @JsonKey(
    
    name: r'category',
    required: false,
    includeIfNull: false,
  )


  final String? category;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



      /// True for scorecards from retrospective evaluations
  @JsonKey(
    
    name: r'retrospective',
    required: true,
    includeIfNull: false,
  )


  final bool retrospective;



  @JsonKey(
    
    name: r'entries',
    required: true,
    includeIfNull: false,
  )


  final List<ScorecardEntry> entries;



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
    bool operator ==(Object other) => identical(this, other) || other is ForecastScorecard &&
      other.forecasterId == forecasterId &&
      other.category == category &&
      other.from == from &&
      other.to == to &&
      other.retrospective == retrospective &&
      other.entries == entries &&
      other.scorerVersion == scorerVersion &&
      other.computedAt == computedAt;

    @override
    int get hashCode =>
        (forecasterId == null ? 0 : forecasterId.hashCode) +
        (category == null ? 0 : category.hashCode) +
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode) +
        retrospective.hashCode +
        entries.hashCode +
        scorerVersion.hashCode +
        computedAt.hashCode;

  factory ForecastScorecard.fromJson(Map<String, dynamic> json) => _$ForecastScorecardFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastScorecardToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

