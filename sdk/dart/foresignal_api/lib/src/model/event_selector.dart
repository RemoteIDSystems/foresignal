//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/event_selector_unexplained_move.dart';
import 'package:foresignal_api/src/model/event_selector_change_point.dart';
import 'package:foresignal_api/src/model/event_selector_situation_activated.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_selector.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventSelector {
  /// Returns a new [EventSelector] instance.
  EventSelector({

     this.rule,

     this.alertRuleId,

     this.alertRuleTag,

     this.intelEventType,

     this.anomalyRuleId,

     this.classification,

     this.situationActivated,

     this.changePoint,

     this.unexplainedMove,

     this.referenceFamily,

     this.timestamps,

     this.minSeverity,

     this.regionCodes,

     this.label,
  });

      /// Inline record filter in 017 trigger-condition syntax
  @JsonKey(
    
    name: r'rule',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? rule;



      /// Alerts fired by this 017 rule
  @JsonKey(
    
    name: r'alertRuleId',
    required: false,
    includeIfNull: false,
  )


  final String? alertRuleId;



      /// Alerts fired by rules carrying this tag (120 event families)
  @JsonKey(
    
    name: r'alertRuleTag',
    required: false,
    includeIfNull: false,
  )


  final String? alertRuleTag;



      /// 101 intelligence event type
  @JsonKey(
    
    name: r'intelEventType',
    required: false,
    includeIfNull: false,
  )


  final String? intelEventType;



      /// 100 anomaly rule id; use with `classification`
  @JsonKey(
    
    name: r'anomalyRuleId',
    required: false,
    includeIfNull: false,
  )


  final String? anomalyRuleId;



      /// 100 anomaly classification, e.g. `activity-up`
  @JsonKey(
    
    name: r'classification',
    required: false,
    includeIfNull: false,
  )


  final String? classification;



  @JsonKey(
    
    name: r'situationActivated',
    required: false,
    includeIfNull: false,
  )


  final EventSelectorSituationActivated? situationActivated;



  @JsonKey(
    
    name: r'changePoint',
    required: false,
    includeIfNull: false,
  )


  final EventSelectorChangePoint? changePoint;



  @JsonKey(
    
    name: r'unexplainedMove',
    required: false,
    includeIfNull: false,
  )


  final EventSelectorUnexplainedMove? unexplainedMove;



      /// 121 reference-history family, e.g. `conflict:battles` or `seismic:m5`
  @JsonKey(
    
    name: r'referenceFamily',
    required: false,
    includeIfNull: false,
  )


  final String? referenceFamily;



      /// Explicit event times
  @JsonKey(
    
    name: r'timestamps',
    required: false,
    includeIfNull: false,
  )


  final List<DateTime>? timestamps;



      /// Minimum severity of the selected events (alert/intel severities or finding severities)
  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final EventSelectorMinSeverityEnum? minSeverity;



      /// Only occurrences tagged with one of these regions
  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



      /// Display label (generated when absent)
  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventSelector &&
      other.rule == rule &&
      other.alertRuleId == alertRuleId &&
      other.alertRuleTag == alertRuleTag &&
      other.intelEventType == intelEventType &&
      other.anomalyRuleId == anomalyRuleId &&
      other.classification == classification &&
      other.situationActivated == situationActivated &&
      other.changePoint == changePoint &&
      other.unexplainedMove == unexplainedMove &&
      other.referenceFamily == referenceFamily &&
      other.timestamps == timestamps &&
      other.minSeverity == minSeverity &&
      other.regionCodes == regionCodes &&
      other.label == label;

    @override
    int get hashCode =>
        rule.hashCode +
        alertRuleId.hashCode +
        alertRuleTag.hashCode +
        intelEventType.hashCode +
        anomalyRuleId.hashCode +
        classification.hashCode +
        situationActivated.hashCode +
        changePoint.hashCode +
        unexplainedMove.hashCode +
        referenceFamily.hashCode +
        timestamps.hashCode +
        (minSeverity == null ? 0 : minSeverity.hashCode) +
        regionCodes.hashCode +
        (label == null ? 0 : label.hashCode);

  factory EventSelector.fromJson(Map<String, dynamic> json) => _$EventSelectorFromJson(json);

  Map<String, dynamic> toJson() => _$EventSelectorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Minimum severity of the selected events (alert/intel severities or finding severities)
enum EventSelectorMinSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical'),
@JsonValue(r'notable')
notable(r'notable'),
@JsonValue(r'significant')
significant(r'significant');

const EventSelectorMinSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


