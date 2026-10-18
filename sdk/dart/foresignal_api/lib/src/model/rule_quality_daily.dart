//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/rule_quality_daily_signals.dart';
import 'package:foresignal_api/src/model/rule_quality_daily_feedback.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_quality_daily.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleQualityDaily {
  /// Returns a new [RuleQualityDaily] instance.
  RuleQualityDaily({

    required  this.ruleId,

    required  this.day,

    required  this.fired,

     this.feedback,

     this.signals,

     this.medianAckSeconds,

     this.flaps,

     this.suppressedByCooldown,

     this.suppressedByRateCap,
  });

  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'day',
    required: true,
    includeIfNull: false,
  )


  final DateTime day;



  @JsonKey(
    
    name: r'fired',
    required: true,
    includeIfNull: false,
  )


  final int fired;



  @JsonKey(
    
    name: r'feedback',
    required: false,
    includeIfNull: false,
  )


  final RuleQualityDailyFeedback? feedback;



  @JsonKey(
    
    name: r'signals',
    required: false,
    includeIfNull: false,
  )


  final RuleQualityDailySignals? signals;



  @JsonKey(
    
    name: r'medianAckSeconds',
    required: false,
    includeIfNull: false,
  )


  final num? medianAckSeconds;



  @JsonKey(
    
    name: r'flaps',
    required: false,
    includeIfNull: false,
  )


  final int? flaps;



  @JsonKey(
    
    name: r'suppressedByCooldown',
    required: false,
    includeIfNull: false,
  )


  final int? suppressedByCooldown;



  @JsonKey(
    
    name: r'suppressedByRateCap',
    required: false,
    includeIfNull: false,
  )


  final int? suppressedByRateCap;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleQualityDaily &&
      other.ruleId == ruleId &&
      other.day == day &&
      other.fired == fired &&
      other.feedback == feedback &&
      other.signals == signals &&
      other.medianAckSeconds == medianAckSeconds &&
      other.flaps == flaps &&
      other.suppressedByCooldown == suppressedByCooldown &&
      other.suppressedByRateCap == suppressedByRateCap;

    @override
    int get hashCode =>
        ruleId.hashCode +
        day.hashCode +
        fired.hashCode +
        feedback.hashCode +
        signals.hashCode +
        (medianAckSeconds == null ? 0 : medianAckSeconds.hashCode) +
        flaps.hashCode +
        suppressedByCooldown.hashCode +
        suppressedByRateCap.hashCode;

  factory RuleQualityDaily.fromJson(Map<String, dynamic> json) => _$RuleQualityDailyFromJson(json);

  Map<String, dynamic> toJson() => _$RuleQualityDailyToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

