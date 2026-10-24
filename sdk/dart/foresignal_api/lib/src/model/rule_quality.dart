//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/rule_quality_daily.dart';
import 'package:foresignal_api/src/model/rule_quality_signals.dart';
import 'package:foresignal_api/src/model/rule_quality_precision_ci.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_quality.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleQuality {
  /// Returns a new [RuleQuality] instance.
  RuleQuality({

    required  this.ruleId,

    required  this.since,

    required  this.alerts,

    required  this.labeled,

     this.labeledShare,

    required  this.positive,

    required  this.negative,

    required  this.neutral,

     this.precision,

     this.precisionCi,

     this.medianTimeToAck,

     this.medianTimeToResolve,

     this.flapping,

     this.duplicates,

     this.severityDistribution,

     this.feedbackMapping,

     this.caveats,

     this.signals,

     this.signalScore,

     this.daily,

     this.signalWeights,
  });

  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'since',
    required: true,
    includeIfNull: false,
  )


  final DateTime since;



  @JsonKey(
    
    name: r'alerts',
    required: true,
    includeIfNull: false,
  )


  final int alerts;



  @JsonKey(
    
    name: r'labeled',
    required: true,
    includeIfNull: false,
  )


  final int labeled;



  @JsonKey(
    
    name: r'labeledShare',
    required: false,
    includeIfNull: false,
  )


  final num? labeledShare;



  @JsonKey(
    
    name: r'positive',
    required: true,
    includeIfNull: false,
  )


  final int positive;



  @JsonKey(
    
    name: r'negative',
    required: true,
    includeIfNull: false,
  )


  final int negative;



  @JsonKey(
    
    name: r'neutral',
    required: true,
    includeIfNull: false,
  )


  final int neutral;



      /// positive ÷ (positive + negative); null when labeled < 5
  @JsonKey(
    
    name: r'precision',
    required: false,
    includeIfNull: false,
  )


  final num? precision;



  @JsonKey(
    
    name: r'precisionCi',
    required: false,
    includeIfNull: false,
  )


  final RuleQualityPrecisionCi? precisionCi;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'medianTimeToAck',
    required: false,
    includeIfNull: false,
  )


  final String? medianTimeToAck;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'medianTimeToResolve',
    required: false,
    includeIfNull: false,
  )


  final String? medianTimeToResolve;



      /// enter → exit within 10 min
  @JsonKey(
    
    name: r'flapping',
    required: false,
    includeIfNull: false,
  )


  final int? flapping;



      /// Same entity within 2 × cooldown
  @JsonKey(
    
    name: r'duplicates',
    required: false,
    includeIfNull: false,
  )


  final int? duplicates;



  @JsonKey(
    
    name: r'severityDistribution',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? severityDistribution;



  @JsonKey(
    
    name: r'feedbackMapping',
    required: false,
    includeIfNull: false,
  )


  final Map<String, RuleQualityFeedbackMappingEnum>? feedbackMapping;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;



  @JsonKey(
    
    name: r'signals',
    required: false,
    includeIfNull: false,
  )


  final RuleQualitySignals? signals;



      /// Weighted implicit-signal score (ACK_FAST +0.5, SNOOZED −0.5, FLAP −0.5, UNOPENED −0.25), shown next to, never mixed into, feedback precision
  @JsonKey(
    
    name: r'signalScore',
    required: false,
    includeIfNull: false,
  )


  final num? signalScore;



      /// Daily trend rows since `since`
  @JsonKey(
    
    name: r'daily',
    required: false,
    includeIfNull: false,
  )


  final List<RuleQualityDaily>? daily;



      /// The weight table of signalScore (ACK_FAST 0.5, SNOOZED -0.5, FLAP -0.5, UNOPENED -0.25); signalScore = sum(weight x count) / alerts
  @JsonKey(
    
    name: r'signalWeights',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num>? signalWeights;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleQuality &&
      other.ruleId == ruleId &&
      other.since == since &&
      other.alerts == alerts &&
      other.labeled == labeled &&
      other.labeledShare == labeledShare &&
      other.positive == positive &&
      other.negative == negative &&
      other.neutral == neutral &&
      other.precision == precision &&
      other.precisionCi == precisionCi &&
      other.medianTimeToAck == medianTimeToAck &&
      other.medianTimeToResolve == medianTimeToResolve &&
      other.flapping == flapping &&
      other.duplicates == duplicates &&
      other.severityDistribution == severityDistribution &&
      other.feedbackMapping == feedbackMapping &&
      other.caveats == caveats &&
      other.signals == signals &&
      other.signalScore == signalScore &&
      other.daily == daily &&
      other.signalWeights == signalWeights;

    @override
    int get hashCode =>
        ruleId.hashCode +
        since.hashCode +
        alerts.hashCode +
        labeled.hashCode +
        labeledShare.hashCode +
        positive.hashCode +
        negative.hashCode +
        neutral.hashCode +
        (precision == null ? 0 : precision.hashCode) +
        (precisionCi == null ? 0 : precisionCi.hashCode) +
        (medianTimeToAck == null ? 0 : medianTimeToAck.hashCode) +
        (medianTimeToResolve == null ? 0 : medianTimeToResolve.hashCode) +
        flapping.hashCode +
        duplicates.hashCode +
        severityDistribution.hashCode +
        feedbackMapping.hashCode +
        caveats.hashCode +
        signals.hashCode +
        (signalScore == null ? 0 : signalScore.hashCode) +
        daily.hashCode +
        signalWeights.hashCode;

  factory RuleQuality.fromJson(Map<String, dynamic> json) => _$RuleQualityFromJson(json);

  Map<String, dynamic> toJson() => _$RuleQualityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RuleQualityFeedbackMappingEnum {
@JsonValue(r'positive')
positive(r'positive'),
@JsonValue(r'negative')
negative(r'negative'),
@JsonValue(r'neutral')
neutral(r'neutral');

const RuleQualityFeedbackMappingEnum(this.value);

final String value;

@override
String toString() => value;
}


