//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/precursor_test_antecedent_one_of2.dart';
import 'package:foresignal_api/src/model/precursor_test_antecedent_one_of.dart';
import 'package:foresignal_api/src/model/precursor_test_antecedent_one_of3.dart';
import 'package:foresignal_api/src/model/precursor_test_antecedent_one_of1.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_antecedent.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestAntecedent {
  /// Returns a new [PrecursorTestAntecedent] instance.
  PrecursorTestAntecedent({

    required  this.rule,

    required  this.alertRuleId,

    required  this.intelEventType,

    required  this.anomalyRuleId,

     this.classification,
  });

      /// Partial TriggerRuleInput: source/kind arrays, conditions, geofence
  @JsonKey(
    
    name: r'rule',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> rule;



  @JsonKey(
    
    name: r'alertRuleId',
    required: true,
    includeIfNull: false,
  )


  final String alertRuleId;



  @JsonKey(
    
    name: r'intelEventType',
    required: true,
    includeIfNull: false,
  )


  final PrecursorTestAntecedentIntelEventTypeEnum intelEventType;



  @JsonKey(
    
    name: r'anomalyRuleId',
    required: true,
    includeIfNull: false,
  )


  final String anomalyRuleId;



  @JsonKey(
    
    name: r'classification',
    required: false,
    includeIfNull: false,
  )


  final PrecursorTestAntecedentClassificationEnum? classification;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestAntecedent &&
      other.rule == rule &&
      other.alertRuleId == alertRuleId &&
      other.intelEventType == intelEventType &&
      other.anomalyRuleId == anomalyRuleId &&
      other.classification == classification;

    @override
    int get hashCode =>
        rule.hashCode +
        alertRuleId.hashCode +
        intelEventType.hashCode +
        anomalyRuleId.hashCode +
        classification.hashCode;

  factory PrecursorTestAntecedent.fromJson(Map<String, dynamic> json) => _$PrecursorTestAntecedentFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestAntecedentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorTestAntecedentIntelEventTypeEnum {
@JsonValue(r'maritime-disruption')
maritimeDisruption(r'maritime-disruption'),
@JsonValue(r'aviation-anomaly')
aviationAnomaly(r'aviation-anomaly'),
@JsonValue(r'infrastructure-disruption')
infrastructureDisruption(r'infrastructure-disruption'),
@JsonValue(r'weather-impact')
weatherImpact(r'weather-impact'),
@JsonValue(r'seismic')
seismic(r'seismic'),
@JsonValue(r'custom')
custom(r'custom');

const PrecursorTestAntecedentIntelEventTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PrecursorTestAntecedentClassificationEnum {
@JsonValue(r'activity-up')
activityUp(r'activity-up'),
@JsonValue(r'activity-down')
activityDown(r'activity-down'),
@JsonValue(r'coverage-drop')
coverageDrop(r'coverage-drop'),
@JsonValue(r'insufficient-coverage')
insufficientCoverage(r'insufficient-coverage'),
@JsonValue(r'learning')
learning(r'learning'),
@JsonValue(r'normal')
normal(r'normal');

const PrecursorTestAntecedentClassificationEnum(this.value);

final String value;

@override
String toString() => value;
}


