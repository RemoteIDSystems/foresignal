//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_quality_daily_signals.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleQualityDailySignals {
  /// Returns a new [RuleQualityDailySignals] instance.
  RuleQualityDailySignals({

     this.ACK_FAST,

     this.SNOOZED,

     this.FLAP,

     this.UNOPENED,
  });

  @JsonKey(
    
    name: r'ACK_FAST',
    required: false,
    includeIfNull: false,
  )


  final int? ACK_FAST;



  @JsonKey(
    
    name: r'SNOOZED',
    required: false,
    includeIfNull: false,
  )


  final int? SNOOZED;



  @JsonKey(
    
    name: r'FLAP',
    required: false,
    includeIfNull: false,
  )


  final int? FLAP;



  @JsonKey(
    
    name: r'UNOPENED',
    required: false,
    includeIfNull: false,
  )


  final int? UNOPENED;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleQualityDailySignals &&
      other.ACK_FAST == ACK_FAST &&
      other.SNOOZED == SNOOZED &&
      other.FLAP == FLAP &&
      other.UNOPENED == UNOPENED;

    @override
    int get hashCode =>
        ACK_FAST.hashCode +
        SNOOZED.hashCode +
        FLAP.hashCode +
        UNOPENED.hashCode;

  factory RuleQualityDailySignals.fromJson(Map<String, dynamic> json) => _$RuleQualityDailySignalsFromJson(json);

  Map<String, dynamic> toJson() => _$RuleQualityDailySignalsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

