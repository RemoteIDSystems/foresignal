//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/scorecard_entry_interval_coverage.dart';
import 'package:foresignal_api/src/model/scorecard_entry_recalibration.dart';
import 'package:foresignal_api/src/model/scorecard_entry_skill.dart';
import 'package:foresignal_api/src/model/scorecard_entry_bias.dart';
import 'package:foresignal_api/src/model/scorecard_entry_murphy.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'scorecard_entry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ScorecardEntry {
  /// Returns a new [ScorecardEntry] instance.
  ScorecardEntry({

    required  this.forecasterId,

     this.forecasterType,

     this.category,

    required  this.nResolved,

     this.nForecasts,

    required  this.label,

     this.brier,

     this.logScore,

     this.rps,

     this.crps,

     this.timeAveragedBrier,

     this.intervalCoverage,

     this.murphy,

     this.skill,

     this.bias,

     this.recalibration,

     this.ensembleWeight,
  });

  @JsonKey(
    
    name: r'forecasterId',
    required: true,
    includeIfNull: false,
  )


  final String forecasterId;



      /// Expected to grow (`x-extensible-enum`): a client must accept a value not listed here.
  @JsonKey(
    
    name: r'forecasterType',
    required: false,
    includeIfNull: false,
  )


  final String? forecasterType;



      /// Null = all categories
  @JsonKey(
    
    name: r'category',
    required: false,
    includeIfNull: false,
  )


  final String? category;



  @JsonKey(
    
    name: r'nResolved',
    required: true,
    includeIfNull: false,
  )


  final int nResolved;



  @JsonKey(
    
    name: r'nForecasts',
    required: false,
    includeIfNull: false,
  )


  final int? nForecasts;



      /// `insufficient` under 20 resolved questions
  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final ScorecardEntryLabelEnum label;



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



  @JsonKey(
    
    name: r'timeAveragedBrier',
    required: false,
    includeIfNull: false,
  )


  final num? timeAveragedBrier;



  @JsonKey(
    
    name: r'intervalCoverage',
    required: false,
    includeIfNull: false,
  )


  final ScorecardEntryIntervalCoverage? intervalCoverage;



  @JsonKey(
    
    name: r'murphy',
    required: false,
    includeIfNull: false,
  )


  final ScorecardEntryMurphy? murphy;



  @JsonKey(
    
    name: r'skill',
    required: false,
    includeIfNull: false,
  )


  final ScorecardEntrySkill? skill;



  @JsonKey(
    
    name: r'bias',
    required: false,
    includeIfNull: false,
  )


  final ScorecardEntryBias? bias;



  @JsonKey(
    
    name: r'recalibration',
    required: false,
    includeIfNull: false,
  )


  final ScorecardEntryRecalibration? recalibration;



  @JsonKey(
    
    name: r'ensembleWeight',
    required: false,
    includeIfNull: false,
  )


  final num? ensembleWeight;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ScorecardEntry &&
      other.forecasterId == forecasterId &&
      other.forecasterType == forecasterType &&
      other.category == category &&
      other.nResolved == nResolved &&
      other.nForecasts == nForecasts &&
      other.label == label &&
      other.brier == brier &&
      other.logScore == logScore &&
      other.rps == rps &&
      other.crps == crps &&
      other.timeAveragedBrier == timeAveragedBrier &&
      other.intervalCoverage == intervalCoverage &&
      other.murphy == murphy &&
      other.skill == skill &&
      other.bias == bias &&
      other.recalibration == recalibration &&
      other.ensembleWeight == ensembleWeight;

    @override
    int get hashCode =>
        forecasterId.hashCode +
        forecasterType.hashCode +
        (category == null ? 0 : category.hashCode) +
        nResolved.hashCode +
        nForecasts.hashCode +
        label.hashCode +
        (brier == null ? 0 : brier.hashCode) +
        (logScore == null ? 0 : logScore.hashCode) +
        (rps == null ? 0 : rps.hashCode) +
        (crps == null ? 0 : crps.hashCode) +
        (timeAveragedBrier == null ? 0 : timeAveragedBrier.hashCode) +
        intervalCoverage.hashCode +
        (murphy == null ? 0 : murphy.hashCode) +
        (skill == null ? 0 : skill.hashCode) +
        (bias == null ? 0 : bias.hashCode) +
        (recalibration == null ? 0 : recalibration.hashCode) +
        (ensembleWeight == null ? 0 : ensembleWeight.hashCode);

  factory ScorecardEntry.fromJson(Map<String, dynamic> json) => _$ScorecardEntryFromJson(json);

  Map<String, dynamic> toJson() => _$ScorecardEntryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `insufficient` under 20 resolved questions
enum ScorecardEntryLabelEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const ScorecardEntryLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


