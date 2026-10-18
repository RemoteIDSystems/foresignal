//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/exercise_stats_skipped_rules_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exercise_stats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExerciseStats {
  /// Returns a new [ExerciseStats] instance.
  ExerciseStats({

    required  this.versionsScanned,

     this.alertsByRule,

     this.notificationsByChannel,

     this.suppressedByCooldown,

     this.suppressedByRateCap,

     this.skippedRules,

     this.caveats,

     this.alertsRaised,

     this.error,
  });

  @JsonKey(
    
    name: r'versionsScanned',
    required: true,
    includeIfNull: false,
  )


  final int versionsScanned;



  @JsonKey(
    
    name: r'alertsByRule',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? alertsByRule;



  @JsonKey(
    
    name: r'notificationsByChannel',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? notificationsByChannel;



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



  @JsonKey(
    
    name: r'skippedRules',
    required: false,
    includeIfNull: false,
  )


  final List<ExerciseStatsSkippedRulesInner>? skippedRules;



      /// e.g. PREDECESSOR_OUTSIDE_WINDOW
  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;



      /// Exercise alerts actually raised (a canceled run stops early); absent for a dry run
  @JsonKey(
    
    name: r'alertsRaised',
    required: false,
    includeIfNull: false,
  )


  final int? alertsRaised;



      /// Why a FAILED run failed
  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExerciseStats &&
      other.versionsScanned == versionsScanned &&
      other.alertsByRule == alertsByRule &&
      other.notificationsByChannel == notificationsByChannel &&
      other.suppressedByCooldown == suppressedByCooldown &&
      other.suppressedByRateCap == suppressedByRateCap &&
      other.skippedRules == skippedRules &&
      other.caveats == caveats &&
      other.alertsRaised == alertsRaised &&
      other.error == error;

    @override
    int get hashCode =>
        versionsScanned.hashCode +
        alertsByRule.hashCode +
        notificationsByChannel.hashCode +
        suppressedByCooldown.hashCode +
        suppressedByRateCap.hashCode +
        skippedRules.hashCode +
        caveats.hashCode +
        alertsRaised.hashCode +
        (error == null ? 0 : error.hashCode);

  factory ExerciseStats.fromJson(Map<String, dynamic> json) => _$ExerciseStatsFromJson(json);

  Map<String, dynamic> toJson() => _$ExerciseStatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

