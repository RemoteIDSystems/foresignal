//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/stat_interval.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analog_result_outcome_distribution.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalogResultOutcomeDistribution {
  /// Returns a new [AnalogResultOutcomeDistribution] instance.
  AnalogResultOutcomeDistribution({

    required  this.outcome,

    required  this.analogCount,

    required  this.hits,

     this.frequency,

     this.baseRate,

     this.baseRateId,

    required  this.probability,

    required  this.interval,
  });

      /// Target outcome: `critical` (situation reaches critical) or `{family}/P{n}D` (a family within n days), e.g. `conflict:battles/P30D`
  @JsonKey(
    
    name: r'outcome',
    required: true,
    includeIfNull: false,
  )


  final String outcome;



  @JsonKey(
    
    name: r'analogCount',
    required: true,
    includeIfNull: false,
  )


  final int analogCount;



  @JsonKey(
    
    name: r'hits',
    required: true,
    includeIfNull: false,
  )


  final int hits;



  @JsonKey(
    
    name: r'frequency',
    required: false,
    includeIfNull: false,
  )


  final num? frequency;



  @JsonKey(
    
    name: r'baseRate',
    required: false,
    includeIfNull: false,
  )


  final num? baseRate;



      /// ULID identifier
  @JsonKey(
    
    name: r'baseRateId',
    required: false,
    includeIfNull: false,
  )


  final String? baseRateId;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: true,
    includeIfNull: false,
  )


  final num probability;



  @JsonKey(
    
    name: r'interval',
    required: true,
    includeIfNull: false,
  )


  final StatInterval interval;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalogResultOutcomeDistribution &&
      other.outcome == outcome &&
      other.analogCount == analogCount &&
      other.hits == hits &&
      other.frequency == frequency &&
      other.baseRate == baseRate &&
      other.baseRateId == baseRateId &&
      other.probability == probability &&
      other.interval == interval;

    @override
    int get hashCode =>
        outcome.hashCode +
        analogCount.hashCode +
        hits.hashCode +
        (frequency == null ? 0 : frequency.hashCode) +
        (baseRate == null ? 0 : baseRate.hashCode) +
        (baseRateId == null ? 0 : baseRateId.hashCode) +
        probability.hashCode +
        interval.hashCode;

  factory AnalogResultOutcomeDistribution.fromJson(Map<String, dynamic> json) => _$AnalogResultOutcomeDistributionFromJson(json);

  Map<String, dynamic> toJson() => _$AnalogResultOutcomeDistributionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

